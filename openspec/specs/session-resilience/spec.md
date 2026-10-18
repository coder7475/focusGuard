# Session Resilience Specification

## Purpose
Maintains session state and execution integrity across process restarts, device reboots, and deep Doze cycles through a foreground service ticker, exact wall-clock alarms, and boot reconciliation.

## Requirements

### Requirement: Foreground Service Countdown Preservation
The system SHALL run a foreground service with `specialUse` type to preserve session countdown timing and state integrity in the background.

#### Scenario: Start foreground service with persistent notification
- **WHEN** a focus session starts
- **THEN** `FocusGuardService` MUST start in the foreground with notification channel `ID_SESSION_TRACKER` displaying the remaining focus duration

#### Scenario: Notify session completion
- **WHEN** the session duration elapses
- **THEN** `FocusGuardService` MUST transition the notification to channel `ID_SESSION_COMPLETION` with high priority and alert sound

#### Scenario: Reconcile active session upon countdown expiration
- **WHEN** the foreground service ticker reaches zero remaining milliseconds (`remainingMs <= 0L`)
- **THEN** `FocusGuardService` MUST directly finalize the active session state in the database via `SessionEngine.completeActiveSession()` before stopping the service

### Requirement: Exact Wall-Clock Alarm Scheduling
The system SHALL schedule exact device wakeups via `AlarmManager.setExactAndAllowWhileIdle()` to guarantee session completion survives deep Doze.

#### Scenario: Schedule exact completion alarm
- **WHEN** a focus session is initialized with `endsAtEpochMs`
- **THEN** `AlarmCoordinator` MUST register an exact alarm targeted to `endsAtEpochMs` waking `SessionAlarmReceiver`

#### Scenario: Handle exact alarm delivery
- **WHEN** `SessionAlarmReceiver` receives the session completion intent
- **THEN** it MUST invoke `SessionEngine.completeActiveSession()` to finalize database state and dismiss lock overlays

### Requirement: Boot & Crash State Reconciliation
The system SHALL reconcile active focus session records upon device boot or process restart against absolute wall-clock epoch time.

#### Scenario: Re-arm unexpired session after reboot
- **WHEN** the device completes booting and an active session exists with `endsAtEpochMs > currentTimeMillis()`
- **THEN** `BootCompletedReceiver` MUST re-arm the exact alarm and re-launch `FocusGuardService`

#### Scenario: Finalize expired session after reboot
- **WHEN** the device completes booting and an active session exists with `endsAtEpochMs <= currentTimeMillis()`
- **THEN** `BootCompletedReceiver` MUST update the session status to `COMPLETED` and clear pending alarms
