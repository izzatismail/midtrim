# AGENTS.md — MidTrim Repo Guide

## Repo state (August 2026)

Phase 4 is **In Progress** (80%); Phase 5 (IAP) is **In Progress** — Android 100% complete, iOS now has StoreKit 2 integrated (StoreKitService, .entitlements, .storekit config, Paywall/HelpSettings wiring, restore-on-launch, SKTestSession integration tests). Theme stubs still use placeholder colors — **not yet matching** DESIGN.md spec.

## Structure

Single monorepo — no shared code between platforms.

| Directory | Contents |
|---|---|
| `ios/MidTrim/` | Xcode project, SwiftUI app, test targets |
| `android/` | Gradle project (`./gradlew` from here), Compose app |
| `documentation/` | Authoritative spec — PRD, ARCHITECTURE, DESIGN, SCHEMA, RULES, CI, TIMELINE |

## Build & test commands

**Android** (from `android/`):
- `./gradlew ktlintCheck detekt` — lint + format
- `./gradlew test` — unit tests
- `./gradlew assembleDebug` — build verification

Key config: AGP 9.2.1, Kotlin 2.2.10, Compose BOM 2026.02.01, Java 11, minSdk 26 / targetSdk 36. Version catalog at `android/gradle/libs.versions.toml`.

**iOS** (from `ios/`):
- `swiftlint --strict` — lint
- `xcodebuild test -project MidTrim/MidTrim.xcodeproj -scheme MidTrim -destination 'platform=iOS Simulator,name=iPhone 15'`

Minimum OS versions: **iOS 15+**, **Android API 26+ / Android 8.0+** (both confirmed, not placeholders).

## Branch & commit rules

- Branches: `feat/`, `fix/`, `chore/` prefix only. Never push to `main`.
- Commits: `<prefix>: <short description>`. Never bundle unrelated changes.
- Pre-commit order: **lint → format → test** (all three must pass). Never `--no-verify` without logged approval.

## PR workflow (single source of truth — see RULES.md §7 for full detail)

- After completing work on a feature branch, push it and create a **PR** targeting `main`. ("PR" and "MR" mean the same thing in this repo — always use "PR.")
- Before opening the PR, run through the **Pre-PR Security Checklist** below and include the results in the PR description. This applies to **every** PR, not just ones where the developer explicitly asks for a check.
- **Never merge.** Stop after creating the PR — a human reviews and merges manually, every time, with no exceptions.
- If a Critical/High severity issue turns up during your own pre-PR check, still open the PR (so the developer can see it) and flag the severity clearly in the description — don't silently fix-and-recheck in a loop before the human ever sees it.

### Pre-PR Security Checklist (report each item explicitly, not a generic "no issues found")
- [ ] No networking code added outside StoreKit 2 / Play Billing.
- [ ] No custom/third-party backend touched for purchase verification.
- [ ] Entitlement cache only written by `PurchaseEntitlementUseCase` or `RestoreEntitlementUseCase`.
- [ ] No plaintext storage of entitlement, purchase, or video file data.
- [ ] No sensitive file paths, user data, or purchase/transaction details logged in release builds.
- [ ] No `.env`, API key, signing certificate, or keystore committed.
- [ ] A failed/timed-out restore check does not silently clear or downgrade cached entitlement.

## Documentation as spec

`documentation/` is the ground truth. If code changes contradict it, update docs in the **same change set**. Always consult TIMELINE.md before starting work and update it after completing deliverables.

## Architecture

Clean Architecture + MVVM. Independent per platform (no KMP, no shared code). Fully offline — **no networking, analytics, or telemetry**. The only network exception is StoreKit 2 (iOS) / Play Billing (Android) for IAP.

- **iOS**: SwiftUI + `@Observable`, AVFoundation, SwiftData, Keychain
- **Android**: Jetpack Compose + `ViewModel`/`StateFlow`, Media3 Transformer, Room, `EncryptedSharedPreferences`

There are **16 core Use Cases** total (ARCHITECTURE.md §4), including `GenerateThumbnailUseCase` — extracts a single frame (t=0) from the merged output as a JPEG, runs right after merge and before project save. Don't skip it: it's easy to miss since it's not part of the original trim/merge/save trio.

Restore-on-launch (`RestoreEntitlementUseCase`) must be **non-blocking** — the Projects List screen renders immediately with the last-cached entitlement state; the restore check updates it silently if it changes (ARCHITECTURE.md §7).

## Key constraints (from RULES.md)

- No FFmpeg or third-party video libs unless native API is proven insufficient
- No partial/silent merge failures — fail the whole project with a clear message
- Never modify, overwrite, or delete source video files
- Always encrypt output videos at rest
- Always clean up temp segments on merge failure
- Tier limits derived from `FetchEntitlementStatusUseCase`, never stale UI state

## Tier limits (free vs. paid — the core IAP feature)

| Feature | Free | Paid |
|---|---|---|
| Trim duration | Fixed: 1s, 2s, or 3s | Custom: 1.0–5.0s |
| Max videos/project | 10 | 20 |
| Export quality | Capped at 720p | Up to source resolution (1080p/4K) |

One-time purchase, **$5.00 USD**, no subscription.

## Entitlement / IAP rules

- Cache in **Keychain** (iOS) / **EncryptedSharedPreferences** (Android) — never in the main DB
- Only `PurchaseEntitlementUseCase` and `RestoreEntitlementUseCase` may write to it
- A failed restore check must **never** downgrade cached entitlement — only an explicit "not found" from the store may do that
- Existing `Project` records are immutable regardless of later purchase **or refund**

## Code Review Guide for AI Agents

### Review Role
Act as a Staff Engineer performing a pull request review.

### Authority limits (see RULES.md §7.3)
- Output is a **report of findings only** — never merge the PR, never push a fix directly to the branch under review on your own initiative, even for a Critical finding. Propose fixes in review comments; implementing them is a separate, explicitly-requested action.

### Review Dimensions
1. Correctness and hidden bugs
2. Security — check explicitly against the **Pre-PR Security Checklist** above, plus accidentally committed secrets (API keys, signing certs, keystores) as its own line item
3. Performance regressions
4. UI & Schema Design consistency
5. Maintainability
6. Test coverage gaps
7. Concurrency / race conditions
8. Backward compatibility risks

### Issue Reporting Format
For every finding:
- **Severity:** Critical / High / Medium / Low
- **Location:** File path + line reference
- **Why:** Why it matters + potential impact
- **Fix:** Suggested fix or mitigation

### Guidelines
- Do not comment on formatting or style unless it affects maintainability
- Prioritize findings by risk (Critical → Low)
- Flag any deviation from the agreed stack

### When to Review
- On every commit pushed to a feature/phase branch before merging to `main`
- Developer may request ad-hoc review at any time via `"review my code"` instruction

## Design system

- Font: **Nunito** (bundled, OFL-licensed, 3 weights). Must use `.scaledFont`/`sp` for accessibility.
- Dark-first palette: `#0A0A0B` bg, `#5B5FEF` accent, `#F5B841` premium-accent. Light mode has adjusted values for contrast.
- Cross-platform **visual parity required** — iOS and Android screenshots should be indistinguishable.