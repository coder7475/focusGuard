# Design

## Context

FocusGuard enforces focus discipline via full-screen overlay locking and automated recurring routines. While the core background service, Room persistence, and AlarmManager scheduling operate reliably, the user experience suffers from significant gaps:
- Creating routines relies on tap-cycling number spinners locked to 5-minute multiples with no arbitrary minute entry.
- Existing routines cannot be edited once created; deleting routines occurs instantly via raw `runBlocking` without confirmation.
- The lock overlay view lacks context, displaying only a raw countdown timer without routine identification, timeframe context, or visual session progress.

See `proposal.md` for user problem statements and `specs/` for normative behavioral requirements.

## Goals / Non-Goals

**Goals:**
- Provide precise custom minute and hour input for recurring routines with intuitive AM/PM controls and duration previews.
- Support full editing of existing routines (name, start time, end time, active days) with immediate alarm recalculation.
- Add safe deletion confirmation for routines with non-blocking asynchronous database and alarm cancellation.
- Elevate `LockOverlayScreen` into an informative, polished cyber-discipline dashboard displaying routine name, schedule window, progress bar, and remaining countdown.
- Ensure all UI flows conform to the tactile hardware aesthetic (Matte Charcoal `#121214`, Dark Gunmetal `#1A1A1E`, Safety Orange `#FF5500`, Electric Cyan `#00E5FF`).

**Non-Goals:**
- Schema changes to Room database tables (`FocusScheduleEntity` and `FocusSessionEntity` already contain all necessary columns).
- Modifying underlying alarm receiver scheduling logic in `AlarmCoordinator`.
- Adding notification shade custom controls or third-party time picker libraries (retaining pure Compose implementation without heavy external dependencies).

## Decisions

### 1. Unified Schedule Dialog Component for Create and Edit
- **Decision**: Create a reusable `ScheduleConfigDialog` (or paired `AddScheduleDialog` / `EditScheduleDialog`) that accepts an initial `FocusSchedule?` and outputs updated configuration.
- **Rationale**: Eliminates duplication between creation and editing logic. Ensures identical validation, time conversion, and active day toggling.
- **Alternatives considered**: Separate disparate modal sheets; rejected due to maintenance overhead and potential diverging UX.

### 2. Custom Minute & Hour Picker with Direct Numeric Input and Steppers
- **Decision**: Provide a dual-mode time picker supporting both quick adjustment buttons (+/- 1, +/- 5) and direct numeric text input (0–59 minutes, 1–12 hours), alongside prominent AM/PM segment toggles and real-time total duration calculation.
- **Rationale**: Solves the "no custom time" limitation cleanly while maintaining fast thumb ergonomics on mobile.
- **Alternatives considered**: Android system `TimePickerDialog` (inconsistent across OEMs and breaks custom dark cyber design language); continuous wheel sliders (fiddly to hit exact minute values).

### 3. Safe Asynchronous Routine Management in UI
- **Decision**: Migrate all schedule updates and deletions in `SchedulesScreen` from `runBlocking` to Compose `rememberCoroutineScope()` with `Dispatchers.IO` dispatch.
- **Rationale**: `runBlocking` on the Android main thread causes micro-stutters and risks ANR dialogs.
- **Alternatives considered**: ViewModel-based state management; `SchedulesScreen` already receives `ScheduleRepository` directly in line with current app architecture, so coroutine scope launch preserves consistency.

### 4. Rich Context State Propagation to Lock Overlay
- **Decision**: Expand `LockOverlayWindowManager` to accept and observe active session context (session type, routine name, total duration, start/end timestamps), supplied reactively by `FocusGuardAccessibilityService` and `FocusGuardService`.
- **Rationale**: Gives `LockOverlayScreen` the state needed to display whether the lock is a routine (with its specific name and schedule window) or a quick focus session, plus compute progress percentages.
- **Alternatives considered**: Polling Room inside the overlay composable; rejected because `FocusGuardAccessibilityService` already maintains reactive database observation and centralizes window lifecycle.

## Risks / Trade-offs

- **[Risk] Overnight Routine Duration Calculation**: Calculating progress and duration for routines that span midnight (e.g. 22:00 to 07:00) can result in negative numbers if simple subtraction is used.
  - *Mitigation*: Use modular arithmetic: `val totalMinutes = if (endMin >= startMin) endMin - startMin else (1440 - startMin) + endMin`.
- **[Risk] State Desync on Edit**: Editing a routine that is currently active could cause alarm cancellation conflicts.
  - *Mitigation*: Updating a schedule updates Room and re-invokes `alarmCoordinator.scheduleRecurringRoutine(updated)` which replaces pending intents cleanly via unique schedule request codes.
