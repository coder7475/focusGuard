# Spec Delta

## MODIFIED Requirements

### Requirement: Edge-to-Edge Jetpack Compose Overlay Surface
The system SHALL render an informative, distraction-free Jetpack Compose UI surface inside the overlay window styled with industrial hardware aesthetics (Matte Charcoal `#121214`, Safety Orange `#FF5500`, and monospaced typography), wrapped in `FocusGuardTheme`, bound to an attached lifecycle owner, and consuming all root pointer inputs.

#### Scenario: Render live remaining session countdown
- **WHEN** the lock overlay is visible
- **THEN** the UI MUST display a live countdown formatted as MM:SS (or HH:MM:SS for sessions >= 1 hour) dynamically derived from `max(0, endsAtEpochMs - currentTimeMillis)` in prominent Safety Orange monospaced typography

#### Scenario: Render contextual routine and session details
- **WHEN** the lock overlay is rendered during an active routine or quick session
- **THEN** the overlay MUST display the active session type badge, routine title (if triggered by a schedule), scheduled window timeframe, and visual progress indicator showing elapsed focus proportion

#### Scenario: Provide emergency dialer action
- **WHEN** the user taps the Emergency button on the overlay
- **THEN** the system MUST launch the system dialer via `Intent.ACTION_DIAL` and allow emergency outgoing communication without blocking

#### Scenario: Consume unhandled pointer input events
- **WHEN** touch events occur on the background or unclickable areas of the Compose overlay surface
- **THEN** the root layout MUST explicitly consume every pointer change to guarantee zero touch events pass through to background views

#### Scenario: Initialize overlay lifecycle with attached saved state
- **WHEN** `OverlayLifecycleOwner.onCreate()` is invoked
- **THEN** `savedStateRegistryController.performAttach()` MUST be executed prior to `performRestore()` to prevent lifecycle initialization exceptions in Compose
