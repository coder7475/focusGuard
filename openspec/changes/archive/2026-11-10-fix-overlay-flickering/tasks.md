# Tasks

## 1. Native Service Event Filtering & Loop Suppression

- [x] 1.1 Add self-package event filtering in `FocusAccessibilityService.kt` to ignore all window events originating from `context.packageName` unless `event.className` matches the application's `MainActivity`. Verify by checking that overlay window view updates do not pass into allowlist evaluation.
- [x] 1.2 Implement active overlay visibility retention in `FocusAccessibilityService.kt` so that if `overlayManager.isVisible` is true and the session has not elapsed, self-dismissal is prevented. Verify logic by inspecting conditional branching.

## 2. Window Layout Parameters & Modal Isolation

- [x] 2.1 Update `WindowManager.LayoutParams` in `LockOverlayWindowManager.kt` to include `FLAG_NOT_FOCUSABLE` and remove `FLAG_NOT_TOUCH_MODAL`. Verify that the overlay attaches as a non-focusable modal surface without dispatching window state change broadcasts.

## 3. System Role Resolver & Transsion OEM Support

- [x] 3.1 Expand `SystemRoleResolver.kt` allowlist definitions to include `com.android.systemui`, `android`, Transsion launchers (`com.transsion.XOSLauncher`, `com.transsion.hilauncher`), and Transsion dialer (`com.transsion.smartcaller`). Verify definitions via unit or integration check.
- [x] 3.2 Update `isAllowlisted(pkg)` in `SystemRoleResolver.kt` to check `OEM_LAUNCHER_PACKAGES.contains(pkg)` and platform UI surfaces before defaulting to false. Verify by asserting allowlist resolution for Transsion launcher and SystemUI packages.

## 4. Package Visibility Manifest Declarations & Build Verification

- [x] 4.1 Update `focusGuard/plugins/withFocusEnforcer.js` to declare `<queries>` in `AndroidManifest.xml` covering `Intent.ACTION_MAIN / CATEGORY_HOME`, `Intent.ACTION_DIAL`, and `Intent.ACTION_SENDTO`. Verify that the generated manifest includes the required `<queries>` intent filters.
- [x] 4.2 Run project typecheck, lint, or gradle validation commands in `focusGuard` to verify that all native Kotlin files and config plugin changes compile cleanly without syntax errors.

## Workflow follow-up

- Archive the change after review and testing on physical/emulated Android devices.
- Verify that opening blocked applications during an active focus session maintains a stable, flicker-free lock overlay.
