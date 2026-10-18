## 1. UI Components: 12-Hour AM/PM Time Selector

- [x] 1.1 Implement `AmPmToggle` segmented button composable in `SchedulesScreen.kt` with tactile haptic feedback
- [x] 1.2 Refactor `TimePickerSelector` to accept 12-hour values (`1..12`), minute values, and `isAm` state
- [x] 1.3 Update `AddScheduleDialog` to convert between 12-hour AM/PM inputs and 24-hour total minute-of-day values

## 2. Formatters & Schedule Card Display

- [x] 2.1 Implement `formatMinuteOfDay(minuteOfDay: Int): String` helper converting total minutes to `hh:mm a` format
- [x] 2.2 Update `ScheduleCard` time range text to display formatted 12-hour AM/PM strings (e.g. `09:00 AM – 05:00 PM`)

## 3. Testing & Verification

- [x] 3.1 Add unit tests for 12-hour AM/PM to minute-of-day conversions covering 12:00 AM, 12:00 PM, and boundary minutes
- [x] 3.2 Add unit tests verifying `formatMinuteOfDay` output formatting
- [x] 3.3 Execute unit test suite via `./gradlew testDebugUnitTest` and ensure 100% pass rate
- [x] 3.4 Validate OpenSpec change status with `openspec validate`
