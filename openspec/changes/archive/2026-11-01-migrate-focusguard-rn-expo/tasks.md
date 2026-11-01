# Tasks

## 1. Division 1: Native Systems Core & Expo Module Setup

- [x] 1.1 Install required dependencies in `focusGuard/package.json` (`expo-sqlite`, `expo-haptics`, `expo-intent-launcher`, `@expo/vector-icons`) and verify package resolution
- [x] 1.2 Implement Expo Config Plugin `focusGuard/plugins/withFocusEnforcer.js` to inject Android permissions (`BIND_ACCESSIBILITY_SERVICE`, `SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE_SPECIAL_USE`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`), service declarations, and intent filters into `AndroidManifest.xml` and verify syntax
- [x] 1.3 Scaffold local native module `focusGuard/modules/focus-enforcer/` with `expo-module.config.json` and `build.gradle` and verify module discovery
- [x] 1.4 Implement `FocusAccessibilityService.kt` to monitor `TYPE_WINDOW_STATE_CHANGED` events with `canRetrieveWindowContent = false` and package change notification
- [x] 1.5 Implement `LockOverlayWindowManager.kt` with full-screen `TYPE_ACCESSIBILITY_OVERLAY` view surface, input event swallowing, countdown display, and direct emergency call button
- [x] 1.6 Implement `FocusForegroundService.kt` with `specialUse` foreground type and 1-second countdown notification ticker on `ID_SESSION_TRACKER` channel
- [x] 1.7 Implement `AlarmCoordinator.kt` with `AlarmManager.setExactAndAllowWhileIdle()` and `AlarmReceiver.kt` for Doze-proof session expiration
- [x] 1.8 Implement `BootReceiver.kt` for `ACTION_BOOT_COMPLETED` handling and autonomous session reconciliation
- [x] 1.9 Implement `SystemRoleResolver.kt` for dynamic default dialer, SMS, and launcher package querying
- [x] 1.10 Expose native methods in `FocusEnforcerModule.kt` (`startFocusLock`, `stopFocusLock`, `getPermissionStates`, `requestPermission`, `openEmergencyDialer`) and create TypeScript wrapper `src/modules/FocusEnforcer.ts`

## 2. Division 2: Data & Persistence

- [x] 2.1 Implement `FocusDatabaseHelper.kt` in the native module providing direct SQLite access (`focusguard.db`) in WAL mode for background services and `BootReceiver`
- [x] 2.2 Implement `src/data/db.ts` using `expo-sqlite` with initial schema creation (`focus_sessions` and `focus_schedules` tables) and WAL mode pragma configuration
- [x] 2.3 Implement `src/data/repositories/SessionRepository.ts` for session CRUD, active session retrieval, and session completion recording
- [x] 2.4 Implement `src/data/repositories/ScheduleRepository.ts` for recurring routine persistence, weekday bitmask queries, and toggle updates
- [x] 2.5 Implement `src/data/storage/PreferenceStorage.ts` for local key-value state (onboarding completion, default duration) and verify database initialization

## 3. Division 3: Domain & Business Engines

- [x] 3.1 Implement `src/domain/types.ts` defining data models (`FocusSession`, `FocusSchedule`, `PermissionStatus`, `SessionStatus`)
- [x] 3.2 Implement `src/domain/AllowlistPolicy.ts` defining system package allowlists, telecom role resolution rules, and emergency bypass conditions
- [x] 3.3 Implement `src/domain/ScheduleEngine.ts` containing overnight midnight-crossing schedule calculations, active window evaluation, and next trigger epoch derivation
- [x] 3.4 Implement `src/domain/SessionEngine.ts` managing session lifecycle transitions (start, tick, complete, abort validation)
- [x] 3.5 Create unit test suites for `ScheduleEngine` and `SessionEngine` covering midnight-crossing intervals and verify tests pass

## 4. Division 4: Frontend & Tactile UI

- [x] 4.1 Update design tokens and theme constants in `src/constants/theme.ts` matching FocusGuard's industrial cyberpunk aesthetic (dark charcoal backgrounds, illuminated neon cyan/amber accents)
- [x] 4.2 Build `src/components/RotaryDial.tsx` using `react-native-gesture-handler` and `react-native-reanimated` 4 with polar angle tracking, discrete detent snapping (15m to 240m), and `expo-haptics` tick feedback
- [x] 4.3 Build preset chips (15m, 25m, 45m, 60m) and custom duration sheet with $>180$ minute discipline warning
- [x] 4.4 Build `src/app/(tabs)/index.tsx` Focus Dashboard integrating the Rotary Dial, live active countdown timer, lock engagement button, and quick statistics
- [x] 4.5 Build `src/app/(tabs)/schedules.tsx` for recurring routines with AM/PM time picker, weekday selector chips, midnight-crossing badges, and enable/disable toggles
- [x] 4.6 Build `src/app/onboarding.tsx` 7-step diagnostics and permissions wizard with real-time grant indicators and direct system settings launch triggers
- [x] 4.7 Build `src/components/OemBatteryGuideModal.tsx` providing step-by-step background battery exemption instructions for Samsung One UI, Xiaomi HyperOS, and OnePlus OxygenOS
- [x] 4.8 Configure Expo Router navigation in `src/app/_layout.tsx` and `src/app/(tabs)/_layout.tsx` with onboarding redirection and industrial theme styling

## 5. Division 5: QA & System Resilience

- [x] 5.1 Implement end-to-end integration test runner / verification script simulating session start, lock enforcement, and notification updates
- [x] 5.2 Validate ADB Doze mode resilience via `dumpsys deviceidle force-idle` and verify exact alarm fires on time
- [x] 5.3 Validate device reboot survival via `adb reboot` and verify `BootReceiver` re-engages lock without JS startup
- [x] 5.4 Validate emergency dialer pass-through ensuring emergency numbers (911, 112) and system dialer never trigger the lock overlay

## 6. Division 6: DevOps, Tooling & Store Release

- [x] 6.1 Update `focusGuard/app.json` with package name `com.coder7575.SleepGuard`, plugin declaration `./plugins/withFocusEnforcer.js`, and Android permissions
- [x] 6.2 Execute `npx expo prebuild --clean` in `focusGuard/` and verify generated Android manifest and Gradle configurations build without errors
- [x] 6.3 Author Google Play Store Accessibility Service Prominent Disclosure documentation and foreground service justification in `docs/play/PLAY_STORE_DISCLOSURES.md`
