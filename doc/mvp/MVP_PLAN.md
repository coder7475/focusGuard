# FocusGuard — MVP Plan

**Status:** Draft v1 · **Owner:** Fahad (@coder7475) · **Last updated:** 2026-10-04
**Source repo:** `com.coder7475.focusguard` · Kotlin + Jetpack Compose · minSdk 24 / targetSdk 37

---

## 1. Problem & Vision

Phone addiction is fought badly by willpower alone. FocusGuard's promise:

> **When a focus session is active, the phone is unusable for everything except calls and messages.**

The lock is device-wide, has priority over other apps, and cannot be dismissed by
switching apps, opening recents, or pressing back. Users choose either a
**quick session** (15 min / 45 min / 1 hr) or a **recurring schedule**
(e.g. 8:00 PM → 8:00 AM).

## 2. Goals & Non-Goals

### Goals (must ship in MVP)

| # | Goal |
| --- | --- |
| G1 | Start a timed focus session from one tap (15 / 45 / 60 min presets + custom) |
| G2 | Define a recurring schedule with an overnight window (20:00 → 08:00) |
| G3 | Enforce a full-screen lock over any non-allowlisted app while active |
| G4 | Keep calls, SMS/messaging, and the emergency dialer usable at all times |
| G5 | Survive reboot, app-swipe, and Doze; lock state is persisted, not in-memory |
| G6 | Onboarding that explains and grants every required permission in order |
| G7 | Zero network, zero tracking — 100% local storage (privacy is the pitch) |

### Non-Goals (explicitly out of MVP)

- Website / browser-URL blocking (needs a VPN or per-browser hooks)
- App-by-app block lists and per-app time budgets — MVP locks *everything* non-allowlisted
- Social/account features, cloud sync, paid tier
- Parental controls, kiosk/device-owner mode, root
- Screen-time analytics dashboards (only a minimal "sessions completed" counter)

## 3. Target User

- **Primary:** 16–30, student or knowledge worker, tries Digital Wellbeing /
  Screen Time, disables them because they are too easy to override.
- **Trigger:** "I picked up my phone 80 times today."
- **Success moment:** the 45-minute session ends and they did not touch Instagram.

## 4. Research Summary

Findings from the reference set in [§13](#13-references):

1. **Accessibility Service is the right enforcement primitive** for a
   non-enterprise app. It receives `TYPE_WINDOW_STATE_CHANGED` /
   `TYPE_WINDOWS_CHANGED` events for foreground app changes and can draw a
   `TYPE_ACCESSIBILITY_OVERLAY` window that sits above *other apps' UI* —
   including the Settings screen — without needing `SYSTEM_ALERT_WINDOW`
   ([a11y service guide][r1], [overlay codelab][r4]).
2. **DevicePolicyManager / lock-task mode is not viable** for Play
   distribution: real device control requires device-owner or profile-owner
   provisioning, which a normal consumer app cannot obtain
   ([DevicePolicyManager][r6], [lock task mode][r7]).
3. **UsageStatsManager is a good backup detector, a bad primary one.**
   `queryEvents` data is delayed and only retained for a few days; on Android 14+
   the most recent events can lag, so polling it adds latency and battery cost
   ([UsageStatsManager][r8], [UsageEvents][r9]). Use it for stats and as a
   fallback, not as the blocking trigger.
4. **Foreground service types are mandatory** when targeting API 34+, and
   Android 15 further restricts launching FGS from `BOOT_COMPLETED` and
   narrows the `SYSTEM_ALERT_WINDOW` background-start exemption
   ([FGS types required][r10], [FGS changes][r11]). Plan for `specialUse`
   with a written Play justification.
5. **Exact alarms are denied by default** on Android 14+ unless the app
   declares core functionality ([exact alarms][r12]). Schedule boundaries
   should prefer `setExactAndAllowWhileIdle()` behind a
   `SCHEDULE_EXACT_ALARM` grant check, with an inexact fallback.
6. **Google Play scrutinises accessibility usage.** The API must be declared
   in the store listing, must not be used to circumvent privacy controls, and
   must be the app's core, disclosed functionality
   ([accessibility policy][r13], [Play policies][r14]). FocusGuard's whole
   product *is* assistive enforcement, which is the defensible position —
   but the declaration and a user-facing explanation are mandatory.
7. **Shipped open-source comparables** confirm the architecture and reveal
   the expected bypass holes to design against: turning the accessibility
   service off, uninstalling, and OEM battery killers
   ([AppBlock][r15], [SelfLock][r16], [UltraFocus][r17], [AppBlockr][r18]).

### Approach decision

| Option | Verdict | Why |
| --- | --- | --- |
| **Accessibility Service + overlay** | **Chosen** | Works on stock Android without enterprise provisioning; event-driven (low battery); overlay draws above other apps |
| Device admin / device owner | Rejected | Requires enterprise provisioning, not available on Play for consumers |
| `SYSTEM_ALERT_WINDOW` only | Rejected | Detects nothing by itself — you still need a trigger; and Android 15 tightened its FGS exemption |
| UsageStats polling loop | Rejected as primary | Latency + battery; kept as fallback detector |
| VPN / LocalVPN for web | Deferred | Out of MVP scope, conflicts with users' own VPNs |

## 5. User Stories (MVP)

| ID | Story | Acceptance |
| --- | --- | --- |
| US1 | As a user I can start a 15/45/60-min session with one tap | Tap → confirm → lock active within 2 s |
| US2 | As a user I can create a recurring overnight schedule | 20:00–08:00 saved, persists reboot, auto-arms |
| US3 | As a user I see a countdown while locked | Overlay shows remaining mm:ss, updates each second |
| US4 | As a user I can still make and receive calls | Dialer + Phone UI never covered by overlay |
| US5 | As a user I can still send/receive messages | Allowlisted messaging apps open normally |
| US6 | As a user I cannot escape early | Back, Home, recents, and notification shade do not dismiss the lock |
| US7 | As a user I am guided through permissions | Ordered onboarding; each step verifies grant state |
| US8 | As a user I survive a reboot mid-session | Session resumes with correct remaining time |
| US9 | As a user I get a completion notification | "Session complete" fires at end + on unlock |
| US10 | As a user I can see how many sessions I finished | Simple counter, no analytics |

## 6. Scope

### P0 — must have (MVP)

- Session engine: presets 15/45/60 min + custom, start/cancel-before-start
- Schedule engine: weekly days + start/end time, overnight window support
- `FocusGuardAccessibilityService` — foreground-app detection
- Full-screen lock overlay (`TYPE_ACCESSIBILITY_OVERLAY`, Compose content)
- Allowlist: Phone/dialer, emergency dialer, Messages (default, non-editable in MVP)
- Foreground service with `specialUse` type holding the countdown
- Persistence: Room (sessions, schedules) + DataStore (flags)
- Reboot receiver + schedule re-arm via `AlarmManager`
- Permission onboarding flow (7 steps, see §7)
- Session-complete notification
- Minimal history: completed-session counter

### P1 — should have (stretch, if time allows)

- Emergency "hard unlock" with a 60 s cooldown and a logged attempt
- Battery-optimisation exemption prompt (OEM-specific guidance)
- Home-screen widget with active-session countdown
- Grayscale / dimming option on the lock screen

### P2 — later

- Per-app block lists, website blocking, screen-time stats
- Streaks and charts, export
- Monetisation, themes

## 7. Architecture

```
┌─────────────────────────────────────────────────────────────┐
│ UI (Jetpack Compose, Material 3)                           │
│  Onboarding · Home · Schedule editor · History             │
└───────────────┬─────────────────────────────────────────────┘
                │ StateFlow / callbacks
┌───────────────▼─────────────────────────────────────────────┐
│ domain/                                                    │
│  SessionEngine  ·  ScheduleEngine  ·  AllowlistPolicy      │
└───────┬──────────────────────────────┬──────────────────────┘
        │                              │
┌───────▼───────────────┐   ┌──────────▼──────────────────────┐
│ FocusGuardService     │   │ FocusGuardAccessibilityService │
│ (foreground,          │   │  onAccessibilityEvent →        │
│  specialUse, tick +   │   │  TYPE_WINDOW_STATE_CHANGED     │
│  alarm-driven start)  │   │  → show/hide lock overlay      │
└───────┬───────────────┘   └──────────┬──────────────────────┘
        │                              │ WindowManager
┌───────▼──────────────────────────────▼──────────────────────┐
│ LockOverlay (Compose in TYPE_ACCESSIBILITY_OVERLAY window) │
└─────────────────────────────────────────────────────────────┘
        │
┌───────▼───────────────┐   ┌───────────────────────────────┐
│ data/                │   │ DetectedAppRepository         │
│  Room: Session,      │   │  UsageStatsManager (fallback  │
│  Schedule            │   │  + history only)              │
│  DataStore: settings │   └───────────────────────────────┘
└───────────────────────┘
```

### Component notes

- **Detection:** rely on `AccessibilityServiceInfo.eventTypes =
  TYPE_WINDOW_STATE_CHANGED | TYPE_WINDOWS_CHANGED` with
  `notificationTimeout` tuned low. Only act when
  `event.packageName` is outside the allowlist **and** a session is active.
- **Overlay:** inflated from Compose via `AbstractComposeView` added to
  `WindowManager` with `TYPE_ACCESSIBILITY_OVERLAY`,
  `FLAG_NOT_FOCUSABLE` off (so it eats back/home), `FLAG_LAYOUT_IN_SCREEN`,
  and `setShowWhenLocked`. Never use `SYSTEM_ALERT_WINDOW` as the primary path.
- **Countdown truth** lives in `SessionEngine`, driven by
  `AlarmManager.setExactAndAllowWhileIdle` for the end boundary and a
  foreground-service ticker for display only. Single source of truth =
  `endsAtEpochMillis` in Room, so reboot/kill cannot desync it.
- **Allowlist (MVP, hard-coded):** `com.google.android.dialer`,
  `com.android.dialer`, `com.google.android.apps.messaging`,
  `com.android.mms`, `com.android.server.telecom`, plus the emergency
  activity `com.android.phone.EmergencyDialer`. The system ringing/incoming
  call UI is a system window and is never covered by our overlay.
- **Bypass resistance:** overlay window flags swallow Back/Home;
  recents and notification shade remain reachable by design (blocking them
  requires device-owner); we detect service-disabled state and warn loudly.

### Manifest permissions (MVP)

| Permission | Why | Grant path |
| --- | --- | --- |
| `BIND_ACCESSIBILITY_SERVICE` | system-only bind for detection | Settings → Accessibility |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` | countdown/session holder | install-time |
| `POST_NOTIFICATIONS` | session complete + warnings | runtime (API 33+) |
| `SCHEDULE_EXACT_ALARM` | precise session/schedule end | special-access screen |
| `RECEIVE_BOOT_COMPLETED` | re-arm schedules after reboot | install-time |
| `USE_FULL_SCREEN_INTENT` | full-screen "session complete" | install-time / Play declaration |
| `PACKAGE_USAGE_STATS` | fallback detection + history | special-access (Usage Access) |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | survive Doze on aggressive OEMs | runtime, justified dialog |
| `QUERY_ALL_PACKAGES` | list installed apps for allowlist UI | Play declaration required |

> **Play note:** `QUERY_ALL_PACKAGES`, `PACKAGE_USAGE_STATS`, exact alarms, and
> the accessibility API each need a declaration in Play Console. Budget review
> time for this (§9, M4).

## 8. Skills Needed

### Technical (build the thing)

| Skill | Level | Why the MVP needs it | Learn |
| --- | --- | --- | --- |
| Kotlin (coroutines, Flow, sealed types) | Advanced | engine + service plumbing | [Kotlin docs][s1] |
| Jetpack Compose + Material 3 | Advanced | all UI incl. the lock overlay | [Compose pathway][s2] |
| Android Service lifecycle, FGS types | Advanced | countdown service, Android 14/15 rules | [FGS guide][s3] |
| **AccessibilityService API** | **Advanced — core** | detection + overlay window | [A11y service guide][s4], [codelab][s5] |
| `WindowManager` overlay windows | Advanced | lock screen above other apps | [TYPE_ACCESSIBILITY_OVERLAY sample][s6] |
| Room + DataStore | Intermediate | sessions, schedules, settings | [Room guide][s7] |
| `AlarmManager` / exact alarms | Intermediate | precise schedule boundaries | [Schedule alarms][s8] |
| Permissions & special-access flows | Intermediate | onboarding of 7 grants | [App permissions][s9] |
| `UsageStatsManager` | Intermediate | fallback detection, history | [Usage stats][s10] |
| Notifications + full-screen intent | Intermediate | session complete | [Notifications][s11] |
| Git / branching / PR hygiene | Intermediate | milestone-based delivery | — |
| JUnit + Compose UI test + instrumented tests | Intermediate | regression on lock behaviour | [Testing overview][s12] |

### Platform & product (ship the thing safely)

| Skill | Why |
| --- | --- |
| Google Play policy literacy | Accessibility API declaration, FGS type declarations, `QUERY_ALL_PACKAGES` justification — rejection here blocks launch |
| OEM battery-management knowledge | Xiaomi/Huawei/Samsung/OnePlus each kill background services differently; needs a per-OEM help screen |
| Permission UX / microcopy | 7 sensitive grants in sequence — bad wording = drop-off |
| Android 14/15 behaviour-change tracking | FGS types, exact alarms, background-start exemptions change yearly |
| Threat modelling for self-control apps | Design against the obvious bypasses (disable service, uninstall, force-stop) |
| Accessibility (TalkBack) testing | Ironically, an app built on the a11y API must itself be usable with TalkBack |

### Tooling

Android Studio (Ladybug+), JDK 11+, Gradle Kotlin DSL, ADB, a physical test
device (emulator's accessibility settings differ from OEM builds), Firebase
Test Lab or a device farm for OEM coverage, GitHub Actions for CI
(`assembleDebug` + `test` + lint on every PR).

## 9. Milestones

| M | Deliverable | Est. | Exit criteria |
| --- | --- | --- | --- |
| **M0 — Skeleton** | Navigation shell, theme, data layer (Room models, DAOs), CI green | 3 d | `./gradlew test` passes on CI |
| **M1 — Detection spike** | Accessibility service logs foreground package live; Usage Access fallback works | 4 d | Correct package on 3 real devices, <100 ms latency |
| **M2 — Lock** | Overlay shows over a blocked app, swallows back/home, allowlist passes dialer + messages | 5 d | US3, US4, US5, US6 pass on-device |
| **M3 — Engine** | Presets, schedules, alarms, foreground service, reboot resume, notifications | 6 d | US1, US2, US8, US9 pass; 24 h soak with no missed end |
| **M4 — Onboarding + hardening** | Permission wizard, battery-killer help screens, Play declarations drafted, store listing text | 5 d | New user reaches active session in ≤90 s |
| **M5 — Test + release** | Automated suite, manual matrix (API 24/29/31/34/37 + 4 OEMs), internal track | 4 d | All P0 stories signed off |

**Total ≈ 5–6 weeks** for a single developer.

### Test matrix (M5)

- API levels 24, 29, 31, 34, 37 · devices: Pixel + Samsung + Xiaomi + OnePlus
- Cases: start/cancel, back/home/recents during lock, incoming + outgoing call
  during lock, SMS during lock, reboot mid-session, alarm at schedule boundary,
  accessibility service toggled off mid-session, Doze overnight, battery
  saver on, app force-stopped by user.
- Automation: Room/engine unit tests, Compose UI tests for onboarding,
  instrumented test asserting the overlay view is attached while a session
  is active.

## 10. Success Metrics

| Metric | MVP target |
| --- | --- |
| Session completion rate (started → finished without force-stop) | ≥ 90 % |
| Onboarding → first active session | ≥ 60 % of installs, ≤ 90 s |
| Crash-free sessions (Play Vitals) | ≥ 99.5 % |
| Missed schedule/endTime alarms in 7-day soak | 0 |
| Accessibility service disabled mid-session detection | 100 % |

## 11. Risks & Mitigations

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Play rejects accessibility-API usage | Launch blocker | Disclose as core functionality in listing + in-app; keep `IsAccessibilityTool` honest; prepare an appeal path |
| OEM kills the foreground service | Lock silently stops | Battery-optimisation exemption + per-OEM help screens; alarm-driven re-check of `endsAt` |
| User disables the accessibility service to escape | Core promise broken | Detect `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` change, show persistent warning notification; P1 cooldown on re-enable |
| Android 15+ FGS background-start rules | Session fails to start from alarm | Exact-alarm exemption path; fall back to full-screen-intent notification |
| Overlay doesn't cover system dialogs / recents | Partial escape | Document honestly; recents shade intentionally reachable (calls/notifications remain usable) |
| `QUERY_ALL_PACKAGES` or `PACKAGE_USAGE_STATS` declaration rejected | Feature cut | Fallback: derive allowlist from system dialer/messaging roles only — no broad package query |
| Scope creep (web blocking, stats) | MVP slips | §2 non-goals are binding; anything new goes to P2 backlog |

## 12. Definition of Done (MVP)

- [ ] All P0 user stories (US1–US10) verified on-device across the test matrix
- [ ] `./gradlew lint test connectedAndroidTest` green in CI
- [ ] 24-hour soak: session started before sleep ends on time after Doze + reboot
- [ ] Onboarding copy reviewed; every permission explains why in one sentence
- [ ] Play Console declarations drafted (accessibility, FGS type, QUERY_ALL_PACKAGES, usage access)
- [ ] README architecture section matches the shipped code

---

## 13. References

### Official Android documentation

- `r1`: [Create an accessibility service](https://developer.android.com/guide/topics/ui/accessibility/service) — service lifecycle, event types, overlays
- `r2`: [Create your own accessibility service (Views)](https://developer.android.com/guide/topics/ui/accessibility/views/service) — manifest + `AccessibilityServiceInfo` config
- `r3`: [AccessibilityService API reference](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService) — `performGlobalAction`, `disableSelf`, overlay attach APIs
- `r4`: [Developing an Accessibility Service for Android (Google Codelab)](https://codelabs.developers.google.com/codelabs/developing-android-a11y-service) — working overlay example, `TYPE_ACCESSIBILITY_OVERLAY`
- `r5`: [android-accessibility-overlay sample (GitHub)](https://github.com/thbecker/android-accessibility-overlay) — minimal overlay-above-Settings example
- `r6`: [DevicePolicyManager](https://developer.android.com/reference/kotlin/android/app/admin/DevicePolicyManager.html) — why device-owner control is not available to consumer apps
- `r7`: [Lock task mode (Android Enterprise)](https://developer.android.com/work/dpc/dedicated-devices/lock-task-mode)
- `r8`: [UsageStatsManager](https://developer.android.com/reference/android/app/usage/UsageStatsManager) — `queryEvents`, retention limits
- `r9`: [UsageEvents.Event](https://developer.android.com/reference/android/app/usage/UsageEvents.Event) — `ACTIVITY_RESUMED` as foreground signal
- `r10`: [Foreground service types are required (Android 14)](https://developer.android.com/about/versions/14/changes/fgs-types-required)
- `r11`: [Changes to foreground services](https://developer.android.com/develop/background-work/services/fgs/changes) — Android 15 `SYSTEM_ALERT_WINDOW` and `BOOT_COMPLETED` limits
- `r12`: [Schedule exact alarms are denied by default (Android 14)](https://developer.android.com/about/versions/14/changes/schedule-exact-alarms) and [Schedule alarms guide](https://developer.android.com/develop/background-work/services/alarms)
- `r13`: [Accessibility features in Google Play](https://support.google.com/googleplay/answer/16318151)
- `r14`: [Google Play Policies (Android Developers)](https://developer.android.com/distribute/play-policies)
- `r19`: [Set up edge-to-edge (Compose)](https://developer.android.com/develop/ui/compose/system/setup-e2e) and [Window insets](https://developer.android.com/develop/ui/compose/system/insets) — immersive lock overlay
- `r20`: [Foreground service types reference](https://developer.android.com/develop/background-work/services/fgs/service-types) — `specialUse` + `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`

### Comparable open-source implementations

- `r15`: [AppBlock (SadeekFarhan21)](https://github.com/SadeekFarhan21/AppBlock) — a11y-driven blocking, quick-block presets, strict mode; closest MVP analogue
- `r16`: [SelfLock (EtashTyagi)](https://github.com/EtashTyagi/SelfLock) — Kotlin + Compose + Hilt + Room, exact alarms, boot receiver, permission checklist
- `r17`: [UltraFocus (Binondi)](https://github.com/Binondi/UltraFocus) — duration-based focus mode, permission list mirrors ours
- `r18`: [AppBlockr (diyarfaraj)](https://github.com/diyarfaraj/AppBlockr) — minimal background-service blocker
- `r21`: [compose-overlay-window (ArthurKun21)](https://github.com/ArthurKun21/compose-overlay-window) — rendering Compose UI in a global overlay window
- `r22`: [Curbox (F-Droid)](https://f-droid.org/en/packages/neth.iecal.curbox) — reference for unlock-challenge UX (post-MVP)

### Commercial references (behaviour, not code)

- `r23`: [Freedom](https://freedom.to) — session model + cross-device blocking expectations
- Digital Wellbeing (built into Android) — what users already tried and found too weak

### Skills learning resources

- `s1`: [Kotlin official docs](https://kotlinlang.org/docs/home.html)
- `s2`: [Jetpack Compose pathway (Android Developers)](https://developer.android.com/courses/pathways/compose)
- `s3`: [Foreground services guide](https://developer.android.com/develop/background-work/services/fgs)
- `s4`: [Create an accessibility service](https://developer.android.com/guide/topics/ui/accessibility/service)
- `s5`: [Accessibility Service codelab](https://codelabs.developers.google.com/codelabs/developing-android-a11y-service)
- `s6`: [android-accessibility-overlay sample](https://github.com/thbecker/android-accessibility-overlay)
- `s7`: [Room persistence library](https://developer.android.com/training/data-storage/room)
- `s8`: [Schedule alarms](https://developer.android.com/develop/background-work/services/alarms)
- `s9`: [App permissions overview](https://developer.android.com/guide/topics/permissions/overview)
- `s10`: [Monitor device activity with UsageStatsManager](https://developer.android.com/app-hub/usage-stats)
- `s11`: [Notifications overview](https://developer.android.com/develop/ui/views/notifications)
- `s12`: [Test your app (Android)](https://developer.android.com/training/testing)

[r1]: https://developer.android.com/guide/topics/ui/accessibility/service
[r2]: https://developer.android.com/guide/topics/ui/accessibility/views/service
[r3]: https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
[r4]: https://codelabs.developers.google.com/codelabs/developing-android-a11y-service
[r5]: https://github.com/thbecker/android-accessibility-overlay
[r6]: https://developer.android.com/reference/kotlin/android/app/admin/DevicePolicyManager.html
[r7]: https://developer.android.com/work/dpc/dedicated-devices/lock-task-mode
[r8]: https://developer.android.com/reference/android/app/usage/UsageStatsManager
[r9]: https://developer.android.com/reference/android/app/usage/UsageEvents.Event
[r10]: https://developer.android.com/about/versions/14/changes/fgs-types-required
[r11]: https://developer.android.com/develop/background-work/services/fgs/changes
[r12]: https://developer.android.com/about/versions/14/changes/schedule-exact-alarms
[r13]: https://support.google.com/googleplay/answer/16318151
[r14]: https://developer.android.com/distribute/play-policies
[r15]: https://github.com/SadeekFarhan21/AppBlock
[r16]: https://github.com/EtashTyagi/SelfLock
[r17]: https://github.com/Binondi/UltraFocus
[r18]: https://github.com/diyarfaraj/AppBlockr
[r19]: https://developer.android.com/develop/ui/compose/system/setup-e2e
[r20]: https://developer.android.com/develop/background-work/services/fgs/service-types
[r21]: https://github.com/ArthurKun21/compose-overlay-window
[r22]: https://f-droid.org/en/packages/neth.iecal.curbox
[r23]: https://freedom.to
[s1]: https://kotlinlang.org/docs/home.html
[s2]: https://developer.android.com/courses/pathways/compose
[s3]: https://developer.android.com/develop/background-work/services/fgs
[s4]: https://developer.android.com/guide/topics/ui/accessibility/service
[s5]: https://codelabs.developers.google.com/codelabs/developing-android-a11y-service
[s6]: https://github.com/thbecker/android-accessibility-overlay
[s7]: https://developer.android.com/training/data-storage/room
[s8]: https://developer.android.com/develop/background-work/services/alarms
[s9]: https://developer.android.com/guide/topics/permissions/overview
[s10]: https://developer.android.com/app-hub/usage-stats
[s11]: https://developer.android.com/develop/ui/views/notifications
[s12]: https://developer.android.com/training/testing
