# Lock Overlay Specification

## Purpose

Renders an un-dismissible, full-screen overlay over blocked applications during active focus sessions, displaying remaining session time and providing one-touch emergency phone access.

## Requirements

### Requirement: Full-Screen Lock Overlay Window
The system SHALL display an un-dismissible full-screen accessibility overlay window over non-allowlisted applications during active focus sessions with non-focusable input isolation, managing window attachment and removal dynamically without relying on root view visibility mutations.

#### Scenario: Display overlay over blocked application
- **WHEN** a focus session is active and the foreground package does not belong to the allowlist
- **THEN** the overlay manager MUST present the full-screen overlay window by attaching the view surface to the system window manager within 100ms without crashing on duplicate additions

#### Scenario: Hide overlay for allowlisted application
- **WHEN** the foreground package transitions to an allowlisted package
- **THEN** the overlay manager MUST immediately remove the overlay window surface from the system window manager

#### Scenario: Intercept hardware back and home interactions
- **WHEN** the user interacts with the device navigation bar or gestures while the overlay is displayed
- **THEN** the overlay window flags MUST swallow the input events and prevent navigating to the background application

#### Scenario: Modal touch capture without touch leakage
- **WHEN** user taps, scrolls, or performs gestures anywhere on the screen while the lock overlay is displayed
- **THEN** window flags MUST omit not-touch-modal flags so that the window acts as a true modal barrier consuming all pointer events across the entire display

#### Scenario: Non-focusable window surface to suppress accessibility feedback loops
- **WHEN** the lock overlay window is initialized and attached to the system window manager
- **THEN** layout parameters MUST include FLAG_NOT_FOCUSABLE so the window absorbs touch events without stealing top-level window focus or triggering recurring window transition events

#### Scenario: Prevent overlay self-dismissal and crash recovery
- **WHEN** the overlay window view transitions or receives focus updates
- **THEN** the window manager MUST maintain valid layout parameters without throwing unhandled view attachment exceptions

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

### Requirement: Emergency Dialer Access from Overlay
The system SHALL provide a dedicated, accessible emergency call button on the lock overlay that launches the device dialer immediately.

#### Scenario: Trigger emergency dialer from lock screen
- **WHEN** user taps the emergency call button on the lock overlay
- **THEN** the overlay MUST launch an action dial intent with new task flags and temporarily dismiss the lock overlay
