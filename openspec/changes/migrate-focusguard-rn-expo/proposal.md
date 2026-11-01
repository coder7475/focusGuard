# Proposal

## Why

Digital distraction and screen overuse require uncompromising system-level enforcement that cannot be easily dismissed or bypassed. While FocusGuard proved this concept as a native Kotlin/Jetpack Compose Android application, expanding its modern, tactile experience, multi-platform readiness, and developer iteration speed requires migrating to React Native with Expo SDK 58 (Expo Router v5, Continuous Native Generation) while preserving 100% of the uncompromising low-level native enforcement, deep Doze/reboot survival, and zero-telemetry offline privacy.

## What Changes

- **Hybrid Native-JS Core Architecture**: Scaffold a local native Expo module (`modules/focus-enforcer/`) and Expo Config Plugin (`plugins/withFocusEnforcer.js`) to provide Android `AccessibilityService`, `TYPE_ACCESSIBILITY_OVERLAY` window manager, `specialUse` foreground service, and exact wall-clock alarm scheduling without breaking Expo Continuous Native Generation (CNG).
- **Engineering Division Governance**: Structure the implementation across 6 cross-functional engineering divisions:
  - Division 1: Native Systems Core (`team-native-core`) — Accessibility monitoring, full-screen lock overlay, foreground service, boot receiver, and system role resolution.
  - Division 2: Data & Persistence (`team-data-storage`) — Shared SQLite database (`focusguard.db` in WAL mode) accessible simultaneously by React Native (`expo-sqlite`) and Kotlin background services (`SQLiteOpenHelper`).
  - Division 3: Domain & Business Engines (`team-domain-engine`) — Pure TypeScript implementations of `SessionEngine`, `ScheduleEngine` (overnight midnight-crossing calculation), and allowlist policy evaluation with automated test suites.
  - Division 4: Frontend & Tactile UI (`team-frontend-ui`) — Industrial cyberpunk aesthetic, 60 FPS haptic Rotary Dial (`react-native-gesture-handler` + `react-native-reanimated` 4 + `expo-haptics`), schedule manager, 7-step onboarding wizard, and OEM battery guides.
  - Division 5: QA & System Resilience (`team-qa-resilience`) — ADB Doze mode verification, reboot reconciliation testing, and emergency dialer pass-through guarantees.
  - Division 6: DevOps, Tooling & Store Release (`team-devops-release`) — Clean CNG prebuild scripts, EAS build profiles, and Play Store accessibility service prominent disclosure assets.
- **Expo App Replacement in `focusGuard`**: Transform the boilerplate Expo project in `focusGuard/` into a complete, production-ready FocusGuard mobile application.

## Capabilities

### New Capabilities
- `permission-onboarding`: Multi-step permission verification wizard, OEM battery optimization guides, and system settings intent dispatchers for all required Android permissions.
- `app-detection-allowlist`: Foreground window transition interception via native Accessibility Service with sub-100ms response, zero screen content inspection, and dynamic system role allowlisting (default dialer, SMS, launcher).
- `lock-overlay`: Un-dismissible full-screen `TYPE_ACCESSIBILITY_OVERLAY` window displayed over non-allowlisted apps with hardware back/home interception and one-touch emergency phone bypass.
- `session-resilience`: Foreground service (`specialUse`) countdown ticker, `AlarmManager.setExactAndAllowWhileIdle()` Doze-proof timing, and autonomous native boot reconciliation (`ACTION_BOOT_COMPLETED`).
- `session-storage`: Shared SQLite database (`focusguard.db` with WAL mode) and repository interfaces enabling concurrent, safe read/write access from both JavaScript and native Kotlin background processes.
- `schedule-management`: Recurring focus routines with day-of-week bitmasks, overnight midnight-crossing schedule calculations, and automated background alarm registration.
- `tactile-rotary-timer`: Circular rotary dial component with polar angle gesture tracking, discrete detent snapping (15m to 240m), illuminated progress arc, and physical haptic click feedback.

### Modified Capabilities
<!-- None: This is a greenfield OpenSpec change introducing the capability specifications into the React Native / Expo repository. -->

## Impact

- **Target Codebase**: `focusGuard/` (React Native 0.88.x + Expo SDK 58 + Expo Router v5).
- **Native Android Modules**: Local Expo Module `focusGuard/modules/focus-enforcer/` and Expo Config Plugin `focusGuard/plugins/withFocusEnforcer.js`.
- **Database**: Local SQLite database located at `focusguard.db` in app sandboxed storage, accessed via `expo-sqlite` in TS and Android SQLite in Kotlin.
- **Dependencies**: Adds `expo-sqlite`, `expo-haptics`, `react-native-reanimated`, `react-native-gesture-handler`, `@expo/vector-icons`, and related UI packages.
- **Continuous Generation**: Native Android artifacts generated strictly via `npx expo prebuild`, keeping `android/` reproducible and clean.
