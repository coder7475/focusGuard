## Why

The current recurring schedule creation interface uses raw 24-hour integer inputs without AM/PM selection, creating friction and confusion for users accustomed to standard 12-hour time formats (e.g. distinguishing between 9:00 AM vs 9:00 PM). Providing an intuitive 12-hour hour/minute selector with explicit AM/PM toggle switches ensures error-free routine scheduling for work hours, study blocks, and overnight sleep locks.

## What Changes

- Update `AddScheduleDialog` and `TimePickerSelector` in `SchedulesScreen` to support 12-hour time selection (hours 1–12, minutes 0–55) with explicit AM and PM selection buttons.
- Automatically convert 12-hour AM/PM selections into total minute-of-day integers (0–1439) for compatibility with `ScheduleEngine` and Room database entities.
- Update `ScheduleCard` and schedule list displays to present formatted 12-hour AM/PM timestamps (e.g. `09:00 AM – 05:00 PM`, `10:30 PM – 07:00 AM`).
- Maintain full backward compatibility with existing stored 24-hour total minute integer offsets (`startMinuteOfDay`, `endMinuteOfDay`).

## Capabilities

### New Capabilities
<!-- None: all changes fit within existing schedule management capability -->

### Modified Capabilities
- `schedule-management`: Enhance routine time configuration with 12-hour AM/PM selectors and 12-hour formatted time range presentation.

## Impact

- **UI Layer:** `SchedulesScreen.kt` (`AddScheduleDialog`, `TimePickerSelector`, `ScheduleCard`).
- **Domain / Utilities:** Formatting utility function for converting minute-of-day offsets to localized 12-hour AM/PM strings.
- **Data Layer:** Zero database schema migrations needed; internal representation remains `startMinuteOfDay` / `endMinuteOfDay` (0–1439).
