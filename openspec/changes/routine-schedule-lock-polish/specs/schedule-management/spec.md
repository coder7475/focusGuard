# Spec Delta

## MODIFIED Requirements

### Requirement: Recurring Routine Configuration
The system SHALL provide a dedicated user interface allowing users to create, inspect, edit, toggle, and delete recurring focus schedules backed by persistent SQLite storage, with 12-hour AM/PM time selection and formatted timestamp displays.

#### Scenario: Create new recurring schedule with 12-hour AM/PM selection
- **WHEN** user specifies a name, 12-hour start time with AM/PM indicator, 12-hour end time with AM/PM indicator, and enabled days of week
- **THEN** the system MUST correctly convert the 12-hour times to 24-hour minute-of-day offsets, save the `FocusSchedule` entity to Room, and arm the corresponding alarms

#### Scenario: Edit existing recurring schedule
- **WHEN** user selects an existing schedule card to edit and updates its name, time boundaries, or active days
- **THEN** the system MUST open a pre-populated edit dialog, validate the updated configuration, update the entity in Room, and refresh active alarms accordingly

#### Scenario: Toggle schedule active state
- **WHEN** user toggles the enable/disable switch on a schedule card
- **THEN** the system MUST update `isEnabled` in Room and arm or disarm the corresponding exact alarms immediately

#### Scenario: Delete recurring schedule
- **WHEN** user requests deletion of an existing routine
- **THEN** the system MUST present a confirmation dialog to verify intent before removing the record from Room and cancelling pending alarms

#### Scenario: Display formatted AM/PM timestamps on schedule cards
- **WHEN** existing schedules are rendered in the routine list
- **THEN** `ScheduleCard` MUST format the start and end minute-of-day values as 12-hour timestamps with explicit AM/PM suffixes (e.g. `09:00 AM – 05:00 PM`)

### Requirement: 12-Hour AM/PM Time Picker Selector
The system SHALL provide a 12-hour time picker selector component with discrete hour (1–12), custom minute input (0–59), and segmented AM/PM controls.

#### Scenario: Toggle between AM and PM periods
- **WHEN** user clicks the AM or PM segment in the time picker
- **THEN** the selector MUST immediately reflect the active period in Safety Orange styling and update the calculated minute-of-day offset

#### Scenario: Direct custom minute entry
- **WHEN** user selects or enters a custom minute value (0–59) in the time picker
- **THEN** the selector MUST accept arbitrary minute values rather than strictly locking to 5-minute multiples and recalculate the schedule window

#### Scenario: Handle 12:00 AM midnight conversion
- **WHEN** user selects hour 12, minute 0, and AM
- **THEN** the calculated minute-of-day MUST equal 0

#### Scenario: Handle 12:00 PM noon conversion
- **WHEN** user selects hour 12, minute 0, and PM
- **THEN** the calculated minute-of-day MUST equal 720 (12 * 60)
