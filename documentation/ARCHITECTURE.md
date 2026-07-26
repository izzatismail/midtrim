# ARCHITECTURE.md — System Architecture

## 1. High-Level Overview
This is a **fully offline, native, single-device** application, with one narrow exception: the platform-managed **in-app purchase (IAP)** flow, which necessarily communicates with Apple's/Google's own servers (StoreKit / Google Play Billing) to process the one-time purchase and verify/restore entitlement. There is no custom backend server, no proprietary API layer, and no other network communication of any kind — the only network traffic in this app is the OS-managed IAP transaction itself, which the app does not directly control or observe beyond the SDK's callback/result. Each platform (iOS, Android) is built as an **independent native codebase**, sharing only conceptual architecture and design language — not code.

```
┌─────────────────────────────────────────┐
│              Presentation Layer           │
│   (SwiftUI / Jetpack Compose Views)       │
├─────────────────────────────────────────┤
│              ViewModel Layer              │
│   (State management, user intent)         │
├─────────────────────────────────────────┤
│               Domain Layer                │
│   (Use Cases: TrimVideo, SaveProject...)  │
├─────────────────────────────────────────┤
│                Data Layer                 │
│  (Local DB, File Storage, Media Engine)   │
└─────────────────────────────────────────┘
```

### 1.1 Repository Structure
MidTrim is developed as a **single monorepo** containing both native codebases plus all project documentation. There is no separate docs repo and no per-platform repo split — this is a deliberate decision (see RULES.md §6.5) to keep documentation and implementation in sync and to simplify CI/CD (see CI.md).

```
midtrim/
├── ios/                        # Native iOS app (Swift/SwiftUI)
│   ├── MidTrim/
│   │   ├── App/                # App entry point, DI composition root
│   │   ├── Presentation/       # SwiftUI Views + ViewModels
│   │   ├── Domain/              # Use Cases, domain models, protocols
│   │   ├── Data/                 # SwiftData models, repositories, file storage
│   │   └── Resources/           # Assets, localization
│   ├── MidTrimTests/            # Unit tests (domain + data layer)
│   ├── MidTrimUITests/          # UI tests (critical flows only)
│   └── MidTrim.xcodeproj
│
├── android/                    # Native Android app (Kotlin/Compose)
│   ├── app/
│   │   ├── src/main/java/com/midtrim/
│   │   │   ├── presentation/    # Compose UI + ViewModels
│   │   │   ├── domain/           # Use Cases, domain models, interfaces
│   │   │   └── data/             # Room entities/DAOs, repositories, file storage
│   │   ├── src/test/             # Unit tests (domain + data layer)
│   │   └── src/androidTest/      # UI tests (critical flows only)
│   ├── build.gradle.kts
│   └── gradle/
│
├── docs/                        # All project reference documentation
│   ├── PRD.md
│   ├── DESIGN.md
│   ├── ARCHITECTURE.md          # this file
│   ├── SCHEMA.md
│   ├── RULES.md
│   └── CI.md
│
├── .github/
│   └── workflows/               # android-ci.yml, ios-ci.yml (see CI.md)
│
├── scripts/                     # Shared tooling (e.g., setup-hooks.sh for pre-commit)
│
└── .gitignore                   # Includes .env and platform build artifacts
```

Key structural rules:
- `ios/` and `android/` remain fully independent — no shared source code between them (per Section 10, "Why No Shared Cross-Platform Code").
- Each platform's internal folder structure (`Presentation/`, `Domain/`, `Data/`) mirrors the layered architecture described in Section 1's diagram — this mapping should stay 1:1 so any contributor or agent can navigate from architecture doc to code layer without guesswork.
- `docs/` is the single source of truth referenced throughout all five (now six, including this structure) reference documents — code changes that contradict `docs/` must update `docs/` in the same change set (per RULES.md §7).

## 2. Design Methodology
**Clean Architecture** (adapted, pragmatic — not dogmatic layering for a small MVP), combined with **MVVM** for the presentation layer on both platforms.

Rationale:
- Clear separation between UI, business logic, and data/storage — critical since video processing (domain logic) must remain platform-native but testable independently of UI.
- MVVM is the natural fit for both SwiftUI (`@Observable`/`ObservableObject`) and Jetpack Compose (`ViewModel` + `StateFlow`), keeping parity in structure across platforms without forcing shared code that doesn't exist.
- Use Cases (domain layer) encapsulate single responsibilities (SOLID — see RULES.md) like `TrimVideoUseCase`, `SaveProjectUseCase`, `DeleteProjectUseCase`, `ImportVideoUseCase`.

## 3. Platform-Specific Stack

### 3.1 iOS
| Layer | Technology |
|---|---|
| UI | SwiftUI |
| State Management | `@Observable` (iOS 17+) or `ObservableObject`/`Combine` (iOS 15+) |
| Video Processing | `AVFoundation` (`AVAssetExportSession`, `AVMutableComposition`) |
| Local Database | `SwiftData` (iOS 17+) or `Core Data` (fallback for broader iOS support) |
| File Storage | App Sandbox `Documents/` directory + `FileManager` |
| Encryption | `NSFileProtectionComplete` (iOS Data Protection API) for stored video files, secure entitlement cache (see Section 6.1) |
| Media Picker | `PHPickerViewController` (privacy-preserving, no full-library permission required) |
| In-App Purchase | `StoreKit 2` (`Product`, `Transaction`, `Transaction.currentEntitlements` for restore) |

### 3.2 Android
| Layer | Technology |
|---|---|
| UI | Jetpack Compose |
| State Management | `ViewModel` + `StateFlow`/`Compose State` |
| Video Processing | `Media3 Transformer` (AndroidX, successor to MediaCodec boilerplate) — native, no FFmpeg needed for basic trim |
| Local Database | `Room` (SQLite abstraction) |
| File Storage | App-specific storage (`context.filesDir` / scoped storage via `MediaStore` for user-visible exports) |
| Encryption | `EncryptedFile` (Jetpack Security / `androidx.security.crypto`) for stored video files, secure entitlement cache (see Section 6.1) |
| Media Picker | `Photo Picker` (`ActivityResultContracts.PickVisualMedia`) — privacy-preserving, no broad storage permission required |
| In-App Purchase | `Google Play Billing Library` (v6+, `BillingClient`, `queryPurchasesAsync` for restore) |

**Note on third-party libraries**: Both `AVFoundation` and `Media3 Transformer` natively support trimming without re-encoding quality loss. **No FFmpeg or third-party video library is required for MVP trim functionality.** Third-party libraries should only be considered if a native gap is discovered during implementation (per PRD requirement: native-first).

## 4. Core Domain Use Cases
These represent the platform-agnostic *logic* (implemented natively per platform, not shared code):

1. **ImportVideosUseCase** — validates 1–10 (free) or 1–20 (paid) selected videos (format, duration each), returns metadata (duration, resolution, file size, URI) per video. Surfaces per-file errors if part of a batch is invalid. Requires current entitlement status as input to determine the applicable cap.
2. **ReorderVideosUseCase** — pure function: given a list of selected videos and a new order (from drag-to-reorder), returns the reordered list. Operates purely on in-memory state prior to trim/merge.
3. **CalculateTrimWindowUseCase** — pure function: given a single video's `videoDuration` + `trimDuration`, returns `(startTime, endTime)` centered window. Called once per video in the project. Accepts any `trimDuration` from 1–3s (free) or 1–5s (paid); does not itself enforce the tier cap (see `ValidateTrimDurationUseCase` below) — stays a pure calculation.
4. **CalculateMergedDurationUseCase** — pure function: given `trimDuration` and `videoCount`, returns total projected output duration (`trimDuration × videoCount`) for live display on the selection screen.
5. **ValidateTrimDurationUseCase** *(new)* — pure function: given a requested `trimDuration` and current entitlement status, returns whether the duration is allowed (free: must be exactly 1, 2, or 3; paid: any value from 1–5 inclusive) — the single source of truth for this rule, called by both the UI (to disable/enable controls) and `TrimVideoUseCase` (to reject invalid requests defensively, never trusting the UI layer alone).
6. **TrimVideoUseCase** — executes native trim/export operation for a **single** video, outputs a temporary trimmed segment. Delegates to `ValidateTrimDurationUseCase` before proceeding.
7. **MergeVideoSegmentsUseCase** — takes an ordered list of trimmed segments and concatenates them into a **single output file**, normalizing resolution/frame rate/aspect ratio as needed (see Section 5.1), encoded at the quality level determined by `ResolveExportQualityUseCase`.
8. **ResolveExportQualityUseCase** *(new)* — pure function: given current entitlement status and the source video's native resolution, returns the target export resolution (free: capped at 720p; paid: source resolution up to the codec's practical limits, no upscaling). Called by `MergeVideoSegmentsUseCase` before encoding.
9. **GenerateThumbnailUseCase** *(new)* — given the merged output video's URI, extracts a single frame at `t=0` and writes it as a small JPEG, returning the new file's URI. Runs once, immediately after `MergeVideoSegmentsUseCase` succeeds and before `SaveProjectUseCase` persists the project (see Section 5.2 for the full extraction strategy). Deliberately simple: no frame selection logic, no multiple sizes.
10. **SaveProjectUseCase** — persists project metadata (name, ordered source URIs, merged output URI, **thumbnail URI**, trim duration, **export quality tier used**, timestamp) to local DB.
11. **FetchProjectsUseCase** — retrieves all saved projects for display on home screen.
12. **DeleteProjectUseCase** — removes project record + associated merged output file **and thumbnail file** (never touches source videos).
13. **RenameProjectUseCase** — updates project name in local DB.
14. **PurchaseEntitlementUseCase** *(new)* — initiates the platform purchase flow (StoreKit 2 `Product.purchase()` / Play Billing `launchBillingFlow`), handles the transaction result, and on success persists the verified entitlement to the secure local cache (see Section 6.1).
15. **RestoreEntitlementUseCase** *(new)* — queries the platform's purchase records (StoreKit 2 `Transaction.currentEntitlements` / Play Billing `queryPurchasesAsync`) for an existing valid purchase, and if found, updates the secure local entitlement cache. Used both for **automatic restore-on-launch** and the **manual "Restore Purchases" button** — same Use Case, two callers.
16. **FetchEntitlementStatusUseCase** *(new)* — pure read of the cached local entitlement state (not a live store query) — used by UI/ViewModels to decide which controls to show as locked/unlocked without hitting the store API on every render.

## 5. Data Flow (Trim Creation Flow)

```
App launch → RestoreEntitlementUseCase runs automatically (Section 5.3)
        │
        ▼
User selects 1–10 (free) or 1–20 (paid) videos (Picker, multi-select)
        │   (cap enforced using FetchEntitlementStatusUseCase's cached result)
        ▼
ImportVideosUseCase → validates + extracts duration/size/metadata per video
        │
        ▼
UI shows selection list: thumbnail, name, size, duration + live merged total
        │   ("+" shows lock affordance past 10 videos if free-tier)
        ▼
User drags to reorder → ReorderVideosUseCase (pure, in-memory)
        │
        ▼
UI displays duration options: 1s/2s/3s fixed (both tiers) + "Custom" (paid only,
locked with paywall tap-through if free), disabling any option/value
longer than the SHORTEST selected video's duration
        │
        ▼
User selects duration → ValidateTrimDurationUseCase confirms it's allowed for
the current tier → CalculateTrimWindowUseCase runs per video (pure, in-memory)
        │                CalculateMergedDurationUseCase updates live total
        ▼
User confirms → TrimVideoUseCase runs once per video (re-validates duration
        │   defensively via ValidateTrimDurationUseCase before executing)
        │   (AVFoundation / Media3 Transformer executes export per segment)
        ▼
Trimmed segments (temporary) produced in confirmed order
        │
        ▼
ResolveExportQualityUseCase determines target resolution (720p free / source paid)
        │
        ▼
MergeVideoSegmentsUseCase concatenates segments into a single file at resolved quality
        │   (normalizes resolution/frame rate/aspect ratio if sources differ)
        ▼
Merged file written to local sandboxed storage (encrypted at rest)
        │
        ▼
GenerateThumbnailUseCase extracts first frame (t=0) of merged output → JPEG
        │   (see Section 5.2 — simple, single-frame, no selection logic)
        ▼
User previews merged result, names project → SaveProjectUseCase
        │   (persists trim duration, export quality tier used, AND thumbnail URI)
        ▼
Project record + ordered source video rows persisted to local DB (SwiftData/Room)
        │
        ▼
Home screen refreshed via FetchProjectsUseCase
```

### 5.1 Merge Normalization Strategy
Since source videos in a single project may differ in resolution, aspect ratio, or frame rate, `MergeVideoSegmentsUseCase` must normalize all trimmed segments to a **consistent output format** before/during concatenation:
- **Reference format**: derived from the **first video in the confirmed order** (its resolution/aspect ratio/frame rate becomes the target for the merged output), unless native platform guidance suggests a better default (e.g., normalizing to the highest common resolution). This should be validated during implementation and documented once confirmed.
- The reference resolution is then clamped by `ResolveExportQualityUseCase`'s output — e.g., if the reference video is 4K but the user is on the free tier, the actual encode target is 720p, not 4K.
- iOS: achieved via `AVMutableComposition` + `AVMutableVideoComposition` (applies consistent render size/frame rate across all track segments).
- Android: achieved via `Media3 Transformer`'s `EditedMediaItemSequence`, applying consistent `Effects`/output configuration across all sequence items.
- If normalization would cause unacceptable quality loss on non-reference clips, this must be flagged during implementation for product review — not silently degraded.
- **Phase 6 validation checkpoint (confirmed approach, pending real-world test)**: before considering the merge pipeline complete, run a real side-by-side test — merge a real low-resolution source (e.g., 720p) with a real high-resolution source (e.g., 4K) in both possible orders, and visually inspect both outputs on an actual device screen. If the resulting quality loss (on whichever clip ends up non-reference) is not acceptable for casual social sharing, escalate to the product owner before proceeding — this may require pivoting to a "highest-resolution-as-reference" strategy instead. This is a deliberate checkpoint, not an assumption that the current approach is final.

### 5.2 Thumbnail Generation Strategy
A single small thumbnail image is generated per project, for display on the Projects List screen (DESIGN.md §6.1). Kept **deliberately simple** — no smart frame selection, no scene detection, no on-demand/lazy generation:

- **New Use Case: `GenerateThumbnailUseCase`** — added to the Core Domain Use Cases list (Section 4) as a 16th Use Case. Pure responsibility: given the merged output video's URI, extract a single frame and write it to a JPEG file, returning the new file's URI.
- **Source**: the frame is always extracted from the **merged output video** (`outputVideoURI`), never a source video — this guarantees exactly one thumbnail strategy regardless of how many videos were in the project, and the thumbnail always reflects what the project's saved output actually looks like.
- **Which frame**: always the **first frame, at timestamp `t=0`**, of the merged output. No "pick the best/most interesting frame" logic — this keeps the Use Case a simple, fast, single-call operation with no ambiguity or extra processing cost.
- **When it runs**: as the **final step of the trim/merge pipeline**, immediately after `MergeVideoSegmentsUseCase` produces the merged output file and **before** `SaveProjectUseCase` persists the `Project` row. This keeps thumbnail generation synchronous and part of the same pipeline the user is already waiting on (per Section 7's progress-indicator pattern), rather than a separate background job with its own failure/retry semantics.
- **Failure handling**: if thumbnail extraction fails, the whole project-save operation fails (per Section 8's error handling strategy, consistent with "no partial/silent merges") — the app does not persist a `Project` row with a missing or broken `thumbnailURI`.
- **Platform implementation**:
  - iOS: `AVAssetImageGenerator` (from `AVFoundation`, already a dependency per Section 3.1) — call `image(at: CMTime.zero)` against the merged output asset, then encode the resulting `CGImage` as JPEG via `UIImage`/`.jpegData(compressionQuality:)`.
  - Android: `Media3`'s `MediaMetadataRetriever` (or `Media3 Transformer`'s frame extraction utilities, already a dependency per Section 3.2) — call `getFrameAtTime(0)`, then encode the resulting `Bitmap` as JPEG via `Bitmap.compress(Bitmap.CompressFormat.JPEG, ...)`.
  - Both platforms: a modest, fixed JPEG compression quality (e.g., ~80%) and a capped output dimension (e.g., longest side ~480px) are sufficient for a small list-row thumbnail — no need to preserve full resolution, since this file is never shown at full-screen size.
- **Storage**: written to the same app-private storage directory as the merged output file (see Section 6), encrypted at rest using the same mechanism as other output files. Its path is persisted in `Project.thumbnailURI` (see SCHEMA.md §2.1).
- **Deletion**: cascade-deleted alongside the merged output file whenever a `Project` is deleted — handled by the same repository call, not a separate cleanup step (see Section 6, SCHEMA.md §4).
- **Explicitly out of scope for MVP** (kept simple on purpose): thumbnail regeneration/versioning if source videos change (they can't — sources are immutable per PRD), multiple thumbnail sizes/resolutions, animated/video thumbnails, or any user-facing control over which frame is used.

### 5.3 Data Flow — In-App Purchase & Restore

```
App launch
        │
        ▼
RestoreEntitlementUseCase runs automatically (silent, no UI blocking)
        │   queries StoreKit 2 Transaction.currentEntitlements /
        │   Play Billing queryPurchasesAsync
        ▼
   ┌────┴────┐
   ▼         ▼
Found      Not Found
   │         │
   ▼         ▼
Update     Leave cached entitlement as-is (default: not purchased)
secure
cache
   │         │
   └────┬────┘
        ▼
FetchEntitlementStatusUseCase used throughout the app to gate UI state
(no further store queries needed until next launch or manual restore)

─────────────────────────────────────────────────────────

User taps "Upgrade" entry point OR a locked control (lock icon)
        │
        ▼
Paywall screen shown (DESIGN.md §6.5)
        │
        ▼
User taps "Unlock MidTrim" → PurchaseEntitlementUseCase
        │   initiates StoreKit 2 Product.purchase() / Play Billing launchBillingFlow
        ▼
   ┌────┴────┐
   ▼         ▼
Success    Failure/Cancelled
   │         │
   ▼         ▼
Verify     Surface error/cancellation to UI (non-blocking, user can retry)
transaction,
update secure
cache, dismiss
paywall, unlock
UI immediately
        
─────────────────────────────────────────────────────────

User taps "Restore Purchases" (Help/Settings screen, DESIGN.md §6.6, 
or the secondary link on the Paywall screen itself)
        │
        ▼
RestoreEntitlementUseCase runs on-demand (same Use Case as launch-time restore)
        │
        ▼
Success/failure toast shown ("Purchases restored" / "No purchases found")
```

**Key architectural point**: `RestoreEntitlementUseCase` is a **single Use Case with two callers** (automatic on launch, manual via button) — not two separate implementations. This keeps the entitlement-restoration logic in one place per SOLID's Single Responsibility Principle (see RULES.md §1.1).

## 6. File Storage Strategy
- **Source videos** (up to 10 free / 20 paid per project): never copied or modified. App stores only a reference per video (security-scoped bookmark on iOS via `PHPicker`/security-scoped URL; persisted URI permission on Android via `Photo Picker` + `ContentResolver.takePersistableUriPermission` if needed), plus its `orderIndex` within the project.
- **Intermediate trimmed segments**: written to a temporary/scratch directory during processing, deleted once the merge step completes successfully — these are not part of the persisted project.
- **Output (merged, trimmed) video**: a single file written to app-private storage directory, encrypted at rest (see Section 3 encryption tools per platform), encoded at the resolution determined by `ResolveExportQualityUseCase` at creation time.
- **Thumbnail image**: a single small JPEG written alongside the output video in the same app-private storage directory, encrypted at rest using the same mechanism — see Section 5.2 for the extraction strategy. Deleted together with the output video whenever a project is deleted (single repository call, no separate cleanup path).
- **Sharing/export**: when the user shares/saves the merged trimmed video externally (e.g., to Camera Roll/Gallery), a **decrypted temporary copy** is generated at share-time and discarded after the share sheet completes — the persistent encrypted copy remains the source of truth inside the app. The thumbnail is not part of this share flow (it's an internal list-display asset only, never shared/exported).

### 6.1 Entitlement Cache Storage
- Purchase entitlement state (`purchased: Bool` + minimal supporting metadata like product ID and verification timestamp) is stored **on-device only**, using the same encryption mechanism as video files:
  - iOS: Keychain (preferred over `NSFileProtectionComplete` for this specific small, sensitive value — Keychain is the platform-idiomatic place for credential/entitlement-like data, not a video file).
  - Android: `EncryptedSharedPreferences` (Jetpack Security) — same rationale, idiomatic for small key-value secure data vs. `EncryptedFile` for larger blobs.
- This cache is a **local mirror of the store's own record**, not a source of truth in itself — `RestoreEntitlementUseCase` can always re-derive it from StoreKit/Play Billing. Losing this cache (e.g., app data cleared) is recoverable via restore, not a data-loss event.
- **Never** trust a locally-cached "purchased: true" value without having derived it from an actual StoreKit/Play Billing verification at least once per app install — this cache is a performance/UX optimization (avoid querying the store on every screen), not an independent authorization mechanism.

## 7. Concurrency & Performance
- Video trim/export operations run on a **background thread/task** (iOS: `Task`/async-await with `AVAssetExportSession`; Android: `Dispatchers.IO` coroutine with `Media3 Transformer` listener callbacks) — never block the main/UI thread.
- For multi-video projects, individual trims may run sequentially or in parallel (implementation detail), but the **merge step must wait for all individual trims to complete successfully** before starting concatenation.
- Progress reporting surfaced to the ViewModel via native async patterns (Combine publisher on iOS, `StateFlow` on Android) to drive the UI progress indicator described in DESIGN.md — for multi-video projects, progress should reflect overall pipeline state (e.g., "Trimming video 3 of 7", then "Merging clips...").
- Large video files: stream-based processing only (no full in-memory loading of raw video data).
- **`RestoreEntitlementUseCase` on launch runs asynchronously and must never block app startup or the initial render of the Projects List screen** — the UI renders immediately with the last-cached entitlement state, then silently updates if the restore check changes it. This avoids a slow/failed store query delaying the entire app launch.
- `PurchaseEntitlementUseCase` runs on a background task/coroutine, surfacing loading state to the Paywall screen's CTA (per DESIGN.md's loading-state pattern) — never blocks the main thread during the platform purchase flow.

## 8. Error Handling Strategy
- All Use Cases return a typed **Result** (`Result<Success, Error>` on iOS / Kotlin `Result<T>` or sealed class on Android) — no silent failures.
- User-facing errors mapped to friendly messages (e.g., "This video is too short to trim to 3 seconds" instead of raw system errors).
- Trim failures (e.g., corrupted file, unsupported codec) must not crash the app — caught at the Use Case boundary and surfaced to UI as a recoverable error state.
- For multi-video projects: if **any single video** fails validation or trim, the whole project creation must fail gracefully with a clear message identifying which video failed — partial/silent merges (dropping the failed video and continuing) are not allowed in MVP, since it would produce an unexpected output the user didn't confirm.
- If `MergeVideoSegmentsUseCase` fails after individual trims succeed, all temporary trimmed segments must be cleaned up and the user notified — no orphaned temp files left on disk.
- **`PurchaseEntitlementUseCase` failures** (network error, user cancellation, payment declined, etc.) must be surfaced as distinct, friendly states on the Paywall screen (e.g., "Purchase cancelled" vs. "Something went wrong — please try again") — never a generic crash or silent no-op. Cancellation is not an error state requiring alarm; a declined payment or network failure should invite retry.
- **`RestoreEntitlementUseCase` failures** (e.g., no network for the store query) must not lock a previously-entitled user out silently — the last-known cached entitlement state remains in effect until a successful restore/query updates it. Only an explicit "not found" result from the store should downgrade cached entitlement, never a network/timeout failure.
- **Entitlement revocation (refunds)**: if `RestoreEntitlementUseCase` receives an explicit, successful result from the store indicating the purchase is no longer active (e.g., refunded), this **is** a valid reason to downgrade the cached entitlement to `false` — this is distinct from the network/timeout case above, which must never downgrade. The downgrade only affects the ability to *create* new paid-tier projects going forward; it must never cascade into modifying, hiding, or restricting access to already-saved `Project` records (see SCHEMA.md §4's immutability rule for `exportQualityTier`/`trimDuration`, which applies symmetrically here).

## 9. Testing Strategy
- **Domain layer** (all Use Cases — `CalculateTrimWindowUseCase`, `CalculateMergedDurationUseCase`, `ReorderVideosUseCase`, `ImportVideosUseCase` validation logic, `MergeVideoSegmentsUseCase` ordering/error-path logic, `ValidateTrimDurationUseCase`, `ResolveExportQualityUseCase`, etc.) must be unit-testable in isolation — pure functions and validation logic with no platform/UI dependency.
- **Minimum coverage bar for MVP**: every Use Case in Section 4 must have unit tests covering (a) the expected/happy path, (b) each documented validation failure (e.g., video too short, >10/>20 videos depending on tier, unsupported format, invalid custom duration), and (c) boundary conditions (e.g., video duration exactly equal to trim duration, exactly 1 video, exactly 10/20 videos, exactly 5s custom duration).
- **IAP-specific test requirements**: `PurchaseEntitlementUseCase` and `RestoreEntitlementUseCase` must be tested against mocked StoreKit/Play Billing responses covering: successful purchase, user cancellation, payment failure, successful restore with existing purchase, restore with no purchase found, and restore failing due to network/store unavailability (verifying cached entitlement is preserved, not cleared, in this last case).
- **Thumbnail-specific test requirements**: `GenerateThumbnailUseCase` must be tested covering: successful extraction produces a valid JPEG at the expected path, extraction failure causes the overall project-save to fail cleanly (no `Project` row persisted with a missing `thumbnailURI`), and deletion of a project removes both the output video **and** its thumbnail file in the same operation.
- **Data layer** (DB operations, repository implementations) tested via in-memory database instances (SwiftData in-memory container / Room in-memory database) — at minimum: insert, fetch, cascade-delete (Project → SourceVideoItem rows, **and associated output/thumbnail files**), and rename.
- **UI layer**: platform-native UI testing (XCTest/XCUITest for iOS, Compose UI Test for Android) for critical flows only (multi-select import → reorder → trim → save → delete; plus the paywall → purchase → unlock flow using StoreKit/Play Billing sandbox/test accounts).
- Unit tests are **not optional or deferrable** — see RULES.md Section 5.3 for the enforcement rule tying this to every change touching domain logic.

## 10. Why No Shared Cross-Platform Code
Per PRD requirement (native-only, native APIs first), this project intentionally avoids:
- React Native, Flutter, or Kotlin Multiplatform for MVP.
- Shared business logic modules across platforms.

Rationale: video processing performance and quality are best served by direct native framework access (AVFoundation, Media3), and avoiding cross-platform abstraction reduces risk of subtle quality/performance regressions in the core trim feature — the single most important operation in this app.

## 11. Future Architecture Considerations (Post-MVP)
- If manual trim scrubbing is added, introduce a dedicated `TrimEditorViewModel` with scrubber state management (debounced preview generation).
- If cloud backup is introduced later, add a `SyncRepository` abstraction at the data layer without disrupting existing local-first Use Cases.
- Consider Kotlin Multiplatform **only** for non-UI shared logic (e.g., trim window calculation) if maintenance burden across platforms grows significantly — not in MVP scope.
- If subscription tiers or multiple paid feature bundles are introduced post-MVP, `PurchaseEntitlementUseCase`/`RestoreEntitlementUseCase` will need to evolve from a single boolean entitlement to a richer entitlement model (product IDs, expiry dates for subscriptions) — the current one-time-purchase design intentionally keeps this simple and should not be over-engineered for a subscription future that isn't committed to yet.
- Server-side receipt validation (rather than relying solely on client-side StoreKit/Play Billing SDK verification) could be added later for stronger anti-fraud guarantees, but requires introducing a backend — out of scope while the app remains fully offline-first.
