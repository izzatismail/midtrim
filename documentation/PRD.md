# PRD.md — Product Requirements Document

## Product Name
**MidTrim**

## 1. Overview
A native mobile app (iOS + Android) that lets users create short, social-media-ready video clips by automatically trimming one or more source videos, always centered on the **middle** of each original clip, then merging them into a single output clip in user-defined order. The app runs **entirely offline**, storing all projects and exported files locally on-device. MidTrim ships as a **free app with a one-time in-app purchase** that unlocks custom trim durations, full-quality exports, and a higher per-project video limit (see Section 4A for the full free/paid breakdown).

## 2. Problem Statement
Users often want to extract the "best moment" from a longer video without complex editing tools. Most editing apps are overloaded with features (multi-track timelines, transitions, effects) for a task that should take seconds. This app solves that by offering a **single-purpose, frictionless trim tool**.

## 3. Goals
- Let users trim any video to a short duration in under 3 taps — **1s, 2s, or 3s free**; **any custom duration up to 5s with the paid unlock**.
- Always trim from the **center** of each video (auto-calculated, no manual scrubbing in MVP, on either tier).
- Allow users to select **multiple source videos** in a single project (**up to 10 free, up to 20 paid**), all trimmed to the same duration and merged into one output clip, in a user-defined order.
- Allow users to save trims as **named projects** for later reference.
- Keep everything **on-device** — no custom accounts, no cloud sync, no network calls beyond the platform's native purchase/restore flow.
- Ensure exported videos are high quality and ready to share on social platforms — **capped at 720p free**, **up to original resolution (1080p/4K) with the paid unlock**.
- Offer a **single, affordable one-time purchase** (target: USD $5.00) to unlock the full feature set — no subscriptions, no recurring billing, no tiered pricing in MVP.

## 4. Free vs. Paid Tier (In-App Purchase)

### 4.1 Monetization Model
- **One-time in-app purchase**, no subscription, no recurring billing.
- Price: **USD $5.00**, confirmed for MVP launch (one-time purchase, no subscription).
- Purchase unlocks all paid features permanently on that platform account (Apple ID / Google Account) — see Section 4.3 for restore behavior.
- No server-side account system: entitlement is verified directly against the platform's purchase records (StoreKit on iOS, Google Play Billing on Android), not a custom backend.

### 4.2 Feature Comparison

| Feature | Free Tier | Paid Tier (Unlocked) |
|---|---|---|
| Trim duration | Fixed: **1s, 2s, or 3s** only | **Custom duration**, any value from 1s up to **5s max** (MVP cap) |
| Export quality | Capped at **720p** | Up to **original resolution** (1080p / 4K / source resolution, whichever applies) |
| Max videos per project | **10** | **20** |
| Trim behavior (center-aligned) | Same on both tiers | Same on both tiers |
| Merge behavior (single shared duration per project) | Same on both tiers | Same on both tiers |
| Project save/rename/delete | Same on both tiers | Same on both tiers |
| Ads | None (no ads on either tier in MVP) | None |

### 4.3 Restore Purchases
- **Automatic restore-on-launch** is the primary mechanism: on every app launch, the app silently checks StoreKit (iOS) / Google Play Billing (Android) for an existing valid purchase tied to the signed-in platform account, and unlocks paid features automatically if found.
- A manual **"Restore Purchases" button** is available as a failsafe inside a new **Help/Settings section**, for cases where automatic restore doesn't trigger (e.g., network hiccup, edge-case timing).
- No custom login, no email/password account, and no cloud save/sync are part of this mechanism — restoration relies entirely on the native platform account already signed into the device (Apple ID / Google Account).
- Entitlement state is cached securely on-device after verification (see ARCHITECTURE.md/RULES.md for storage mechanism) so the app doesn't need to re-verify against the store on every single screen interaction — only on launch and on manual restore.
- **Refunds / entitlement revocation**: if a launch-time or manual restore check finds the purchase is **no longer active** (e.g., the user obtained a refund through Apple/Google), the app re-locks paid-tier creation controls (custom duration, 20-video cap, full-quality export) going forward, exactly as if the purchase had never been made. This is treated the same as any other "not purchased" state — no special messaging or warning is shown; the lock affordances simply reappear the next time the user attempts to use them.
- **Existing projects are never affected by a later entitlement change in either direction** — a project created while paid (e.g., a 20-video, 1080p project) remains fully viewable, playable, renameable, deletable, and shareable even if the purchase is later refunded. Only the ability to *create new* paid-tier projects is gated by current entitlement status. This keeps the rule consistent with Section 4.2/8.4's "projects are historical records" principle, applied in both directions (purchase and refund).

### 4.4 Paywall UX Behavior
- **Inline, disabled-state pattern** (not an interrupting modal): when a free-tier user reaches a locked boundary, the relevant control is shown in a **disabled state with a lock affordance** (e.g., greyed-out custom duration option, greyed-out "add 11th video" action, a small lock icon on a quality selector).
- Tapping a locked/disabled control opens the **paywall screen**, explaining the unlocked features and offering the one-time purchase.
- A persistent **"Upgrade" entry point** is also present on the main Projects screen (not just reactive to hitting a limit) so users can discover and purchase proactively.
- Free-tier boundaries that trigger the lock affordance:
  - Attempting to select a custom trim duration outside {1, 2, 3}s.
  - Attempting to add an 11th video to a project.
  - Attempting to export at a quality above 720p.

## 5. Non-Goals (Out of Scope for MVP)
- Manual timeline scrubbing / custom trim range selection (custom duration input, not a scrubber, is the paid mechanism — no drag-to-scrub UI in MVP for either tier).
- Per-video custom trim duration within a multi-video project (all videos in a project share one selected duration, on both tiers).
- Transitions, filters, text overlays, or music/audio mixing between merged clips.
- More than **20 source videos** per project (even for paid tier).
- Custom durations beyond **5 seconds**, even for paid tier (MVP cap).
- Export quality beyond the source video's original resolution (no upscaling).
- Subscriptions, tiered pricing plans, or recurring billing of any kind.
- Custom user accounts, login/signup, or cloud sync/backup of any kind — restore relies solely on native platform account (Apple ID / Google Account).
- Cloud sync, backup, or account system.
- Android/iOS cross-device sync of projects.
- Social sharing integrations (share sheet only, not native platform APIs).

## 6. Target Users
- Social media users (TikTok, Instagram Reels, Shorts creators) who want a fast way to isolate a highlight moment.
- Casual users who want to shorten long clips (e.g., a funny 1-second reaction) without learning an editing tool.

## 7. Core User Flow (MVP)
1. User opens app → sees list of saved **Projects** (empty state on first launch), with an **"Upgrade" entry point** visible for free-tier users.
2. User taps **"New Project"**.
3. User selects **one or more source videos** (up to **10 free / 20 paid**) from device storage (gallery/files). Attempting to exceed the tier's cap shows the video-add action as disabled with a lock affordance (tap opens paywall).
4. User sees a **selection list** showing, per video: thumbnail/name, file size, and individual duration — plus a **running total duration after merge** (updates live based on selected trim duration).
5. User can **drag-to-reorder** videos in this list; the resulting order determines merge order in the final output.
6. App calculates the shortest video's duration and shows trim options: **1s / 2s / 3s** for free users, or a **custom duration input (1–5s)** for paid users (any option/value longer than the shortest selected video's duration is disabled, since the same duration is applied to every video). Free users see the custom duration option in a disabled state with a lock affordance.
7. User selects desired duration (applied uniformly to all selected videos).
8. App auto-trims each video to its own center segment of that duration, then **merges all trimmed segments in the user-defined order** into a single output clip, encoded at **720p for free users** or **up to original resolution for paid users**.
9. User previews the merged result.
10. User names the project and taps **Save**.
11. Merged trimmed video is saved as a **new file** (original source videos are never modified) and the project appears in the Projects list.
12. User can reuse any source video(s) in additional future projects (same source may appear across multiple projects).

## 8. Functional Requirements

### 8.1 Video Import
- Support importing **one or more videos** per project from native device gallery/file picker in a single selection action — minimum 1, maximum **10 (free tier)** or **20 (paid tier)**.
- Support common formats: MP4, MOV (native container formats per platform).
- Reject unsupported formats with a clear error message; if part of a multi-selection is unsupported, identify which file(s) failed rather than rejecting the whole batch silently.
- Display, per selected video: thumbnail, file name, file size, and duration.
- Display a live **running total duration** for the merged output, calculated as `trimDuration × number of selected videos`.
- If a free-tier user attempts to add a video beyond the 10-video cap, the add action is disabled with a lock affordance; tapping it opens the paywall (see Section 4.4).

### 8.2 Video Ordering
- Users can **drag-to-reorder** selected videos in the selection list before confirming trim duration.
- The final order shown in the list at confirmation time determines the **merge order** of trimmed segments in the output file.
- Order must be persisted as part of the project record (see SCHEMA.md) so it can be referenced/audited later even though MVP has no re-edit flow.

### 8.3 Trimming Logic
- **One trim duration applies to all videos in a project** — no per-video custom durations, on either tier.
- **Free tier**: trim duration selectable from a fixed set — **1s, 2s, or 3s only**.
- **Paid tier**: trim duration selectable as a **custom value from 1s up to 5s** (MVP hard cap even for paid).
- Each selected video's duration must be **> selected trim duration** individually. If any selected video is shorter than or equal to a given duration (fixed option or custom value), that duration is disabled/rejected for the whole project (since it must apply uniformly).
- Trim window per video = center-aligned, calculated independently for each source video:
  - `start = (videoDuration - trimDuration) / 2`
  - `end = start + trimDuration`
- After individual trimming, all trimmed segments are **concatenated/merged in user-defined order** into a single output file.
- Output video must preserve original aspect ratio and be encoded as closely to source quality as the tier allows (see Section 8.6 for the quality-tier specifics). If source videos differ in resolution/aspect ratio/frame rate, the merge process must normalize to a consistent output format (see ARCHITECTURE.md for the chosen normalization approach) rather than producing a broken or inconsistent merged file.

### 8.4 Project Management
- Each project includes:
  - User-defined project name
  - One or more source video references (path/URI), in order (max 10 free / 20 paid)
  - Trim duration selected (applies to all videos in the project; fixed value for free-tier projects, custom value up to 5s for paid-tier projects)
  - Output file path (single merged file)
  - Export quality tier used (720p vs. original) at time of creation
  - Created timestamp
- Projects persist **indefinitely** (no auto-expiry/cleanup).
- Users can rename or delete projects.
- Deleting a project removes the merged trimmed output file but **never** any of the original source videos.
- A project's recorded trim duration/quality reflects what was available **at the time it was created** — if a user purchases the unlock after creating free-tier projects, those existing projects are not automatically re-processed (see Section 8.7).

### 8.5 Export & File Handling
- Exported (trimmed and merged) video is always saved as a **new single file** — original source videos are never overwritten or modified.
- Users may reuse the same source video(s) across multiple projects, including within different multi-video combinations.
- Exported files stored in app-specific local storage (sandboxed).
- Users can export/share the merged trimmed file via native OS share sheet (e.g., Save to Camera Roll/Gallery, share to other apps).

### 8.6 Export Quality Tiers
- **Free tier**: merged output is encoded/capped at **720p**, regardless of source resolution.
- **Paid tier**: merged output is encoded at **up to the source video's original resolution** (e.g., 1080p, 4K, or whatever the source provides) — no upscaling beyond source resolution.
- Quality tier is determined by entitlement status **at the time of export/save**, not retroactively applied to previously saved projects.
- If a free-tier user attempts to change an export-quality-related setting beyond 720p (should such a setting be exposed in UI), the control is disabled with a lock affordance; tapping it opens the paywall (see Section 4.4).

### 8.7 In-App Purchase & Entitlement
- See Section 4 for the full free/paid feature breakdown, pricing, and paywall UX.
- Entitlement (purchased or not) is checked automatically on app launch (StoreKit / Google Play Billing) and cached securely on-device.
- A manual **"Restore Purchases"** button exists in a new **Help/Settings** section as a failsafe.
- No custom account system, login, or cloud sync is introduced to support this — restore relies solely on the native platform account already signed into the device.
- Purchasing does **not** retroactively re-process previously created free-tier projects (e.g., a 720p project stays 720p unless the user re-creates it after purchasing).

### 8.8 Security & Privacy (MVP scope)
- No network requests of any kind, **except** the platform's native in-app purchase and restore-purchase flows (StoreKit / Google Play Billing), which are Apple/Google-managed and required for the IAP feature.
- No analytics, tracking, or telemetry in MVP.
- Local file encryption at rest where feasible without material quality/performance/storage cost (see RULES.md for implementation stance).
- Metadata (EXIF/creation date/device info) preserved by default in MVP (see DESIGN/RULES for rationale).
- Entitlement/purchase state stored securely on-device (see ARCHITECTURE.md/RULES.md) — never transmitted anywhere outside the platform's own purchase APIs.

## 9. Success Metrics (MVP)
- User can complete import → trim → save flow in under 30 seconds for a single video, and under 60 seconds for a 10-video project (free tier) or 20-video project (paid tier).
- Zero crashes on trim or merge operation across supported OS versions.
- Merged trimmed output plays correctly on both platforms and common social apps, with no visual glitches at segment boundaries.
- In-app purchase completes and unlocks features within 5 seconds of successful platform transaction confirmation.
- Restore purchases (automatic or manual) correctly re-unlocks features within 5 seconds on a fresh install with a prior purchase.

## 10. Platform Requirements
- **iOS**: Native Swift app, **minimum iOS 15+ (confirmed)**. StoreKit 2 for in-app purchase.
- **Android**: Native Kotlin app, **minimum SDK API 26+ / Android 8.0+ (confirmed)**. Google Play Billing Library for in-app purchase.
- **Video codecs**: H.264 and HEVC, via native platform frameworks (AVFoundation / Media3) — **confirmed** as the supported set for MVP; no other codecs in scope.
- Both apps functionally equivalent; UI adapted to each platform's design language while following shared DESIGN.md brand direction.

## 11. Future Considerations (Post-MVP, not built now)
- Manual trim range selection via scrubber/timeline.
- Custom trim durations beyond 5 seconds (even for paid tier).
- Per-video custom trim duration within a single multi-video project.
- Raising the 20-video cap per project (paid tier).
- Metadata stripping toggle for privacy-conscious users.
- Basic enhancements: speed ramp, crop/aspect ratio presets for social platforms, transitions between merged segments.
- Cloud backup (optional, opt-in).
- Subscription tiers or additional paid feature bundles beyond the single one-time unlock.
- Promotional pricing, discounts, or regional pricing strategy (MVP ships with one flat target price).

## 12. Open Questions / Assumptions
- **Resolved**: Minimum OS versions confirmed — iOS 15+, Android API 26+/Android 8.0+ (see Section 10).
- **Resolved**: Video codec support confirmed — H.264/HEVC via native frameworks only (see Section 10).
- **Resolved**: Final IAP price point confirmed at **USD $5.00** one-time purchase for MVP launch.
- Apple/Google's respective cut of IAP revenue (standard ~15–30% depending on developer program tier) is a business consideration, not a technical one, but affects net revenue expectations — no action needed, noted for financial planning only.
- Merge normalization reference strategy (first-video-in-order sets the output resolution, per ARCHITECTURE.md §5.1) is implementation-confirmed but carries a **Phase 6 quality validation checkpoint** — see ARCHITECTURE.md §5.1 for the exact test to run before considering the merge pipeline complete.
