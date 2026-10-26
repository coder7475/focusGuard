# ADR-002: Persistent Wall-Clock Epoch State and Boot Reconciliation

## Status
Accepted

## Context
A critical vulnerability in digital wellbeing and focus timer apps is state volatility:
1. Users reboot their device to escape an active lock screen.
2. Aggressive OEM task killers (such as Xiaomi MIUI, OnePlus OxygenOS, Samsung OneUI) silently kill background processes during screen-off or deep Doze states.
3. In-memory counters (e.g. coroutine `delay(1000)` loops, countdown integers in `ViewModel` or `Service`) reset or drift upon process restart.

If the lock state is maintained only in memory or relies on relative ticks (e.g., "30 minutes left"), a process restart either drops the lock entirely or resets the duration, punishing or frustrating the user.

## Decision
We enforce that the single source of truth for active session state is **Room database persistence storing absolute wall-clock epoch timestamps (`endsAtEpochMs`)**.

1. When a session starts:
   - Compute $\text{endsAtEpochMs} = \text{System.currentTimeMillis()} + \text{durationMs}$.
   - Atomically persist the record in Room before displaying any lock UI.
   - Register an exact OS wake alarm for `endsAtEpochMs`.
2. Dynamic Countdown Derivation:
   - Remaining time is always derived: $\text{remainingMs} = \max(0L, \text{endsAtEpochMs} - \text{System.currentTimeMillis()})$.
   - The Foreground Service countdown tick is strictly a presentation projection for the notification and overlay UI, not a state-keeping engine.
3. Boot & Crash Reconciliation:
   - A `BroadcastReceiver` listening for `ACTION_BOOT_COMPLETED` and `ACTION_LOCKED_BOOT_COMPLETED` immediately queries the Room database upon device startup.
   - If $\text{currentTime} < \text{endsAtEpochMs}$, the session immediately resumes enforcement and reschedules alarms.
   - If $\text{currentTime} \ge \text{endsAtEpochMs}$, the session is marked `COMPLETED` and a session-complete notification is posted.

## Consequences
### Positive (What becomes easier):
- **Impenetrable Reboot Resistance:** Rebooting the phone does not cancel the session. The user wakes up with the lock immediately re-engaged with mathematically exact remaining time.
- **Process Death Immunity:** If the OS kills `FocusGuardService` or `MainActivity`, the next accessibility event or alarm automatically re-synchronizes from SQLite with zero state corruption.
- **Architectural Determinism:** UI components, background services, and accessibility overlays all bind to the same reactive Room `Flow<ActiveSessionRecord?>`.

### Negative / Trade-Offs (What becomes harder):
- **Clock Tampering Surface:** If a user manually alters the system time in device settings (e.g., advancing the date by one day), wall-clock calculations could prematurely expire.
  - *Mitigation:* We record `SystemClock.elapsedRealtime()` at session start and listen to `ACTION_TIME_CHANGED` broadcasts to flag potential clock drift and fall back to monotonic elapsed time.
- **Disk I/O on Session Start:** Starting a session requires a blocking SQLite write before showing the overlay, adding ~5–15 ms compared to pure in-memory state.
