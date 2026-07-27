# CI.md — Continuous Integration & Local Development Workflow

## 1. Overview
MidTrim uses **GitHub Actions** (free tier) for CI/CD across both native codebases (iOS + Android). Since the two platforms are fully independent (per ARCHITECTURE.md), CI is split into **two separate workflows** that run in parallel and only trigger based on changes to their respective directories.

This document covers:
- GitHub Actions pipeline structure (per platform)
- Branch protection rules
- Secrets management (signing, future release credentials)
- **Local pre-commit enforcement** (lint, format, affected tests) — required per RULES.md §6.4

---

## 2. Repository Structure
MidTrim is a **single monorepo** containing both native codebases plus all documentation (`docs/`, including this file). The authoritative folder layout is defined in **ARCHITECTURE.md, Section 1.1** — refer there for the full tree; it is not repeated here to avoid drift between documents.

For CI purposes, the two relevant top-level paths are:
```
ios/           → triggers ios-ci.yml
android/       → triggers android-ci.yml
```
CI workflows use `paths:` filters against these two directories so an iOS-only change doesn't trigger the Android pipeline and vice versa — this keeps free-tier minutes efficient.

---

## 3. GitHub Actions — Android Workflow

### 3.1 Platform Notes
- Runs on standard Linux/Ubuntu runners (`ubuntu-latest`) — fast and cheap on the free tier.
- Gradle build caching should be enabled to speed up repeated runs.

### 3.2 Workflow: `.github/workflows/android-ci.yml`
Triggers: on every push to a `feat/`, `fix/`, or `chore/` branch, and on every pull request targeting `main`.

Pipeline steps:
1. Checkout code
2. Set up JDK (Temurin, version matching project's Kotlin/AGP requirements)
3. Cache Gradle dependencies
4. Run `ktlint`/`detekt` (lint + format check)
5. Run unit tests (`./gradlew test`)
6. Run Room in-memory DB tests (included in step 5 if configured in the same test source set)
7. Build debug APK (`./gradlew assembleDebug`) — build verification only, not distributed
8. Upload test results + lint report as workflow artifacts

### 3.3 Example Skeleton
```yaml
name: Android CI

on:
  push:
    branches: ["feat/**", "fix/**", "chore/**"]
    paths: ["android/**"]
  pull_request:
    branches: ["main"]
    paths: ["android/**"]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: android
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"
      - uses: gradle/actions/setup-gradle@v4
      - name: Lint
        run: ./gradlew ktlintCheck detekt
      - name: Unit Tests
        run: ./gradlew test
      - name: Build Debug APK
        run: ./gradlew assembleDebug
      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: android-test-results
          path: android/**/build/reports/tests/
```

---

## 4. GitHub Actions — iOS Workflow

### 4.1 Platform Notes
- Requires a **macOS runner** (`macos-latest` or a pinned version like `macos-14`) — free tier includes macOS minutes, but they consume the monthly quota **faster** than Linux minutes (GitHub multiplies macOS minute usage at a higher rate against the free allotment).
- Xcode version should be pinned explicitly (e.g., via `xcode-select` or `maxim-lobanov/setup-xcode` action) to avoid CI breaking when GitHub updates the default Xcode on runners.
- MVP CI builds/tests only target the **iOS Simulator** — no physical device testing, no App Store signing required for CI verification purposes.

### 4.2 Workflow: `.github/workflows/ios-ci.yml`
Triggers: same branch/PR pattern as Android, scoped to `ios/**` paths.

Pipeline steps:
1. Checkout code
2. Select Xcode version
3. Run `swiftlint` (lint + format check)
4. Run unit tests via `xcodebuild test` targeting the iOS Simulator
5. Build for simulator (`xcodebuild build`) — build verification only
6. Upload test results (`.xcresult`) as workflow artifacts

### 4.3 Example Skeleton
```yaml
name: iOS CI

on:
  push:
    branches: ["feat/**", "fix/**", "chore/**"]
    paths: ["ios/**"]
  pull_request:
    branches: ["main"]
    paths: ["ios/**"]

jobs:
  build-and-test:
    runs-on: macos-14
    defaults:
      run:
        working-directory: ios
    steps:
      - uses: actions/checkout@v4
      - name: Select Xcode
        run: sudo xcode-select -s /Applications/Xcode_15.4.app
      - name: Install SwiftLint
        run: brew install swiftlint
      - name: Lint
        run: swiftlint --strict
      - name: Unit Tests (Simulator)
        run: |
          xcodebuild test \
            -scheme MidTrim \
            -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest' \
            -resultBundlePath TestResults.xcresult
      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: ios-test-results
          path: ios/TestResults.xcresult
```

### 4.4 Known Free-Tier Constraints (iOS)
- No physical device farm — Simulator only. This is acceptable for MVP CI verification but should not be treated as equivalent to real-device QA before release.
- Build times are slower than Android (~10–15 min typical) — keep iOS-specific CI jobs scoped tightly via path filters to avoid wasting minutes on unrelated changes.
- App Store distribution signing (certificates, provisioning profiles) is **out of scope** for MVP CI — this pipeline verifies build + test correctness only, not release readiness. A future `ios-release.yml` workflow should be added separately once App Store distribution is in scope, using GitHub encrypted secrets for the signing certificate and provisioning profile.

---

## 5. In-App Purchase Testing in CI

### 5.1 What CI Can and Cannot Verify
Real StoreKit/Play Billing purchase transactions require a signed-in sandbox/test account and, in Play Billing's case, an app already uploaded to at least an internal testing track — neither is achievable in a stock GitHub Actions runner. CI's role for IAP is therefore **narrower** than for the rest of the app:

**CI can verify:**
- `ValidateTrimDurationUseCase`, `ResolveExportQualityUseCase` — pure functions, fully unit-testable with no SDK dependency.
- `PurchaseEntitlementUseCase` / `RestoreEntitlementUseCase` logic **against a mocked/faked StoreKit/Play Billing client** — i.e., unit tests that inject a fake `Transaction`/`PurchaseResult` and assert the Use Case reacts correctly (updates cache, handles cancellation, preserves cache on network failure, etc., per ARCHITECTURE.md §8's error-handling rules).
- Entitlement cache read/write logic, tested against an in-memory or fake Keychain/`EncryptedSharedPreferences` implementation (never the real secure storage in CI — see RULES.md §2.6, this is a test double, not a security bypass).
- UI state (lock icons, disabled controls, paywall screen rendering) driven by a **fake/injected entitlement state** in UI tests — verifying the UI reacts correctly to `isPurchased = true/false`, without needing a real purchase to occur.

**CI cannot verify (requires manual/beta testing instead — see TIMELINE.md Phase 7):**
- An actual real-money (or sandbox-money) transaction completing successfully end-to-end against Apple's/Google's live systems.
- StoreKit sandbox account behavior nuances (e.g., renewal/refund edge cases — less relevant for a one-time purchase, but worth noting for completeness).
- Google Play Billing's requirement that the app be uploaded to at least an internal testing track before **any** purchase (including test purchases) can be made against it.

### 5.2 iOS: StoreKit Testing Framework (Local + CI)
- Use Apple's **StoreKit Testing framework** (a `.storekit` configuration file + `SKTestSession` in XCTest) to simulate purchases **without any network calls or real sandbox account** — this runs entirely locally/in CI on the Simulator.
- Add a `.storekit` configuration file to the Xcode project defining the one-time non-consumable product (e.g., `com.midtrim.fullunlock`).
- Unit/UI tests use `SKTestSession` to simulate: successful purchase, cancelled purchase, and (where the framework supports it) transaction failures — these tests run in the same `xcodebuild test` CI step as the rest of the iOS suite, no special CI configuration needed beyond including the `.storekit` file in the test target.

### 5.3 Android: Play Billing Testing
- Google Play Billing does not have an equivalent fully-offline local testing framework as robust as StoreKit Testing. For MVP CI, rely on:
  - Unit tests against a **fake/mocked `BillingClient`** (e.g., a hand-rolled test double or a library like `mockk` wrapping the `BillingClient` interface) to test `PurchaseEntitlementUseCase`/`RestoreEntitlementUseCase` logic in isolation — this is sufficient for CI and does not require Play Store infrastructure.
  - Real end-to-end Play Billing testing (license testers, internal testing track) happens **manually during Phase 7 beta testing** (see TIMELINE.md), not in automated CI, since it requires an actual Play Console app listing.

### 5.4 CI Workflow Additions for IAP
No new top-level workflow file is needed — IAP unit/UI tests using the mocked/fake approaches above run as part of the existing `android-ci.yml` / `ios-ci.yml` unit test steps (Sections 3.2/4.2). No additional CI secrets or configuration are required for MVP, since no real purchase infrastructure is touched in CI.

---

## 6. Branch Protection Rules (GitHub Repository Settings)
To enforce RULES.md §6 at the platform level, configure the following on the `main` branch:
- **Require a pull request before merging** — direct pushes to `main` disabled for all contributors, including admins if possible.
- **Require status checks to pass before merging**: both `Android CI` and `iOS CI` workflows (only the one(s) relevant to changed paths will run, but both must be green if both triggered).
- **Require branches to be up to date before merging** — avoids merging stale branches that haven't accounted for recent `main` changes.
- **Require conversation resolution before merging** (if using PR review comments).
- **AI agents never merge**, regardless of these settings — this is enforced as an agent behavior rule (RULES.md §7.1), not just a GitHub permission. Branch protection settings above are a technical backstop; RULES.md §7 is the actual source of truth for the PR/review workflow AI agents must follow, including the Pre-PR Security Checklist (§7.2).

---

## 7. Secrets Management
- No secrets are required for MVP CI (build + test verification only, no release signing).
- If/when release signing is introduced:
  - Android: store keystore file (base64-encoded) + keystore password + key alias/password as GitHub encrypted secrets (`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, etc.).
  - iOS: store distribution certificate (base64-encoded `.p12`) + provisioning profile + certificate password as GitHub encrypted secrets.
- **Never** store secrets in `.env` files committed to the repo (see RULES.md §6.3) — GitHub encrypted secrets (Settings → Secrets and variables → Actions) are the only approved mechanism for CI credentials.

---

## 8. Local Pre-Commit Enforcement
Per RULES.md §6.4, every commit must be preceded locally by: **lint/format check → affected unit tests**. This section defines exactly how that's wired up on each platform so the rule is actually enforceable, not just aspirational.

### 8.1 Tooling Choice
- **Git hook manager**: [Husky](https://typicode.github.io/husky/) is not native to Swift/Kotlin projects, so instead this project uses native, platform-appropriate pre-commit hooks via a simple shared `.git/hooks/pre-commit` script (or the [pre-commit](https://pre-commit.com/) Python-based framework, which supports arbitrary language hooks and is a reasonable lightweight choice for a two-platform repo). Either approach is acceptable; the **behavior**, not the tool, is what's enforced.

### 8.2 Android Pre-Commit Behavior
On `git commit`, the hook must run, scoped only to staged/changed files where possible:
```bash
# 1. Lint + format
./gradlew ktlintCheck detekt

# 2. Affected unit tests
./gradlew test --tests "*<AffectedModulePattern>*"
# or, if a test-impact-analysis plugin isn't set up yet, fall back to:
./gradlew test
```
- If `ktlintCheck`/`detekt` fails, the commit is blocked until issues are fixed (or auto-fixed via `ktlintFormat` and re-staged).
- If any affected test fails, the commit is blocked.

### 8.3 iOS Pre-Commit Behavior
On `git commit`, the hook must run:
```bash
# 1. Lint + format
swiftlint --strict
swiftformat --lint .

# 2. Affected unit tests
xcodebuild test \
  -scheme MidTrim \
  -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest' \
  -only-testing:MidTrimTests/<AffectedTestClass>
# or, absent a reliable "affected tests" mapping, run the full unit test target:
xcodebuild test -scheme MidTrim -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest'
```
- If `swiftlint`/`swiftformat` reports violations, the commit is blocked until fixed.
- If any affected test fails, the commit is blocked.

### 8.4 Determining "Affected Tests"
For MVP, "affected" is approximated pragmatically rather than via a full dependency graph tool:
- If a Use Case file changes → run that Use Case's tests **and** any Use Case tests that directly depend on it (see RULES.md §6.4 example: `CalculateTrimWindowUseCase` → also re-run `TrimVideoUseCase`, `MergeVideoSegmentsUseCase`).
- If a Repository/DAO file changes → run all data-layer tests for that entity (e.g., `Project`/`SourceVideoItem` tests).
- If a shared/core utility changes → run the full unit test suite, since impact is hard to scope narrowly.
- When in doubt, run the full suite rather than under-scoping — a slower commit is preferable to a false-positive pass.

### 8.5 Bypassing Hooks
- Bypassing via `git commit --no-verify` is **disallowed** by RULES.md §6.4 except in a logged emergency with product owner approval — CI will still catch violations on push, so this is a hard backstop, not the primary enforcement point. The local hook is the first line of defense; CI is the second.

### 8.6 Setup Instructions (for new contributors/agents)
1. Clone the repo.
2. Run the platform-appropriate setup script (`./scripts/setup-hooks.sh`, to be added at project init) which symlinks or installs the pre-commit hook for the active platform folder(s) being worked on.
3. Verify hooks are active: `git config core.hooksPath` should point to the project's hooks directory (or `.git/hooks` should contain the installed `pre-commit` script).

---

## 9. Future CI Considerations (Post-MVP)
- Add code coverage reporting/thresholds (e.g., via `kover` for Android, `xccov`/`slather` for iOS) surfaced as a PR check.
- Add release workflows (`android-release.yml`, `ios-release.yml`) once Play Store/App Store distribution is in scope, with proper signing secrets.
- Consider a "test impact analysis" tool (e.g., Gradle's built-in test filtering, or a dependency-graph-aware test selector) to make "affected tests" precise rather than pattern/heuristic-based.
- Monitor GitHub Actions free-tier minute usage monthly, especially macOS runner minutes, and consider self-hosted runners if usage grows significantly.
- If subscription-based entitlements are introduced post-MVP, revisit Section 5's testing approach — subscription lifecycle (renewal, grace period, expiry) testing is meaningfully more complex than the one-time-purchase testing described here and may need dedicated sandbox test scenarios beyond what's outlined for MVP.
