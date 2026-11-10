# Tasks

## 1. Custom Time Selection & Routine Configuration

- [x] 1.1 Upgrade `TimePickerSelector` in `ui/schedule/SchedulesScreen.kt` to support custom arbitrary minute entry (0–59), hour input (1–12), increment/decrement steppers, and reliable AM/PM toggling. Verify time-to-minute conversions handle arbitrary minutes and midnight/noon edge cases.
- [x] 1.2 Add live schedule window duration calculation and overnight indicator in the routine dialog (e.g., "7 hrs 30 mins • Overnight"). Verify overnight calculations correctly handle start times greater than end times.

## 2. Routine Edit, Deletion, and Asynchronous Operations

- [x] 2.1 Implement routine editing by creating `EditScheduleDialog` (or shared `ScheduleConfigDialog`) and wiring edit action on `ScheduleCard`. Verify clicking edit opens pre-populated dialog and saving persists changes to Room and updates alarms.
- [x] 2.2 Implement safe deletion confirmation dialog for routines before removing records from `ScheduleRepository`. Verify cancelling retains the routine and confirming deletes it asynchronously and cancels associated alarms.
- [x] 2.3 Remove all `runBlocking` calls from `SchedulesScreen.kt`, moving database mutations and alarm updates to `rememberCoroutineScope()` and `Dispatchers.IO`. Verify no UI freeze occurs during routine save, edit, toggle, or delete operations.

## 3. Informative Lock Overlay Screen

- [x] 3.1 Expand `LockOverlayWindowManager` to accept and maintain active session context (session type, routine name, scheduled timeframe, total duration). Verify context changes are reflected reactively on the overlay view.
- [x] 3.2 Update `FocusGuardAccessibilityService` to resolve and supply active routine metadata (routine name, start/end time window) when a scheduled focus session is active. Verify quick sessions vs routine sessions are properly distinguished.
- [x] 3.3 Redesign `LockOverlayScreen` to display an informative hardware-discipline dashboard: session type badge (`ROUTINE LOCK` vs `FOCUS LOCK`), routine name, timeframe summary, visual session progress bar, high-contrast countdown, and tactile emergency dialer button. Verify countdown and progress bar render accurately.

## 4. UI/UX Refinement & Verification

- [x] 4.1 Apply design polish to `SchedulesScreen` and `LockOverlayScreen` including industrial border strokes, typography hierarchy, consistent badge chips, and tactile haptic feedback on interactive controls. Verify visual coherence with `MatteCharcoal`, `DarkGunmetal`, `SafetyOrange`, and `ElectricCyan`.
- [x] 4.2 Run unit tests via `./gradlew testDebugUnitTest` and verify that all schedule management, time formatting, and session logic tests pass cleanly without regressions.
