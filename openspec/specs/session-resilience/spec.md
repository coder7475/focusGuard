# Session Resilience Specification

## Purpose

Maintains session state and execution integrity across process restarts, device reboots, and deep Doze cycles through a foreground service ticker, exact wall-clock alarms, and boot reconciliation.

## Requirements

### Requirement: Foreground Service Countdown Preservation
The system SHALL run a foreground service with `specialUse` type to preserve session countdown timing and state integrity in the background.

#### Scenario: Start foreground service with persistent notification
- **WHEN** a focus session starts
- **THEN** the native foreground service MUST start with a persistent notification displaying the remaining focus duration

#### Scenario: Update notification ticker
- **WHEN** the focus session is ticking in the background
- **THEN** the foreground service MUST update the notification content every second with remaining time formatted as MM:SS

#### Scenario: Notify session completion
- **WHEN** the session duration elapses
- **THEN** the foreground service MUST transition the notification to a completion alert with high priority

#### Scenario: Reconcile active session upon countdown expiration
- **WHEN** the foreground service ticker reaches zero remaining milliseconds (`remainingMs <= 0L`)
- **THEN** the service MUST finalize the active session state in the database before stopping the foreground service

### Requirement: Exact Wall-Clock Alarm Scheduling
The system SHALL schedule exact device wakeups via `AlarmManager.setExactAndAllowWhileIdle()` to guarantee session completion survives deep Doze.

#### Scenario: Schedule exact alarm on session start
- **WHEN** a focus session starts with target end epoch
- **THEN** the system MUST register an exact wake-up alarm targeting the end epoch timestamp

#### Scenario: Handle exact alarm delivery
- **WHEN** the session alarm receiver receives the session completion intent
- **THEN** it MUST finalize database state and dismiss lock overlays

#### Scenario: Cancel pending alarm on early session conclusion
- **WHEN** a focus session completes or terminates
- **THEN** the system MUST cancel any pending alarm intents associated with that session

### Requirement: Boot & Crash State Reconciliation
The system SHALL reconcile active focus session records upon device boot or process restart against absolute wall-clock epoch time.

#### Scenario: Restore active focus lock on reboot
- **WHEN** device boot completed broadcast is received while an active session exists in the database
- **THEN** the native boot receiver MUST immediately re-arm the exact alarm, restart the foreground service, and enforce lock status

#### Scenario: Finalize expired session after reboot
- **WHEN** the device completes booting and an active session exists with end time in the past (`endsAtEpochMs <= currentTimeMillis()`)
- **THEN** the boot receiver MUST update the session status to `COMPLETED` and clear pending alarms
