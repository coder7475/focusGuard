package com.coder7475.focusguard.service.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coder7475.focusguard.domain.model.SessionType
import com.coder7475.focusguard.ui.schedule.ScheduleTimeFormatter
import com.coder7475.focusguard.ui.theme.BorderDark
import com.coder7475.focusguard.ui.theme.DangerRed
import com.coder7475.focusguard.ui.theme.DarkGunmetal
import com.coder7475.focusguard.ui.theme.ElectricCyan
import com.coder7475.focusguard.ui.theme.MatteCharcoal
import com.coder7475.focusguard.ui.theme.OffWhite
import com.coder7475.focusguard.ui.theme.SafetyOrange
import com.coder7475.focusguard.ui.theme.TextMuted
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.max

@Composable
fun LockOverlayScreen(
    sessionInfoProvider: () -> LockSessionInfo,
    onEmergencyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMs = System.currentTimeMillis()
            delay(500L)
        }
    }

    val sessionInfo = sessionInfoProvider()
    val remainingMs = max(0L, sessionInfo.endsAtEpochMs - currentTimeMs)
    val remainingSeconds = remainingMs / 1000L
    val hours = remainingSeconds / 3600L
    val minutes = (remainingSeconds % 3600L) / 60L
    val seconds = remainingSeconds % 60L

    val formattedTime = if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    val totalDurationMs = sessionInfo.totalDurationMs
    val progress = if (totalDurationMs > 0) {
        ((totalDurationMs - remainingMs).toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val isRoutine = sessionInfo.sessionType == SessionType.SCHEDULED
    val badgeTitle = if (isRoutine) {
        "ROUTINE LOCK: ${sessionInfo.routineName?.uppercase() ?: "SCHEDULED"}"
    } else {
        "HARDWARE FOCUS LOCK"
    }
    val badgeColor = if (isRoutine) ElectricCyan else SafetyOrange

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MatteCharcoal)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val pointerEvent = awaitPointerEvent()
                        pointerEvent.changes.forEach { it.consume() }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            // Status Badge
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkGunmetal),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(badgeColor.copy(alpha = 0.5f))
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = badgeTitle,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // Scheduled Window Subtitle (if available)
            if (sessionInfo.timeRangeFormatted != null) {
                Text(
                    text = "WINDOW: ${sessionInfo.timeRangeFormatted}",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Countdown Display
            Text(
                text = formattedTime,
                color = OffWhite,
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1).sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "TIME REMAINING",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar & Duration Stats
            if (totalDurationMs > 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = badgeColor,
                        trackColor = DarkGunmetal,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ELAPSED: ${(progress * 100).toInt()}%",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        val totalMinutes = (totalDurationMs / 60000L).toInt()
                        Text(
                            text = "TOTAL: ${ScheduleTimeFormatter.formatDuration(totalMinutes)}",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Informative Discipline Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkGunmetal),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BorderDark)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DISCIPLINE BARRIER ACTIVE",
                        color = OffWhite,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Non-essential apps & notifications are strictly blocked.\nEmergency phone access remains unrestricted.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Emergency Call Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEmergencyClick()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRed,
                    contentColor = OffWhite
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "EMERGENCY CALLS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

/**
 * Convenience overload for callers providing only an endsAtEpochMs provider.
 */
@JvmName("LockOverlayScreenWithEndsAt")
@Composable
fun LockOverlayScreen(
    endsAtEpochMsProvider: () -> Long,
    onEmergencyClick: () -> Unit,
    modifier: Modifier = Modifier
) = LockOverlayScreen(
    sessionInfoProvider = { LockSessionInfo(endsAtEpochMs = endsAtEpochMsProvider()) },
    onEmergencyClick = onEmergencyClick,
    modifier = modifier
)
