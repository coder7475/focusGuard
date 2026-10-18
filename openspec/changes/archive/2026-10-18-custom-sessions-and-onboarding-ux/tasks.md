## 1. Custom Session Duration & Preset Configuration

- [x] 1.1 Add quick duration preset chips (15m, 25m, 45m, 60m, 120m, Custom) to `HomeScreen.kt` below the rotary dial
- [x] 1.2 Implement `CustomDurationDialog` composable for direct numeric minute entry (1–1440m) with validation feedback
- [x] 1.3 Add a high-duration discipline confirmation dialog when a custom session exceeds 180 minutes (3 hours)
- [x] 1.4 Persist the last selected duration in `FocusPreferences` using DataStore and observe in `HomeScreen`

## 2. Streamlined Onboarding UX & Prioritized Tiers

- [x] 2.1 Refactor `OnboardingScreen` layout to partition permissions into "Core Enforcers" and "Background Durability" tiers
- [x] 2.2 Implement a primary "Grant Next" action button that automatically resolves and dispatches the intent for the next ungranted mandatory permission
- [x] 2.3 Add Android 13+ (API 33+) Restricted Settings guidance card with a direct "Open App Info" button on the Accessibility permission step
- [x] 2.4 Add an animated progress header with completion fraction and real-time state synchronization on `ON_RESUME`

## 3. Testing & Verification

- [x] 3.1 Add unit tests for custom duration persistence and bounds checking
- [x] 3.2 Add tests for sequential permission resolution logic in `PermissionChecker`
- [x] 3.3 Execute full unit test suite via `./gradlew testDebugUnitTest` and verify 100% pass rate
- [x] 3.4 Validate OpenSpec change status with `openspec validate`
