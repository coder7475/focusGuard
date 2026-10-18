## Why

During physical device testing, active focus sessions suffered from severe screen flickering (~20Hz oscillation loop) and complete touch bleed-through where users could continue interacting with underlying blocked apps and launchers. Furthermore, session expiration left the Room database and Accessibility Service desynchronized if OEM background restrictions delayed alarm delivery. Resolving these issues is critical to ensure the core security, stability, and distraction-blocking promise of FocusGuard.

## What Changes

- **Harden Accessibility Event Evaluation**: Filter out `TYPE_WINDOWS_CHANGED` events and ignore null/blank package events; distinguish FocusGuard overlay window events from `MainActivity` so the overlay window itself does not trigger self-dismissal and feedback oscillation.
- **Enforce True Touch Modality on Lock Overlay**: Remove `FLAG_NOT_TOUCH_MODAL` from `WindowManager.LayoutParams` and attach pointer input consumption to the root Compose layout to intercept 100% of gestures and prevent touch leakage to background apps.
- **Reconcile Session Expiration in Foreground Service**: Directly invoke `SessionEngine.completeActiveSession()` when the foreground countdown ticker reaches zero, preventing state desync if exact alarms are deferred by Doze or OEM power managers.
- **Update Accessibility Configuration**: Adjust `accessibility_service_config.xml` to listen only to `typeWindowStateChanged` with debounce timeout to avoid unnecessary event jitter.

## Capabilities

### New Capabilities

*(None)*

### Modified Capabilities

- `lock-overlay`: Require modal full-screen touch interception without `FLAG_NOT_TOUCH_MODAL` and consume pointer events on the root Compose surface to eliminate touch bleed-through.
- `app-detection-allowlist`: Require filtering out null/blank package events, ignoring structural `TYPE_WINDOWS_CHANGED` events, and filtering out non-MainActivity FocusGuard package events to eliminate oscillation feedback loops.
- `session-resilience`: Require the foreground service ticker to explicitly finalize the active session state in the database upon countdown expiration.

## Impact

- **Source Code**:
  - `FocusGuardAccessibilityService.kt`: Window state event filtering, null safety, self-overlay event disambiguation, state debouncing.
  - `LockOverlayWindowManager.kt`: LayoutParams flags adjustment (removal of `FLAG_NOT_TOUCH_MODAL`).
  - `LockOverlayScreen.kt`: Root container `pointerInput` event consumption.
  - `FocusGuardService.kt`: Database session completion call on ticker zero.
  - `accessibility_service_config.xml`: Event type restriction to `typeWindowStateChanged`.
- **APIs & Dependencies**: No external API changes; internal domain models and database schemas remain intact.
- **User Experience**: Completely eliminates flickering strobe and guarantees underlying apps cannot be touched or operated during active focus sessions.
