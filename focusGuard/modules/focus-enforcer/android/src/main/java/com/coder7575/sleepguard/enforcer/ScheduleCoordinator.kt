package com.coder7575.sleepguard.enforcer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

object ScheduleCoordinator {
    const val ACTION_SCHEDULE_TRIGGER = "com.coder7575.sleepguard.ACTION_SCHEDULE_TRIGGER"
    private const val REQUEST_CODE_SCHEDULE = 998
    private const val TAG = "ScheduleCoordinator"

    fun getDayOfWeek(cal: Calendar): Int {
        val day = cal.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Monday = 2, ..., Saturday = 7
        return if (day == Calendar.SUNDAY) 7 else day - 1
    }

    fun isOvernight(sched: NativeSchedule): Boolean {
        val startMin = sched.startHour * 60 + sched.startMinute
        val endMin = sched.endHour * 60 + sched.endMinute
        return endMin <= startMin
    }

    fun isInsideScheduleWindow(sched: NativeSchedule, cal: Calendar): Boolean {
        if (!sched.isEnabled) return false

        val currentMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val today = getDayOfWeek(cal)
        val yesterday = if (today == 1) 7 else today - 1

        val startMin = sched.startHour * 60 + sched.startMinute
        val endMin = sched.endHour * 60 + sched.endMinute

        return if (!isOvernight(sched)) {
            sched.daysOfWeek.contains(today) && currentMin >= startMin && currentMin < endMin
        } else {
            val startedToday = sched.daysOfWeek.contains(today) && currentMin >= startMin
            val startedYesterday = sched.daysOfWeek.contains(yesterday) && currentMin < endMin
            startedToday || startedYesterday
        }
    }

    fun computeActiveWindowEnd(sched: NativeSchedule, cal: Calendar): Long? {
        if (!isInsideScheduleWindow(sched, cal)) return null

        val currentMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val startMin = sched.startHour * 60 + sched.startMinute

        val endCal = cal.clone() as Calendar
        endCal.set(Calendar.HOUR_OF_DAY, sched.endHour)
        endCal.set(Calendar.MINUTE, sched.endMinute)
        endCal.set(Calendar.SECOND, 0)
        endCal.set(Calendar.MILLISECOND, 0)

        if (isOvernight(sched) && currentMin >= startMin) {
            endCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return endCal.timeInMillis
    }

    fun computeNextStart(sched: NativeSchedule, cal: Calendar): Long? {
        if (!sched.isEnabled) return null

        val nowMs = cal.timeInMillis
        for (dayOffset in 0..7) {
            val candidateCal = cal.clone() as Calendar
            candidateCal.add(Calendar.DAY_OF_YEAR, dayOffset)
            candidateCal.set(Calendar.HOUR_OF_DAY, sched.startHour)
            candidateCal.set(Calendar.MINUTE, sched.startMinute)
            candidateCal.set(Calendar.SECOND, 0)
            candidateCal.set(Calendar.MILLISECOND, 0)

            val candidateDay = getDayOfWeek(candidateCal)
            if (sched.daysOfWeek.contains(candidateDay)) {
                if (candidateCal.timeInMillis > nowMs) {
                    return candidateCal.timeInMillis
                }
            }
        }
        return null
    }

    fun evaluateAndScheduleNext(context: Context) {
        val dbHelper = NativeDbHelper(context)
        val enabledSchedules = dbHelper.getEnabledSchedules()
        val nowCal = Calendar.getInstance()
        val nowMs = nowCal.timeInMillis

        var earliestNextTrigger: Long? = null

        for (sched in enabledSchedules) {
            if (isInsideScheduleWindow(sched, nowCal)) {
                val windowEnd = computeActiveWindowEnd(sched, nowCal)
                if (windowEnd != null && windowEnd > nowMs) {
                    // Activate lock session if not already running for this or longer window
                    val currentActive = dbHelper.getActiveSessionEndsAt()
                    if (currentActive < windowEnd) {
                        val sessionId = "routine_${sched.id}_${nowMs}"
                        val durationMs = windowEnd - nowMs
                        dbHelper.recordSessionActive(sessionId, nowMs, windowEnd, durationMs, "SCHEDULED")
                        FocusAccessibilityService.activeSessionEndsAt = windowEnd
                        FocusForegroundService.start(context, windowEnd)
                        AlarmCoordinator.scheduleSessionCompletion(context, windowEnd)
                        Log.d(TAG, "Activated scheduled routine '${sched.name}' until $windowEnd")
                    }

                    // Window end is a candidate for the next trigger point to re-evaluate schedules
                    if (earliestNextTrigger == null || windowEnd < earliestNextTrigger) {
                        earliestNextTrigger = windowEnd
                    }
                }
            } else {
                val nextStart = computeNextStart(sched, nowCal)
                if (nextStart != null && nextStart > nowMs) {
                    if (earliestNextTrigger == null || nextStart < earliestNextTrigger) {
                        earliestNextTrigger = nextStart
                    }
                }
            }
        }

        if (earliestNextTrigger != null) {
            scheduleAlarm(context, earliestNextTrigger)
        } else {
            cancelAlarm(context)
        }
    }

    private fun scheduleAlarm(context: Context, triggerEpochMs: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SCHEDULE_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SCHEDULE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMs,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
            }
            Log.d(TAG, "Next routine trigger scheduled at $triggerEpochMs")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule routine alarm", e)
        }
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SCHEDULE_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SCHEDULE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled routine alarm")
    }
}
