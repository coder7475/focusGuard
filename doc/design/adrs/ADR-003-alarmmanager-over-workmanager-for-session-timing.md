# ADR-003: Timing Engine via AlarmManager Exact Alarms vs WorkManager

## Status
Accepted

## Context
FocusGuard requires two categories of timed triggers:
1. **Session Completion Boundary:** Waking the system precisely when an active focus session finishes, releasing the lock overlay, posting a completion notification, and updating history.
2. **Scheduled Window Boundaries:** Waking the system at specific scheduled start and end times (e.g. starting an overnight focus session at 20:00:00 and concluding at 08:00:00).

Android offers two primary background scheduling APIs:
- **`WorkManager`:** Recommended by Google for deferrable, persistent background work. However, `WorkManager` imposes a minimum periodic interval of 15 minutes, batches job execution to preserve battery, and does not support exact to-the-second execution. When the device enters Doze mode, `WorkManager` jobs may be delayed by tens of minutes.
- **`AlarmManager`:** Allows scheduling wakeups using `setExactAndAllowWhileIdle()`. These alarms bypass standard Doze maintenance windows and execute within milliseconds of the target epoch time. However, starting with Android 12 (API 31) and Android 14 (API 34), `SCHEDULE_EXACT_ALARM` is restricted and may require user authorization.

## Decision
We select **`AlarmManager` using `setExactAndAllowWhileIdle()`** for all session completion and schedule boundary triggers.

`WorkManager` will **not** be used for focus session boundaries.

1. **Permission Handling:**
   - On Android 12+, we check `AlarmManager.canScheduleExactAlarms()`.
   - If denied, we guide the user to the "Alarms & Reminders" special access screen during onboarding.
   - Fallback: If exact alarms are unavailable, the system uses `setAndAllowWhileIdle()` as a graceful degradation.
2. **Intent Mechanism:**
   - Alarms broadcast explicit intents to `SessionAlarmReceiver` using pending intents with `FLAG_IMMUTABLE`.

## Consequences
### Positive (What becomes easier):
- **Precision:** Sessions end exactly when the countdown reaches 00:00. The user is never trapped in a lock screen past their committed focus window due to OS batching.
- **Doze Survival:** Alarms registered with `allowWhileIdle` wake the device CPU even in deep sleep states, ensuring overnight schedules arm and disarm reliably.

### Negative / Trade-Offs (What becomes harder):
- **Permission Overhead:** Requires requesting the `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` permission and educating the user during onboarding.
- **Battery Scrutiny:** Overuse of exact alarms can impact battery; however, FocusGuard only schedules at most two alarms at any time (one for the active session end, one for the next recurring schedule boundary).
