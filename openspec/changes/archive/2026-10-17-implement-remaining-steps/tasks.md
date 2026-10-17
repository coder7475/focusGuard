## 1. Detection Spike & Allowlist Engine (Step 1)

- [x] 1.1 Create `@xml/accessibility_service_config` with `typeWindowStateChanged|typeWindowsChanged`, `feedbackGeneric`, and `canRetrieveWindowContent="false"`
- [x] 1.2 Declare `FocusGuardAccessibilityService` with `BIND_ACCESSIBILITY_SERVICE` in `AndroidManifest.xml`
- [x] 1.3 Implement `SystemRoleResolver` querying `TelecomManager.getDefaultDialerPackage()` and `Telephony.Sms.getDefaultSmsPackage()` with OEM fallbacks
- [x] 1.4 Implement `AllowlistPolicy` evaluating package names against emergency, system roles, and own package without `QUERY_ALL_PACKAGES`
- [x] 1.5 Implement `FocusGuardAccessibilityService` window event listener capturing `TYPE_WINDOW_STATE_CHANGED` events
- [x] 1.6 Implement JVM unit tests for `AllowlistPolicy` and `SystemRoleResolver`

## 2. Lock Overlay Engine & Compose Surface (Step 2)

- [x] 2.1 Implement `OverlayLifecycleOwner` providing synthetic `LifecycleOwner`, `ViewModelStoreOwner`, and `SavedStateRegistryOwner`
- [x] 2.2 Implement `LockOverlayWindowManager` managing `TYPE_ACCESSIBILITY_OVERLAY` with `FLAG_LAYOUT_IN_SCREEN`, `FLAG_NOT_TOUCH_MODAL`, and `FLAG_SHOW_WHEN_LOCKED`
- [x] 2.3 Create `LockOverlayScreen` Jetpack Compose UI rendering live countdown MM:SS, focus motivation, and Emergency dialer action
- [x] 2.4 Integrate `LockOverlayWindowManager` with `FocusGuardAccessibilityService` to toggle overlay visibility on `BLOCKED` vs `ALLOWED` decisions
- [x] 2.5 Implement UI tests and synthetic lifecycle unit tests for `OverlayLifecycleOwner` and `LockOverlayScreen`

## 3. Core Domain Engine & Resilience Subsystem (Step 3)

- [x] 3.1 Implement `SessionEngine` lifecycle triggers (`startQuickSession`, `completeActiveSession`, `reconcileState`)
- [x] 3.2 Implement `ScheduleEngine` with overnight midnight-crossing recurrence calculations using `java.time.ZonedDateTime`
- [x] 3.3 Implement `AlarmCoordinator` scheduling exact alarms with `AlarmManager.setExactAndAllowWhileIdle()`
- [x] 3.4 Implement `SessionAlarmReceiver` waking `SessionEngine` upon alarm execution
- [x] 3.5 Implement `FocusGuardService` (FGS `specialUse`) with continuous notification ticker on `ID_SESSION_TRACKER` and completion alert on `ID_SESSION_COMPLETION`
- [x] 3.6 Implement `BootCompletedReceiver` listening for `ACTION_BOOT_COMPLETED` and `ACTION_LOCKED_BOOT_COMPLETED` to reconcile session state
- [x] 3.7 Write unit tests for `SessionEngine`, `ScheduleEngine`, and `BootCompletedReceiver`

## 4. Permission Onboarding Wizard & System Hardening (Step 4)

- [x] 4.1 Implement `PermissionChecker` registry evaluating grant status for 7 special permissions
- [x] 4.2 Build 7-step Onboarding Wizard UI in Jetpack Compose with direct system configuration intent launchers
- [x] 4.3 Integrate onboarding completion persistence in `FocusPreferences` (`KEY_ONBOARDING_COMPLETED`)
- [x] 4.4 Implement `ContentObserver` in `FocusGuardService` monitoring `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` for tampering during active sessions
- [x] 4.5 Build OEM-specific battery optimization guidance dialogs (Samsung, Xiaomi, OnePlus)
- [x] 4.6 Document Google Play Store prominent disclosure copy for accessibility and foreground service usage

## 5. End-to-End QA, Failure Mode Verification & Release (Step 5)

- [x] 5.1 Run full automated JVM unit test suite (`./gradlew test`) ensuring 100% pass rate
- [x] 5.2 Validate manifest integrity confirming absence of `QUERY_ALL_PACKAGES` and correctness of FGS `specialUse` subtype
- [x] 5.3 Execute manual smoke test verification checklist (quick session, call pass-through, emergency dialer, reboot survival)
- [x] 5.4 Execute release build packaging and lint check (`./gradlew lint compileReleaseKotlin assembleRelease`)
