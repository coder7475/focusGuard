## MODIFIED Requirements

### Requirement: Quick-Select Hardware Preset Switches
The system SHALL render tactile preset switches below the rotary dial that immediately update the dial position, allow instant session launching, and provide a direct entry point for custom numeric durations.

#### Scenario: Tap preset button
- **WHEN** user taps a preset switch (e.g. 15m, 25m, 45m, 60m, 120m)
- **THEN** the rotary dial MUST animate its angle to the selected preset duration and update the central readout

#### Scenario: Open custom duration dialog
- **WHEN** user taps the "Custom..." preset switch
- **THEN** the system MUST display a numeric input modal allowing arbitrary duration entry between 1 and 1440 minutes

## ADDED Requirements

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
