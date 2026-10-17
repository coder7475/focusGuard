package com.coder7475.focusguard.platform.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.coder7475.focusguard.data.local.FocusGuardDatabase
import com.coder7475.focusguard.data.repository.RoomScheduleRepository
import com.coder7475.focusguard.data.repository.RoomSessionRepository
import com.coder7475.focusguard.domain.engine.ScheduleEngine
import com.coder7475.focusguard.domain.engine.SessionEngine
import com.coder7475.focusguard.service.foreground.FocusGuardService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class SessionAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d(TAG, "Received alarm broadcast action: $action")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FocusGuardDatabase.getInstance(context)
                val sessionRepo = RoomSessionRepository(db.sessionDao())
                val sessionEngine = SessionEngine(sessionRepo)

                when (action) {
                    AlarmCoordinator.ACTION_COMPLETE_SESSION -> {
                        val sessionId = intent.getStringExtra(AlarmCoordinator.EXTRA_SESSION_ID)
                        Log.d(TAG, "Completing session from alarm: $sessionId")
                        sessionEngine.completeActiveSession(sessionId)
                        FocusGuardService.stop(context)
                        FocusGuardService.showCompletionNotification(context)
                    }
                    AlarmCoordinator.ACTION_START_SCHEDULED_SESSION -> {
                        val scheduleId = intent.getStringExtra(AlarmCoordinator.EXTRA_SCHEDULE_ID) ?: return@launch
                        val scheduleRepo = RoomScheduleRepository(db.scheduleDao())
                        val schedule = scheduleRepo.getScheduleById(scheduleId)
                        if (schedule != null && schedule.isEnabled) {
                            val scheduleEngine = ScheduleEngine()
                            val now = ZonedDateTime.now()
                            val endZdt = scheduleEngine.computeActiveWindowEnd(schedule, now)
                            if (endZdt != null) {
                                val durationMs = endZdt.toInstant().toEpochMilli() - System.currentTimeMillis()
                                if (durationMs > 0) {
                                    val session = sessionEngine.startQuickSession(durationMs).getOrNull()
                                    if (session != null) {
                                        val alarmCoordinator = AlarmCoordinator(context)
                                        alarmCoordinator.scheduleSessionCompletion(session.endsAtEpochMs, session.id)
                                        FocusGuardService.start(context, session.endsAtEpochMs)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling alarm in SessionAlarmReceiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "SessionAlarmReceiver"
    }
}
