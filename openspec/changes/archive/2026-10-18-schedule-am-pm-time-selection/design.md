## Context

FocusGuard allows users to create recurring schedules (routines) that automatically engage the focus lock during predefined windows (e.g., bedtime or workday). The current implementation in `SchedulesScreen.kt` uses a 24-hour integer spinner (`0..23`) without AM/PM differentiation. Users frequently misconfigure routines (such as setting 9:00 instead of 21:00 for evening study blocks) due to the lack of 12-hour AM/PM controls.

## Goals / Non-Goals

**Goals:**
- Provide a dual-mode 12-hour time picker in `AddScheduleDialog` with explicit `AM` and `PM` segmented selector buttons.
- Format schedule cards to display clear 12-hour timestamps with AM/PM indicators (e.g., `09:00 AM – 05:00 PM` and `10:30 PM – 07:00 AM`).
- Seamlessly convert between 12-hour AM/PM inputs and the internal 24-hour total minute-of-day integer format (`0..1439`) used by Room and `ScheduleEngine`.
- Maintain industrial hardware aesthetic with high-contrast Safety Orange selection indicators.

**Non-Goals:**
- Modifying the underlying SQLite Room schema (entities remain `startMinuteOfDay` and `endMinuteOfDay` as integers).
- Changing the alarm scheduling engine or overnight calculation algorithms.

## Decisions

### 1. 12-Hour AM/PM Segmented Control
* **Choice:** Split the time picker component into:
  - Hour selector: values `1` through `12`.
  - Minute selector: values `00` through `55` (step 5) or direct.
  - AM/PM toggle: horizontal segmented switch with tactile haptic feedback.
* **Rationale:** A segmented AM/PM toggle is immediately obvious and eliminates ambiguous 24-hour mental conversions for users.
* **Conversion Formula:**
  $$\text{hour}_{24} = \begin{cases} 0 & \text{if hour}_{12} = 12 \text{ and AM} \\ \text{hour}_{12} & \text{if hour}_{12} < 12 \text{ and AM} \\ 12 & \text{if hour}_{12} = 12 \text{ and PM} \\ \text{hour}_{12} + 12 & \text{if hour}_{12} < 12 \text{ and PM} \end{cases}$$
  $$\text{totalMinuteOfDay} = \text{hour}_{24} \times 60 + \text{minute}$$

### 2. Time Range Display Formatting
* **Choice:** Provide a dedicated formatter function `formatMinuteOfDay(totalMinutes: Int): String` that converts any integer between 0 and 1439 into `hh:mm a` format (e.g. `09:00 AM`, `10:30 PM`).
* **Rationale:** Standardizes display across `ScheduleCard`, `AddScheduleDialog`, and notifications without duplicating string formatting logic.

## Risks / Trade-offs

- **[Risk] Midnight & Noon edge cases:** 12:00 AM is 00:00 (start of day), while 12:00 PM is 12:00 (noon).
  $\rightarrow$ **Mitigation:** Write comprehensive unit test assertions covering 12:00 AM, 12:00 PM, 11:59 PM, and overnight transitions.
