## MODIFIED Requirements

### Requirement: Full-Screen Lock Overlay Window
The system SHALL display an un-dismissible full-screen `TYPE_ACCESSIBILITY_OVERLAY` window over non-allowlisted applications during active focus sessions, managing window attachment and removal dynamically without relying on root view visibility mutations.

#### Scenario: Display overlay over blocked application
- **WHEN** a focus session is active and the foreground package does not belong to the allowlist
- **THEN** `LockOverlayWindowManager` MUST present the full-screen overlay window by adding the view surface to `WindowManager` within 100ms

#### Scenario: Hide overlay for allowlisted application
- **WHEN** the foreground package transitions to an allowlisted package (system dialer or SMS)
- **THEN** `LockOverlayWindowManager` MUST immediately remove the overlay window surface from `WindowManager`

#### Scenario: Intercept hardware back and home interactions
- **WHEN** the user interacts with the device navigation bar or gestures while the overlay is displayed
- **THEN** the overlay window flags MUST swallow the input events and prevent navigating to the background application

#### Scenario: Modal touch capture without touch leakage
- **WHEN** user taps, scrolls, or performs gestures anywhere on the screen while the lock overlay is displayed
- **THEN** `WindowManager.LayoutParams` flags MUST omit `FLAG_NOT_TOUCH_MODAL` so that the window acts as a true modal barrier consuming all pointer events across the entire display

### Requirement: Edge-to-Edge Jetpack Compose Overlay Surface
The system SHALL render a distraction-free Jetpack Compose UI surface inside the overlay window styled with industrial hardware aesthetics (Matte Charcoal `#121214`, Safety Orange `#FF5500`, and monospaced typography), wrapped in `FocusGuardTheme`, bound to an attached lifecycle owner, and consuming all root pointer inputs.

#### Scenario: Render live remaining session countdown
- **WHEN** the lock overlay is visible
- **THEN** the UI MUST display a live countdown formatted as MM:SS dynamically derived from `max(0, endsAtEpochMs - currentTimeMillis)` in prominent Safety Orange monospaced typography

#### Scenario: Provide emergency dialer action
- **WHEN** the user taps the Emergency button on the overlay
- **THEN** the system MUST launch the system dialer via `Intent.ACTION_DIAL` and allow emergency outgoing communication without blocking

#### Scenario: Consume unhandled pointer input events
- **WHEN** touch events occur on the background or unclickable areas of the Compose overlay surface
- **THEN** the root layout MUST explicitly consume every pointer change to guarantee zero touch events pass through to background views

#### Scenario: Initialize overlay lifecycle with attached saved state
- **WHEN** `OverlayLifecycleOwner.onCreate()` is invoked
- **THEN** `savedStateRegistryController.performAttach()` MUST be executed prior to `performRestore()` to prevent lifecycle initialization exceptions in Compose
