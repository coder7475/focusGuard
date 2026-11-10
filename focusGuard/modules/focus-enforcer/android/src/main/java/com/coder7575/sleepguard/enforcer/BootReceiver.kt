package com.coder7575.sleepguard.enforcer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            val activeTarget = NativeDbHelper(context).getActiveSessionEndsAt()
            if (activeTarget > System.currentTimeMillis()) {
                FocusAccessibilityService.activeSessionEndsAt = activeTarget
                FocusForegroundService.start(context, activeTarget)
                AlarmCoordinator.scheduleSessionCompletion(context, activeTarget)
            }
            ScheduleCoordinator.evaluateAndScheduleNext(context)
        }
    }
}
