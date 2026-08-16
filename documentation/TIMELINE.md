# TIMELINE.md — MVP Development Phases & Progress

## 👉 Current Phase: Phase 4 — UI Layer — Presentation & ViewModels
*(Update this pointer every time a phase's status changes. This is the first thing to check before starting any work.)*

## Overview
MidTrim MVP development is organized into **8 sequential phases**, each with clear deliverables and gate criteria. This document tracks progress through each phase and serves as the source of truth for the development roadmap. **No specific calendar dates are assigned** — phases advance as criteria are met, not on a fixed schedule.

---

## Phase 1: Project Setup & Foundations
**Status:** `Complete` | **Current Progress:** 100%

### Deliverables
- [x] Repository initialized with monorepo structure (`ios/`, `android/`, `docs/`, `.github/workflows/`, `scripts/`) per ARCHITECTURE.md §1.1
- [x] GitHub Actions CI pipelines live (`android-ci.yml`, `ios-ci.yml`)
  - [x] Android: lint + format + unit tests passing
  - [x] iOS: lint + format + unit tests passing
- [x] Pre-commit hooks configured and tested locally on both platforms
- [x] `.gitignore` finalized (`.env`, build artifacts, IDE config, `.DS_Store`, etc.)
- [x] Base Gradle project structure (Android) with placeholder app
- [x] Base Xcode project structure (iOS) with placeholder app
- [x] Android: Room DB schema (Project + SourceVideoItem entities) created, DAO stubs
- [x] iOS: SwiftData models (Project + SourceVideoItem) created, repository stubs
- [x] CI pipelines green on empty project (first merge to `main`)

### Effort Estimate
- Android: 1 week
- iOS: 1 week
- (Parallelizable)

### Gate Criteria (Must Pass Before Phase 2)
- ✅ Both CI pipelines green
- ✅ Local pre-commit hooks functional on both platforms
- ✅ First feature branch (`feat/project-setup-complete`) successfully merged to `main`
- ✅ Both developers can `git clone`, `git checkout`, run hooks, and pass CI without errors

*Governing rules for this phase: RULES.md §6 (Git branching, commits, monorepo, pre-commit enforcement); see CI.md for exact workflow/hook implementation.*

### Blockers / Notes
- (None yet)

---

## Phase 2: Domain Layer & Use Cases
**Status:** `Complete` | **Current Progress:** 100%

### Deliverables
**All 16 Use Cases from ARCHITECTURE.md §4, per platform (Swift + Kotlin):**

- [x] **ImportVideosUseCase** — validate 1–10 (free) / 1–20 (paid) videos, extract metadata (duration, file size, resolution)
  - [x] Unit tests: happy path, unsupported format, exceeds tier cap, empty selection
- [x] **ReorderVideosUseCase** — drag-to-reorder state management (pure function)
  - [x] Unit tests: valid reorder, boundary cases (empty list, single item)
- [x] **CalculateTrimWindowUseCase** — pure: given duration + trim duration, return (start, end) centered
  - [x] Unit tests: valid trim, video = trim duration, video < trim duration, boundary edge cases
- [x] **CalculateMergedDurationUseCase** — pure: given trim duration + video count, return total duration
  - [x] Unit tests: 1 video, 10 videos, 20 videos, valid durations only
- [x] **ValidateTrimDurationUseCase** *(new — IAP-related)* — pure: given requested duration + entitlement status, return allowed/rejected
  - [x] Unit tests: free tier accepts 1/2/3 only, paid tier accepts 1.0–5.0, free tier rejects custom values, paid tier rejects >5.0
- [x] **TrimVideoUseCase** — native trim operation per video (AVFoundation / Media3), delegates to ValidateTrimDurationUseCase
  - [x] Unit tests: success, corrupted file, unsupported codec, file not found, rejected duration for current tier
- [x] **MergeVideoSegmentsUseCase** — concatenate trimmed segments, normalize resolution/frame rate, encode at resolved quality
  - [x] Unit tests: success, empty segment list, 1 segment, 20 segments, mismatched formats
- [x] **ResolveExportQualityUseCase** *(new — IAP-related)* — pure: given entitlement status + source resolution, return target export resolution
  - [x] Unit tests: free tier always caps at 720p, paid tier returns source resolution, paid tier never upscales beyond source
- [x] **GenerateThumbnailUseCase** *(new — was missing from this checklist, now added)* — given the merged output video's URI, extract a single frame at t=0 and write it as a JPEG, returning the new file's URI (see ARCHITECTURE.md §5.2)
  - [x] Unit tests: successful extraction produces a valid JPEG at the expected path, extraction failure causes the overall project-save to fail cleanly (no `Project` row persisted with a missing `thumbnailURI`)
- [x] **SaveProjectUseCase** — persist project + SourceVideoItem rows to DB, including trimDuration/wasCustomDuration/exportQualityTier/**thumbnailURI**
  - [x] Unit tests: success, duplicate name, invalid project structure, correct quality tier recorded
- [x] **FetchProjectsUseCase** — retrieve all projects, sorted by createdAt descending
  - [x] Unit tests: empty DB, multiple projects, ordering
- [x] **DeleteProjectUseCase** — cascade delete project + output file (never source videos)
  - [x] Unit tests: success, file not found, DB cascade verified
- [x] **RenameProjectUseCase** — update project name
  - [x] Unit tests: success, empty name, duplicate name
- [x] **PurchaseEntitlementUseCase** *(new — IAP-related)* — initiate platform purchase flow, persist entitlement on success
  - [x] Unit tests (against mocked StoreKit/Play Billing client): successful purchase updates cache, cancellation surfaces non-error state, payment failure surfaces retryable error
- [x] **RestoreEntitlementUseCase** *(new — IAP-related)* — query platform purchase records, update cache if found; used by both auto-restore-on-launch and manual restore button
  - [x] Unit tests (against mocked client): purchase found updates cache, no purchase found leaves cache as default, network/store failure preserves existing cached entitlement (never silently downgrades)
- [x] **FetchEntitlementStatusUseCase** *(new — IAP-related)* — pure read of cached local entitlement state
  - [x] Unit tests: returns cached true/false correctly, does not trigger a live store query

### Effort Estimate
- Android: 3–4 weeks (increased from 2–3 to account for 5 additional IAP-related Use Cases)
- iOS: 3–4 weeks
- (Parallelizable)

### Gate Criteria (Must Pass Before Phase 3)
- ✅ **100% of domain logic is unit-tested** — every Use Case has tests covering happy path + all documented failures + boundary conditions (per ARCHITECTURE.md §9, RULES.md §5.3)
- ✅ All tests passing in CI, including IAP Use Cases tested against mocked StoreKit/Play Billing clients (see CI.md §5.1)
- ✅ Code review confirms SOLID principle adherence (especially Single Responsibility, Dependency Inversion — see RULES.md §1)
- ✅ Code review confirms only `PurchaseEntitlementUseCase`/`RestoreEntitlementUseCase` can write to the entitlement cache (RULES.md §2.6)
- ✅ No untested domain logic ships from this phase

*Governing rules for this phase: RULES.md §1 (SOLID), §2.5 (input validation), §2.6 (IAP/entitlement security), §5.3 (testing enforcement).*

### Blockers / Notes
- (None yet)

---

## Phase 3: Data Layer & Persistence
**Status:** `Complete` | **Current Progress:** 100%

### Deliverables

**Android:**
- [x] Room Database setup + initialization (AppDatabase.kt created)
- [x] ProjectEntity and SourceVideoItemEntity fully defined with cascade delete rules
- [x] ProjectDao (CRUD, fetch by ID, sorted queries)
- [x] SourceVideoItemDao (CRUD, fetch by projectId in order)
- [x] Room Migrations (v1 baseline confirmed — no migration needed per SCHEMA.md §6)
- [x] ProjectRepository implementation (ProjectRepositoryImpl via Room)
- [x] VideoFileRepository implementation (protocol extended with save/create/cleanup methods; encrypt/decrypt/temp-segment handling)
- [x] **EntitlementCache implementation via `EncryptedSharedPreferences`** (not Room) per SCHEMA.md §2.3 — read/write `isPurchased`, `productId`, `lastVerifiedAt`
- [x] **Entity ↔ Domain mappers** (EntityMappers.kt with round-trip conversion)
- [x] **androidx.security-crypto dependency added**
- [x] **Test-time DI wiring — androidTest directory with in-memory DB tests**
- [x] In-memory DB tests: insert, update, delete, cascade delete, ordering
- [x] Entitlement cache tests: read/write against a real `EncryptedSharedPreferences` instance
- [x] Mapper correctness tests: round-trip conversion for both entities

**iOS:**
- [x] SwiftData models (Project + SourceVideoItem) with cascade delete rules
- [x] ModelContainer setup + configuration in MidTrimApp.swift
- [x] ProjectRepository implementation (SwiftDataProjectRepository via SwiftData)
- [x] VideoFileRepository implementation (protocol extended with save/create/cleanup methods; encrypt/decrypt/temp-segment handling via DefaultVideoFileRepository)
- [x] **EntitlementCache implementation via Keychain** (not SwiftData) per SCHEMA.md §2.3 — read/write `isPurchased`, `productId`, `lastVerifiedAt`
- [x] **Model ↔ Domain mappers** (ModelMappers.swift with round-trip conversion)
- [x] **Stale Data/ protocol files deprecated**
- [x] **Test-time DI wiring — in-memory ModelContainer tests**
- [x] In-memory SwiftData tests: insert, update, delete, cascade delete, ordering
- [x] Entitlement cache tests: read/write against a real Keychain wrapper
- [x] Mapper correctness tests: round-trip conversion for both models

**Both platforms:**
- [x] Verify encryption at rest: NSFileProtectionComplete (iOS) / EncryptedFile (Android) for video files; Keychain / EncryptedSharedPreferences for entitlement cache
- [x] Entitlement cache read/write survives app restart (Android + iOS tests write → re-initialize → read back)
- [ ] Integration test: temp segment cleanup on merge failure *(blocked on Phase 6 — trim/merge pipeline needed)*
- [ ] Integration test: source video files never modified/overwritten *(blocked on Phase 6)*
- [ ] Integration test: orphaned files never left on disk *(blocked on Phase 6)*

### Effort Estimate
- Android: 2–3 weeks (entitlement cache work is small, doesn't meaningfully add to estimate)
- iOS: 2–3 weeks
- (Parallelizable)

### Gate Criteria (Must Pass Before Phase 4)
- ✅ All data-layer unit tests passing in CI
- ✅ In-memory DB tests confirm DAO/repository behavior
- ✅ Encryption-at-rest confirmed working (not just "yes, we called the API") — for both video files and the entitlement cache
- ✅ Integration tests verify file lifecycle: cascade delete confirmed working; temp segment cleanup, source-file-immutability, and orphan-file tests deferred to Phase 6 per blockers below
- ✅ Code review confirms proper error handling in persistence layer
- ✅ Code review confirms entitlement cache lives in Keychain/`EncryptedSharedPreferences` only, never the main DB (RULES.md §2.6)

*Governing rules for this phase: RULES.md §2.2 (file & data security), §2.6 (IAP/entitlement security), §4 (data & storage rules).*

### Blockers / Notes
- Temp segment cleanup, source-file-immutability, and orphan-file tests deferred to Phase 6 — they require a running trim/merge pipeline to produce the conditions they verify.
- Room Migrations checkbox confirmed: v1 baseline, no prior production schema, no Migration objects required per SCHEMA.md §6.

---

## Phase 4: UI Layer — Presentation & ViewModels
**Status:** `In Progress` | **Current Progress:** 80%

### Deliverables

**Android (Jetpack Compose):**
- [x] ProjectListScreen (list projects, empty state, delete/rename, FAB "New Project", **Upgrade entry point banner for free-tier users** per DESIGN.md §6.1)
- [x] VideoSelectionScreen (multi-select picker, drag-to-reorder list, live merged duration total, **"+" shows lock icon past 10 videos on free tier** per DESIGN.md §6.2)
- [x] TrimDurationScreen (segmented control 1s/2s/3s, **"Custom" 4th segment shown locked on free tier** per DESIGN.md §6.3, preview, disabled options logic, **720p/quality badge with lock icon on free tier**)
- [x] NameProjectScreen (text input with default, save/discard CTA)
- [x] ProjectListViewModel (observe projects flow, delete, rename, navigation, **read entitlement status to show/hide upgrade banner**)
- [x] VideoSelectionViewModel (manage selected videos in-memory, reorder, trim duration selection, merged total calculation, **enforce tier-appropriate video cap and duration options via ValidateTrimDurationUseCase/FetchEntitlementStatusUseCase**)
- [x] Navigation: Compose Navigation or similar (ProjectList → VideoSelection → TrimDuration → NameProject → back to ProjectList)
- [ ] Basic UI tests (critical flow: select → reorder → trim → save → delete), **tested against both a fake free-tier and fake paid-tier entitlement state** *(moved to Phase 8)*

**iOS (SwiftUI):**
- [x] Same four screens as Android, SwiftUI implementation, same tiered lock-affordance behavior
- [x] Same ViewModels (adapted to @Observable or ObservableObject)
- [x] Navigation: NavigationStack or NavigationView (iOS 16+ or fallback)
- [ ] Basic UI tests (XCUITest critical flow), tested against both fake free-tier and paid-tier entitlement states *(moved to Phase 8)*

**Both platforms:**
- [ ] No crashes on typical workflow
- [ ] All screens render and navigate correctly
- [ ] UI tests pass for critical flow (import → reorder → trim → save) *(moved to Phase 8)*
- [ ] Lock-affordance UI (disabled state + lock icon, tap-to-open-paywall stub) renders correctly on all applicable controls — **actual Paywall screen navigation target can be a placeholder/stub in this phase**, since the real Paywall screen is built in Phase 5

### Effort Estimate
- Android: 3–4 weeks (modest increase for tiered UI state handling)
- iOS: 3–4 weeks
- (Highly parallelizable, but coordinate on UX parity)

### Gate Criteria (Must Pass Before Phase 5)
- ✅ All four screens render without errors
- ✅ Critical user flow (import → trim → save) works end-to-end for both free and paid entitlement states
- ✅ UI tests pass on both platforms
- ✅ No crashes on typical device (tested on simulator/emulator at minimum)
- ✅ Code review confirms ViewModel logic is testable (dependencies injected, no tightly-coupled UI logic)
- ✅ Lock affordances correctly reflect `FetchEntitlementStatusUseCase`'s cached value, not a hardcoded/assumed tier

*Governing rules for this phase: RULES.md §2.5 (input validation), §2.6 (IAP/entitlement security — UI must read, never write, entitlement state).*

### Blockers / Notes
- (None yet)

---

## Phase 5: In-App Purchase & Entitlement
**Status:** `In Progress` | **Current Progress:** 45%

### Deliverables

**Android:**
- [x] Google Play Billing Library integrated (`BillingClient` setup, connection lifecycle handling)
- [ ] Non-consumable product configured in Play Console (`com.midtrim.fullunlock` or final product ID) for later manual/beta testing — CI itself uses mocked `BillingClient`, not the real product
- [x] `PurchaseEntitlementUseCase` wired to real `BillingClient.launchBillingFlow`
- [x] `RestoreEntitlementUseCase` wired to real `BillingClient.queryPurchasesAsync`
- [x] Automatic restore-on-launch wired into app startup (non-blocking, per ARCHITECTURE.md §7)
- [x] Paywall screen (Compose) built per DESIGN.md §6.5
- [x] Help/Settings screen (Compose) built per DESIGN.md §6.6, including manual "Restore Purchases" button
- [x] Lock-affordance tap targets (from Phase 4 stubs) now navigate to the real Paywall screen
- [x] Unit tests against mocked `BillingClient`: success, cancellation, failure, restore-found, restore-not-found, restore-network-failure-preserves-cache, **restore-detects-refund-downgrades-cache-without-touching-existing-projects**

**iOS:**
- [ ] StoreKit 2 integrated (`Product`, `Transaction` APIs)
- [ ] Non-consumable product configured in App Store Connect + local `.storekit` configuration file for testing (per CI.md §5.2)
- [ ] `PurchaseEntitlementUseCase` wired to real `Product.purchase()`
- [ ] `RestoreEntitlementUseCase` wired to real `Transaction.currentEntitlements`
- [ ] Automatic restore-on-launch wired into app startup (non-blocking, per ARCHITECTURE.md §7)
- [ ] Paywall screen (SwiftUI) built per DESIGN.md §6.5
- [ ] Help/Settings screen (SwiftUI) built per DESIGN.md §6.6, including manual "Restore Purchases" button
- [ ] Lock-affordance tap targets (from Phase 4 stubs) now navigate to the real Paywall screen
- [ ] Unit/UI tests using `SKTestSession` (StoreKit Testing framework, per CI.md §5.2): success, cancellation, failure, restore-found, restore-not-found, **refund simulation (StoreKit Testing supports simulating refunds via `Transaction.revocationDate`) confirms re-lock without affecting existing projects**

**Both platforms:**
- [ ] Purchasing successfully unlocks: custom trim duration control, 20-video cap, full-quality export — verified end-to-end in a test/sandbox environment
- [ ] Existing free-tier projects are confirmed **not** retroactively upgraded after a later purchase (per PRD §8.4/§8.7)
- [ ] Haptic feedback on purchase/restore success (per DESIGN.md §7)
- [ ] Entitlement state correctly cached and read on subsequent app launches without re-querying the store unnecessarily

### Effort Estimate
- Android: 2 weeks
- iOS: 2 weeks
- (Parallelizable, but both depend on Phase 4's lock-affordance stubs being in place)

### Gate Criteria (Must Pass Before Phase 6)
- ✅ A test/sandbox purchase successfully unlocks all three paid features on both platforms
- ✅ A test/sandbox restore (fresh install + existing purchase) correctly re-unlocks features on both platforms
- ✅ All IAP unit tests passing in CI against mocked/StoreKit-Testing clients (see CI.md §5)
- ✅ Code review confirms RULES.md §2.6 is fully respected (secure storage only, no unauthorized cache writes, no silent downgrade on failed restore)
- ✅ Refund/revocation handling verified: a simulated refund re-locks paid-tier creation controls without altering any existing saved project
- ✅ Paywall and Help/Settings screens match DESIGN.md §6.5/§6.6
- ✅ Previously-created free-tier projects remain untouched after a test purchase (no retroactive re-processing)

*Governing rules for this phase: RULES.md §2.1 (offline-only exception scope), §2.6 (IAP/entitlement security), §7 (Concurrency — non-blocking restore); DESIGN.md §6.5–§6.6.*

### Blockers / Notes
- Phase 5 cannot be marked `Complete` until the iOS StoreKit 2 PR is also merged to main (Android PR #xx provides the Android half only).
- (None yet)
- Note: real end-to-end Play Billing verification (beyond mocked/unit-tested logic) requires the app to exist on at least a Play Console internal testing track — if this isn't set up yet, flag it here as a blocker until Play Console access is ready.

---

## Phase 6: Video Processing — Trim & Merge
**Status:** `Not Started` | **Current Progress:** 0%

### Deliverables

**Android (Media3 Transformer):**
- [ ] Media3 Transformer integrated into TrimVideoUseCase
- [ ] MergeVideoSegmentsUseCase implemented (concatenate segments, normalize resolution/frame rate/aspect ratio, **encode at the resolution returned by ResolveExportQualityUseCase**)
- [ ] Trim failure handling: temp file cleanup, error surfacing to UI
- [ ] Test with real videos: MP4, MOV (if supported); multiple resolutions; durations
- [ ] Performance validation: single trim <10s, 20-video merge <45s (revised up slightly from the original 10-video/30s target given the higher paid-tier cap)
- [ ] **Verify 720p cap is correctly applied for free-tier exports, and original-resolution output for paid-tier exports, across a range of source resolutions (e.g., 480p, 720p, 1080p, 4K sources)**

**iOS (AVFoundation):**
- [ ] AVAssetExportSession + AVMutableComposition integrated into TrimVideoUseCase
- [ ] MergeVideoSegmentsUseCase implemented (AVMutableVideoComposition for normalization, **encode at the resolution returned by ResolveExportQualityUseCase**)
- [ ] Trim failure handling: temp file cleanup, error surfacing to UI
- [ ] Test with real videos: same formats + resolutions as Android
- [ ] Performance validation: same targets as Android
- [ ] **Verify 720p cap and original-resolution paid-tier output across the same range of source resolutions as Android**

**Both platforms:**
- [ ] Trim + merge produce playable output (verified on device/real app, not just CI)
- [ ] Output plays correctly on both platforms
- [ ] Tested on common social apps (Instagram, TikTok, if possible)
- [ ] No quality loss vs native camera roll exports **on the paid tier specifically** (720p free-tier output is an intentional, expected reduction, not a bug)
- [ ] Edge case testing: 1-second videos, exactly-trim-duration videos, largest-practical video sizes, **exactly-5-second custom paid-tier trims**

### Effort Estimate
- Android: 2–3 weeks
- iOS: 2–3 weeks
- (Sequential debugging, limited parallelization possible)

### Gate Criteria (Must Pass Before Phase 7)
- ✅ Trim + merge produce playable, verified output on real devices
- ✅ Output plays on both platforms + tested social apps
- ✅ No quality loss observed on paid tier (subjective, but critical); 720p cap confirmed working as intended on free tier
- ✅ Performance within acceptable bounds (<10s single trim, <45s 20-video merge)
- ✅ Edge cases handled gracefully (no crashes, clear error messages)
- ✅ Temp files cleaned up on success and failure
- ✅ **Merge normalization quality checkpoint passed** (per ARCHITECTURE.md §5.1): a real mixed-resolution merge test (e.g., 720p + 4K sources, both orderings) has been visually inspected on-device, and the "first-video-as-reference" strategy is confirmed acceptable for casual social sharing — or, if not, escalated to the product owner for a strategy pivot before this phase is marked complete.

*Governing rules for this phase: RULES.md §3 (native development rules), §4 (no partial/silent merges).*

### Blockers / Notes
- (None yet)

---

## Phase 7: Design Polish & Brand Integration
**Status:** `Not Started` | **Current Progress:** 0%

### Deliverables
- [ ] Color palette (DESIGN.md §3) applied throughout both platforms
  - [ ] Primary bg, elevated, surface, accent, text colors verified
- [ ] Typography (DESIGN.md §4) consistent
  - [ ] Title, body, caption sizes applied; line heights correct
- [ ] Spacing & layout (DESIGN.md §5)
  - [ ] 8pt base grid used; corners, margins, padding per spec
- [ ] Haptic feedback wired
  - [ ] Duration selection, save confirmation, delete confirmation
- [ ] Motion & transitions (DESIGN.md §7)
  - [ ] 200–250ms fades/slides; ease-in-out timing
- [ ] Empty states, loading indicators, error states visually polished
- [ ] Accessibility audit (DESIGN.md §8)
  - [ ] VoiceOver (iOS) + TalkBack (Android) tested
  - [ ] WCAG AA color contrast verified
  - [ ] Dynamic Type (iOS) / font scaling (Android) tested at 130%+
- [ ] No visual regressions on multiple screen sizes (small, normal, large phones)

### Effort Estimate
- Combined: 2–3 weeks (can run partially in parallel with late Phase 6 non-blocking work)

### Gate Criteria (Must Pass Before Phase 8)
- ✅ Design review against DESIGN.md passes (all color, type, spacing rules applied)
- ✅ Accessibility audit confirms WCAG AA compliance
- ✅ VoiceOver/TalkBack test pass on representative device
- ✅ Font scaling tested at minimum 130% without layout breaks
- ✅ No visual regressions on small/large screens

*Governing rules for this phase: DESIGN.md §3–§8 (color, typography, spacing, motion, accessibility).*

### Blockers / Notes
- (None yet)

---

## Phase 8: Beta Testing & Bug Fixes
**Status:** `Not Started` | **Current Progress:** 0%

### Deliverables
- [ ] Internal beta builds created (TestFlight for iOS, Google Play internal testing for Android)
- [ ] **UI tests (deferred from Phase 4):**
  - [ ] Android: Basic Compose UI tests covering critical flow (select → reorder → trim → save → delete), tested against both fake free-tier and fake paid-tier entitlement state
  - [ ] iOS: Basic XCUITest covering critical flow, tested against both fake free-tier and paid-tier entitlement states
- [ ] Real device testing across multiple models:
  - [ ] iOS: 3+ models (e.g., iPhone 13, iPhone 15, iPhone SE)
  - [ ] Android: 3+ devices (e.g., Pixel 5, Samsung Galaxy, OnePlus; different OS versions API 26–35)
- [ ] **Real end-to-end IAP testing** (per CI.md §5.1's note that this can't be done in CI):
  - [ ] iOS: real sandbox Apple ID test purchase, verify unlock, verify restore on reinstall
  - [ ] Android: Play Console license tester account, real test purchase via internal testing track, verify unlock, verify restore on reinstall
  - [ ] Verify automatic restore-on-launch works correctly on a fresh install with a prior purchase
  - [ ] Verify manual "Restore Purchases" button works as a failsafe when automatic restore is (artificially) delayed or skipped
  - [ ] Verify purchasing does not retroactively affect previously-created free-tier projects
- [ ] Bug triage (P0: crashes; P1: UX issues; P2: polish)
- [ ] Fix all P0 crashes
- [ ] Fix critical P1 issues (trim/merge failures, data loss, incorrect output, **IAP/restore failures**)
- [ ] P2 polish deferred to post-launch if needed
- [ ] Performance re-validated on real devices

### Effort Estimate
- 3–4 weeks (iterative: test → fix → re-test)

### Gate Criteria (Must Pass Before Phase 9)
- ✅ Zero crashes on typical workflows
- ✅ Merge/trim success rate ≥99.5% (at most 1 failure per 200 operations)
- ✅ Performance within acceptable bounds on tested devices
- ✅ All P0 + critical P1 issues fixed and re-tested
- ✅ No data loss observed (original videos untouched, output files created correctly)

### Blockers / Notes
- (None yet)

---

## Phase 9: Pre-Release Preparation
**Status:** `Not Started` | **Current Progress:** 0%

### Deliverables
- [ ] App Store metadata drafted (name, description, keywords, category, rating, privacy policy link, **in-app purchase disclosure**)
- [ ] Play Store metadata drafted (same, plus app screenshots in required formats/sizes, **in-app product listing for the one-time unlock**)
- [ ] Privacy policy finalized — app itself is offline-only, no tracking, no data collection (see RULES.md §2.1), **but must disclose that purchase transactions are processed by Apple/Google directly** (standard StoreKit/Play Billing disclosure, not custom data collection by MidTrim itself)
- [ ] Screenshots/preview videos for both stores (3–5 per platform, showing critical flow, **plus at least one screenshot showcasing the paid-tier features/paywall**)
- [ ] iOS signing:
  - [ ] Distribution certificate created + secured in GitHub Secrets
  - [ ] App Store provisioning profile created
  - [ ] Build signed and tested
  - [ ] **In-app purchase product (`com.midtrim.fullunlock` or final ID) submitted for review alongside the app binary** (required — IAP products need App Store review too)
- [ ] Android signing:
  - [ ] Keystore finalized + secured in GitHub Secrets
  - [ ] APK/AAB signed and tested
  - [ ] **In-app product configured and activated in Play Console** prior to submission
- [ ] Version number set to 1.0.0
- [ ] Release notes drafted (mentioning the one-time unlock as a launch feature)
- [ ] Final QA pass on signed build (ensure no regressions from signing process), **including a final real purchase/restore test on the signed release build specifically** (sandbox/test purchases can behave differently on a release-signed build vs. a debug build)

### Effort Estimate
- 1–2 weeks (mostly administrative)

### Gate Criteria (Must Pass Before Launch)
- ✅ All metadata reviewed and finalized
- ✅ Privacy policy accurate and linked, including IAP/purchase-processing disclosure
- ✅ Signing verified (build plays correctly post-signature)
- ✅ Screenshots + preview accurate and pass store guidelines
- ✅ IAP product configured and tested on both platforms' store consoles
- ✅ Final QA confirms no regressions, including a real purchase/restore test on the release-signed build

### Blockers / Notes
- (None yet)

---

## Overall Progress Summary

| Phase | Status | Progress | Next Gate |
|-------|--------|----------|-----------|
| 1. Setup | Complete | 100% | CI green, first merge |
| 2. Domain | Complete | 100% | 100% tests, code review |
| 3. Data | Complete | 100% | Integration tests pass; 3 items blocked on Phase 6 |
| 4. UI | In Progress | 80% | Critical flow works |
| 5. In-App Purchase & Entitlement | In Progress | 45% | Test purchase/restore unlocks features |
| 6. Video Processing | Not Started | 0% | Playable output, perf OK, quality tiers verified |
| 7. Design Polish | Not Started | 0% | Accessibility audit pass |
| 8. Beta Testing | Not Started | 0% | Zero crashes, 99.5% success, real IAP verified |
| 9. Pre-Release | Not Started | 0% | Metadata ready, signed, IAP product configured |

---

## How to Use This Document

**Before Starting Work:**
- Read the current phase description and gate criteria (per RULES.md §6, agents must consult TIMELINE.md before development).
- Understand what must be delivered and what criteria unlock the next phase.
- Check "Blockers / Notes" — any known issues?

**During Work:**
- Break deliverables into feature branches (`feat/...`, `fix/...`, `chore/...` per RULES.md §6.1).
- Lint, format, and unit test pass locally before commit (RULES.md §6.4).

**After Passing Lint + Tests:**
- Update TIMELINE.md as part of the same commit whenever practical. If the code change and the TIMELINE.md update can't reasonably land together (e.g., a PR merge triggers the update), use an immediately-following commit with message `chore: updated TIMELINE.md — <phase> progress <X>%` — don't leave TIMELINE.md stale for more than one commit.
  - Check off completed deliverables.
  - Update "Current Progress" percentage for the phase.
  - Add any blockers discovered.
  - Move "Status" to "In Progress" if not already.
  - Move to "Complete" when all deliverables ✅ and gate criteria pass.
  - Update the "👉 Current Phase" pointer at the top of this document if the phase changed.

**If a Gate Criterion Fails or a Regression Is Found:**
- **Never** mark a phase "Complete" if any gate criterion is unverified or failing — revert "Status" to `In Progress` if it was prematurely marked complete.
- Log the failure under that phase's "Blockers / Notes" with enough detail for the next person/agent to understand it (what failed, suspected cause if known).
- Do not advance the "👉 Current Phase" pointer until the blocker is resolved and the gate re-verified.
- If a regression is discovered in an earlier "Complete" phase while working on a later phase, add a note under the earlier phase's "Blockers / Notes" and flag it — don't silently fix it without recording that the phase's gate was violated.

**At Phase Boundaries:**
- Verify all gate criteria are met before advancing to the next phase.
- Update "Status" to "Complete" for the finished phase.
- Update the "👉 Current Phase" pointer to the next phase.
- Ensure next phase's first tasks are clear and prioritized.

**For CI/CD Deliverables Specifically (Phase 1 and ongoing):**
- Refer to `CI.md` for the exact GitHub Actions workflow specs, branch protection rules, and pre-commit hook setup — TIMELINE.md tracks *whether* these are done, `CI.md` defines *how* to do them.

---

## Revision History

| # | Change | Phase(s) Affected |
|---|--------|-------------------|
| 1 | Created TIMELINE.md with 8 phases | All |
| 2 | Phase 1 completed: repo structure, CI pipelines, linting configs, pre-commit hooks, .gitignore, data layer stubs | Phase 1 |
| 3 | Phase 2 completed: all 16 Use Cases implemented per platform (Swift + Kotlin), domain entities, error types, repository/processing/IAP protocols, and 100% unit test coverage for all Use Cases across both platforms. | Phase 2 |
| 4 | Phase 3 completed: Room + SwiftData data layers, encrypted file repositories, Keychain/EncryptedSharedPreferences entitlement caches, entity mappers, in-memory DB tests. 3 file-lifecycle integration tests deferred to Phase 6. Phase 4 started with design system foundation. | Phase 3, Phase 4 |

*(Use a simple incrementing sequence number, not a calendar date, consistent with this document's no-fixed-dates approach. Append a new row for each material change to scope, gate criteria, or phase structure — not for routine checkbox/progress updates.)*
