package com.coder7475.focusguard.ui.schedule

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coder7475.focusguard.domain.engine.ScheduleEngine
import com.coder7475.focusguard.domain.model.FocusSchedule
import com.coder7475.focusguard.domain.repository.ScheduleRepository
import com.coder7475.focusguard.ui.theme.BorderDark
import com.coder7475.focusguard.ui.theme.DangerRed
import com.coder7475.focusguard.ui.theme.DarkGunmetal
import com.coder7475.focusguard.ui.theme.ElectricCyan
import com.coder7475.focusguard.ui.theme.MatteCharcoal
import com.coder7475.focusguard.ui.theme.OffWhite
import com.coder7475.focusguard.ui.theme.SafetyOrange
import com.coder7475.focusguard.ui.theme.SignalAmber
import com.coder7475.focusguard.ui.theme.TextMuted
import java.time.DayOfWeek
import java.util.Locale

@Composable
fun SchedulesScreen(
    scheduleRepository: ScheduleRepository,
    onScheduleChanged: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val schedules by scheduleRepository.observeAllSchedules().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FOCUS ROUTINES",
                        color = OffWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Automated recurring lock windows",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "+ NEW",
                        color = MatteCharcoal,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (schedules.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(DarkGunmetal, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO ROUTINES CONFIGURED",
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add automated windows like Bedtime (22:30–07:00) or Work Focus (09:00–17:00) to lock distracting apps automatically.",
                            color = TextMuted.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onToggle = { isEnabled ->
                                kotlinx.coroutines.runBlocking {
                                    scheduleRepository.updateSchedule(schedule.copy(isEnabled = isEnabled))
                                }
                                onScheduleChanged()
                            },
                            onDelete = {
                                kotlinx.coroutines.runBlocking {
                                    scheduleRepository.deleteSchedule(schedule.id)
                                }
                                onScheduleChanged()
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddScheduleDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, startMin, endMin, bitmask ->
                    kotlinx.coroutines.runBlocking {
                        val schedule = FocusSchedule(
                            name = name,
                            startMinuteOfDay = startMin,
                            endMinuteOfDay = endMin,
                            daysOfWeekBitmask = bitmask,
                            isEnabled = true
                        )
                        scheduleRepository.saveSchedule(schedule)
                    }
                    showAddDialog = false
                    onScheduleChanged()
                }
            )
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: FocusSchedule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val startH = schedule.startMinuteOfDay / 60
    val startM = schedule.startMinuteOfDay % 60
    val endH = schedule.endMinuteOfDay / 60
    val endM = schedule.endMinuteOfDay % 60

    val timeFormatted = String.format(Locale.US, "%02d:%02d – %02d:%02d", startH, startM, endH, endM)
    val isOvernight = schedule.endMinuteOfDay <= schedule.startMinuteOfDay

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkGunmetal),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = schedule.name,
                            color = OffWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isOvernight) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OVERNIGHT",
                                color = SignalAmber,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(SignalAmber.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timeFormatted,
                        color = if (schedule.isEnabled) SafetyOrange else TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = schedule.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MatteCharcoal,
                            checkedTrackColor = SafetyOrange,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = MatteCharcoal
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete) {
                        Text(
                            text = "✕",
                            color = DangerRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weekday active chips
            val days = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                days.forEachIndexed { index, label ->
                    val dayMask = 1 shl index
                    val isActiveDay = (schedule.daysOfWeekBitmask and dayMask) != 0

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isActiveDay) ElectricCyan.copy(alpha = 0.2f) else MatteCharcoal)
                            .border(
                                width = 1.dp,
                                color = if (isActiveDay) ElectricCyan else BorderDark,
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = label,
                            color = if (isActiveDay) ElectricCyan else TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, startMinute: Int, endMinute: Int, daysBitmask: Int) -> Unit
) {
    var name by remember { mutableStateOf("Deep Work") }
    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(17) }
    var endMinute by remember { mutableIntStateOf(0) }
    var daysBitmask by remember { mutableIntStateOf(ScheduleEngine.createWeekdayBitmask(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    )) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkGunmetal,
        title = {
            Text(
                text = "CREATE ROUTINE",
                color = OffWhite,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Routine Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyOrange,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "TIME RANGE",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimePickerSelector(
                        label = "START",
                        hour = startHour,
                        minute = startMinute,
                        onTimeChanged = { h, m ->
                            startHour = h
                            startMinute = m
                        }
                    )
                    Text(text = "➔", color = TextMuted, fontSize = 18.sp)
                    TimePickerSelector(
                        label = "END",
                        hour = endHour,
                        minute = endMinute,
                        onTimeChanged = { h, m ->
                            endHour = h
                            endMinute = m
                        }
                    )
                }

                Text(
                    text = "ACTIVE DAYS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayNames.forEachIndexed { index, label ->
                        val mask = 1 shl index
                        val isSelected = (daysBitmask and mask) != 0

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SafetyOrange else MatteCharcoal)
                                .border(1.dp, if (isSelected) SafetyOrange else BorderDark, CircleShape)
                                .clickable {
                                    daysBitmask = if (isSelected) {
                                        daysBitmask and mask.inv()
                                    } else {
                                        daysBitmask or mask
                                    }
                                }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MatteCharcoal else TextMuted,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val startTotal = startHour * 60 + startMinute
                    val endTotal = endHour * 60 + endMinute
                    onConfirm(name, startTotal, endTotal, daysBitmask)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("SAVE ROUTINE", color = MatteCharcoal, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Text("CANCEL", color = TextMuted, fontFamily = FontFamily.Monospace)
            }
        }
    )
}

@Composable
private fun TimePickerSelector(
    label: String,
    hour: Int,
    minute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberSpinner(
                value = hour,
                max = 23,
                onValueChange = { onTimeChanged(it, minute) }
            )
            Text(text = ":", color = OffWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
            NumberSpinner(
                value = minute,
                max = 59,
                step = 5,
                onValueChange = { onTimeChanged(hour, it) }
            )
        }
    }
}

@Composable
private fun NumberSpinner(
    value: Int,
    max: Int,
    step: Int = 1,
    onValueChange: (Int) -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(MatteCharcoal, RoundedCornerShape(6.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clickable {
                val next = (value + step) % (max + 1)
                onValueChange(next)
            }
    ) {
        Text(
            text = String.format(Locale.US, "%02d", value),
            color = OffWhite,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
