package com.coder7475.focusguard.platform.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.coder7475.focusguard.data.local.FocusGuardDatabase
import com.coder7475.focusguard.data.repository.RoomSessionRepository
import com.coder7475.focusguard.domain.engine.ReconciliationResult
import com.coder7475.focusguard.domain.engine.SessionEngine
import com.coder7475.focusguard.platform.alarm.AlarmCoordinator
import com.coder7475.focusguard.service.foreground.FocusGuardService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            return
        }

        Log.d(TAG, "Device booted ($action), initiating session state reconciliation")
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FocusGuardDatabase.getInstance(context)
                val sessionRepo = RoomSessionRepository(db.sessionDao())
                val sessionEngine = SessionEngine(sessionRepo)
                val alarmCoordinator = AlarmCoordinator(context)

                when (val result = sessionEngine.reconcileState()) {
                    is ReconciliationResult.StillActive -> {
                        Log.i(TAG, "Re-arming active session after reboot: id=${result.session.id}, remainingMs=${result.remainingMs}")
                        alarmCoordinator.scheduleSessionCompletion(result.session.endsAtEpochMs, result.session.id)
                        FocusGuardService.start(context, result.session.endsAtEpochMs)
                    }
                    is ReconciliationResult.CompletedExpired -> {
                        Log.i(TAG, "Expired session finalized upon reboot: id=${result.session.id}")
                        alarmCoordinator.cancelSessionCompletion(result.session.id)
                        FocusGuardService.showCompletionNotification(context)
                    }
                    is ReconciliationResult.NoActiveSession -> {
                        Log.d(TAG, "No active focus session requiring restoration after boot")
                    }
                }

                // Re-arm enabled recurring schedules
                val scheduleRepo = com.coder7475.focusguard.data.repository.RoomScheduleRepository(db.scheduleDao())
                val enabledSchedules = scheduleRepo.getEnabledSchedules()
                for (schedule in enabledSchedules) {
                    alarmCoordinator.scheduleRecurringRoutine(schedule)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during boot reconciliation", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}
