## Context

FocusGuard provides rock-solid enforcement of focus sessions, but its user interface currently lacks the tactile delight and industrial design described in the reverse-engineering plan (`doc/design/BLOCKIT_IMPLEMENTATION_PLAN.md`). Moreover, while `ScheduleEngine` and Room's `ScheduleDao` exist in the data/domain layer, there is no user-facing screen or background coordinator to create, edit, or execute recurring schedules (e.g. Bedtime Routine 22:30–07:00 or Work Routine Mon–Fri 09:00–17:00).

## Goals / Non-Goals

**Goals:**
- Implement a custom Jetpack Compose `RotaryDialTimer` canvas component with polar angle gesture tracking and haptic detent feedback.
- Deliver a dedicated routines management UI (`SchedulesScreen.kt`) with day-of-week bitmask selectors, time picker dialogs, active toggles, and deletion.
- Build an automated schedule execution pipeline (`ScheduleAlarmReceiver` + `AlarmCoordinator`) to automatically start and end sessions for active routines.
- Overhaul the visual design system (`Color.kt`, `Theme.kt`, `HomeScreen.kt`, `LockOverlayScreen.kt`) to match the Teenage Engineering / Braun industrial hardware aesthetic.
- Preserve 100% of existing unit tests and database integrity.

**Non-Goals:**
- Cloud synchronization of routines.
- Complex calendar integration (Google Calendar / Outlook sync).
- In-app purchases or paywalled features.

## Decisions

### 1. Rotary Dial Architecture & Polar Math
- **Component**: `RotaryDialTimer.kt` rendered via Compose `Canvas` with `Modifier.pointerInput(Unit) { detectDragGestures { ... } }`.
- **Math**:
  - Calculate angle $\theta = \operatorname{atan2}(y - center_y, x - center_x)$.
  - Normalize to clock angle starting at 12 o'clock ($-\pi/2$ radians): $\text{angleDeg} = ((\text{deg} + 90 + 360) \pmod{360})$.
  - Map degrees to minutes from a pre-defined discrete progression (e.g., 0 to 180 minutes).
  - Whenever the mapped discrete step changes, invoke `hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)` to simulate physical mechanical detents.
- **Rendering**:
  - Background circle with dark gunmetal fill (`#1C1D21`) and outer 1px border (`#2A2B30`).
  - Graduated radial tick marks every 15 degrees.
  - Active sweep arc drawn with `drawArc` in illuminated Safety Orange (`#FF5500`).
  - Center indicator displaying large monospaced digits and duration label.

### 2. Teenage Engineering Inspired Design System
- **Colors**:
  - Background: Matte Charcoal `#121214`
  - Surface / Cards: Dark Gunmetal `#1C1D21`
  - Outline / Dividers: `#2A2B30`
  - Primary Accent: Safety Orange `#FF5500`
  - Amber Warning / Presets: `#FFAA00`
  - Cyan Highlights: Electric Cyan `#00E5FF`
  - Text: Off-white `#F2F3F5` / Slate Muted `#94A3B8`
- **Typography & Layout**: Monospaced numbers, uppercase tracking labels (`letterSpacing = 1.5.sp`), high-contrast segmented pill switches.

### 3. Schedules Management UI
- **Navigation**: Dual-mode segment switch at the top of `HomeScreen` allowing seamless toggling between **FOCUS DIAL** and **ROUTINES**.
- **Routines Screen**:
  - Displays list of configured recurring schedules observed from `RoomScheduleRepository.observeAllSchedules()`.
  - Shows schedule name, time range (formatted as `HH:mm – HH:mm`), active day-of-week badges (M, T, W, T, F, S, S), and an active toggle switch.
  - Floating Action Button / Add Routine button opening a modal dialog with name input, start/end time selectors, and weekday chip toggles.

### 4. Background Schedule Alarm Coordinator & Receiver
- **Coordinator**: Extend `AlarmCoordinator` to compute the next transition (start or end) of all enabled schedules using `ScheduleEngine` and schedule exact alarms with `AlarmManager.setExactAndAllowWhileIdle()`.
- **Receiver**: `ScheduleAlarmReceiver` listening for `ACTION_SCHEDULE_START` and `ACTION_SCHEDULE_END`:
  - When `ACTION_SCHEDULE_START` fires: starts `FocusGuardService` with the calculated duration and creates an active session in Room.
  - When `ACTION_SCHEDULE_END` fires: completes active session, stops `FocusGuardService`, and schedules the next recurrence.

### 5. Lock Overlay Modernization
- Update `LockOverlayScreen` to use Matte Charcoal `#121214` background, illuminated Safety Orange countdown typography, and physical-styled emergency dialer action button.

## Risks / Trade-offs

- **[Risk] OEM Background battery optimization killing scheduled alarms**:
  - *Mitigation*: Use `AlarmManager.setExactAndAllowWhileIdle()` and re-arm schedules on device boot in `BootCompletedReceiver`.
- **[Risk] Dial gesture conflicts with parent scroll container**:
  - *Mitigation*: Place the rotary dial in a fixed vertical area that handles its own touch gestures without parent scroll interference.
- **[Risk] Multiple overlapping schedule windows**:
  - *Mitigation*: `ScheduleEngine` checks if any schedule is currently active; if a session is already running, it extends or maintains the active session without creating duplicate concurrent sessions.
