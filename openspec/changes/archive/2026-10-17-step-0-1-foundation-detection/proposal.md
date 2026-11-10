## Why

FocusGuard enforces phone-lock discipline during active focus sessions by disallowing non-essential apps while strictly safeguarding essential communications (calls, emergency dialer, and SMS). To achieve reboot-proof state integrity and instant (<100ms) app switching detection without Google Play policy violations, the app requires a deterministic persistent data layer (Room SQLite + Preferences DataStore) and a zero-permission accessibility-driven detection engine with dynamic role resolution.

## What Changes

- **Gradle Version Catalog & Build Configuration**: Add dependencies for Room runtime/KTX, Room compiler via KSP (`2.6.1`), Preferences DataStore (`1.1.1`), Kotlinx Coroutines Android/Test (`1.8.1`), Lifecycle Service/ViewModel Compose (`2.8.4`), and MockK / Robolectric.
- **Clean Architecture Package Structure**: Organize the codebase into modular packages (`core/`, `domain/`, `data/`, `platform/`, `service/`, `ui/`).
- **Persistent Data Subsystem (Room + DataStore)**:
  - Create `FocusSessionEntity` and `FocusScheduleEntity` with absolute wall-clock epoch timestamps and status index.
  - Create `SessionDao` and `ScheduleDao` with reactive Flow queries and atomic mutations.
  - Create `FocusGuardDatabase` with schema generation.
  - Create `FocusPreferences` with Preferences DataStore for onboarding and guide status.
- **Accessibility Service Configuration**:
  - Implement `FocusGuardAccessibilityService` declared with `android.permission.BIND_ACCESSIBILITY_SERVICE`.
  - Provide `@xml/accessibility_service_config` tuned for window state and hierarchy changes (`typeWindowStateChanged|typeWindowsChanged`) with `canRetrieveWindowContent="false"`.
- **System Role Resolution & Allowlist Policy Engine**:
  - Implement `SystemRoleResolver` dynamically querying `TelecomManager.getDefaultDialerPackage()` and `Telephony.Sms.getDefaultSmsPackage()`.
  - Implement immutable fallback lists for manufacturer dialers, emergency intents/packages, and FocusGuard's own package.
  - Implement `AllowlistPolicy` producing `AccessDecision` (`ALLOWED` vs `BLOCKED`) without requiring `QUERY_ALL_PACKAGES`.
  - Integrate event filtering in `FocusGuardAccessibilityService.onAccessibilityEvent()` to measure latency and evaluate packages.

## Capabilities

### New Capabilities
- `session-storage`: Room SQLite entities, DAOs, and DataStore preferences for resilient focus sessions and schedule configurations.
- `app-detection-allowlist`: Accessibility-driven window event detection and dynamic system-role allowlist resolution without broad package queries.

### Modified Capabilities
<!-- None -->

## Impact

- **Build & Dependencies**: Introduces KSP plugin, Room 2.6.1, DataStore 1.1.1, Coroutines 1.8.1, and MockK/Robolectric test runners to `gradle/libs.versions.toml` and `app/build.gradle.kts`.
- **Android Manifest**: Registers `FocusGuardAccessibilityService` with `@xml/accessibility_service_config` and appropriate permissions. Avoids `QUERY_ALL_PACKAGES` to maintain Play Store policy compliance.
- **Architecture**: Establishes clean domain models decoupled from Android framework imports and establishes unit test coverage for persistence and policy evaluation.
