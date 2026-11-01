package com.coder7575.sleepguard.enforcer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == AlarmCoordinator.ACTION_SESSION_EXPIRED) {
            FocusForegroundService.stop(context)
        }
    }
}
