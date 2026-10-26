## 1. Lifecycle & Window Management Hardening

- [x] 1.1 Update `OverlayLifecycleOwner` to invoke `savedStateRegistryController.performAttach()` before `performRestore()`
- [x] 1.2 Refactor `LockOverlayWindowManager` to dynamically add the view surface on `showOverlay()` and remove it on `hideOverlay()`
- [x] 1.3 Configure `ViewCompositionStrategy.DisposeOnLifecycleDestroyed` and wrap overlay content in `FocusGuardTheme`
- [x] 1.4 Update `LockOverlayWindowManagerTest` to verify dynamic WindowManager addition, removal, and lifecycle attachment

## 2. System Manifest & Permissions

- [x] 2.1 Declare `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` in `AndroidManifest.xml`
- [x] 2.2 Declare `SYSTEM_ALERT_WINDOW` in `AndroidManifest.xml`
- [x] 2.3 Verify battery optimization intent generation and permission check in `PermissionChecker`

## 3. Onboarding & Restricted Settings Guidance

- [x] 3.1 Add Android 13+ (API 33+) sideload guidance banner in `OnboardingScreen` for the Accessibility Service step
- [x] 3.2 Provide direct App Info action button launching `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` in the guidance banner

## 4. Role Resolution & Launcher Handling

- [x] 4.1 Implement `getDefaultLauncherPackage()` in `SystemRoleResolver` using `Intent.CATEGORY_HOME`
- [x] 4.2 Update `AllowlistPolicy` to evaluate the dynamically resolved default launcher package as `AccessDecision.ALLOWED`
- [x] 4.3 Add unit tests for launcher role resolution and allowlist evaluation in `AllowlistPolicyTest` and `SystemRoleResolverTest`

## 5. Verification & Build Validation

- [x] 5.1 Execute all unit tests via `./gradlew testDebugUnitTest` and ensure 100% pass rate
- [x] 5.2 Validate OpenSpec change status and synchronization
