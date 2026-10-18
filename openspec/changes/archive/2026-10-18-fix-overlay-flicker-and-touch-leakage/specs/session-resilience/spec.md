## MODIFIED Requirements

### Requirement: Foreground Service Countdown Preservation
The system SHALL run a foreground service with `specialUse` type to preserve session countdown timing and state integrity in the background, finalizing active sessions in the database upon timer completion.

#### Scenario: Start foreground service with persistent notification
- **WHEN** a focus session starts
- **THEN** `FocusGuardService` MUST start in the foreground with notification channel `ID_SESSION_TRACKER` displaying the remaining focus duration

#### Scenario: Notify session completion
- **WHEN** the session duration elapses
- **THEN** `FocusGuardService` MUST transition the notification to channel `ID_SESSION_COMPLETION` with high priority and alert sound

#### Scenario: Reconcile active session upon countdown expiration
- **WHEN** the foreground service ticker reaches zero remaining milliseconds (`remainingMs <= 0L`)
- **THEN** `FocusGuardService` MUST directly finalize the active session state in the database via `SessionEngine.completeActiveSession()` before stopping the service
