# Design: Overlay Stabilization and Ping-Pong Suppression

## Context

See [proposal.md](proposal.md) for motivation and root-cause summary.

The FocusGuard native enforcer module (`com.coder7575.sleepguard.enforcer`) deploys `FocusAccessibilityService` to detect foreground window transitions and `LockOverlayWindowManager` to render a full-screen `TYPE_ACCESSIBILITY_OVERLAY` view over blocked apps during active sessions.

On Android 14 (specifically observed on Transsion XOS / Infinix Hot 50i), adding an overlay window broadcasts `AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED` for `com.coder7575.SleepGuard`. `FocusAccessibilityService` processes this event as an allowlisted package switch, calls `hideOverlay()`, which yields focus back to the blocked app, which immediately re-triggers `showOverlay()`. Additionally, window layout parameters lack `FLAG_NOT_FOCUSABLE` and inadvertently include `FLAG_NOT_TOUCH_MODAL`, Transsion launcher packages are omitted from the allowlist, and package visibility queries are missing from `AndroidManifest.xml`.

## Goals / Non-Goals

**Goals:**
- Eliminate the infinite overlay show/hide strobe cycle by ignoring self-generated window events from the overlay surface.
- Prevent the overlay window from stealing top-level focus or emitting window-state accessibility events via proper `WindowManager.LayoutParams` flags.
- Support Transsion XOS devices (Infinix, Tecno, itel) and other OEM launchers by including known package identifiers and enabling dynamic intent resolution via Android 11+ `<queries>`.
- Allow non-disruptive interactions with platform UI surfaces (`com.android.systemui`, `android`).

**Non-Goals:**
- Refactoring the lock screen countdown UI, animations, or styling.
- Changing the TypeScript native module bridge signature or session storage schema.
- Creating custom launcher replacements or kiosk device-admin lock modes.

## Decisions

### 1. Filter Self-Package Accessibility Events via Activity Class Introspection
- **Decision**: In `FocusAccessibilityService.onAccessibilityEvent`, if `targetPkg == context.packageName`, check `event.className`. Only permit dismissal or processing if `event.className == "${context.packageName}.MainActivity"`. All other self-package events (such as the Compose/Android View hierarchy of `LockOverlayWindowManager`) are immediately ignored with an early return.
- **Rationale**: The overlay window is created under the application's process and package identity. When attached, WindowManager fires events tagged with `context.packageName`. By inspecting `className`, we distinguish user interaction with the actual FocusGuard React Native app from overlay window attachments.
- **Alternatives Considered**:
  - *Hardcoding a boolean flag `isShowingOverlay` to drop all events for N milliseconds*: Fragile across device speeds and causes dropped transitions if a user swiftly opens another app.
  - *Using `TYPE_APPLICATION_OVERLAY` with `SYSTEM_ALERT_WINDOW` instead of `TYPE_ACCESSIBILITY_OVERLAY`*: Accessibility overlays do not require separate system alert window grant prompts and stay bound to the service lifecycle, adhering to the project's accessibility-driven architecture.

### 2. Configure Modal Isolation Window Flags
- **Decision**: In `LockOverlayWindowManager`, configure `WindowManager.LayoutParams`:
  - Add `WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE`
  - Remove `WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL`
  - Retain `FLAG_LAYOUT_IN_SCREEN`, `FLAG_LAYOUT_NO_LIMITS`, `FLAG_SHOW_WHEN_LOCKED`, and `FLAG_KEEP_SCREEN_ON`
- **Rationale**: `FLAG_NOT_FOCUSABLE` ensures the overlay window does not steal window or key focus from the OS, eliminating top-level accessibility window state transition dispatches upon attachment. Omitting `FLAG_NOT_TOUCH_MODAL` makes the overlay window truly modal, absorbing all pointer events and gestures without leaking them to the background app.
- **Alternatives Considered**:
  - *Keeping focusable overlay with custom key event listener*: Requires handling every hardware key and back gesture manually while still emitting window focus transitions.

### 3. Broaden System Surface and OEM Allowlisting in `SystemRoleResolver`
- **Decision**:
  - Allowlist `"com.android.systemui"` and `"android"` as non-blockable platform UI surfaces.
  - Add `com.transsion.XOSLauncher` and `com.transsion.hilauncher` to `OEM_LAUNCHER_PACKAGES`.
  - Add `com.transsion.smartcaller` to `OEM_DIALER_PACKAGES`.
  - Explicitly invoke `OEM_LAUNCHER_PACKAGES.contains(pkg)` inside `isAllowlisted(pkg)`.
- **Rationale**: On Transsion XOS, the home launcher is named `com.transsion.XOSLauncher` or `com.transsion.hilauncher`. When the user is on their home screen, the system must not classify the desktop as an unauthorized app. Furthermore, quick settings shade and gesture pill interactions invoke `com.android.systemui`.
- **Alternatives Considered**:
  - *Allowlisting all packages with "launcher" in the name*: Security risk; rogue or blocked apps could disguise themselves by including "launcher" in their package name.

### 4. Inject Android 11+ Package Visibility Queries in Expo Plugin
- **Decision**: In `plugins/withFocusEnforcer.js`, add a `<queries>` block to `AndroidManifest.xml` declaring intents:
  - `<intent><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.HOME"/></intent>`
  - `<intent><action android:name="android.intent.action.DIAL"/></intent>`
  - `<intent><action android:name="android.intent.action.SENDTO"/><data android:scheme="smsto"/></intent>`
- **Rationale**: Android 11 (API level 30) introduces package visibility filtering. Without explicit `<queries>`, `PackageManager.resolveActivity` for default launcher or dialer returns null or fallbacks to `"android"`, breaking dynamic role detection on modern Android releases.
- **Alternatives Considered**:
  - *Requesting `QUERY_ALL_PACKAGES` permission*: Google Play Store strictly restricts `QUERY_ALL_PACKAGES` and will reject apps without verified core need, whereas intent-based `<queries>` is standard and compliant.

## Risks / Trade-offs

- **[Risk]** When `FLAG_NOT_FOCUSABLE` is applied, the overlay cannot receive hardware physical keyboard text input directly.
  → **Mitigation**: The lock overlay only provides touch button controls (e.g. "Emergency Call") and a timer display; it does not contain text inputs. Touch events are fully received and processed by clickable views.
- **[Risk]** Notification shade pull-down is permitted via `com.android.systemui` allowlist.
  → **Mitigation**: System UI allows users to see battery/network status and incoming emergency alerts, but tapping a notification to launch a blocked app immediately generates a window transition event for the blocked package, restoring the overlay.
- **[Risk]** Future OEM launchers may have unknown package names.
  → **Mitigation**: Dynamic resolution via `Intent(ACTION_MAIN, CATEGORY_HOME)` handles arbitrary launchers, enabled by the new manifest `<queries>`. Static OEM sets act as a reliable fallback.
