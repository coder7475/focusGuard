## ADDED Requirements

### Requirement: Recurring Routine Configuration
The system SHALL provide a dedicated user interface allowing users to create, inspect, edit, toggle, and delete recurring focus schedules backed by persistent SQLite storage.

#### Scenario: Create new recurring schedule
- **WHEN** user specifies a name, start time, end time, and enabled days of week
- **THEN** the system MUST save the `FocusSchedule` entity with a computed bitmask to the repository and register alarms

#### Scenario: Toggle schedule active state
- **WHEN** user toggles the enable/disable switch on a schedule card
- **THEN** the system MUST update `isEnabled` in Room and arm or disarm the corresponding exact alarms immediately

#### Scenario: Delete recurring schedule
- **WHEN** user deletes an existing routine
- **THEN** the system MUST remove the record from Room and cancel any pending alarms associated with that schedule ID

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
