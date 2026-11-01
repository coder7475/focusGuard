package com.coder7575.sleepguard.enforcer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmCoordinator {
    const val ACTION_SESSION_EXPIRED = "com.coder7575.sleepguard.ACTION_SESSION_EXPIRED"
    private const val REQUEST_CODE_EXPIRED = 999

    fun scheduleSessionCompletion(context: Context, triggerEpochMs: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SESSION_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_EXPIRED,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerEpochMs,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
        }
    }

    fun cancelSessionCompletion(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SESSION_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_EXPIRED,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
