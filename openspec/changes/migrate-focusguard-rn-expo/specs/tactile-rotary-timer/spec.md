# Spec Delta

## Purpose

Provides a tactile, industrial hardware-inspired circular dial timer interface for setting and tracking focus sessions with discrete detents, haptic feedback, and illuminated progress indicators.

## ADDED Requirements

### Requirement: Circular Polar Angle Gesture Tracking
The system SHALL track rotational gestures on a circular dial and convert touch coordinates into polar angles mapped to discrete focus durations.

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
- **THEN** the system MUST execute a light tactile vibration via haptic feedback to simulate a physical mechanical detent

### Requirement: Preset Duration Chips and Quick Start
The system SHALL provide quick selection chips for common focus durations and a prominent activation control to initiate focus sessions immediately.

#### Scenario: Select duration via preset chip
- **WHEN** user taps a preset duration chip such as 25m or 45m
- **THEN** the dial pointer MUST smoothly animate to that duration position and update the session duration state
