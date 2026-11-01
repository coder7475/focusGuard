# Spec Delta

## Purpose

Maintains session state and execution integrity across process restarts, device reboots, and deep Doze cycles through a foreground service ticker, exact wall-clock alarms, and boot reconciliation.

## ADDED Requirements

### Requirement: Foreground Service Countdown Preservation
The system SHALL run a foreground service with specialUse type to preserve session countdown timing and state integrity in the background.

#### Scenario: Start foreground service with persistent notification
- **WHEN** a focus session starts
- **THEN** the native foreground service MUST start with a persistent notification displaying the remaining focus duration

#### Scenario: Update notification ticker
- **WHEN** the focus session is ticking in the background
- **THEN** the foreground service MUST update the notification content every second with remaining time formatted as MM:SS

#### Scenario: Reconcile active session upon countdown expiration
- **WHEN** the foreground service ticker reaches zero remaining milliseconds
- **THEN** the service MUST finalize the active session state in the database before stopping the foreground service

### Requirement: Exact Wall-Clock Alarm Scheduling
The system SHALL schedule exact device wakeups via exact and allow-while-idle alarms to guarantee session completion survives deep Doze.

#### Scenario: Schedule exact alarm on session start
- **WHEN** a focus session starts with target end epoch
- **THEN** the system MUST register an exact wake-up alarm targeting the end epoch timestamp

#### Scenario: Cancel pending alarm on early session conclusion
- **WHEN** a focus session completes or terminates
- **THEN** the system MUST cancel any pending alarm intents associated with that session

### Requirement: Autonomous Boot Reconciliation
The system SHALL execute an autonomous boot receiver on device startup to re-arm active focus sessions without requiring the JavaScript engine to initialize.

#### Scenario: Restore active focus lock on reboot
- **WHEN** device boot completed broadcast is received while an active session exists in the database
- **THEN** the native boot receiver MUST immediately re-arm the exact alarm, restart the foreground service, and enforce lock status
