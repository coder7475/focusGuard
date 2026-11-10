## Why

FocusGuard's current interface relies on generic list-based quick-start cards and lacks scheduled routine automation (e.g. Bedtime or Work Focus windows). Drawing inspiration from "blockit" (`com.hypenet.focused`) and Teenage Engineering / Dieter Rams industrial design, users need a tactile, delightful rotary dial timer with haptic detents for quick sessions, alongside comprehensive recurring schedule management that automatically enforces focus windows unattended.

## What Changes

- **Teenage Engineering / Industrial Design System**: Refresh colors, cards, and typography to Matte Charcoal (`#121214`), Dark Gunmetal (`#1C1D21`), Safety Orange (`#FF5500`), Signal Amber (`#FFAA00`), and Electric Cyan (`#00E5FF`) with monospace metrics and physical borders.
- **Tactile Rotary Dial Timer**: Introduce a circular Canvas dial selector with polar angle touch tracking (`atan2`), tactile haptic feedback on detent crossing, discrete time steps (15m, 25m, 45m, 60m, 90m, 120m), illuminated orange progress arcs, and physical-switch preset buttons.
- **Recurring Schedule Timer / Routines Management**: Introduce full CRUD UI for recurring focus routines (e.g. Night Routine, Deep Work block) with start/end time pickers, active day-of-week bitmask toggles, overnight window handling, and automated `AlarmCoordinator` scheduling.
- **Automated Schedule Execution Coordinator**: Reconcile active schedule windows with `AlarmCoordinator` and a background receiver (`ScheduleAlarmReceiver`) so recurring windows automatically trigger `FocusGuardService` and lock overlays without manual user intervention.
- **Unified Industrial UI/UX Navigation**: Add an industrial top/bottom navigation or tab bar to seamlessly switch between the **Rotary Focus Dial** and **Routines / Schedules**, with an updated modern lock overlay.

## Capabilities

### New Capabilities

- `tactile-rotary-timer`: Tactile circular dial timer with polar gesture tracking, haptic feedback detents, preset toggles, and live countdown visualization.
- `schedule-management`: User-facing routines management with day-of-week bitmask selection, overnight time range picker, persistent schedule storage, and automated alarm triggers for recurring focus windows.

### Modified Capabilities

- `lock-overlay`: Updated visual language adopting the Teenage Engineering industrial aesthetic with Safety Orange accents, monospaced typography, and refined emergency access.

## Impact

- **UI Layer**:
  - `RotaryDialTimer.kt`: New custom circular dial composable with polar angle detection and haptics.
  - `SchedulesScreen.kt`: New routines management composable with schedule cards, active toggles, and creation/edit dialogs.
  - `HomeScreen.kt`: Integrated industrial dashboard housing the rotary dial, quick preset hardware buttons, and schedule overview.
  - `Theme.kt`, `Color.kt`, `Type.kt`: Updated color tokens and typography definitions.
  - `LockOverlayScreen.kt`: Themed to modern matte charcoal and safety orange styling.
- **Domain & Platform Layer**:
  - `ScheduleAlarmReceiver.kt`: Background broadcast receiver for handling scheduled routine start/end alarm events.
  - `AlarmCoordinator.kt`: Added methods for scheduling recurring routine alarms.
  - `ScheduleEngine.kt`: Connected to UI state and background alarm delivery.
- **Dependencies & Storage**: Utilizes existing Room database (`FocusGuardDatabase`, `ScheduleDao`, `FocusScheduleEntity`) with zero schema breakages.
