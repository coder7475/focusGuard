# Lock Overlay Specification

## Purpose

Renders an un-dismissible, full-screen overlay over blocked applications during active focus sessions, displaying remaining session time and providing one-touch emergency phone access.

## Requirements

### Requirement: Full-Screen Lock Overlay Window
The system SHALL display an un-dismissible full-screen accessibility overlay window over non-allowlisted applications during active focus sessions.

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
- **THEN** window flags MUST omit not-touch-modal flags so that the window acts as a true modal barrier consuming all pointer events

#### Scenario: Prevent overlay self-dismissal and crash recovery
- **WHEN** the overlay window view transitions or receives focus updates
- **THEN** the window manager MUST maintain valid layout parameters without throwing unhandled view attachment exceptions

### Requirement: Emergency Dialer Access from Overlay
The system SHALL provide a dedicated, accessible emergency call button on the lock overlay that launches the device dialer immediately.

#### Scenario: Trigger emergency dialer from lock screen
- **WHEN** user taps the emergency call button on the lock overlay
- **THEN** the overlay MUST launch an action dial intent with new task flags and temporarily dismiss the lock overlay
