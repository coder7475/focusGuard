## 1. Design System & Industrial Hardware Tokens

- [x] 1.1 In `Color.kt` and `Theme.kt`, define Matte Charcoal (`#121214`), Dark Gunmetal (`#1C1D21`), Border Dark (`#2A2B30`), Safety Orange (`#FF5500`), Signal Amber (`#FFAA00`), Electric Cyan (`#00E5FF`), and Off-White (`#F2F3F5`).
- [x] 1.2 In `Type.kt`, configure industrial typography with monospace figures and tracked uppercase labels.

## 2. Tactile Rotary Dial Timer Component

- [x] 2.1 Create `RotaryDialTimer.kt` with polar angle gesture tracking (`atan2`), discrete detent snapping, illuminated Safety Orange progress arc, and haptic feedback.
- [x] 2.2 Add physical-style preset switch buttons (15m, 25m Pomodoro, 45m Deep Work, 60m Ultra) below the dial.

## 3. Recurring Schedule / Routines UI

- [x] 3.1 Create `SchedulesScreen.kt` displaying configured routines with time range, active weekday badges, and instant toggle switches.
- [x] 3.2 Implement `AddScheduleDialog.kt` supporting routine name, start/end time pickers (with overnight support), and weekday bitmask selection.
- [x] 3.3 Integrate routine persistence, enabling/disabling, and deletion with `RoomScheduleRepository`.

## 4. Background Schedule Alarm Coordinator & Execution

- [x] 4.1 In `AlarmCoordinator.kt`, implement schedule calculation and alarm arming for routine start and termination using `ScheduleEngine`.
- [x] 4.2 Create `ScheduleAlarmReceiver.kt` to handle `ACTION_SCHEDULE_START` and `ACTION_SCHEDULE_END` by managing `FocusGuardService` and Room active sessions.
- [x] 4.3 Register `ScheduleAlarmReceiver` in `AndroidManifest.xml` and re-arm enabled schedules in `BootCompletedReceiver.kt`.

## 5. UI Overhaul & Lock Overlay Polish

- [x] 5.1 Refactor `HomeScreen.kt` into an industrial dual-panel dashboard featuring the tactile Rotary Focus Dial, active session visualizer, and Routines tab.
- [x] 5.2 Refresh `LockOverlayScreen.kt` with Matte Charcoal background, Safety Orange typography, and industrial emergency dialer button.

## 6. Testing & Verification

- [x] 6.1 Add unit tests verifying `ScheduleEngine` overnight calculation, day bitmasks, and schedule alarm trigger logic.
- [x] 6.2 Execute `./gradlew test` to verify all unit and integration tests pass cleanly without regressions.
