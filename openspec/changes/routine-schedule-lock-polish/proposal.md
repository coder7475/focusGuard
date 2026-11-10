# Proposal

## Why

FocusGuard users currently face friction and lack of control in managing focus routines:
1. Creating or adjusting routines does not allow flexible custom minute inputs (only 5-minute increment tap-cycling), making precise scheduling difficult.
2. The Routines tab does not support editing existing routines and lacks safe confirmation for deletion, forcing users to delete and recreate routines from scratch.
3. The Lock Overlay screen is generic and uninformative, showing only a raw countdown without indicating whether a scheduled routine or quick focus session is active, what routine is locking the device, or the routine window time boundaries.
4. Overall Routine management and Lock Overlay UI/UX require aesthetic and functional refinement to align with FocusGuard's tactile, hardware-inspired cyber discipline design language.

## What Changes

- **Custom & Precise Time Input for Routines**: Replace rigid 5-minute tap-cycling with direct custom minute/hour entry, flexible increment/decrement controls, intuitive AM/PM toggling, and calculated duration preview (e.g., "8h 30m").
- **Routine Edit Capability**: Allow users to tap any routine card or click an edit button to open an Edit Routine dialog pre-populated with current settings, modify routine name, start/end times, and active weekdays, and update persistence without breaking alarm schedules.
- **Safe Routine Deletion with Confirmation**: Add a confirmation dialog before deleting routines to prevent accidental data loss, executing cleanup asynchronously with proper alarm cancellation.
- **Informative Lock Overlay Screen**: Upgrade the lock overlay with rich contextual information:
  - Routine/Session identifier badge (e.g., "ROUTINE LOCK: Deep Work" vs "QUICK FOCUS LOCK").
  - Schedule window summary (e.g., "Scheduled: 09:00 AM – 05:00 PM").
  - Visual session progress bar and live remaining countdown.
  - Allowed tools indicator ("Emergency calls & critical utilities permitted").
- **Design & UI/UX Polish**:
  - Consistent industrial typography, badges, and card borders in Dark Gunmetal (`#1A1A1E`), Safety Orange (`#FF5500`), Electric Cyan (`#00E5FF`), and Matte Charcoal (`#121214`).
  - Haptic feedback on all time selections and actions.
  - Non-blocking coroutine execution replacing any synchronous blocking calls on UI threads.

## Capabilities

### Modified Capabilities
- `schedule-management`: Add routine editing, safe deletion confirmation, direct custom minute/hour time picker input, and routine duration previews.
- `lock-overlay`: Display contextual routine metadata (routine name, session type, scheduled window timeframe, and progress bar) on the overlay surface during active locks.

## Impact

- **UI Layer**:
  - `SchedulesScreen.kt`: Add `EditScheduleDialog`, delete confirmation dialog, enhanced `TimePickerSelector` with direct custom input, and clickable schedule cards.
  - `LockOverlayScreen.kt`: Accept session title, session type, schedule window timeframe, and progress calculation; render enriched information card.
  - `LockOverlayWindowManager.kt` & `FocusGuardService.kt`: Pass active session / routine metadata (or observe active session) to `LockOverlayScreen`.
- **Database / Repository**:
  - Uses existing `ScheduleRepository` (`saveSchedule`, `updateSchedule`, `deleteSchedule`, `observeAllSchedules`). No Room schema migration required.
- **Alarms / Engine**:
  - Ensure updated and deleted routines correctly update `AlarmCoordinator` recurring alarms.
