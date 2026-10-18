package com.coder7475.focusguard.ui.home

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coder7475.focusguard.data.local.FocusGuardDatabase
import com.coder7475.focusguard.data.repository.RoomScheduleRepository
import com.coder7475.focusguard.data.repository.RoomSessionRepository
import com.coder7475.focusguard.domain.engine.SessionEngine
import com.coder7475.focusguard.domain.model.FocusSession
import com.coder7475.focusguard.domain.model.SessionStatus
import com.coder7475.focusguard.platform.alarm.AlarmCoordinator
import com.coder7475.focusguard.service.foreground.FocusGuardService
import com.coder7475.focusguard.ui.schedule.SchedulesScreen
import com.coder7475.focusguard.ui.theme.BorderDark
import com.coder7475.focusguard.ui.theme.DangerRed
import com.coder7475.focusguard.ui.theme.DarkGunmetal
import com.coder7475.focusguard.ui.theme.ElectricCyan
import com.coder7475.focusguard.ui.theme.MatteCharcoal
import com.coder7475.focusguard.ui.theme.OffWhite
import com.coder7475.focusguard.ui.theme.SafetyOrange
import com.coder7475.focusguard.ui.theme.SignalAmber
import com.coder7475.focusguard.ui.theme.TextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

enum class HomeTab {
    DIAL,
    ROUTINES
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val db = remember { FocusGuardDatabase.getInstance(context) }
    val sessionRepo = remember { RoomSessionRepository(db.sessionDao()) }
    val scheduleRepo = remember { RoomScheduleRepository(db.scheduleDao()) }
    val sessionEngine = remember { SessionEngine(sessionRepo) }
    val alarmCoordinator = remember { AlarmCoordinator(context) }

    val activeSession by sessionRepo.observeActiveSession().collectAsState(initial = null)
    val completedCount by sessionRepo.observeCompletedSessionCount().collectAsState(initial = 0)

    var currentTab by remember { mutableStateOf(HomeTab.DIAL) }
    var selectedMinutes by remember { mutableIntStateOf(25) }

    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMs = System.currentTimeMillis()
            delay(500L)
        }
    }

    val isSessionActive = activeSession != null &&
        activeSession?.status == SessionStatus.ACTIVE &&
        currentTimeMs < (activeSession?.endsAtEpochMs ?: 0L)

    val remainingMs = if (isSessionActive) {
        max(0L, (activeSession?.endsAtEpochMs ?: 0L) - currentTimeMs)
    } else 0L

    val totalDurationMs = if (isSessionActive) {
        activeSession?.durationMs ?: 0L
    } else 0L

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MatteCharcoal)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "FOCUSGUARD",
                            color = OffWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(if (isSessionActive) SafetyOrange else BorderDark, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isSessionActive) "LOCKED" else "STANDBY",
                                color = if (isSessionActive) MatteCharcoal else TextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Hardware Discipline Lock",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Completed Counter
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkGunmetal),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(ElectricCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$completedCount",
                            color = OffWhite,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Segment Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkGunmetal, RoundedCornerShape(8.dp))
                    .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                    .padding(3.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (currentTab == HomeTab.DIAL) SafetyOrange else Color.Transparent)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentTab = HomeTab.DIAL
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "FOCUS DIAL",
                        color = if (currentTab == HomeTab.DIAL) MatteCharcoal else TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (currentTab == HomeTab.ROUTINES) SafetyOrange else Color.Transparent)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentTab = HomeTab.ROUTINES
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "ROUTINES",
                        color = if (currentTab == HomeTab.ROUTINES) MatteCharcoal else TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content Panel
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "homeTabContent",
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    HomeTab.DIAL -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            RotaryDialTimer(
                                selectedMinutes = selectedMinutes,
                                onMinutesChanged = { selectedMinutes = it },
                                isActiveSession = isSessionActive,
                                remainingMs = remainingMs,
                                totalDurationMs = totalDurationMs
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action button
                            if (isSessionActive) {
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            activeSession?.let { current ->
                                                sessionEngine.completeActiveSession(current.id)
                                                alarmCoordinator.cancelSessionCompletion(current.id)
                                                FocusGuardService.stop(context)
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                ) {
                                    Text(
                                        text = "ABORT FOCUS SESSION",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        coroutineScope.launch {
                                            val durationMs = selectedMinutes * 60 * 1000L
                                            val result = sessionEngine.startQuickSession(durationMs)
                                            val session = result.getOrNull()
                                            if (session != null) {
                                                alarmCoordinator.scheduleSessionCompletion(
                                                    session.endsAtEpochMs,
                                                    session.id
                                                )
                                                FocusGuardService.start(context, session.endsAtEpochMs)
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                ) {
                                    Text(
                                        text = "ENGAGE FOCUS LOCK (${selectedMinutes}M)",
                                        color = MatteCharcoal,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    HomeTab.ROUTINES -> {
                        SchedulesScreen(
                            scheduleRepository = scheduleRepo,
                            onScheduleChanged = {
                                coroutineScope.launch {
                                    val enabled = scheduleRepo.getEnabledSchedules()
                                    enabled.forEach { alarmCoordinator.scheduleRecurringRoutine(it) }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
