## 1. Overlay Window & Touch Interception

- [x] 1.1 In `LockOverlayWindowManager.kt`, remove `FLAG_NOT_TOUCH_MODAL` so that the window layout is strictly modal across the entire display.
- [x] 1.2 In `LockOverlayScreen.kt`, add pointer input consumption (`pointerInput` with `consume()`) on the root `Box` composable to prevent touch events from leaking to background apps.

## 2. Accessibility Service Event Filtering & Stabilization

- [x] 2.1 In `FocusGuardAccessibilityService.kt`, restrict processing strictly to `TYPE_WINDOW_STATE_CHANGED` events and drop events where `targetPackage.isNullOrBlank()`.
- [x] 2.2 In `FocusGuardAccessibilityService.kt`, disambiguate `com.coder7475.focusguard` package events so only `MainActivity` can hide the overlay while internal overlay `ComposeView` events are ignored.
- [x] 2.3 In `FocusGuardAccessibilityService.kt`, debounce repeated window state events when target package has not changed and the overlay is already visible.
- [x] 2.4 In `app/src/main/res/xml/accessibility_service_config.xml`, update `android:accessibilityEventTypes` to `typeWindowStateChanged` and set `notificationTimeout="100"`.

## 3. Session Expiration Synchronization

- [x] 3.1 In `FocusGuardService.kt`, explicitly finalize the active session in Room database via `SessionEngine.completeActiveSession()` when countdown ticker reaches zero (`remainingMs <= 0L`) before stopping the service.

## 4. Testing & Verification

- [x] 4.1 Update existing unit tests (`FocusGuardAccessibilityServiceTest`, `LockOverlayWindowManagerTest`) to match the new WindowManager flags and event filtering logic.
- [x] 4.2 Execute `./gradlew test` to verify that all unit and integration tests pass cleanly without regressions.
