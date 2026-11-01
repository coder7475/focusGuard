## Why

FocusGuard requires complete implementation of remaining roadmap milestones (Steps 1 through 5 from `STEPS.md`) to deliver an un-bypassable focus lock application on native Android (minSdk 24, targetSdk 37). Having established baseline Room storage, DAOs, and domain models (Step 0), the application now needs live accessibility window interception, an un-dismissible lock overlay, deterministic wall-clock session alarms with foreground service ticker, a 7-step onboarding wizard with tamper detection, and end-to-end verification.

## What Changes

- **Accessibility Window Detection & Role-Based Allowlist (Step 1)**: Declare and implement `FocusGuardAccessibilityService` with `canRetrieveWindowContent = false`, `SystemRoleResolver` for dynamic dialer/SMS role lookups without `QUERY_ALL_PACKAGES`, and `AllowlistPolicy` producing `ALLOWED` vs `BLOCKED` decisions.
- **Lock Overlay Engine & Compose Surface (Step 2)**: Implement `OverlayLifecycleOwner` for hosting ComposeView in an accessibility service window, `LockOverlayWindowManager` managing `TYPE_ACCESSIBILITY_OVERLAY`, and `LockOverlayScreen` rendering edge-to-edge countdown with Emergency call triggers.
- **Core Domain Engine & Resilience Subsystem (Step 3)**: Complete `SessionEngine` lifecycle triggers, implement `ScheduleEngine` with overnight midnight-crossing support, `AlarmCoordinator` using exact alarms (`setExactAndAllowWhileIdle`), `FocusGuardService` (FGS `specialUse`) with continuous notification ticker, and `BootCompletedReceiver` for reboot state reconciliation.
- **Permission Onboarding Wizard & System Hardening (Step 4)**: Implement `PermissionChecker` registry for 7 special permissions, Compose 7-step onboarding wizard UI, accessibility service tamper observer via `ContentObserver`, OEM battery optimization dialogs (Samsung, Xiaomi, OnePlus), and Play Store disclosure copy.
- **End-to-End QA, Test Suite & Release Packaging (Step 5)**: Comprehensive JVM unit test suite (engine, allowlist, schedules), instrumented tests, and lint/release build verification.

## Capabilities

### New Capabilities
- `lock-overlay`: Full-screen non-dismissible `TYPE_ACCESSIBILITY_OVERLAY` window hosting Jetpack Compose UI with live remaining time countdown, hardware gesture suppression, and emergency dialer access.
- `session-resilience`: Background execution resilience via `specialUse` Foreground Service, exact `AlarmManager` wakeups surviving deep Doze, and broadcast receivers reconciling session state upon device reboot.
- `permission-onboarding`: Structured 7-step onboarding flow checking special permissions, tamper detection warning when accessibility is disabled during active sessions, and OEM-specific battery killer guidance.

### Modified Capabilities
<!-- None: existing app-detection-allowlist and session-storage requirements remain valid as defined. -->

## Impact

- **Permissions & Manifest**: Declares `BIND_ACCESSIBILITY_SERVICE`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `SCHEDULE_EXACT_ALARM`, `POST_NOTIFICATIONS`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `RECEIVE_BOOT_COMPLETED`, and `USE_FULL_SCREEN_INTENT`. Zero usage of `QUERY_ALL_PACKAGES`.
- **System APIs**: Uses `WindowManager`, `AccessibilityService`, `TelecomManager`, `Telephony.Sms`, `AlarmManager`, `PowerManager`, and `Settings.Secure`.
- **UI Architecture**: Implements Compose inside both Activity and Service contexts via synthetic lifecycle owners.
- **Dependencies**: Utilizes Room, DataStore, Lifecycle Service, Coroutines, Material 3, and Compose.
