## ADDED Requirements

### Requirement: Focus Session Record Persistence
The system SHALL persist focus sessions in a Room SQLite database with absolute wall-clock epoch timestamps and lifecycle statuses.

#### Scenario: Insert active focus session
- **WHEN** a focus session is created with a duration of 25 minutes
- **THEN** the system MUST persist a `FocusSessionEntity` with `status = "ACTIVE"`, `startTimeEpochMs` equal to current epoch timestamp, and `endsAtEpochMs` equal to `startTimeEpochMs + 1,500,000`

#### Scenario: Query active session reactively
- **WHEN** an active focus session exists in the database
- **THEN** `SessionDao.getActiveSession()` MUST emit a `Flow` containing the active `FocusSessionEntity`

#### Scenario: Update session status to completed
- **WHEN** a session reaches its end time or is concluded
- **THEN** the system MUST update the entity's status to `"COMPLETED"` and increment the completed session count

### Requirement: Focus Schedule Record Persistence
The system SHALL persist recurring focus schedules in a Room SQLite database with minute-of-day offsets and weekday recurrence bitmasks.

#### Scenario: Insert recurring schedule
- **WHEN** a schedule is configured from 20:00 (1200 minutes) to 08:00 (480 minutes) across Monday through Friday
- **THEN** the system MUST persist a `FocusScheduleEntity` with `startMinuteOfDay = 1200`, `endMinuteOfDay = 480`, `daysOfWeekBitmask`, and `isEnabled = true`

#### Scenario: Query all configured schedules
- **WHEN** schedules exist in the database
- **THEN** `ScheduleDao.getAllSchedules()` MUST emit a reactive `Flow` of all persisted schedules sorted by start time

### Requirement: Application Preferences Persistence
The system SHALL persist key-value flags using AndroidX Preferences DataStore to preserve onboarding and system guidance states.

#### Scenario: Store onboarding completion flag
- **WHEN** the user completes initial setup
- **THEN** the system MUST asynchronously write `true` to `KEY_ONBOARDING_COMPLETED` in DataStore and emit the updated boolean state via Flow

#### Scenario: Store battery guide dismissal flag
- **WHEN** the user acknowledges the OEM battery guidance dialog
- **THEN** the system MUST persist `true` to `KEY_BATTERY_GUIDE_DISMISSED` in DataStore
