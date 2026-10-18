# Tactile Rotary Timer Specification

## Purpose
Provides a tactile, industrial hardware-inspired circular dial timer interface for setting and tracking focus sessions with discrete detents, haptic feedback, and illuminated progress indicators.

## Requirements

### Requirement: Circular Polar Angle Gesture Tracking
The system SHALL track rotational gestures on a circular Canvas dial and convert touch coordinates into polar angles $\theta = \operatorname{atan2}(y - c_y, x - c_x)$ mapped to discrete focus durations.

#### Scenario: Continuous dial drag
- **WHEN** user drags along the circular track of the rotary dial
- **THEN** the component MUST dynamically update the selected duration in real time and smoothly animate the illuminated arc

#### Scenario: Snap to discrete duration steps
- **WHEN** user releases the dial gesture or moves past threshold boundaries
- **THEN** the system MUST snap the duration to discrete increments (15m, 25m, 45m, 60m, 90m, 120m, 180m, or 240m)

### Requirement: Haptic Detent Feedback
The system SHALL trigger discrete tactile haptic ticks whenever the dial crosses from one discrete duration boundary to another during user interaction.

#### Scenario: Fire haptic tick on step transition
- **WHEN** the dial pointer traverses a discrete interval boundary
- **THEN** the system MUST execute a light tactile vibration via `HapticFeedbackType` to simulate a physical mechanical detent

### Requirement: Industrial Dial Canvas Rendering
The system SHALL render an industrial hardware-styled circular gauge with peripheral tick marks, numerals, illuminated Safety Orange arc, and central control knob.

#### Scenario: Render idle dial surface
- **WHEN** the rotary dial is displayed in idle state
- **THEN** the Canvas MUST draw radial graduation ticks, discrete numeric markers, and an active indicator knob in Matte Charcoal and Safety Orange

#### Scenario: Render live remaining countdown during active session
- **WHEN** a session is active
- **THEN** the dial MUST render a shrinking illuminated sweep arc representing the exact fraction of time remaining

### Requirement: Quick-Select Hardware Preset Switches
The system SHALL render tactile preset switches below the rotary dial that immediately update the dial position, allow instant session launching, and provide a direct entry point for custom numeric durations.

#### Scenario: Tap preset button
- **WHEN** user taps a preset switch (e.g. 15m, 25m, 45m, 60m, 120m)
- **THEN** the rotary dial MUST animate its angle to the selected preset duration and update the central readout

#### Scenario: Open custom duration dialog
- **WHEN** user taps the "Custom..." preset switch
- **THEN** the system MUST display a numeric input modal allowing arbitrary duration entry between 1 and 1440 minutes

### Requirement: Custom Duration Configuration Modal
The system SHALL provide a modal dialog allowing users to enter custom focus durations in minutes, validating bounds and syncing with the rotary dial and persistence layer.

#### Scenario: Valid custom duration entry
- **WHEN** user enters a valid numeric duration (e.g. 90 minutes) and taps Confirm
- **THEN** the system MUST update the active selected minutes, adjust the dial display, and persist the preference

#### Scenario: Out of bounds input validation
- **WHEN** user enters a value less than 1 or greater than 1440 minutes
- **THEN** the system MUST disable confirmation and display a clear boundary validation message

#### Scenario: High duration discipline warning
- **WHEN** user selects a duration exceeding 180 minutes (3 hours)
- **THEN** the system MUST present an explicit hardware discipline confirmation warning before engaging the lock
