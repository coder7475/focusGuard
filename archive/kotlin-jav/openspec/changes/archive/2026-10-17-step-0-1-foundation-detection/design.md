## Context

FocusGuard is designed to enforce rigid phone lockouts during scheduled or ad-hoc focus periods. Unlike soft screen-time managers, FocusGuard provides an un-bypassable lock over non-allowlisted applications while strictly protecting essential voice calls, emergency dialers, and SMS messaging.

Milestones M0 (Step 0) and M1 (Step 1) establish the architectural skeleton, persistent database, DataStore preferences, and the core accessibility detection spike. The application must guarantee that session state is preserved across process death and system reboots via wall-clock epoch timestamps (ADR-002), and that foreground application changes are detected in sub-100ms without requesting dangerous permissions such as `QUERY_ALL_PACKAGES` (ADR-001, ADR-005).

## Goals / Non-Goals

**Goals:**
- Configure Gradle version catalogs (`libs.versions.toml`) and `app/build.gradle.kts` for Room 2.6.1 with KSP, Preferences DataStore 1.1.1, Coroutines 1.8.1, Lifecycle 2.8.4, and testing frameworks (MockK, Robolectric).
- Establish clean architectural layering: `core/`, `domain/`, `data/`, `platform/`, `service/`, and `ui/`.
- Implement Room database (`FocusGuardDatabase`) with SQLite entities (`FocusSessionEntity`, `FocusScheduleEntity`) indexing `status` and `endsAtEpochMs`.
- Implement `FocusPreferences` with Jetpack Preferences DataStore for onboarding and guide tracking.
- Implement `FocusGuardAccessibilityService` with `@xml/accessibility_service_config` capturing `typeWindowStateChanged|typeWindowsChanged` events without retrieving window contents (`canRetrieveWindowContent="false"`).
- Implement `SystemRoleResolver` dynamically querying `TelecomManager` and `Telephony.Sms` for default dialer and SMS packages, paired with immutable fallback constants for common OEM dialers and emergency intents.
- Implement `AllowlistPolicy` delivering instantaneous `AccessDecision` (`ALLOWED` or `BLOCKED`) without requiring `QUERY_ALL_PACKAGES`.
- Validate with JVM unit tests for DAOs, DataStore, and `AllowlistPolicy`.

**Non-Goals:**
- UI Overlay rendering via `WindowManager` or Compose (`TYPE_ACCESSIBILITY_OVERLAY` is deferred to Step 2 / Milestone M2).
- Foreground service ticker (`FocusGuardService`), `AlarmManager` coordination, and boot receivers (deferred to Step 3 / Milestone M3).
- Permission onboarding wizard screens and tamper-detection `ContentObserver` (deferred to Step 4 / Milestone M4).
- Web URL filtering, per-app customizable blocklists, or cloud sync.

## Decisions

### 1. Persistent Epoch Timestamps in Room SQLite over In-Memory Timers (ADR-002)
- **Decision**: Persist `startTimeEpochMs` and `endsAtEpochMs` directly in SQLite. Timers are derived dynamically from `endsAtEpochMs - System.currentTimeMillis()`.
- **Alternatives Considered**: In-memory countdown coroutines or timer jobs.
- **Rationale**: In-memory state is destroyed when OEM battery killers or the Android OS reclaim background processes. Wall-clock epoch storage ensures absolute recovery on process restart or device reboot.

### 2. KSP for Room Annotation Processing
- **Decision**: Use Kotlin Symbol Processing (`ksp`) instead of `kapt`.
- **Alternatives Considered**: `kapt` (Kotlin Annotation Processing Tool).
- **Rationale**: KSP provides significantly faster compilation times, native Kotlin 2.x support, and prevents known Java-stubs overhead.

### 3. Preferences DataStore over SharedPreferences
- **Decision**: Use `androidx.datastore.preferences.core` for simple key-value pairs (`KEY_ONBOARDING_COMPLETED`, `KEY_BATTERY_GUIDE_DISMISSED`).
- **Alternatives Considered**: `SharedPreferences`.
- **Rationale**: `SharedPreferences` runs synchronously on the main thread and lacks type safety and transactional guarantees. DataStore provides asynchronous Coroutine Flow primitives and safe file operations.

### 4. Accessibility Window Events over `UsageStatsManager` Polling (ADR-001)
- **Decision**: Intercept foreground app switches in `FocusGuardAccessibilityService.onAccessibilityEvent()` using `TYPE_WINDOW_STATE_CHANGED`.
- **Alternatives Considered**: Polling `UsageStatsManager.queryUsageStats()` every 500–1000ms.
- **Rationale**: `UsageStatsManager` incurs significant battery consumption, suffers delayed updates on Android 14+, and leaves an unacceptable 500ms–2s window where blocked apps are visible. Accessibility window events are dispatched instantaneously (<100ms) by `WindowManagerService`.

### 5. Dynamic Role Resolution with Static Fallbacks over `QUERY_ALL_PACKAGES` (ADR-005)
- **Decision**: Query `telecomManager.defaultDialerPackage` and `Telephony.Sms.getDefaultSmsPackage(context)` dynamically at runtime, complemented by hardcoded OEM dialers and emergency packages (`com.android.phone`, `com.google.android.dialer`, `com.samsung.android.dialer`, etc.).
- **Alternatives Considered**: Requesting `android.permission.QUERY_ALL_PACKAGES` to inspect all installed applications.
- **Rationale**: Google Play enforces strict approval criteria for `QUERY_ALL_PACKAGES`, and focus apps are routinely rejected. Dynamic role resolution requires zero special permissions while safeguarding dialer and messaging functionality.

## Risks / Trade-offs

- **[Risk] OEM Accessibility Service Death**: Aggressive battery savers (e.g. Xiaomi MIUI, Samsung OneUI) may stop accessibility services in low-memory situations.
  - *Mitigation*: Service declares low notification timeout (50ms); Step 3 will pair this with a Foreground Service (`specialUse`), and Step 4 will introduce battery optimization onboarding and tamper detection.
- **[Risk] Missing Default Dialer/SMS on Non-Standard Android Forks**: Unofficial ROMs or stripped AOSP builds might return null for system roles.
  - *Mitigation*: `SystemRoleResolver` includes static fallback sets containing standard AOSP, Google, and Samsung package identifiers, plus emergency intent resolution (`ACTION_EMERGENCY_ASSIST`, `ACTION_DIAL`).
- **[Risk] Rapid Window State Event Churn**: Switching apps quickly or opening multi-window dialogs dispatches high event volumes.
  - *Mitigation*: Configure `notificationTimeout = 50` in `@xml/accessibility_service_config` and ignore events where `packageName` matches the currently active decision or is null.
