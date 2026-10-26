## Why

Testing the debug build of FocusGuard on physical Android hardware (Android 12–15) reveals that the lock overlay fails to appear and blocked applications remain fully accessible. This is caused by an unhandled `IllegalStateException` during `SavedStateRegistryController` restoration in `OverlayLifecycleOwner`, Android 13+ "Restricted Settings" blocking the Accessibility Service on sideloaded/debug builds, flawed WindowManager root view visibility toggling (`View.GONE`), missing manifest permissions (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`), and unhandled launcher package transitions. Fixing these issues is critical to ensure FocusGuard reliably enforces hardware locks on real devices.

## What Changes

- Fix `OverlayLifecycleOwner` initialization by invoking `savedStateRegistryController.performAttach()` before `performRestore()` to eliminate the fatal `IllegalStateException`.
- Refactor `LockOverlayWindowManager` to dynamically add the view surface on `showOverlay()` and remove it on `hideOverlay()` rather than mutating `view.visibility` on a detached WindowManager root.
- Wrap `LockOverlayScreen` in `FocusGuardTheme` within `LockOverlayWindowManager` so Material3 tokens, colors, and typography render correctly in an unthemed Service window.
- Declare `android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` and `android.permission.SYSTEM_ALERT_WINDOW` in `AndroidManifest.xml`.
- Enhance `OnboardingScreen` and `PermissionChecker` with detection and guidance for Android 13+ "Restricted Settings" on sideloaded/debug APKs.
- Dynamically resolve the default device home launcher in `SystemRoleResolver` and update `AllowlistPolicy` to prevent home screen lock deadlocks.

## Capabilities

### New Capabilities
<!-- None: all changes map to existing system capabilities -->

### Modified Capabilities
- `lock-overlay`: Ensure reliable overlay window attachment across Android versions by fixing lifecycle attachment order, dynamic window addition/removal, and Service theme wrapping.
- `permission-onboarding`: Add Android 13+ Restricted Settings detection/guidance and declare battery optimization permissions in the manifest.
- `app-detection-allowlist`: Dynamically resolve default launcher packages and improve gesture navigation transition handling.

## Impact

- **Core Service Layer:** `OverlayLifecycleOwner`, `LockOverlayWindowManager`, `FocusGuardAccessibilityService`.
- **UI & Onboarding:** `OnboardingScreen`, `PermissionChecker`.
- **System Manifest:** `AndroidManifest.xml` (permissions added).
- **Domain & Platform:** `SystemRoleResolver`, `AllowlistPolicy`.
- **Dependencies:** No new external libraries required; leverages existing AndroidX and Jetpack Compose dependencies.
