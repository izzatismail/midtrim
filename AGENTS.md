# AGENTS.md — MidTrim Repo Guide

## Repo state (July 2026)

Both platforms are **scaffolding-only**. Phase 1 of TIMELINE.md is "Not Started" (0%). No domain layer, no data layer, no UI screens. No CI workflows exist yet. Theme stubs use placeholder colors — **not yet matching** DESIGN.md spec.

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
- `xcodebuild test -scheme MidTrim -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest'`

## Branch & commit rules

- Branches: `feat/`, `fix/`, `chore/` prefix only. Never push to `main`.
- Commits: `<prefix>: <short description>`. Never bundle unrelated changes.
- Pre-commit order: **lint → format → test** (all three must pass). Never `--no-verify` without logged approval.

## Documentation as spec

`documentation/` is the ground truth. If code changes contradict it, update docs in the **same change set**. Always consult TIMELINE.md before starting work and update it after completing deliverables.

## Architecture

Clean Architecture + MVVM. Independent per platform (no KMP, no shared code). Fully offline — **no networking, analytics, or telemetry**. The only network exception is StoreKit 2 (iOS) / Play Billing (Android) for IAP.

- **iOS**: SwiftUI + `@Observable`, AVFoundation, SwiftData, Keychain
- **Android**: Jetpack Compose + `ViewModel`/`StateFlow`, Media3 Transformer, Room, `EncryptedSharedPreferences`

## Key constraints (from RULES.md)

- No FFmpeg or third-party video libs unless native API is proven insufficient
- No partial/silent merge failures — fail the whole project with a clear message
- Never modify, overwrite, or delete source video files
- Always encrypt output videos at rest
- Always clean up temp segments on merge failure
- Tier limits derived from `FetchEntitlementStatusUseCase`, never stale UI state

## Entitlement / IAP rules

- Cache in **Keychain** (iOS) / **EncryptedSharedPreferences** (Android) — never in the main DB
- Only `PurchaseEntitlementUseCase` and `RestoreEntitlementUseCase` may write to it
- A failed restore check must **never** downgrade cached entitlement — only an explicit "not found" from the store may do that
- Existing `Project` records are immutable regardless of later purchase **or refund**

## Design system

- Font: **Nunito** (bundled, OFL-licensed, 3 weights). Must use `.scaledFont`/`sp` for accessibility.
- Dark-first palette: `#0A0A0B` bg, `#5B5FEF` accent, `#F5B841` premium-accent. Light mode has adjusted values for contrast.
- Cross-platform **visual parity required** — iOS and Android screenshots should be indistinguishable.