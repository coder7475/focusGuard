## 1. Build & Dependency Setup (Step 0)

- [x] 1.1 Update `gradle/libs.versions.toml` with Room 2.6.1, KSP plugin, DataStore 1.1.1, Coroutines 1.8.1, Lifecycle Service / ViewModel Compose 2.8.4, and MockK/Robolectric test dependencies
- [x] 1.2 Update `build.gradle.kts` and `app/build.gradle.kts` to apply KSP plugin and configure Room compiler and dependencies
- [x] 1.3 Establish Clean Architecture directory packages (`core/`, `domain/`, `data/`, `platform/`, `service/`, `ui/`)
- [x] 1.4 Verify baseline project compilation and dependency resolution via `./gradlew compileDebugSources`

## 2. Persistent Storage Subsystem (Step 0)

- [x] 2.1 Implement domain models (`FocusSession`, `FocusSchedule`, `SessionStatus`) in `domain/model/`
- [ ] 2.2 Implement Room database entities (`FocusSessionEntity`, `FocusScheduleEntity`) with indexing on `status` and `endsAtEpochMs`
- [ ] 2.3 Implement `SessionDao` and `ScheduleDao` with reactive Flow queries and atomic mutations
- [ ] 2.4 Implement `FocusGuardDatabase` Room database definition with schema generation
- [ ] 2.5 Implement `FocusPreferences` using AndroidX Preferences DataStore for `KEY_ONBOARDING_COMPLETED` and `KEY_BATTERY_GUIDE_DISMISSED`
- [ ] 2.6 Write unit tests for `SessionDao` and `ScheduleDao` using in-memory Room database
- [ ] 2.7 Write unit tests for `FocusPreferences`

## 3. Accessibility Detection Spike & Allowlist Engine (Step 1)

- [ ] 3.1 Create `@xml/accessibility_service_config` with `typeWindowStateChanged|typeWindowsChanged` and `canRetrieveWindowContent="false"`
- [ ] 3.2 Declare `FocusGuardAccessibilityService` with `BIND_ACCESSIBILITY_SERVICE` in `AndroidManifest.xml`
- [ ] 3.3 Implement `SystemRoleResolver` querying `TelecomManager` and `Telephony.Sms` with OEM fallbacks and emergency dialer resolution
- [ ] 3.4 Implement `AllowlistPolicy` evaluating packages to produce `AccessDecision` (`ALLOWED` vs `BLOCKED`) without `QUERY_ALL_PACKAGES`
- [ ] 3.5 Implement `FocusGuardAccessibilityService` window event inspector filtering `TYPE_WINDOW_STATE_CHANGED` events
- [ ] 3.6 Write comprehensive unit tests for `SystemRoleResolver` and `AllowlistPolicy` covering dialer, SMS, emergency, own-app, and third-party apps

## 4. Verification & Baseline Check

- [ ] 4.1 Run full JVM unit test suite (`./gradlew test`) ensuring 100% pass rate
- [ ] 4.2 Verify manifest integrity to confirm absence of broad package queries (`QUERY_ALL_PACKAGES`)
