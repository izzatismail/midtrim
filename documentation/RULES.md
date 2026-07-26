# RULES.md — Coding Rules for AI Agents

**This document is binding.** Any AI agent (or human contributor) generating or modifying code in this repository MUST adhere to every rule below. Where a rule conflicts with a request, the agent must flag the conflict rather than silently violate this document.

---

## 1. SOLID Principles — Mandatory Application

### 1.1 Single Responsibility Principle
- Every class/struct/file has exactly **one reason to change**.
- ✅ DO: `TrimVideoUseCase` only performs trimming. `SaveProjectUseCase` only persists data.
- ❌ DON'T: Combine trimming logic and database persistence in a single class/function.
- ❌ DON'T: Put business logic (e.g., trim window calculation) inside a View/ViewModel — it belongs in the Domain layer (see ARCHITECTURE.md).

### 1.2 Open/Closed Principle
- Code must be **open for extension, closed for modification**.
- ✅ DO: Use protocols/interfaces for video export operations (`VideoTrimmer` protocol on iOS, `VideoTrimmer` interface on Android) so new export strategies can be added without modifying existing Use Cases.
- ❌ DON'T: Add `if platform == X` branching logic scattered across multiple files when a protocol/interface abstraction would suffice.

### 1.3 Liskov Substitution Principle
- Any implementation of an interface/protocol must be fully substitutable without breaking behavior.
- ✅ DO: Ensure any `VideoTrimmer` implementation (native AVFoundation-based, or a future alternative) honors the same contract (inputs, outputs, error types).
- ❌ DON'T: Create a subclass/implementation that throws unexpected errors or silently changes behavior compared to its interface contract.

### 1.4 Interface Segregation Principle
- Prefer small, focused interfaces over large, monolithic ones.
- ✅ DO: Separate `ProjectRepository` (CRUD for projects) from `VideoFileRepository` (file storage operations) — don't force one interface to do both.
- ❌ DON'T: Create a single "God repository" interface handling database, file storage, and encryption all at once.

### 1.5 Dependency Inversion Principle
- High-level modules (Use Cases) must depend on **abstractions**, not concrete implementations.
- ✅ DO: `TrimVideoUseCase` depends on a `VideoTrimmer` protocol/interface, injected at construction time.
- ❌ DON'T: Hard-code `AVAssetExportSession` or `Media3 Transformer` calls directly inside a ViewModel or Use Case without an abstraction layer — this breaks testability.

---

## 2. Security Rules (Strict)

### 2.1 Offline-Only Enforcement
- **NEVER** add any networking code (no `URLSession`, no `Retrofit`/`OkHttp`, no analytics SDKs, no crash reporting SDKs that phone home) without explicit product owner approval. This app is offline-only by design, with **one narrow, explicit exception**: the platform's own in-app purchase SDK (StoreKit 2 on iOS, Google Play Billing on Android).
- The IAP exception is **strictly scoped**: only `PurchaseEntitlementUseCase` and `RestoreEntitlementUseCase` (and their underlying platform SDK calls) may perform network communication, and only through the official StoreKit/Play Billing APIs — never a custom HTTP call to a third-party or self-hosted endpoint for purchase verification.
- **NEVER** add any network code outside the IAP flow (e.g., no analytics, no crash reporting, no remote config, no ads) without explicit product owner approval — the IAP exception does not open the door to other networked features.
- **NEVER** introduce a dependency that silently makes network calls (e.g., some analytics/ad libraries do this by default) — audit all third-party dependencies before adding them. This applies doubly to any IAP-adjacent third-party library (e.g., a purchase-analytics SDK) — if it's not the platform's own StoreKit/Play Billing, treat it as out of scope without approval.

### 2.2 File & Data Security
- **ALWAYS** store trimmed output videos using platform encryption-at-rest mechanisms:
  - iOS: `NSFileProtectionComplete` attribute on files in `Documents/`.
  - Android: `EncryptedFile` (Jetpack Security library).
- **NEVER** write trimmed videos to public/shared storage (e.g., unrestricted external storage on Android) except as a deliberate, user-initiated "export/share" action — and even then, only as a transient decrypted copy discarded after the share completes (see ARCHITECTURE.md Section 6).
- **NEVER** log file paths, video content, or user-identifiable data to console/crash logs in release builds.
- **ALWAYS** validate imported file types/formats before processing — never trust file extensions alone; verify via native format-checking APIs.

### 2.3 Metadata Handling
- MVP default: **preserve metadata** (EXIF, creation date, device info) on the source video reference. Trimmed *output* files should only retain metadata that is technically necessary for playback (duration, codec, resolution) — avoid unnecessarily propagating original device/location metadata into exported files where the native export API allows selective control.
- If the native trim/export API strips or preserves metadata by default, **do not fight the platform default** — document actual behavior observed during implementation rather than assuming.
- Any future "strip metadata" toggle must be implemented as an explicit, visible user setting — never silent.

### 2.4 Permissions
- Request **only** the minimum permissions required:
  - iOS: Use `PHPickerViewController` (no photo library permission prompt required for basic picking).
  - Android: Use `Photo Picker` (`ActivityResultContracts.PickVisualMedia`) — avoids broad `READ_EXTERNAL_STORAGE` permission on modern Android versions.
- **NEVER** request broader storage/media permissions than strictly necessary for the current feature set.

### 2.5 Input Validation
- **ALWAYS** validate trim duration against the current entitlement tier at the domain layer, via `ValidateTrimDurationUseCase` — never trust UI-layer validation alone. Free tier: must be exactly one of `{1, 2, 3}` seconds. Paid tier: must be in range `1.0–5.0` seconds inclusive.
- **ALWAYS** validate that `sourceVideoDuration > trimDuration` for **every** selected video before allowing a trim operation to proceed — a duration option/value must be disabled/rejected project-wide if it fails this check for even one selected video.
- **ALWAYS** validate that a project contains between **1 and 10** source videos (free tier) or **1 and 20** (paid tier) before allowing save — reject attempts to exceed the current tier's cap with a clear, non-technical message, distinct from a hard error (it's a paywall opportunity, not a failure — see §2.6 and DESIGN.md §6.2/§6.3).
- **NEVER** assume a file URI/path is still valid at time of use — re-validate existence/accessibility before every read operation (source videos are user-controlled and may be deleted/moved outside the app). This applies to **every** video in a multi-video project, not just the first.
- **NEVER** derive the applicable tier limits (video cap, trim duration range, export quality) from anything other than `FetchEntitlementStatusUseCase`'s current cached value at the moment of the check — never from a stale UI state variable that could have been captured before an entitlement change.

### 2.6 In-App Purchase & Entitlement Security
- **NEVER** implement a custom backend, server, or third-party service for purchase verification — entitlement is derived **exclusively** from StoreKit 2 (`Transaction.currentEntitlements`) on iOS and Google Play Billing (`queryPurchasesAsync`) on Android. No exceptions in MVP.
- **ALWAYS** store the local entitlement cache using platform-idiomatic secure storage — iOS Keychain, Android `EncryptedSharedPreferences` — never plain `UserDefaults`/`SharedPreferences`, never a plaintext file, never inside the main SwiftData/Room database (see ARCHITECTURE.md §6.1, SCHEMA.md §2.3).
- **NEVER** treat a cached `isPurchased: true` value as authoritative without it having been derived from at least one real StoreKit/Play Billing verification during the current app install — this is a performance cache of a platform-verified fact, not an independent claim.
- **NEVER** let a failed or timed-out restore/verification check silently clear or downgrade an existing cached entitlement — only an explicit, successful "no purchase found" result from the store may downgrade cached entitlement (see ARCHITECTURE.md §8's error handling strategy for this exact scenario).
- **NEVER** hardcode, obfuscate-but-still-embed, or otherwise attempt to store the entitlement flag in a way designed to be user-editable/hackable "gracefully" — this is not a DRM system, but the app must not make bypassing the paywall trivially easy via a plainly-editable local file either. Platform secure storage (Keychain/`EncryptedSharedPreferences`) is the agreed-upon bar for MVP; do not weaken it for convenience.
- **NEVER** log purchase transaction details, receipts, or entitlement state changes in a way that could leak in release build logs (consistent with §2.2's general logging restriction, applied specifically to purchase data).
- **ALWAYS** treat `PurchaseEntitlementUseCase` and `RestoreEntitlementUseCase` as the **only** two code paths permitted to write to the entitlement cache — no other Use Case, ViewModel, or UI code may set `isPurchased` directly, per the Single Responsibility Principle (§1.1).
- **ALWAYS** handle refunds/entitlement revocation the same way as any other "not purchased" state going forward (re-lock paid-tier creation controls) — but **NEVER** let a revocation modify, hide, restrict, or delete any already-saved `Project` record. Existing projects are permanent historical records regardless of later entitlement changes in either direction (see SCHEMA.md §4, ARCHITECTURE.md §8).

---

## 3. Native Development Rules

### 3.1 Native-First Mandate
- **ALWAYS** attempt a native framework solution first (AVFoundation on iOS, Media3/MediaCodec on Android) before considering any third-party library.
- **NEVER** add FFmpeg or other third-party video processing libraries without documenting *why* the native API was insufficient — this must be justified in a code comment or PR description, not silently substituted.

### 3.2 No Cross-Platform Shared Code (MVP)
- **NEVER** introduce React Native, Flutter, Kotlin Multiplatform, or any shared-code framework into this project without explicit product owner approval — this violates the native-only architecture decision (see ARCHITECTURE.md).
- Each platform's codebase must remain fully independent.

### 3.3 Threading & Performance
- **NEVER** perform video trim/export operations on the main/UI thread.
- **ALWAYS** surface trim progress/errors back to the UI via the platform's standard async/reactive pattern (async-await + Combine on iOS, Coroutines + StateFlow on Android).
- **NEVER** load an entire video file into memory for processing — use streaming/native export APIs only.

---

## 4. Data & Storage Rules

- **NEVER** overwrite or modify the original source video file, under any circumstance.
- **ALWAYS** save trimmed output as a new, distinct file (per PRD requirement) — for multi-video projects, this is the single **merged** output file.
- **NEVER** silently drop a failed video from a multi-video merge and continue with the rest — if any video in the batch fails validation or trim, the entire project creation must fail with a clear error identifying which video failed. Partial/silent merges are not allowed in MVP.
- **ALWAYS** clean up temporary trimmed segment files if a merge operation fails partway through — no orphaned temp files left on disk.
- **ALWAYS** preserve and persist `orderIndex` for each source video exactly as confirmed by the user (post drag-to-reorder) — the merge step must consume videos in this exact order, never re-sorted by import order, file name, or any other implicit criteria.
- **ALWAYS** cascade-delete the associated merged output file and all child `SourceVideoItem` metadata rows when a Project record is deleted from the database — but **NEVER** delete any source video file.
- **NEVER** silently fail a database write — all persistence operations must surface errors through the Result/error-handling pattern defined in ARCHITECTURE.md.
- **ALWAYS** use the schema defined in SCHEMA.md as the source of truth for Project and SourceVideoItem data structure; any schema change requires updating SCHEMA.md in the same change set.

---

## 5. Code Style & Structure

### 5.1 General
- **ALWAYS** follow platform-idiomatic style guides: Swift API Design Guidelines (iOS), Kotlin Coding Conventions (Android).
- **NEVER** mix architectural patterns within a single feature (e.g., don't introduce MVC in one screen and MVVM in another — MVVM is standard across this project per ARCHITECTURE.md).
- **ALWAYS** name Use Cases with a verb-noun pattern reflecting a single action: `TrimVideoUseCase`, `SaveProjectUseCase`, `DeleteProjectUseCase`.

### 5.2 Error Handling
- **ALWAYS** use typed error handling (`Result<Success, Failure>` on iOS, sealed classes/`Result` on Android) — **NEVER** use generic/untyped exceptions for expected failure states (e.g., "video too short to trim").
- **ALWAYS** provide user-facing error messages that are actionable and non-technical (see DESIGN.md tone guidelines).

### 5.3 Testing (Enforced, Non-Negotiable)
- **ALWAYS** write unit tests for **every** piece of domain logic (all Use Cases listed in ARCHITECTURE.md Section 4) — this logic must be deterministic and testable without platform/UI dependencies.
- **NEVER** merge or submit a change that adds or modifies domain logic (Use Cases, validation rules, trim/merge/reorder calculations) without accompanying unit tests covering the happy path, documented validation failures, and boundary conditions (see ARCHITECTURE.md Section 9 for the required coverage bar).
- **NEVER** treat unit tests as optional, "nice to have," or something to add in a follow-up change — a Use Case without tests is considered incomplete, not done.
- **ALWAYS** add or update data-layer tests (in-memory DB) when modifying schema, repository, or persistence logic — including cascade-delete behavior.
- **NEVER** reduce or remove existing test coverage to make a change land faster — if a change appears to require deleting a test, treat that as a signal to stop and flag it rather than proceeding.

---

## 6. Git Branching & Commit Rules

### 6.1 Branching
- **NEVER** commit or push directly to `main`. All development must happen on a dedicated branch.
- **ALWAYS** prefix branch names with one of: `feat/`, `chore/`, or `fix/`, followed by a short, hyphenated description of the work.
  - ✅ `feat/trim-usecase-implementation`
  - ✅ `fix/merge-order-bug`
  - ✅ `chore/update-room-dependency`
  - ❌ `main` (never work directly here)
  - ❌ `my-branch`, `test123`, `patch-1` (no prefix, not descriptive)
- **NEVER** create a branch prefix outside this set (`feat/`, `chore/`, `fix/`) without explicit product owner approval.

### 6.2 Commit Messages
- **ALWAYS** prefix commit messages to match the branch type, in the form `<prefix>: <short description>`.
  - ✅ `feat: added trim use case implementation`
  - ✅ `fix: corrected merge order when reordering videos`
  - ✅ `chore: bumped Room dependency to 2.7.0`
  - ❌ `updated stuff`, `wip`, `fix bug` (no prefix, not descriptive)
- **ALWAYS** keep the description concise and specific to the change (avoid vague messages like "misc changes").
- **NEVER** bundle unrelated changes (e.g., a `feat` and a `fix`) into a single commit — split them so each commit has one clear prefix and purpose.

### 6.3 Environment Files
- **NEVER** commit `.env` files, or any file containing secrets, API keys, or environment-specific configuration, to the repository under any circumstance.
- **NEVER** read the contents of a `.env` file as part of completing a task, even if asked — treat this the same as any other credential-handling boundary in this document.
- **ALWAYS** ensure `.env` (and common variants: `.env.local`, `.env.*.local`, etc.) are present in `.gitignore` from the first commit of the project.
- Note: this app is offline-only and has no backend, so `.env` usage should be minimal to non-existent in MVP (e.g., no API keys expected) — but this rule applies unconditionally regardless of whether secrets currently exist in the project.

### 6.4 Pre-Commit Enforcement (Lint, Format, Affected Tests)
- **NEVER** create a commit without first running, locally: (a) the platform linter/formatter, and (b) unit tests affected by the changed files.
- **ALWAYS** run lint + format **before** running tests, so formatting issues don't mask genuine test failures.
- **ALWAYS** fix all lint/format violations before committing — **NEVER** commit with `--no-verify` or any other flag that bypasses configured pre-commit hooks, except in a genuine emergency, and only with explicit product owner approval logged in the PR description.
- **"Affected tests"** means: at minimum, all unit tests in the same module/feature as the changed files, plus any domain-layer test suite covering Use Cases touched directly or transitively (e.g., changing `CalculateTrimWindowUseCase` requires re-running `TrimVideoUseCase` and `MergeVideoSegmentsUseCase` tests too, since they depend on it).
- **NEVER** assume "it built fine" is equivalent to "lint/format/tests passed" — these are three distinct checks and all three must pass locally before a commit is created.
- See `CI.md` for the exact tooling, hook setup, and commands used to run these checks locally on each platform.

### 6.5 Monorepo Structure
- MidTrim is a **single monorepo** containing `ios/`, `android/`, and `docs/` (see ARCHITECTURE.md Section 1.1 for the authoritative folder layout) — **NEVER** propose or create a separate repository for either platform or for documentation without explicit product owner approval.
- **ALWAYS** place new documentation inside `docs/` and new platform code inside its respective `ios/` or `android/` directory — never at the repo root, and never cross-placed (e.g., no Kotlin files under `ios/`).
- **NEVER** let `ios/` and `android/` share source code or build artifacts — the monorepo houses both codebases side by side but they remain fully independent (per Section 10 of ARCHITECTURE.md).

### 6.6 Timeline Consultation & Updates
- **ALWAYS** consult `TIMELINE.md` (in `/docs/`) before starting any development work to understand:
  - What phase we're in.
  - What deliverables are in scope for the current phase.
  - What gate criteria must pass to advance to the next phase.
  - What blockers, if any, are blocking progress.
- **NEVER** start work on a deliverable from a future phase without product owner approval (e.g., don't build Phase 6 UI polish if Phase 5 video processing isn't done).
- **ALWAYS** update `TIMELINE.md` after every commit that passes lint/format + unit test checks (per RULES.md §6.4):
  - Check off completed deliverables (☑️ instead of ☐).
  - Update "Current Progress" percentage for the current phase.
  - Add any new blockers or notes discovered during implementation.
  - Move "Status" to "In Progress" if it was "Not Started", or to "Complete" if all deliverables ✅ and gate criteria pass.
- **NEVER** cherry-pick deliverables out of order or claim a phase is "done" without all gate criteria verified and documented in TIMELINE.md.
- TIMELINE.md updates must be in the **same commit** as the code/test changes that unlock them, or in an immediately-following commit with message `chore: updated TIMELINE.md — <phase> progress <X>%`.

---

## 7. Things Agents Must NEVER Do (Summary Checklist)

- ❌ Never add networking/analytics/telemetry code.
- ❌ Never overwrite or modify the original source video(s).
- ❌ Never store unencrypted trimmed/merged video files persistently.
- ❌ Never request broader OS permissions than strictly necessary.
- ❌ Never introduce a cross-platform framework or shared codebase.
- ❌ Never add a third-party video library without documenting why native APIs were insufficient.
- ❌ Never perform video processing (trim or merge) on the main/UI thread.
- ❌ Never allow trim durations outside `{1, 2, 3}` seconds (free tier) or `1.0–5.0` seconds (paid tier).
- ❌ Never allow a project to be saved with 0 videos, more than 10 videos on free tier, or more than 20 on paid tier.
- ❌ Never silently drop a failed video from a multi-video merge — fail the whole operation with a clear message instead.
- ❌ Never re-order or ignore the user-confirmed `orderIndex` when merging.
- ❌ Never silently swallow errors — all failures must be typed and surfaced to the UI.
- ❌ Never log sensitive file paths, user data, or purchase/entitlement details in release builds.
- ❌ Never commit or push directly to `main`.
- ❌ Never use a branch or commit prefix outside `feat/`, `chore/`, `fix/`.
- ❌ Never commit a `.env` file or read its contents.
- ❌ Never merge domain logic changes without accompanying unit tests.
- ❌ Never commit without running lint/format and affected unit tests locally first.
- ❌ Never bypass pre-commit hooks (e.g., `--no-verify`) without logged product owner approval.
- ❌ Never create a separate repo for a platform or for docs — this project is a single monorepo.
- ❌ Never start work on a future phase's deliverables without product owner approval.
- ❌ Never claim a phase is complete without all gate criteria verified.
- ❌ Never add network code outside the StoreKit/Play Billing IAP flow without product owner approval.
- ❌ Never implement a custom/third-party backend for purchase verification — StoreKit 2 / Play Billing only.
- ❌ Never store the entitlement cache anywhere but platform secure storage (Keychain / `EncryptedSharedPreferences`).
- ❌ Never let a failed or timed-out restore check silently clear or downgrade a cached entitlement.
- ❌ Never write to the entitlement cache from anywhere other than `PurchaseEntitlementUseCase` or `RestoreEntitlementUseCase`.
- ❌ Never retroactively change an already-saved project's `exportQualityTier`, `trimDuration`, or `videoCount` after a later purchase **or refund** — existing projects are permanent historical records, not re-processed in either direction.

## 8. Things Agents Must ALWAYS Do (Summary Checklist)

- ✅ Always validate inputs at the domain layer, not just the UI layer.
- ✅ Always follow SOLID principles as detailed in Section 1.
- ✅ Always keep PRD.md, DESIGN.md, ARCHITECTURE.md, and SCHEMA.md in sync with implementation — if code changes contradict these docs, update the docs in the same change set.
- ✅ Always default to native-first solutions.
- ✅ Always encrypt persisted video output files at rest.
- ✅ Always cascade-delete output files and child metadata rows (never source files) when a project is deleted.
- ✅ Always clean up temporary trimmed segments if a merge fails partway through.
- ✅ Always work on a correctly-prefixed branch (`feat/`, `chore/`, `fix/`) and write matching commit messages.
- ✅ Always write unit tests for every Use Case before considering it complete.
- ✅ Always run lint, format, and affected unit tests locally before every commit (see CI.md for setup).
- ✅ Always consult TIMELINE.md before starting work to understand current phase and scope.
- ✅ Always update TIMELINE.md after passing lint/format + unit tests to reflect progress.
- ✅ Always derive tier limits (video cap, trim range, export quality) from `FetchEntitlementStatusUseCase`'s current cached value, never a stale or assumed state.
- ✅ Always show locked free-tier controls as disabled-with-lock-icon rather than hidden, per the inline paywall pattern (DESIGN.md §6.2/§6.3).
