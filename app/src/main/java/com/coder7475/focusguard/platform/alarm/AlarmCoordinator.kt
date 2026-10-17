package com.coder7475.focusguard.platform.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

open class AlarmCoordinator(private val context: Context) {

    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    open fun scheduleSessionCompletion(triggerEpochMs: Long, sessionId: String) {
        val intent = Intent(context, SessionAlarmReceiver::class.java).apply {
            action = ACTION_COMPLETE_SESSION
            putExtra(EXTRA_SESSION_ID, sessionId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SESSION_COMPLETION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(triggerEpochMs, pendingIntent)
        Log.d(TAG, "Scheduled session completion alarm at epoch: $triggerEpochMs for session: $sessionId")
    }

    open fun scheduleSessionStart(triggerEpochMs: Long, scheduleId: String) {
        val intent = Intent(context, SessionAlarmReceiver::class.java).apply {
            action = ACTION_START_SCHEDULED_SESSION
            putExtra(EXTRA_SCHEDULE_ID, scheduleId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SCHEDULE_START + scheduleId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(triggerEpochMs, pendingIntent)
        Log.d(TAG, "Scheduled schedule start alarm at epoch: $triggerEpochMs for schedule: $scheduleId")
    }

    open fun cancelSessionCompletion(sessionId: String) {
        val intent = Intent(context, SessionAlarmReceiver::class.java).apply {
            action = ACTION_COMPLETE_SESSION
            putExtra(EXTRA_SESSION_ID, sessionId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SESSION_COMPLETION,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled session completion alarm for session: $sessionId")
        }
    }

    private fun scheduleExact(triggerEpochMs: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMs,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMs,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission denied, falling back to setAndAllowWhileIdle", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerEpochMs,
                pendingIntent
            )
        }
    }

    companion object {
        private const val TAG = "AlarmCoordinator"
        const val ACTION_COMPLETE_SESSION = "com.coder7475.focusguard.action.COMPLETE_SESSION"
        const val ACTION_START_SCHEDULED_SESSION = "com.coder7475.focusguard.action.START_SCHEDULED_SESSION"
        const val EXTRA_SESSION_ID = "extra_session_id"
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"

        const val REQUEST_CODE_SESSION_COMPLETION = 1001
        const val REQUEST_CODE_SCHEDULE_START = 2000
    }
}
