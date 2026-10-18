package com.coder7475.focusguard.ui.home

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coder7475.focusguard.ui.theme.BorderDark
import com.coder7475.focusguard.ui.theme.DarkGunmetal
import com.coder7475.focusguard.ui.theme.MatteCharcoal
import com.coder7475.focusguard.ui.theme.OffWhite
import com.coder7475.focusguard.ui.theme.SafetyOrange
import com.coder7475.focusguard.ui.theme.SignalAmber
import com.coder7475.focusguard.ui.theme.TextMuted
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val MAX_MINUTES = 120f

@Composable
fun RotaryDialTimer(
    selectedMinutes: Int,
    onMinutesChanged: (Int) -> Unit,
    isActiveSession: Boolean = false,
    remainingMs: Long = 0L,
    totalDurationMs: Long = 0L,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    // Current angle mapped from selectedMinutes
    val sweepFraction = if (isActiveSession && totalDurationMs > 0) {
        (remainingMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        (selectedMinutes / MAX_MINUTES).coerceIn(0f, 1f)
    }

    val animatedSweepAngle by animateFloatAsState(
        targetValue = sweepFraction * 360f,
        animationSpec = tween(durationMillis = if (isActiveSession) 200 else 100),
        label = "dialSweep"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(DarkGunmetal)
                .border(2.dp, BorderDark, CircleShape)
                .then(
                    if (!isActiveSession) {
                        Modifier.pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val touchPos = change.position
                                val rad = atan2(touchPos.y - center.y, touchPos.x - center.x)
                                var deg = Math.toDegrees(rad.toDouble()).toFloat() + 90f
                                if (deg < 0) deg += 360f

                                val rawMinutes = (deg / 360f) * MAX_MINUTES
                                // Snap to 5-minute detents
                                val snapped = ((rawMinutes / 5f).roundToInt() * 5).coerceIn(5, MAX_MINUTES.toInt())
                                if (snapped != selectedMinutes) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onMinutesChanged(snapped)
                                }
                            }
                        }
                    } else Modifier
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (size.minDimension / 2f) - 24.dp.toPx()
                val strokeWidth = 14.dp.toPx()

                // Draw tick marks around circumference
                val totalTicks = 60
                for (i in 0 until totalTicks) {
                    val angleDeg = (i * 360f / totalTicks) - 90f
                    val angleRad = (angleDeg * PI / 180f).toFloat()
                    val isMajor = i % 5 == 0
                    val tickLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                    val tickColor = if (isMajor) SignalAmber.copy(alpha = 0.8f) else BorderDark.copy(alpha = 0.6f)
                    val tickWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()

                    val startX = center.x + (radius + 12.dp.toPx()) * cos(angleRad)
                    val startY = center.y + (radius + 12.dp.toPx()) * sin(angleRad)
                    val endX = center.x + (radius + 12.dp.toPx() + tickLength) * cos(angleRad)
                    val endY = center.y + (radius + 12.dp.toPx() + tickLength) * sin(angleRad)

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = tickWidth
                    )
                }

                // Background track arc
                drawCircle(
                    color = MatteCharcoal,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Active illuminated Safety Orange arc
                if (animatedSweepAngle > 0f) {
                    drawArc(
                        color = SafetyOrange,
                        startAngle = -90f,
                        sweepAngle = animatedSweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2f, radius * 2f),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Control knob at tip of sweep arc
                    val knobAngleRad = ((-90f + animatedSweepAngle) * PI / 180f).toFloat()
                    val knobX = center.x + radius * cos(knobAngleRad)
                    val knobY = center.y + radius * sin(knobAngleRad)

                    drawCircle(
                        color = OffWhite,
                        radius = (strokeWidth / 2f) + 3.dp.toPx(),
                        center = Offset(knobX, knobY)
                    )
                    drawCircle(
                        color = SafetyOrange,
                        radius = (strokeWidth / 2f) - 1.dp.toPx(),
                        center = Offset(knobX, knobY)
                    )
                }
            }

            // Central Readout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                if (isActiveSession) {
                    val remainingSeconds = remainingMs / 1000L
                    val m = (remainingSeconds % 3600L) / 60L
                    val s = remainingSeconds % 60L
                    val timeText = String.format(Locale.US, "%02d:%02d", m, s)

                    Text(
                        text = "LOCK ACTIVE",
                        color = SignalAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timeText,
                        color = OffWhite,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "UNTIL COMPLETE",
                        color = TextMuted,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Text(
                        text = "FOCUS DIAL",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$selectedMinutes",
                        color = OffWhite,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "MINUTES",
                        color = SafetyOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ROTATE TO SET",
                        color = TextMuted.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Preset hardware switches
        if (!isActiveSession) {
            PresetSwitchesRow(
                currentMinutes = selectedMinutes,
                onSelectMinutes = { minutes ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onMinutesChanged(minutes)
                }
            )
        }
    }
}

@Composable
fun PresetSwitchesRow(
    currentMinutes: Int,
    onSelectMinutes: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        15 to "15M",
        25 to "25M",
        45 to "45M",
        60 to "60M",
        90 to "90M"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        presets.forEach { (mins, label) ->
            val isSelected = currentMinutes == mins
            FilterChip(
                selected = isSelected,
                onClick = { onSelectMinutes(mins) },
                label = {
                    Text(
                        text = label,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = DarkGunmetal,
                    labelColor = TextMuted,
                    selectedContainerColor = SafetyOrange,
                    selectedLabelColor = MatteCharcoal
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) SafetyOrange else BorderDark,
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}
