## ADDED Requirements

### Requirement: Full-Screen Lock Overlay Window
The system SHALL display an un-dismissible full-screen `TYPE_ACCESSIBILITY_OVERLAY` window over non-allowlisted applications during active focus sessions.

#### Scenario: Display overlay over blocked application
- **WHEN** a focus session is active and the foreground package does not belong to the allowlist
- **THEN** `LockOverlayWindowManager` MUST present the full-screen overlay window within 100ms

#### Scenario: Hide overlay for allowlisted application
- **WHEN** the foreground package transitions to an allowlisted package (system dialer or SMS)
- **THEN** `LockOverlayWindowManager` MUST immediately dismiss or hide the overlay window

#### Scenario: Intercept hardware back and home interactions
- **WHEN** the user interacts with the device navigation bar or gestures while the overlay is displayed
- **THEN** the overlay window flags MUST swallow the input events and prevent navigating to the background application

### Requirement: Edge-to-Edge Jetpack Compose Overlay Surface
The system SHALL render a distraction-free Jetpack Compose UI surface inside the overlay window utilizing a synthetic lifecycle owner.

#### Scenario: Render live remaining session countdown
- **WHEN** the lock overlay is visible
- **THEN** the UI MUST display a live countdown formatted as MM:SS dynamically derived from `max(0, endsAtEpochMs - currentTimeMillis)`

#### Scenario: Provide emergency dialer action
- **WHEN** the user taps the Emergency button on the overlay
- **THEN** the system MUST launch the system dialer via `Intent.ACTION_DIAL` and allow emergency outgoing communication without blocking
