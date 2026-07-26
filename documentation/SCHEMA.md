# SCHEMA.md — Local Database Schema

## 1. Overview
This app requires a **local-only database** to persist user-created Projects (per PRD requirement: persistent storage, forever, multiple named projects), plus a small **local entitlement cache** for the one-time in-app purchase (see Section 2.3). Each project may contain **1 to 10 source videos (free tier) or 1 to 20 (paid tier)**, trimmed to a shared duration and merged into a single output file, in a user-defined order. There is no remote database, no sync, and no multi-user concept — this schema exists solely on-device.

- **iOS**: Implemented via `SwiftData` (preferred, iOS 17+) or `Core Data` (fallback for wider OS support).
- **Android**: Implemented via `Room` (SQLite abstraction).

Both platforms implement the **same logical schema** independently (no shared code, per ARCHITECTURE.md), described below in platform-agnostic terms first, then per-platform implementation notes.

## 2. Logical Schema

### 2.1 Entity: `Project`
Represents a single saved project. A project now holds **merge-level** metadata; the individual source videos live in a separate child entity (Section 2.2).

| Field | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID / String | Primary Key, non-null | Unique identifier for the project |
| `name` | String | Non-null, max 100 chars | User-defined project name |
| `trimDuration` | Double | Non-null, range 1.0–5.0 | Selected trim duration in seconds, applied to every source video in the project. Free-tier projects will always be exactly 1, 2, or 3; paid-tier projects may be any value in range (see Section 4 for enforcement). Stored as `Double` rather than an integer enum to accommodate paid custom values. |
| `wasCustomDuration` | Boolean | Non-null | Whether this project's `trimDuration` was a custom paid-tier value (`true`) or one of the fixed free-tier options (`false`) — denormalized for quick display/audit without re-deriving from the raw value. |
| `outputVideoURI` | String (URI/path) | Non-null | Path to the encrypted, single **merged** output file in local storage |
| `thumbnailURI` | String (URI/path) | Non-null | Path to a small JPEG thumbnail image, extracted from the **first frame (t=0) of the merged output video** at creation time. Stored as a standalone file (not inline/base64 in the DB) to keep row size small and avoid complicating encryption. Used for the Projects List screen (DESIGN.md §6.1). See ARCHITECTURE.md §5.2 for the extraction strategy. |
| `exportQualityTier` | String (enum: `"free_720p"`, `"paid_original"`) | Non-null | The export quality tier that was actually applied when this project was created — recorded permanently since purchasing later does not retroactively re-process existing projects (per PRD §8.4). |
| `mergedDuration` | Double (seconds) | Non-null | Total duration of the merged output (`trimDuration × videoCount`), stored for quick display without recomputation |
| `videoCount` | Integer | Non-null, 1–20 | Number of source videos in this project (denormalized for quick list display). Range is 1–10 for projects created under free tier, 1–20 for projects created under paid tier — enforced at the domain layer at creation time, not by a DB constraint (see Section 4). |
| `createdAt` | Timestamp (ISO 8601 / Date) | Non-null | Project creation date/time |
| `updatedAt` | Timestamp (ISO 8601 / Date) | Non-null | Last modified date/time (e.g., on rename) |

### 2.2 Entity: `SourceVideoItem`
Represents a single source video within a project. A `Project` has **one-to-many** `SourceVideoItem` rows (minimum 1, maximum 10 for free-tier projects, maximum 20 for paid-tier projects).

| Field | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID / String | Primary Key, non-null | Unique identifier for this source video entry |
| `projectId` | UUID / String | Foreign Key → `Project.id`, non-null | Parent project this video belongs to |
| `sourceVideoURI` | String (URI/path) | Non-null | Reference to original source video (never modified) |
| `sourceVideoDuration` | Double (seconds) | Non-null | Original video duration at time of import |
| `sourceFileSize` | Long (bytes) | Nullable | File size at time of import, used for selection-screen display |
| `orderIndex` | Integer | Non-null, ≥ 0 | Position of this video in the merge order (0-based; determines concatenation order) |
| `trimStartTime` | Double (seconds) | Non-null | Calculated center-aligned start time used for this video's trim |
| `trimEndTime` | Double (seconds) | Non-null | Calculated center-aligned end time used for this video's trim |

### 2.3 Entity: `EntitlementCache`
Represents the locally-cached record of whether the one-time in-app purchase has been made. This is a **single-row table** (or platform-equivalent singleton store) — there is only ever one entitlement state per app install, not a list.

| Field | Type | Constraints | Description |
|---|---|---|---|
| `id` | Integer or fixed String | Primary Key, always the same value (e.g., `1` or `"entitlement_singleton"`) | Ensures only one row ever exists. |
| `isPurchased` | Boolean | Non-null, default `false` | Whether the one-time unlock is currently active on this device, as of the last successful verification. |
| `productId` | String | Nullable | The platform product identifier associated with the purchase (e.g., `"com.midtrim.fullunlock"`), for reference/debugging. Null if `isPurchased` is `false`. |
| `lastVerifiedAt` | Timestamp (ISO 8601 / Date) | Nullable | When this entitlement was last confirmed against StoreKit/Play Billing (via launch-time auto-restore or manual restore). Null if never verified. |

**Storage mechanism note**: unlike `Project`/`SourceVideoItem`, this entity is **not** stored in the main SwiftData/Room database — per ARCHITECTURE.md §6.1, it lives in Keychain (iOS) / `EncryptedSharedPreferences` (Android), since it's a small, security-sensitive singleton value more akin to a credential than to bulk app data. It is documented here alongside the other entities for completeness and because `Project.exportQualityTier`/`Project.wasCustomDuration` are derived from this entitlement at creation time, even though the storage location differs.

### 2.4 Relationships
- `Project (1) —— (1..10 or 1..20) SourceVideoItem` — a one-to-many relationship enforced at the application layer (min 1, max 10 or 20 depending on the tier active at creation time, per `projectId`), since neither SwiftData nor Room natively enforces "max child count" as a DB constraint.
- Deleting a `Project` **must cascade-delete** all associated `SourceVideoItem` rows (metadata only — this does **not** delete the actual source video files, which are user-owned and untouched per PRD/RULES).
- Multiple `Project` rows may reference the **same** `sourceVideoURI` across different `SourceVideoItem` rows — reuse of a source video across projects (or multiple times within the same project, if desired later) remains supported.
- `orderIndex` values within a single `projectId` must be unique and contiguous (0, 1, 2, ...) — validated at the domain layer (`ReorderVideosUseCase` / `SaveProjectUseCase`) before persistence.
- `EntitlementCache` has **no relationship** to `Project`/`SourceVideoItem` — it is not a foreign-keyed entity, just a single independent value read at the time a project is created to determine `exportQualityTier`/`wasCustomDuration`/allowed `videoCount`. Once a `Project` is saved, it does not "point back" to the entitlement state — it only records the outcome (Section 2.1).

### 2.5 Indexes
- `Project.createdAt` (descending) — supports default sort order (most recent projects first) on the home screen.
- `Project.id` (primary key, automatically indexed).
- `SourceVideoItem.projectId` — supports efficient lookup of all videos belonging to a project.
- `SourceVideoItem(projectId, orderIndex)` composite index — supports fetching a project's videos pre-sorted in merge order.
- `EntitlementCache` requires no additional indexes — it is a single-row store, accessed directly by its fixed primary key.

## 3. Platform Implementation

### 3.1 iOS — SwiftData Example
```swift
import SwiftData

@Model
final class Project {
    @Attribute(.unique) var id: UUID
    var name: String
    var trimDuration: Double
    var wasCustomDuration: Bool
    var outputVideoURI: String
    var thumbnailURI: String
    var exportQualityTier: String // "free_720p" | "paid_original"
    var mergedDuration: Double
    var videoCount: Int
    var createdAt: Date
    var updatedAt: Date

    @Relationship(deleteRule: .cascade, inverse: \SourceVideoItem.project)
    var sourceVideos: [SourceVideoItem] = []

    init(
        id: UUID = UUID(),
        name: String,
        trimDuration: Double,
        wasCustomDuration: Bool,
        outputVideoURI: String,
        thumbnailURI: String,
        exportQualityTier: String,
        mergedDuration: Double,
        videoCount: Int,
        createdAt: Date = .now,
        updatedAt: Date = .now
    ) {
        self.id = id
        self.name = name
        self.trimDuration = trimDuration
        self.wasCustomDuration = wasCustomDuration
        self.outputVideoURI = outputVideoURI
        self.thumbnailURI = thumbnailURI
        self.exportQualityTier = exportQualityTier
        self.mergedDuration = mergedDuration
        self.videoCount = videoCount
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}

@Model
final class SourceVideoItem {
    @Attribute(.unique) var id: UUID
    var sourceVideoURI: String
    var sourceVideoDuration: Double
    var sourceFileSize: Int64?
    var orderIndex: Int
    var trimStartTime: Double
    var trimEndTime: Double
    var project: Project?

    init(
        id: UUID = UUID(),
        sourceVideoURI: String,
        sourceVideoDuration: Double,
        sourceFileSize: Int64? = nil,
        orderIndex: Int,
        trimStartTime: Double,
        trimEndTime: Double
    ) {
        self.id = id
        self.sourceVideoURI = sourceVideoURI
        self.sourceVideoDuration = sourceVideoDuration
        self.sourceFileSize = sourceFileSize
        self.orderIndex = orderIndex
        self.trimStartTime = trimStartTime
        self.trimEndTime = trimEndTime
    }
}
```
*(Note: SwiftData's `.cascade` delete rule handles removing `SourceVideoItem` metadata rows automatically. It does **not** delete the actual video files — that cleanup remains an explicit repository-layer responsibility per RULES.md.)*

**`EntitlementCache` — Keychain example (not SwiftData):**
```swift
import Security

struct EntitlementCache: Codable {
    var isPurchased: Bool
    var productId: String?
    var lastVerifiedAt: Date?
}

// Stored as a single Keychain item (e.g., service: "com.midtrim.entitlement",
// account: "entitlement_singleton"), value is the Codable struct above,
// JSON-encoded, written/read via SecItemAdd/SecItemCopyMatching.
// Kept out of SwiftData intentionally — see ARCHITECTURE.md §6.1 for rationale.
```

### 3.2 Android — Room Example
```kotlin
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val trimDuration: Double,
    val wasCustomDuration: Boolean,
    val outputVideoUri: String,
    val thumbnailUri: String,
    val exportQualityTier: String, // "free_720p" | "paid_original"
    val mergedDuration: Double,
    val videoCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "source_video_items",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId", "orderIndex"])]
)
data class SourceVideoItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val sourceVideoUri: String,
    val sourceVideoDuration: Double,
    val sourceFileSize: Long?,
    val orderIndex: Int,
    val trimStartTime: Double,
    val trimEndTime: Double
)
```

```kotlin
@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM source_video_items WHERE projectId = :projectId ORDER BY orderIndex ASC")
    suspend fun getVideosForProject(projectId: String): List<SourceVideoItemEntity>

    @Insert
    suspend fun insert(project: ProjectEntity)

    @Insert
    suspend fun insertVideos(videos: List<SourceVideoItemEntity>)

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity) // relies on Room's CASCADE FK to remove child rows
}
```
*(Note: Room's `onDelete = ForeignKey.CASCADE` removes `SourceVideoItemEntity` metadata rows automatically when a `ProjectEntity` is deleted. This does **not** delete the actual video files on disk — that remains an explicit repository-layer responsibility per RULES.md.)*

**`EntitlementCache` — EncryptedSharedPreferences example (not Room):**
```kotlin
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// Stored as key-value pairs in an EncryptedSharedPreferences instance
// (e.g., file name "entitlement_prefs"), not a Room table:
//   "is_purchased" -> Boolean
//   "product_id" -> String?
//   "last_verified_at" -> Long? (epoch millis)
// Kept out of Room intentionally — see ARCHITECTURE.md §6.1 for rationale.
```

## 4. Data Integrity Rules
- `Project.trimDuration` must satisfy: **exactly 1, 2, or 3** if `wasCustomDuration` is `false`, or **any value from 1.0–5.0 inclusive** if `wasCustomDuration` is `true` — enforced at the application/domain layer (`ValidateTrimDurationUseCase`, per ARCHITECTURE.md), not just DB constraint, since both SwiftData and Room have limited enum/check-constraint support.
- `Project.videoCount` must equal the actual count of associated `SourceVideoItem` rows, and must be between **1 and 10 inclusive for free-tier-created projects, or 1 and 20 inclusive for paid-tier-created projects** — validated by `SaveProjectUseCase` before persistence, cross-checked against `exportQualityTier` as a sanity signal (a `videoCount` >10 should never coexist with `exportQualityTier == "free_720p"`, since exceeding 10 videos requires the paid tier to have been active at creation time).
- For each `SourceVideoItem`, `trimEndTime - trimStartTime` must equal the parent `Project.trimDuration` exactly — validated at creation time by `CalculateTrimWindowUseCase` (see ARCHITECTURE.md).
- `SourceVideoItem.orderIndex` values within a single `projectId` must be unique, zero-based, and contiguous — validated by `ReorderVideosUseCase`/`SaveProjectUseCase` before persistence.
- `Project.mergedDuration` must equal `trimDuration × videoCount` — recalculated and verified by `CalculateMergedDurationUseCase` whenever a project is saved.
- `Project.exportQualityTier` must be set at creation time from `ResolveExportQualityUseCase`'s output and is **immutable thereafter** — it is a historical record of what was used, not a live/recomputed value, and must never be silently changed by a later purchase **or a later refund/entitlement revocation** (per PRD §4.3/§8.6/§8.7). A project's `videoCount`, `trimDuration`, and `exportQualityTier` all remain exactly as recorded regardless of how the user's entitlement status changes afterward, in either direction.
- Deleting a `Project` record **must** cascade-delete its `SourceVideoItem` rows (DB-level cascade, per platform FK/relationship config) **and** must cascade to delete both the corresponding `outputVideoURI` merged file **and** the `thumbnailURI` image file from local storage (handled at the repository layer in a single call, since both files live outside the database — see ARCHITECTURE.md §5.2).
- `Project.thumbnailURI` must be generated and written **before** `SaveProjectUseCase` persists the `Project` row — a project is never saved with a missing or null thumbnail, since it's produced synchronously as the final step of the merge pipeline (see ARCHITECTURE.md §5.2). If thumbnail extraction fails, this is treated the same as any other merge-pipeline failure (per RULES.md §4's no-partial-save rule) — the project save fails cleanly rather than persisting with a broken thumbnail reference.
- Deleting a `Project` must **never** delete or affect any `sourceVideoURI` referenced by its `SourceVideoItem` rows (the original videos remain untouched, per PRD).
- `EntitlementCache.isPurchased` must **never** be set to `true` without a corresponding successful verification from `PurchaseEntitlementUseCase` or `RestoreEntitlementUseCase` — no code path should set this flag directly/manually (see ARCHITECTURE.md §6.1's "never trust a locally-cached value without derivation" rule).

## 5. Storage Growth Considerations
- Since projects are kept **forever** (per PRD), the database itself will remain small (metadata only — a few hundred bytes per `Project` row, plus up to 20 small `SourceVideoItem` rows per project), but **merged output video files** will accumulate on device storage over time, and paid-tier projects at original resolution will be larger than free-tier 720p projects.
- Each `Project` also has one small thumbnail JPEG (per §2.1/§3, typically a few KB to a few tens of KB depending on device resolution) — negligible compared to the merged video file itself, not a meaningful contributor to storage growth.
- Multi-video projects do not duplicate source video files (only URI references are stored), so storage growth is driven by merged output size, not by the number of source videos referenced.
- `EntitlementCache` is a single, tiny (well under 1KB) record — negligible storage impact, not a growth concern.
- No auto-purge logic in MVP. Future consideration: surface total storage used by trimmed videos in a settings screen, with manual bulk-delete options (post-MVP).

## 6. Migration Strategy
- MVP ships with this schema (`Project` + `SourceVideoItem` in SwiftData/Room, plus the separate Keychain/`EncryptedSharedPreferences`-based `EntitlementCache`) as schema version 1 — there is no prior schema in production, so no migration is needed from an earlier structure.
- Both `SwiftData` (via `VersionedSchema`) and `Room` (via `Migration` objects) support forward migrations for future changes to `Project`/`SourceVideoItem`.
- `EntitlementCache`, living outside the main DB, does not need a `Migration` object in the same sense — schema evolution there (e.g., adding a subscription expiry field post-MVP) is a matter of adding new keys/fields to the Keychain item or `EncryptedSharedPreferences` file, handled defensively (missing key = treat as default/unset) rather than via a formal migration.
- Any future schema change (e.g., adding metadata-stripping preference per project, per-video custom trim duration, or subscription-based entitlement fields) must include an explicit migration path — never a destructive reinstall-only change once the app is live.

## 7. No Server-Side / Remote Schema
Per PRD and ARCHITECTURE (offline-only except the platform-managed IAP flow itself), there is **no remote database, no custom API schema, and no sync schema** in this document. The only "remote" data involved is Apple's/Google's own purchase records, which this app queries via SDK (StoreKit/Play Billing) but does not define, own, or replicate a schema for — those records are entirely managed by the platform. This file will need a new "Remote Schema" section only if/when cloud backup is introduced post-MVP.
