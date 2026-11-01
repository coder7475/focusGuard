# Schedule Management Specification

## Purpose

Calculates, persists, and coordinates recurring focus routines, including complex overnight midnight-crossing schedules, weekday recurrence bitmasks, and automated session transitions.

## Requirements

### Requirement: Midnight-Crossing Schedule Evaluation
The system SHALL compute active windows and next trigger epochs for recurring routines that cross midnight boundaries.

#### Scenario: Evaluate overnight active window
- **WHEN** a schedule is defined from 22:00 to 07:00 and current time is 23:30 on an active day
- **THEN** the schedule engine MUST determine that the schedule is currently active

#### Scenario: Evaluate post-midnight active window
- **WHEN** a schedule is defined from 22:00 to 07:00 and current time is 03:00 on the day following an active day
- **THEN** the schedule engine MUST recognize the session as belonging to the previous day's active overnight window

#### Scenario: Calculate next trigger epoch
- **WHEN** a recurring schedule is inactive
- **THEN** the schedule engine MUST calculate the exact future epoch timestamp for the next start window based on the weekday bitmask

### Requirement: Schedule CRUD Operations
The system SHALL support creating, editing, toggling, and deleting recurring focus routines with interactive UI controls.

#### Scenario: Create new recurring routine
- **WHEN** user configures name, start time, end time, and active weekdays, and submits the form
- **THEN** the routine MUST be stored in persistent storage and registered with the alarm scheduler

#### Scenario: Delete recurring routine
- **WHEN** user deletes an existing routine
- **THEN** the record MUST be removed from persistent storage and any pending alarms MUST be cancelled
