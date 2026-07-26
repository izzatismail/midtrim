# DESIGN.md — Brand & Design System

## 1. Design Philosophy
**Minimalist. Fast. Social-native.**

The app should feel like it belongs on the same phone as Instagram, TikTok, and Snapchat — dark-leaning, high-contrast, gesture-friendly, and content-first. Every screen should support the core task (trim a video, fast) with zero visual clutter. No skeuomorphism, no unnecessary chrome, no dense settings screens.

Design principles:
- **Content is the hero** — video previews are large and unobstructed.
- **One primary action per screen** — never make the user choose between more than one clear next step.
- **Speed over depth** — minimize taps, avoid nested menus.
- **Visually identical across platforms** — iOS and Android render the **same UI**: same colors, same typography (Nunito, per §4), same spacing, same component styling, same layout. This is a deliberate choice (see §1.1) that takes precedence over platform-native visual conventions; the app is meant to look and feel like the same product regardless of device.
- **Native-first for implementation, not appearance** — each platform is still built with its native tools (SwiftUI, Jetpack Compose — see ARCHITECTURE.md) and respects native *interaction* patterns (gestures, navigation transitions, accessibility APIs), but the **visual output**, not just the design language, must match exactly. See §1.1 for what this does and doesn't cover.

### 1.1 Cross-Platform Visual Parity (Explicit Policy)
- **iOS and Android must render pixel-equivalent UIs** — identical color values (§3), identical font (Nunito, §4, both weights and sizes), identical spacing/corner radii (§5), and identical screen layouts (§6). A screenshot of any given screen on iOS should be visually indistinguishable from the same screen on Android, aside from the OS status bar/system chrome itself.
- **This intentionally overrides default platform conventions** — e.g., no SF Symbols on iOS vs. Material Symbols on Android (§2.2 already establishes a shared outline-icon style instead), no iOS-style segmented controls vs. Android-style tabs where they'd look different, no platform-default button/card styling. Every visual component in this document is the single source of truth for **both** platforms.
- **What this does NOT cover** — this policy is about *visual appearance*, not implementation or system-level behavior. Each platform still:
  - Uses its own native codebase (SwiftUI / Jetpack Compose — see ARCHITECTURE.md §1, which explicitly keeps the codebases independent).
  - Respects native OS-level behavior that isn't a visual design choice: safe area insets, keyboard avoidance, native share sheets, native file/photo pickers (§6.1's `PHPickerViewController`/`Photo Picker`), native haptics APIs, and Dynamic Type/font-scale accessibility scaling (§4.1).
  - Follows the platform's native gesture conventions (e.g., iOS swipe-back navigation, Android back button/gesture) even though the visual chrome during that navigation should still match this design system.
- **Rationale**: MidTrim's brand identity (§1, §2.1) is meant to feel consistent and recognizable regardless of which device a user or their friend is on — especially relevant for a social-native app where users may see the same output shared across different devices. This is a deliberate trade-off against "feeling native to each OS," made explicitly here so it isn't second-guessed piecemeal during implementation.

## 2. Brand Identity

### 2.1 Name & Tone
- Name: **MidTrim**
- Tone: casual, confident, snappy — microcopy should feel like a friend, not a manual (e.g., "Trim it." not "Please select a duration to proceed.")

### 2.2 Logo & Iconography
- Icon concept: a simple **bracket/crop mark motif** `[ ]` centered around a play triangle, suggesting "capturing the middle."
- Iconography style: **outline icons, 2px stroke weight**, rounded line caps — consistent with SF Symbols (iOS) and Material Symbols (Android) rounded variant.
- Avoid literal scissors/clapperboard clichés — favor abstract, geometric marks.

## 3. Color Palette

### 3.1 Core Palette (Dark-first, social-native)
| Token | Hex | Usage |
|---|---|---|
| `bg-primary` | `#0A0A0B` | App background (near-black, not pure black) |
| `bg-elevated` | `#18181B` | Cards, sheets, modals |
| `bg-surface` | `#242428` | Input fields, secondary surfaces |
| `accent-primary` | `#5B5FEF` | Primary CTA buttons, active states (electric indigo) |
| `accent-primary-pressed` | `#4548C4` | Pressed/active state of primary accent |
| `text-primary` | `#FAFAFA` | Primary text on dark backgrounds |
| `text-secondary` | `#A1A1AA` | Secondary/muted text, timestamps, metadata |
| `text-disabled` | `#52525B` | Disabled states |
| `success` | `#34D399` | Save confirmations, success toasts |
| `error` | `#F87171` | Errors, destructive action warnings |
| `divider` | `#27272A` | Hairline separators |
| `premium-accent` | `#F5B841` | Lock icons, "Upgrade" badges/CTA, paywall highlights (warm gold — distinct from primary accent so premium/paid affordances are visually distinct from standard active states) |

### 3.2 Light Mode (Full Token Table)
- Follow OS-level light/dark switching automatically (respect system setting) — per §1.1, this is the **only** visual axis that's allowed to differ by system state; iOS and Android must still match each other exactly within each mode.
- `accent-primary` and `accent-primary-pressed` remain **identical** to dark mode for brand recognition (§3.1) — not restated in the table below since they don't change.

| Token | Hex | Notes |
|---|---|---|
| `bg-primary` | `#FFFFFF` | App background |
| `bg-elevated` | `#F4F4F5` | Cards, sheets, modals |
| `bg-surface` | `#E4E4E7` | Input fields, secondary surfaces |
| `text-primary` | `#18181B` | Primary text on light backgrounds |
| `text-secondary` | `#71717A` | Secondary/muted text, timestamps, metadata |
| `text-disabled` | `#A1A1AA` | Disabled states |
| `success` | `#059669` | Save confirmations, success toasts (darkened from dark-mode's `#34D399` for adequate contrast on a white background) |
| `error` | `#DC2626` | Errors, destructive action warnings (darkened from dark-mode's `#F87171` for the same contrast reason) |
| `divider` | `#E4E4E7` | Hairline separators |
| `premium-accent` | `#B8860B` | Lock icons, "Upgrade" badges/CTA, paywall highlights — a darker gold than dark-mode's `#F5B841`, since the lighter gold fails contrast against a white/near-white background |

- **Why some tokens change value and others don't**: `accent-primary` is a mid-toned indigo that already has sufficient contrast against both a near-black and a white background, so it stays constant for brand recognition. `success`, `error`, and `premium-accent`, by contrast, were tuned specifically for a dark background in §3.1 — their dark-mode hex values would fail contrast on white, so each has a dedicated, verified-for-light-mode value here rather than reusing the dark-mode hex.
- All light-mode values above must meet the same WCAG AA contrast bar required in §8 (Accessibility) against their respective background token.

### 3.3 Color Usage Rules
- Never use pure black (`#000000`) or pure white (`#FFFFFF`) for primary text/backgrounds — reduces eye strain, matches modern social app conventions.
- Accent color (`accent-primary`) reserved **only** for primary actions and active/selected states (trim duration selector, Save button). Never used decoratively.
- `premium-accent` reserved **only** for paid-feature signaling: lock icons on disabled controls, the "Upgrade" entry point, and paywall screen highlights. Never mixed with `accent-primary` in the same control — a lock icon should never appear on an already-active primary-accent element, since that would visually conflict (locked = not currently usable, accent = currently active).
- This rule applies identically in light mode, using the light-mode token values from §3.2 rather than §3.1's dark-mode values.

## 4. Typography

### 4.1 Font Family
- **Both platforms**: **Nunito** — a rounded, friendly sans-serif bundled into the app (not a system default), reinforcing MidTrim's casual, social-native brand tone (see §2.1) with consistent typography across iOS and Android.
- **Licensing**: Nunito is released under the **SIL Open Font License (OFL)**, which explicitly permits commercial use, bundling, and modification at no cost and with no royalties — safe to use in a monetized/paid app. The OFL license text must be included in the app (e.g., an "Open Source Licenses" entry in the Help/Settings screen, §6.6) per the license's attribution requirement.
- **Bundled weights**: Regular (400), SemiBold (600), Bold (700) — matches the weights used in the type scale (§4.2) only; avoid bundling unused weights to keep app size down.
- **iOS wiring**: bundle Nunito's font files in the app target, register via `Info.plist` (`UIAppFonts`), and apply through a custom `Font` extension. **Must use `.scaledFont`/`UIFontMetrics`-based scaling** (relative to the type scale in §4.2) instead of fixed point sizes, so the font still respects the user's **Dynamic Type** accessibility setting.
- **Android wiring**: bundle Nunito under `res/font/`, define it as a `FontFamily` in the Compose `Typography` object. **Must specify all text in `sp` units** (never `dp`) so the font still respects the user's **font scale** accessibility setting.
- **Trade-off vs. system fonts**: unlike SF Pro/Roboto, Nunito doesn't get accessibility scaling "for free" — the `.scaledFont`/`sp`-unit wiring above is required, not optional, to stay WCAG-compliant (see §8, Accessibility). Skipping this wiring to save implementation time is **not acceptable**. Nunito also adds a small app size increase (~200–300KB for the three weights above) and loses the OS-level rendering optimizations of a system font, both accepted as reasonable trade-offs for stronger brand consistency.

### 4.2 Type Scale
| Style | Size (pt/sp) | Weight | Usage |
|---|---|---|---|
| Display | 32 | Bold (700) | Onboarding / empty state headline |
| Title | 22 | Semibold (600) | Screen titles ("Projects", "New Trim") |
| Body Large | 17 | Regular (400) | Primary body text, project names |
| Body | 15 | Regular (400) | Secondary descriptions |
| Caption | 13 | Regular (400) | Timestamps, durations, metadata |
| Button Label | 16 | Semibold (600) | All button/CTA text |

### 4.3 Typography Rules
- Never use more than 3 type sizes on a single screen.
- Line height: 1.3x–1.4x font size for body text (readability on small screens).
- Left-align all body text; center-align only short titles/empty states.

## 5. Spacing & Layout

### 5.1 Spacing Scale (8pt base grid)
| Token | Value | Usage |
|---|---|---|
| `space-xs` | 4px | Icon-to-label gaps |
| `space-sm` | 8px | Compact internal padding |
| `space-md` | 16px | Standard padding (screen margins, card padding) |
| `space-lg` | 24px | Section spacing |
| `space-xl` | 32px | Major section breaks |
| `space-2xl` | 48px | Empty state vertical spacing |

### 5.2 Layout Rules
- Standard screen horizontal margin: **16px** both platforms.
- Card/list item corner radius: **16px** (soft, modern, matches social app conventions).
- Button corner radius: **12px** for standard buttons, **full pill (999px)** for primary CTA/floating action buttons.
- Minimum tap target: **44x44pt (iOS)** / **48x48dp (Android)** — accessibility requirement, non-negotiable.
- Video preview thumbnails: always rendered in original aspect ratio, never stretched or cropped in list views.

## 6. Key Screens — Design Direction

### 6.1 Projects List (Home)
- Grid or list of video thumbnails (project name + duration badge overlay, e.g., "2s").
- Empty state: centered illustration/icon + single primary CTA "Create your first trim."
- Floating primary action button (bottom-right, thumb-friendly) to start a new project.
- **Upgrade entry point**: a small, persistent pill/banner (using `premium-accent`) near the top of the list — e.g., "Unlock custom trims, full quality & more →" — visible only to free-tier users, hidden entirely once purchased. Tapping opens the Paywall screen (§6.6). This is proactive discovery, separate from the reactive lock affordances described in §6.2/§6.3.
- A settings/help icon (top corner, small, low-emphasis) provides access to the Help/Settings screen (§6.7), which houses the manual "Restore Purchases" failsafe.

### 6.2 Video Selection & Reorder Screen
- After picking videos (1–10 free / 1–20 paid), show a **vertical list** of selected videos: thumbnail (left), file name + file size + duration (middle), and a **drag handle** (right) for reordering.
- Long-press-and-drag (or a dedicated drag handle icon) to reorder — standard native list-reorder interaction (`onMove`/`.onDrag` in SwiftUI's `List`, `ItemTouchHelper` or Compose's reorderable-list pattern on Android).
- Sticky header or footer shows a **live running total**: "Merged length: Xs" — updates instantly as duration selection changes or videos are added/removed.
- A small "+" affordance allows adding more videos, up to the tier's cap (10 free, 20 paid). Once a **free-tier** user hits 10, the "+" affordance shows a small **lock icon** (`premium-accent`) instead of disappearing — tapping it opens the Paywall screen rather than silently doing nothing.
- Each row supports a remove (✕) action to drop a video from the selection before confirming.

### 6.3 New Project / Trim Duration Screen
- Full-bleed video preview at top (largest element on screen) — for multi-video projects, this previews the merged result once trimming/merging completes.
- Duration selector: **segmented control** with 3 fixed options (1s / 2s / 3s), disabled state (greyed, non-tappable) for any option exceeding the **shortest** selected video's length.
- A **4th segment**, "Custom," sits alongside the fixed options:
  - **Free tier**: shown in a disabled state with a small **lock icon** (`premium-accent`). Tapping it opens the Paywall screen.
  - **Paid tier**: tapping "Custom" reveals a simple numeric stepper/input (1–5s, in 0.1s or whole-second increments — exact granularity is an implementation detail, not fixed here) instead of a fixed value.
- A small **quality indicator** near the preview (e.g., a subtle "720p" or "HD" badge) shows the export quality that will apply:
  - **Free tier**: always shows "720p," with a tappable **lock icon** next to it (`premium-accent`) that opens the Paywall screen, explaining the higher-quality unlock.
  - **Paid tier**: shows the actual resolution being used (e.g., "1080p," "4K"), no lock icon.
- Single primary CTA at bottom: "Preview Trim" → "Save Project."
- No nested menus, no settings icons on this screen — keep it single-purpose.

### 6.4 Save/Naming Screen
- Simple text input, pre-filled with a smart default (e.g., first source filename or "Trim {date}").
- Primary CTA: "Save."
- Secondary/ghost action: "Discard."

### 6.5 Paywall Screen
- Presented as a **modal sheet** (not a full navigation push) — reinforces that it's an optional detour, not a required step.
- Simple, benefit-led layout: 3 short rows, one per unlocked feature (custom trim duration, full quality exports, 20-video projects) — each with a small icon and one line of copy, no marketing fluff.
- Price displayed clearly and once: "$5.00 — one-time purchase" (exact formatting/localization handled by the platform's native price string from StoreKit/Play Billing, not hardcoded).
- Single primary CTA: "Unlock MidTrim" (or platform-appropriate purchase button, following StoreKit/Play Billing UI guidelines where applicable).
- Small secondary link: "Restore Purchases" (duplicates the failsafe in Help/Settings, since a user landing here mid-flow shouldn't have to leave to find it).
- Dismiss via standard sheet gesture (swipe down) or a close (✕) affordance — never trap the user.
- Uses `premium-accent` sparingly for the CTA/icons, keeping the rest of the screen consistent with the app's standard dark palette (this is not a separate "special" visual theme, just an accent-highlighted variant of the same design system).

### 6.6 Help/Settings Screen
- Minimal, single-purpose screen (per the app's overall philosophy of avoiding dense settings screens) — this is **not** a general settings hub, just a small utility screen for MVP.
- Contents (MVP scope only):
  - **"Restore Purchases"** button (primary utility of this screen) — tapping it triggers a manual re-check against StoreKit/Play Billing, with a brief loading state and a clear success/failure toast ("Purchases restored" / "No purchases found to restore").
  - App version number (small, low-emphasis text).
  - A link to the privacy policy (required for store listings; simple text link, opens in-app browser or system browser).
  - **"Open Source Licenses"** link — a simple text screen/link listing bundled third-party assets and their licenses, at minimum the **Nunito font (SIL Open Font License)** per §4.1's attribution requirement. Low-emphasis, same visual weight as the privacy policy link.
- Accessed via the small settings/help icon on the Projects List screen (§6.1).
- No account settings, no login, no other configuration options in MVP — consistent with PRD's no-custom-account scope.

## 7. Motion & Interaction
- Transitions: simple fade/slide, **200–250ms** duration, ease-in-out — avoid bouncy/playful physics (keep it fast and functional, not gimmicky).
- Haptic feedback on: duration selection, save confirmation, delete confirmation, **successful purchase, successful restore** (iOS: `UIImpactFeedbackGenerator`/`UINotificationFeedbackGenerator` for success, Android: `HapticFeedbackConstants`).
- Loading states: use a simple native spinner/progress indicator during trim processing — always show determinate progress if trim operation exceeds ~1 second. The same pattern applies to the brief loading state during purchase/restore verification.

## 8. Accessibility
- All interactive elements must support VoiceOver (iOS) / TalkBack (Android) labels.
- Respect system font scaling (Dynamic Type / Android font scale) up to at least 130% without breaking layout.
- Color contrast: all text must meet **WCAG AA** minimum contrast ratio (4.5:1 for body text).

## 9. Design Non-Goals (MVP)
- No custom onboarding carousel/tutorial — keep first-launch experience to a single empty state screen with a clear CTA.
- No theming/customization options (dark/light follows system only).
- No animations beyond standard system transitions.
- No separate "premium" visual theme — paid-tier users see the exact same UI as free-tier users, minus lock icons and the upgrade banner. `premium-accent` is used only for the free-tier's lock/upgrade affordances, never as an ongoing "you're premium" badge or theme.
- No dedicated onboarding/tutorial explaining the paywall — the inline lock-affordance pattern (§6.2, §6.3) and the Paywall screen itself (§6.5) are self-explanatory and carry the full burden of communicating what's paid vs. free.
