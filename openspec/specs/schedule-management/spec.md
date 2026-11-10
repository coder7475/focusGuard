# Schedule Management Specification

## Purpose

Calculates, persists, and coordinates recurring focus routines, including complex overnight midnight-crossing schedules, weekday recurrence bitmasks, and automated session transitions.

## Requirements

### Requirement: Recurring Routine Configuration
The system SHALL provide a dedicated user interface allowing users to create, inspect, edit, toggle, and delete recurring focus schedules backed by persistent SQLite storage, with 12-hour AM/PM time selection and formatted timestamp displays.

#### Scenario: Create new recurring schedule with 12-hour AM/PM selection
- **WHEN** user specifies a name, 12-hour start time with AM/PM indicator, 12-hour end time with AM/PM indicator, and enabled days of week
- **THEN** the system MUST correctly convert the 12-hour times to 24-hour minute-of-day offsets, save the `FocusSchedule` entity to Room or SQLite, and arm the corresponding alarms

#### Scenario: Toggle schedule active state
- **WHEN** user toggles the enable/disable switch on a schedule card
- **THEN** the system MUST update `isEnabled` in the database and arm or disarm the corresponding exact alarms immediately

#### Scenario: Delete recurring schedule
- **WHEN** user deletes an existing routine
- **THEN** the record MUST be removed from persistent storage and any pending alarms associated with that schedule ID MUST be cancelled

#### Scenario: Display formatted AM/PM timestamps on schedule cards
- **WHEN** existing schedules are rendered in the routine list
- **THEN** `ScheduleCard` MUST format the start and end minute-of-day values as 12-hour timestamps with explicit AM/PM suffixes (e.g. `09:00 AM – 05:00 PM`)

### Requirement: 12-Hour AM/PM Time Picker Selector
The system SHALL provide a 12-hour time picker selector component with discrete hour (1–12), minute (0–55), and segmented AM/PM controls.

#### Scenario: Toggle between AM and PM periods
- **WHEN** user clicks the AM or PM segment in the time picker
- **THEN** the selector MUST immediately reflect the active period in Safety Orange styling and update the calculated minute-of-day offset

#### Scenario: Handle 12:00 AM midnight conversion
- **WHEN** user selects hour 12, minute 0, and AM
- **THEN** the calculated minute-of-day MUST equal 0

#### Scenario: Handle 12:00 PM noon conversion
- **WHEN** user selects hour 12, minute 0, and PM
- **THEN** the calculated minute-of-day MUST equal 720 (12 * 60)

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

### Requirement: Automated Schedule Alarm Execution
The system SHALL schedule exact system alarms via `AlarmManager` to autonomously initiate and terminate focus sessions at scheduled window boundaries.

#### Scenario: Alarm fires at schedule window start
- **WHEN** `ScheduleAlarmReceiver` or native alarm receiver receives an alarm intent indicating a schedule window has begun
- **THEN** the system MUST start the foreground service, create an active session in the database, and arm the completion alarm

#### Scenario: Alarm fires at schedule window end
- **WHEN** `ScheduleAlarmReceiver` or native alarm receiver receives an alarm intent indicating a schedule window has ended
- **THEN** the system MUST complete the active session, stop the foreground service, and schedule the next recurrence
