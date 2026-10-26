# FocusGuard — MVP Product Specification

**Status:** Approved MVP · **Owner:** Fahad (@coder7475)  
**Package:** `com.coder7475.focusguard` · Kotlin · Jetpack Compose · minSdk 24 / targetSdk 37  
**Architecture Spec:** [`doc/design/SYSTEM_ARCHITECTURE.md`](file:///home/fahad/AndroidStudioProjects/focusGuard/doc/design/SYSTEM_ARCHITECTURE.md)  
**ADR Records:** [`doc/design/adrs/`](file:///home/fahad/AndroidStudioProjects/focusGuard/doc/design/adrs/)

---

## 1. Problem & Vision

Phone addiction is fought poorly by willpower alone. FocusGuard enforces hard commitment:

> **When a focus session is active, the phone is unusable for everything except essential calls and emergency communications. The lock is un-dismissible, survives reboots, and has priority over third-party applications.**

Users initiate either an **ad-hoc quick session** (15 min / 45 min / 1 hr presets or custom duration) or define an **automated recurring schedule** (e.g. 20:00 → 08:00 bedtime routine).

---

## 2. Goals & Non-Goals

### Core Goals (P0 MVP)
| # | Goal | Description |
| :--- | :--- | :--- |
| **G1** | **Quick Focus Session** | Start a timed focus lock with one tap (15, 45, 60 min presets + custom timer). |
| **G2** | **Recurring Overnight Schedule** | Weekly scheduled windows supporting midnight-crossing intervals (20:00 to 08:00). |
| **G3** | **Enforcement Lock Overlay** | Full-screen non-dismissible lock window over all non-allowlisted apps. |
| **G4** | **Communications Safety** | Incoming/outgoing phone calls, SMS, and emergency dialer remain usable at all times. |
| **G5** | **Reboot & Crash Resilience** | Lock state stored in persistent SQLite; survives process death, Doze, and device reboot. |
| **G6** | **Guided Onboarding** | Step-by-step wizard explaining and verifying each system permission grant state. |
| **G7** | **Zero Network / 100% Offline** | Strictly local execution with zero network telemetry or tracking. |

### Non-Goals (Explicitly Out of MVP)
- Web/URL filtering (deferred; requires local VPN hooks).
- Granular per-app blocking lists (MVP enforces all-app lockdown except essential communications).
- Cloud accounts, social features, or cross-device synchronization.
- Kiosk mode or MDM/Device-Owner provisioning (must remain 100% Play Store compliant for regular consumers).

---

## 3. User Stories (MVP Acceptance Criteria)

| ID | User Story | Acceptance Criteria |
| :--- | :--- | :--- |
| **US1** | Start Quick Session | Tap preset chip $\to$ lock activates within 100ms. |
| **US2** | Configure Overnight Schedule | Save 20:00–08:00 recurring window; persists reboot and auto-arms exact alarm. |
| **US3** | Full-Screen Countdown | Lock overlay displays live `MM:SS` countdown with smooth updates. |
| **US4** | Call Pass-Through | Phone dialer and system ringing UI immediately dismiss or bypass overlay. |
| **US5** | SMS Pass-Through | Default system SMS app opens without lock obstruction. |
| **US6** | Bypass Resistance | Back, Home gestures, and task switches do not dismiss or escape the lock. |
| **US7** | Onboarding Flow | 7-step wizard guides user through permissions; validates live grant status. |
| **US8** | Reboot Survival | Re-arms active session and exact alarm immediately following device restart. |
| **US9** | Session Notifications | Persistent ticker notification during lock; completion notification upon expiration. |
| **US10** | Session History | Displays a count of successfully completed focus sessions. |

---

## 4. Scope & Feature Boundaries

### P0 (Must Have — MVP Release Gate)
- **Session Engine**: Presets (15/45/60 min) + custom picker, start/cancel-before-start.
- **Schedule Engine**: Day-of-week bitmask, overnight interval math, exact alarm arming.
- **Accessibility Service**: Foreground window interception via `TYPE_WINDOW_STATE_CHANGED`.
- **Lock Overlay**: Jetpack Compose rendered within `TYPE_ACCESSIBILITY_OVERLAY` window.
- **Allowlist Policy**: Dynamic system dialer, emergency dialer, and default SMS resolution.
- **Resilience Engine**: `BootCompletedReceiver` + Room SQLite absolute epoch timestamps.
- **Permission Wizard**: 7 ordered grant steps with deep-linking to system settings.

### P1 (Post-MVP Enhancements)
- Emergency "parachute" token exit mechanism with deliberate confirmation friction.
- Rotary dial timer selector with tactile haptic detents.
- OEM-specific battery optimization guides (MIUI, OneUI, OxygenOS).
- Gamified completion tower visualization.

---

## 5. Architectural Alignment

FocusGuard decouples presentation from Android framework services using a clean layered architecture:

```
┌─────────────────────────────────────────────────────────────┐
│ Presentation Layer (Jetpack Compose, Material 3)            │
│  Onboarding · Home Screen · Lock Overlay · Schedule Dialog │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Events
┌──────────────────────────────▼──────────────────────────────┐
│ Domain Engine Layer (Pure Kotlin / Coroutines)             │
│  SessionEngine · ScheduleEngine · AllowlistPolicy           │
└───────────────┬──────────────────────────────┬──────────────┘
                │                              │
┌───────────────▼──────────────┐  ┌────────────▼──────────────┐
│ FocusGuardService            │  │ Accessibility & Overlay   │
│ (Foreground specialUse,      │  │ FocusGuardAccessibility-  │
│  AlarmCoordinator, Ticker)   │  │ Service & WindowManager   │
└───────────────┬──────────────┘  └────────────┬──────────────┘
                │                              │
┌───────────────▼──────────────────────────────▼──────────────┐
│ Data & Persistence Layer (Room SQLite, DataStore)           │
│  FocusGuardDatabase · SessionDao · ScheduleDao · Prefs      │
└─────────────────────────────────────────────────────────────┘
```

*For complete component specs, C4 diagrams, and failure mode analysis, refer to [`doc/design/SYSTEM_ARCHITECTURE.md`](file:///home/fahad/AndroidStudioProjects/focusGuard/doc/design/SYSTEM_ARCHITECTURE.md).*

---

## 6. Permissions & Play Store Policy Compliance

| Permission | Category | Architectural Purpose |
| :--- | :--- | :--- |
| `BIND_ACCESSIBILITY_SERVICE` | Accessibility | Detect foreground window changes and host lock overlay. |
| `FOREGROUND_SERVICE_SPECIAL_USE` | FGS | Maintain persistent countdown notification and session watchdog. |
| `POST_NOTIFICATIONS` | Runtime (API 33+) | Display ongoing timer and session complete notifications. |
| `SCHEDULE_EXACT_ALARM` | Special Access | Guarantee sub-second session expiration timing in deep Doze. |
| `RECEIVE_BOOT_COMPLETED` | Install-time | Reconcile active session and re-arm alarms after reboot. |
| `PACKAGE_USAGE_STATS` | Special Access | Fallback detection and app usage auditing. |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | System Intent | Prevent aggressive OEM process killers from killing background services. |

*Note: FocusGuard avoids `QUERY_ALL_PACKAGES` by dynamically querying only default telephony roles, ensuring full Play Store compliance.*

---

## 7. Success Metrics & Release Criteria

- **Session Completion Rate**: $\ge 90\%$ of initiated sessions completed without manual tampering.
- **Crash-Free Sessions**: $\ge 99.5\%$ on Google Play Vitals.
- **Overnight Alarm Precision**: Zero missed session completion alarms during 7-day soak.
- **Window Interception Latency**: Lock overlay attaches within $<100\text{ms}$ of opening restricted app.
- **Emergency Yield**: $100\%$ zero-delay pass-through for incoming and outgoing phone calls.

---

## 8. Key Reference Documentation

- [Android Accessibility Service Documentation](https://developer.android.com/guide/topics/ui/accessibility/service)
- [Foreground Service Types (Android 14/15)](https://developer.android.com/about/versions/14/changes/fgs-types-required)
- [Schedule Alarms & Doze Management](https://developer.android.com/develop/background-work/services/alarms)
- [Google Play Accessibility Policy Guidelines](https://support.google.com/googleplay/answer/16318151)
