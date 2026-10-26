## Context

FocusGuard is designed as an un-bypassable lock app for Android devices. On real hardware running Android 12–15, testing revealed that:
1. `OverlayLifecycleOwner` crashes in `performRestore()` because AndroidX `SavedStateRegistryController` requires `performAttach()` prior to restoration. This exception is caught silently in `attach()`, leaving the overlay view null and preventing any window display.
2. Android 13+ marks sideloaded/debug builds with "Restricted settings", disabling the Accessibility Service toggle unless explicitly permitted in App Info.
3. Attaching a `ComposeView` with `View.GONE` and toggling `view.visibility = View.VISIBLE` fails on Android's `WindowManagerService`, because top-level windows require dynamic attachment or layout updates.
4. `AndroidManifest.xml` lacks `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` and `SYSTEM_ALERT_WINDOW`, leading to permission check failures or missing fallback window privileges.
5. Home launchers are marked as `BLOCKED` without dynamic role resolution, creating navigation inconsistencies when returning to the launcher.

## Goals / Non-Goals

**Goals:**
- Eliminate the crash in `OverlayLifecycleOwner` by strictly calling `performAttach()` before `performRestore()`.
- Adopt dynamic window management (`windowManager.addView` on show, `windowManager.removeView` on hide) in `LockOverlayWindowManager`.
- Wrap the overlay Compose content in `FocusGuardTheme` with `ViewCompositionStrategy.DisposeOnLifecycleDestroyed`.
- Add `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` and `SYSTEM_ALERT_WINDOW` to `AndroidManifest.xml`.
- Guide users on Android 13+ when Accessibility is restricted on sideloaded/debug APKs.
- Dynamically resolve default launchers in `SystemRoleResolver` to handle home screen transitions cleanly.

**Non-Goals:**
- Replacing the Accessibility Service architecture with pure UsageStats polling.
- Building a custom third-party launcher to replace OEM launchers.
- Removing mandatory onboarding checks for security permissions.

## Decisions

### 1. SavedStateRegistryController Attach Order
* **Choice:** Call `savedStateRegistryController.performAttach()` immediately before `savedStateRegistryController.performRestore(savedInstanceState)`.
* **Rationale:** AndroidX SavedState 1.2+ enforces that `mAttached` is true before restoring state, throwing `IllegalStateException` if omitted.
* **Alternatives Considered:** Removing `SavedStateRegistryOwner` completely. Rejected because Compose runtime components (e.g. rememberSaveable) require it.

### 2. Dynamic WindowManager Add / Remove
* **Choice:** Call `windowManager.addView(overlayView, params)` when `showOverlay()` is triggered, and `windowManager.removeView(overlayView)` when `hideOverlay()` is triggered.
* **Rationale:** In Android `WindowManagerService`, root views added with `visibility = View.GONE` do not reliably recreate or composite window surfaces when mutated to `View.VISIBLE`. Dynamic add/remove guarantees clean surface creation and destruction.
* **Alternatives Considered:** Mutating `params.alpha = 0f` and `FLAG_NOT_TOUCHABLE` with `updateViewLayout`. Rejected because keeping an active window attached consumes GPU compositor layers and can cause touch occlusion bugs on OEM ROMs.

### 3. Service ComposeView Theme & Composition Strategy
* **Choice:** Wrap `LockOverlayScreen` inside `FocusGuardTheme` and set `ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner)`.
* **Rationale:** A Service context has no default Activity theme. Without explicit theme wrapping, Material3 components can fall back to invalid defaults or unreadable colors.

### 4. Android 13+ Restricted Settings Guidance
* **Choice:** Detect API 33+ in `OnboardingScreen` and display a clear guidance card with a one-tap shortcut to `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`.
* **Rationale:** Android does not allow programmatic bypass of Restricted Settings for security reasons. Providing exact instructions and a direct intent to App Info minimizes user friction.

### 5. Dynamic Launcher Package Resolution
* **Choice:** Use `packageManager.resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), MATCH_DEFAULT_ONLY)` in `SystemRoleResolver`.
* **Rationale:** Every device OEM uses a different home launcher package (e.g. `com.google.android.apps.nexuslauncher`, `com.sec.android.app.launcher`). Hardcoding package names is unmaintainable.

## Risks / Trade-offs

- **[Risk] Adding view already added or removing view not attached:** If WindowManager state falls out of sync, `addView` can throw `IllegalStateException` and `removeView` can throw `IllegalArgumentException`.
  $\rightarrow$ **Mitigation:** Synchronize `showOverlay` and `hideOverlay`, track `isVisible` flag, and wrap WindowManager mutations in robust try-catch logging.
- **[Risk] User ignores Restricted Settings instructions:** Sideloaded debug installs will not have functional accessibility until enabled.
  $\rightarrow$ **Mitigation:** Mandatory onboarding gates app entry until Accessibility is active, and the warning banner remains visible with the "Open App Info" button.
