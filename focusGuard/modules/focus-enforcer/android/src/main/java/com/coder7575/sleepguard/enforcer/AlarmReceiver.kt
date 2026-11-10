package com.coder7575.sleepguard.enforcer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            AlarmCoordinator.ACTION_SESSION_EXPIRED -> {
                FocusForegroundService.stop(context)
                FocusAccessibilityService.clearActiveSession()
                NativeDbHelper(context).markActiveSessionsCompleted()
                ScheduleCoordinator.evaluateAndScheduleNext(context)
            }
            ScheduleCoordinator.ACTION_SCHEDULE_TRIGGER -> {
                ScheduleCoordinator.evaluateAndScheduleNext(context)
            }
        }
    }
}
