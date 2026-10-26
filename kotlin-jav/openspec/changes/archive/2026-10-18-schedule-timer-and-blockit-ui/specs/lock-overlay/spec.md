## MODIFIED Requirements

### Requirement: Edge-to-Edge Jetpack Compose Overlay Surface
The system SHALL render a distraction-free Jetpack Compose UI surface inside the overlay window styled with industrial hardware aesthetics (Matte Charcoal `#121214`, Safety Orange `#FF5500`, and monospaced typography) and consuming all root pointer inputs.

#### Scenario: Render live remaining session countdown
- **WHEN** the lock overlay is visible
- **THEN** the UI MUST display a live countdown formatted as MM:SS dynamically derived from `max(0, endsAtEpochMs - currentTimeMillis)` in prominent Safety Orange monospaced typography

#### Scenario: Provide emergency dialer action
- **WHEN** the user taps the Emergency button on the overlay
- **THEN** the system MUST launch the system dialer via `Intent.ACTION_DIAL` and allow emergency outgoing communication without blocking

#### Scenario: Consume unhandled pointer input events
- **WHEN** touch events occur on the background or unclickable areas of the Compose overlay surface
- **THEN** the root layout MUST explicitly consume every pointer change to guarantee zero touch events pass through to background views
