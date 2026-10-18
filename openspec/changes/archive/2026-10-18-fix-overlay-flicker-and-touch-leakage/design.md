## Context

FocusGuard enforces focus sessions by displaying a full-screen Compose overlay whenever an unauthorized application is brought to the foreground. During device testing, active sessions produced two severe defects:
1. **Screen Flickering / Oscillation**: The lock overlay rapidly flashed on and off (~20Hz). Showing the overlay window triggered Android OS window events (`TYPE_WINDOWS_CHANGED` with null `packageName`, or `TYPE_WINDOW_STATE_CHANGED` for FocusGuard's own package), which the accessibility event handler treated as `ALLOWED`, dismissing the overlay immediately, which in turn brought the blocked app back to view and triggered `BLOCKED`, creating an infinite loop.
2. **Touch Bleed-Through**: The overlay used `FLAG_NOT_TOUCH_MODAL` (which specifically commands the OS to forward outside/unconsumed touches to background windows) and the root Jetpack Compose `Box` did not consume pointer events, leaving background apps fully clickable and interactive even while the overlay was visible.
3. **Session Expiry Desynchronization**: The foreground service stopped itself upon ticker countdown expiry without completing the active session in Room, relying entirely on `SessionAlarmReceiver`, which can be delayed by OEM power management.

## Goals / Non-Goals

**Goals:**
- Eliminate the accessibility feedback loop and screen flickering completely.
- Guarantee 100% modal touch blocking on the lock overlay so background applications and launchers cannot receive touches.
- Preserve legitimate access to FocusGuard's `MainActivity` and the Emergency Dialer.
- Reconcile database state immediately upon ticker expiration in `FocusGuardService` to avoid desynchronization.
- Maintain existing architecture, passing all unit tests.

**Non-Goals:**
- Modifying Room database schema or migration scripts.
- Adding new user-facing configuration options or permissions.
- Changing dialer/SMS role resolution strategies or allowlist storage structures.

## Decisions

### 1. Event Type Filtering and Null Safety in `FocusGuardAccessibilityService`
- **Decision**: Filter out any event whose type is not `TYPE_WINDOW_STATE_CHANGED`. Discard any event with a `null` or blank `packageName` without mutating current overlay visibility.
- **Rationale**: `TYPE_WINDOWS_CHANGED` indicates structural window hierarchy changes and does not include package names in the Android framework. Treating null package as `ALLOWED` was the primary cause of immediate overlay dismissal.
- **Alternatives Considered**: Resolving foreground window root node or inspecting window hierarchy. Rejected because `canRetrieveWindowContent = false` for privacy and performance.

### 2. Disambiguate Overlay Window Events from `MainActivity`
- **Decision**: When `targetPackage == packageName` (`com.coder7475.focusguard`), inspect `event.className`. If `className != MainActivity::class.java.name`, treat the event as internal overlay activity and ignore it.
- **Rationale**: When `ComposeView` is attached via `WindowManager`, Android emits window events with FocusGuard's package name. The service must allow `MainActivity` (user in app) but ignore its own overlay window transitions.
- **Alternatives Considered**: Tracking window IDs. Rejected as window IDs are ephemeral and can vary across API levels.

### 3. Transition Debouncing
- **Decision**: Check `if (targetPackage == lastTargetPackage && overlayManager.isVisible) return;` before processing allowlist evaluations and overlay actions.
- **Rationale**: Prevents repetitive show/hide cycles and redundant allocations during rapid window event streams.

### 4. Remove `FLAG_NOT_TOUCH_MODAL` from `LockOverlayWindowManager`
- **Decision**: In `LockOverlayWindowManager.attach()`, remove `WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL`.
- **Rationale**: According to Android SDK specifications, `FLAG_NOT_TOUCH_MODAL` explicitly allows pointer events outside child views to be dispatched to windows behind it. Removing it ensures the window is strictly modal.

### 5. Consume Pointer Events at Root Compose Surface in `LockOverlayScreen`
- **Decision**: Add `.pointerInput(Unit) { awaitPointerEventScope { while (true) { val event = awaitPointerEvent(); event.changes.forEach { it.consume() } } } }` to the root `Box` of `LockOverlayScreen`.
- **Rationale**: Compose containers do not consume pointer events by default. Even with modal window flags, consuming pointer events at the root guarantees that any gesture (tap, drag, multi-touch) hitting non-interactive areas is swallowed.

### 6. Synchronous Room Completion on Ticker Expiry in `FocusGuardService`
- **Decision**: When `remainingMs <= 0L`, directly instantiate `SessionEngine(RoomSessionRepository(...))` and call `sessionEngine.completeActiveSession()` inside `FocusGuardService` before showing completion notification and calling `stopSelf()`.
- **Rationale**: Protects against delayed or coalesced `AlarmManager` intents on aggressive OEM battery managers (MIUI, ColorOS, OneUI), ensuring database state matches UI state instantly.

### 7. Narrow Accessibility Config XML
- **Decision**: In `accessibility_service_config.xml`, specify `android:accessibilityEventTypes="typeWindowStateChanged"` and `android:notificationTimeout="100"`.
- **Rationale**: Prevents the OS from dispatching unnecessary structural events (`typeWindowsChanged`) to the service process.

## Risks / Trade-offs

- **[Risk] Launcher gestures/back buttons navigating to home screen**:
  - *Mitigation*: The overlay remains displayed over third-party launchers because launchers are evaluated against the allowlist policy and blocked, and full-screen modal flags block taps on launcher icons.
- **[Risk] Emergency dialer accessibility access**:
  - *Mitigation*: `launchEmergencyDialer()` launches `Intent.ACTION_DIAL` and calls `overlayManager.hideOverlay()`. When the dialer window comes to foreground, `SystemRoleResolver` identifies it as the default dialer, allowing it to stay open without overlay interference.
- **[Risk] Existing unit test breakages**:
  - *Mitigation*: Existing unit tests in `FocusGuardAccessibilityServiceTest`, `AllowlistPolicyTest`, and `LockOverlayWindowManagerTest` will be updated to reflect the corrected flags and class-name disambiguation logic.
