# Schedule Management Specification

## Purpose
Enables users to configure, persist, and automatically execute recurring focus routines (such as bedtime or work focus windows) across specified days of the week, with full overnight support and background alarm triggers.

## Requirements

### Requirement: Recurring Routine Configuration
The system SHALL provide a dedicated user interface allowing users to create, inspect, edit, toggle, and delete recurring focus schedules backed by persistent SQLite storage, with 12-hour AM/PM time selection and formatted timestamp displays.

#### Scenario: Create new recurring schedule with 12-hour AM/PM selection
- **WHEN** user specifies a name, 12-hour start time with AM/PM indicator, 12-hour end time with AM/PM indicator, and enabled days of week
- **THEN** the system MUST correctly convert the 12-hour times to 24-hour minute-of-day offsets, save the `FocusSchedule` entity to Room, and arm the corresponding alarms

#### Scenario: Toggle schedule active state
- **WHEN** user toggles the enable/disable switch on a schedule card
- **THEN** the system MUST update `isEnabled` in Room and arm or disarm the corresponding exact alarms immediately

#### Scenario: Delete recurring schedule
- **WHEN** user deletes an existing routine
- **THEN** the system MUST remove the record from Room and cancel any pending alarms associated with that schedule ID

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

### Requirement: Overnight Routine Support
The system SHALL evaluate recurring focus routines whose end time precedes or equals start time (crossing midnight) across adjacent calendar days.

#### Scenario: Active overnight window detection
- **WHEN** a schedule is configured from 22:00 to 07:00 and current time is 01:30 on an enabled night
- **THEN** `ScheduleEngine.isInsideScheduleWindow` MUST evaluate to true and compute the correct termination epoch timestamp for 07:00 that morning

### Requirement: Automated Schedule Alarm Execution
The system SHALL schedule exact system alarms via `AlarmManager` to autonomously initiate and terminate focus sessions at scheduled window boundaries.

#### Scenario: Alarm fires at schedule window start
- **WHEN** `ScheduleAlarmReceiver` receives an alarm intent indicating a schedule window has begun
- **THEN** the system MUST start `FocusGuardService`, create an active session in the database, and arm the completion alarm

#### Scenario: Alarm fires at schedule window end
- **WHEN** `ScheduleAlarmReceiver` receives an alarm intent indicating a schedule window has ended
- **THEN** the system MUST invoke `SessionEngine.completeActiveSession()`, stop `FocusGuardService`, and schedule the next recurrence
