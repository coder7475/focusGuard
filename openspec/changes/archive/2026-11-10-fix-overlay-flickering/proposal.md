# Proposal: Fix Overlay Flickering and Window Instability

## Why

During focus sessions on Android devices—specifically tested on Transsion XOS (Infinix Hot 50i, Android 14)—the full-screen lock overlay violently flickers at 30–60Hz instead of remaining anchored over blocked applications. This critical flaw stems from an infinite ping-pong loop where the overlay window's own window-state-changed accessibility event triggers immediate self-dismissal, coupled with missing `FLAG_NOT_FOCUSABLE` window flags, missing Transsion/SystemUI allowlist entries, and missing Android 11+ `<queries>` declarations. Resolving this is necessary to stabilize core enforcement and stop catastrophic UI and CPU churn.

## What Changes

- **Self-Window Suppression**: Update `FocusAccessibilityService` to ignore accessibility events originating from the enforcer app's own package (`context.packageName`) unless the event class is explicitly `MainActivity`.
- **Active Overlay Visibility Retention**: Prevent `FocusAccessibilityService` from hiding an active overlay if the overlay is already marked visible and the session has not expired.
- **Window Layout Flags**: Add `WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE` and remove `WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL` in `LockOverlayWindowManager` to prevent input focus theft, eliminate accessibility loop triggers, and avoid touch leakage.
- **Transsion & System UI Allowlisting**: Add Transsion launchers (`com.transsion.XOSLauncher`, `com.transsion.hilauncher`), dialers (`com.transsion.smartcaller`), and essential system UI surfaces (`com.android.systemui`, `android`) to `SystemRoleResolver`.
- **Package Visibility Queries**: Configure Expo config plugin (`plugins/withFocusEnforcer.js`) to inject Android 11+ `<queries>` declarations into `AndroidManifest.xml` for `CATEGORY_HOME`, `ACTION_DIAL`, and `ACTION_SENDTO` intents.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `app-detection-allowlist`: Add self-package event filtering (ignoring overlay view events while allowing `MainActivity`), include `com.android.systemui` and Transsion OEM packages in allowlist resolvers, and enforce Android 11+ intent queries for home and dialer resolution.
- `lock-overlay`: Require `FLAG_NOT_FOCUSABLE` in overlay window parameters to prevent window focus loops, and strictly prohibit `FLAG_NOT_TOUCH_MODAL`.

## Impact

- **Code Affected**:
  - `focusGuard/modules/focus-enforcer/android/src/main/java/com/coder7575/focusenforcer/FocusAccessibilityService.kt`
  - `focusGuard/modules/focus-enforcer/android/src/main/java/com/coder7575/focusenforcer/LockOverlayWindowManager.kt`
  - `focusGuard/modules/focus-enforcer/android/src/main/java/com/coder7575/focusenforcer/SystemRoleResolver.kt`
  - `focusGuard/plugins/withFocusEnforcer.js`
- **Dependencies**: No new third-party dependencies.
- **APIs**: No changes to the public TypeScript NativeModule API (`startEnforcing`, `stopEnforcing`, etc.).
