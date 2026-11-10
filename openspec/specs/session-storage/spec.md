# Session Storage Specification

## Purpose

Provides persistent SQLite database and key-value state management for focus sessions, recurring schedules, and user onboarding flags, ensuring complete state survival across process death and system reboots.

## Requirements

### Requirement: Focus Session Record Persistence
The system SHALL persist focus sessions in a SQLite database with absolute wall-clock epoch timestamps and lifecycle statuses.

#### Scenario: Insert active focus session
- **WHEN** a focus session is created with a target duration
- **THEN** the system MUST persist a session entity with status ACTIVE, start epoch timestamp, and target end epoch timestamp

#### Scenario: Query active session
- **WHEN** an active focus session exists in the database
- **THEN** repository queries MUST return the active session record with its start and end epoch timestamps

#### Scenario: Update session status to completed
- **WHEN** a session reaches its end time or is concluded
- **THEN** the system MUST update the session record status to COMPLETED and record the completed session statistics

### Requirement: Focus Schedule Record Persistence
The system SHALL persist recurring focus schedules in the SQLite database with minute-of-day offsets and weekday recurrence bitmasks.

#### Scenario: Insert recurring schedule
- **WHEN** a user saves a recurring schedule
- **THEN** the system MUST insert the record with name, start minute, end minute, days bitmask, and enabled status

#### Scenario: Toggle schedule enabled state
- **WHEN** user toggles an existing schedule
- **THEN** the system MUST update the isEnabled flag in the database immediately

#### Scenario: Query all configured schedules
- **WHEN** schedules exist in the database
- **THEN** schedule queries MUST return all persisted schedules sorted by start time

### Requirement: Cross-Process Concurrent Database Access
The system SHALL configure the SQLite database in Write-Ahead Logging mode to allow concurrent safe reads and writes from both the React Native JavaScript environment and native Kotlin background processes.

#### Scenario: Concurrent native and JS database access
- **WHEN** native background services and the JavaScript UI access the database file simultaneously
- **THEN** SQLite Write-Ahead Logging MUST prevent database lock errors and ensure transactional integrity

### Requirement: Application Preferences Persistence
The system SHALL persist key-value flags using persistent preferences storage to preserve onboarding and system guidance states.

#### Scenario: Store onboarding completion flag
- **WHEN** the user completes initial setup
- **THEN** the system MUST asynchronously write true to `KEY_ONBOARDING_COMPLETED` and emit the updated state

#### Scenario: Store battery guide dismissal flag
- **WHEN** the user acknowledges the OEM battery guidance dialog
- **THEN** the system MUST persist true to `KEY_BATTERY_GUIDE_DISMISSED`
