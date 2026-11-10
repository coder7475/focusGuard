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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.util.Locale

@Composable
fun SchedulesScreen(
    scheduleRepository: ScheduleRepository,
    modifier: Modifier = Modifier,
    onScheduleChanged: () -> Unit = {}
) {
    val schedules by scheduleRepository.observeAllSchedules().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var showAddDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<FocusSchedule?>(null) }
    var scheduleToDelete by remember { mutableStateOf<FocusSchedule?>(null) }

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
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showAddDialog = true
                    },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onToggle = { isEnabled ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    scheduleRepository.updateSchedule(schedule.copy(isEnabled = isEnabled))
                                    withContext(Dispatchers.Main) {
                                        onScheduleChanged()
                                    }
                                }
                            },
                            onEdit = {
                                scheduleToEdit = schedule
                            },
                            onDelete = {
                                scheduleToDelete = schedule
                            }
                        )
                    }
                }
            }
        }

        // Create Routine Dialog
        if (showAddDialog) {
            ScheduleConfigDialog(
                initialSchedule = null,
                onDismiss = { showAddDialog = false },
                onConfirm = { name, startMin, endMin, bitmask ->
                    coroutineScope.launch(Dispatchers.IO) {
                        val schedule = FocusSchedule(
                            name = name,
                            startMinuteOfDay = startMin,
                            endMinuteOfDay = endMin,
                            daysOfWeekBitmask = bitmask,
                            isEnabled = true
                        )
                        scheduleRepository.saveSchedule(schedule)
                        withContext(Dispatchers.Main) {
                            showAddDialog = false
                            onScheduleChanged()
                        }
                    }
                }
            )
        }

        // Edit Routine Dialog
        if (scheduleToEdit != null) {
            val target = scheduleToEdit!!
            ScheduleConfigDialog(
                initialSchedule = target,
                onDismiss = { scheduleToEdit = null },
                onConfirm = { name, startMin, endMin, bitmask ->
                    coroutineScope.launch(Dispatchers.IO) {
                        val updated = target.copy(
                            name = name,
                            startMinuteOfDay = startMin,
                            endMinuteOfDay = endMin,
                            daysOfWeekBitmask = bitmask
                        )
                        scheduleRepository.updateSchedule(updated)
                        withContext(Dispatchers.Main) {
                            scheduleToEdit = null
                            onScheduleChanged()
                        }
                    }
                }
            )
        }

        // Safe Delete Confirmation Dialog
        if (scheduleToDelete != null) {
            val target = scheduleToDelete!!
            DeleteScheduleConfirmationDialog(
                schedule = target,
                onDismiss = { scheduleToDelete = null },
                onConfirm = {
                    coroutineScope.launch(Dispatchers.IO) {
                        scheduleRepository.deleteSchedule(target.id)
                        withContext(Dispatchers.Main) {
                            scheduleToDelete = null
                            onScheduleChanged()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: FocusSchedule,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val startFormatted = ScheduleTimeFormatter.formatMinuteOfDay(schedule.startMinuteOfDay)
    val endFormatted = ScheduleTimeFormatter.formatMinuteOfDay(schedule.endMinuteOfDay)
    val timeFormatted = "$startFormatted – $endFormatted"
    val isOvernight = schedule.endMinuteOfDay <= schedule.startMinuteOfDay
    val durationMinutes = ScheduleTimeFormatter.calculateDurationMinutes(schedule.startMinuteOfDay, schedule.endMinuteOfDay)
    val durationFormatted = ScheduleTimeFormatter.formatDuration(durationMinutes)

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkGunmetal),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark)),
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onEdit()
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = schedule.name,
                            color = OffWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isOvernight) {
                            Text(
                                text = "OVERNIGHT",
                                color = SignalAmber,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(SignalAmber.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = timeFormatted,
                            color = if (schedule.isEnabled) SafetyOrange else TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "($durationFormatted)",
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = schedule.isEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggle(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MatteCharcoal,
                            checkedTrackColor = SafetyOrange,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = MatteCharcoal
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onEdit()
                    }) {
                        Text(
                            text = "✎",
                            color = ElectricCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete()
                    }) {
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
fun ScheduleConfigDialog(
    initialSchedule: FocusSchedule? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, startMinute: Int, endMinute: Int, daysBitmask: Int) -> Unit
) {
    val isEdit = initialSchedule != null
    var name by remember { mutableStateOf(initialSchedule?.name ?: "Deep Work") }

    val initialStart = initialSchedule?.startMinuteOfDay ?: 540 // 9:00 AM default
    val (initStartHour, initStartMin, initStartAm) = remember(initialSchedule) {
        ScheduleTimeFormatter.convertMinuteOfDayTo12Hour(initialStart)
    }

    val initialEnd = initialSchedule?.endMinuteOfDay ?: 1020 // 5:00 PM default
    val (initEndHour, initEndMin, initEndAm) = remember(initialSchedule) {
        ScheduleTimeFormatter.convertMinuteOfDayTo12Hour(initialEnd)
    }

    var startHour12 by remember { mutableIntStateOf(initStartHour) }
    var startMinute by remember { mutableIntStateOf(initStartMin) }
    var startIsAm by remember { mutableStateOf(initStartAm) }

    var endHour12 by remember { mutableIntStateOf(initEndHour) }
    var endMinute by remember { mutableIntStateOf(initEndMin) }
    var endIsAm by remember { mutableStateOf(initEndAm) }

    var daysBitmask by remember {
        mutableIntStateOf(
            initialSchedule?.daysOfWeekBitmask ?: ScheduleEngine.createWeekdayBitmask(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
            )
        )
    }

    val startTotal = ScheduleTimeFormatter.convert12HourToMinuteOfDay(startHour12, startMinute, startIsAm)
    val endTotal = ScheduleTimeFormatter.convert12HourToMinuteOfDay(endHour12, endMinute, endIsAm)
    val isOvernight = endTotal <= startTotal
    val durationMinutes = ScheduleTimeFormatter.calculateDurationMinutes(startTotal, endTotal)
    val durationFormatted = ScheduleTimeFormatter.formatDuration(durationMinutes)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkGunmetal,
        title = {
            Text(
                text = if (isEdit) "EDIT ROUTINE" else "CREATE ROUTINE",
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

                // Time Range Header & Calculated Duration
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TIME RANGE",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        if (isOvernight) {
                            Text(
                                text = "OVERNIGHT",
                                color = SignalAmber,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(SignalAmber.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Window: $durationFormatted active lock",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimePickerSelector(
                        label = "START",
                        hour12 = startHour12,
                        minute = startMinute,
                        isAm = startIsAm,
                        onTimeChanged = { h, m, am ->
                            startHour12 = h
                            startMinute = m
                            startIsAm = am
                        }
                    )
                    Text(text = "➔", color = TextMuted, fontSize = 16.sp)
                    TimePickerSelector(
                        label = "END",
                        hour12 = endHour12,
                        minute = endMinute,
                        isAm = endIsAm,
                        onTimeChanged = { h, m, am ->
                            endHour12 = h
                            endMinute = m
                            endIsAm = am
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE DAYS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    // Quick day presets
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "WEEKDAYS",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .background(MatteCharcoal, RoundedCornerShape(4.dp))
                                .border(1.dp, BorderDark, RoundedCornerShape(4.dp))
                                .clickable {
                                    daysBitmask = ScheduleEngine.createWeekdayBitmask(
                                        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
                                    )
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "ALL",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .background(MatteCharcoal, RoundedCornerShape(4.dp))
                                .border(1.dp, BorderDark, RoundedCornerShape(4.dp))
                                .clickable {
                                    daysBitmask = 0b1111111
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

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
                    onConfirm(name, startTotal, endTotal, daysBitmask)
                },
                enabled = name.isNotBlank() && daysBitmask != 0,
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isEdit) "UPDATE ROUTINE" else "SAVE ROUTINE",
                    color = MatteCharcoal,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
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
fun DeleteScheduleConfirmationDialog(
    schedule: FocusSchedule,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkGunmetal,
        title = {
            Text(
                text = "DELETE ROUTINE?",
                color = DangerRed,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete '${schedule.name}'?",
                    color = OffWhite,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All scheduled alarms for this routine will be cancelled immediately.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "DELETE",
                    color = OffWhite,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
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
fun TimePickerSelector(
    label: String,
    hour12: Int,
    minute: Int,
    isAm: Boolean,
    onTimeChanged: (hour12: Int, minute: Int, isAm: Boolean) -> Unit
) {
    var showDirectMinuteDialog by remember { mutableStateOf(false) }
    var showDirectHourDialog by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TimeStepper(
                value = hour12,
                min = 1,
                max = 12,
                onValueChange = { onTimeChanged(it, minute, isAm) },
                onDirectInputClick = { showDirectHourDialog = true }
            )
            Text(
                text = ":",
                color = OffWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            TimeStepper(
                value = minute,
                min = 0,
                max = 59,
                onValueChange = { onTimeChanged(hour12, it, isAm) },
                onDirectInputClick = { showDirectMinuteDialog = true }
            )
            Spacer(modifier = Modifier.width(2.dp))
            AmPmToggle(
                isAm = isAm,
                onAmPmChanged = { onTimeChanged(hour12, minute, it) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Quick minute preset chips + custom button
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(0, 15, 30, 45).forEach { presetMin ->
                val isSelected = minute == presetMin
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(if (isSelected) SafetyOrange.copy(alpha = 0.2f) else MatteCharcoal, RoundedCornerShape(3.dp))
                        .border(1.dp, if (isSelected) SafetyOrange else BorderDark, RoundedCornerShape(3.dp))
                        .clickable { onTimeChanged(hour12, presetMin, isAm) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, ":%02d", presetMin),
                        color = if (isSelected) SafetyOrange else TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    if (showDirectHourDialog) {
        DirectTimeInputDialog(
            title = "SET HOUR (1–12)",
            initialValue = hour12,
            range = 1..12,
            onDismiss = { showDirectHourDialog = false },
            onConfirm = {
                onTimeChanged(it, minute, isAm)
                showDirectHourDialog = false
            }
        )
    }

    if (showDirectMinuteDialog) {
        DirectTimeInputDialog(
            title = "SET MINUTE (0–59)",
            initialValue = minute,
            range = 0..59,
            onDismiss = { showDirectMinuteDialog = false },
            onConfirm = {
                onTimeChanged(hour12, it, isAm)
                showDirectMinuteDialog = false
            }
        )
    }
}

@Composable
private fun TimeStepper(
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit,
    onDirectInputClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Increment button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 36.dp, height = 20.dp)
                .background(DarkGunmetal, RoundedCornerShape(4.dp))
                .border(1.dp, BorderDark, RoundedCornerShape(4.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val next = if (value >= max) min else value + 1
                    onValueChange(next)
                }
        ) {
            Text("▲", color = SafetyOrange, fontSize = 9.sp)
        }

        // Value display (tapping opens direct numeric input)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 36.dp, height = 30.dp)
                .background(MatteCharcoal, RoundedCornerShape(6.dp))
                .border(1.dp, BorderDark, RoundedCornerShape(6.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDirectInputClick()
                }
        ) {
            Text(
                text = String.format(Locale.US, "%02d", value),
                color = OffWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Decrement button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 36.dp, height = 20.dp)
                .background(DarkGunmetal, RoundedCornerShape(4.dp))
                .border(1.dp, BorderDark, RoundedCornerShape(4.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val next = if (value <= min) max else value - 1
                    onValueChange(next)
                }
        ) {
            Text("▼", color = SafetyOrange, fontSize = 9.sp)
        }
    }
}

@Composable
fun DirectTimeInputDialog(
    title: String,
    initialValue: Int,
    range: IntRange,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(initialValue.toString()) }
    val parsed = text.toIntOrNull()
    val isValid = parsed != null && parsed in range

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkGunmetal,
        title = {
            Text(
                text = title,
                color = OffWhite,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(2)
                        text = digits
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyOrange,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Valid range: ${range.first} – ${range.last}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isValid) onConfirm(parsed!!) },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("SET", color = MatteCharcoal, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
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
fun AmPmToggle(
    isAm: Boolean,
    onAmPmChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .background(MatteCharcoal, RoundedCornerShape(6.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(6.dp))
            .padding(2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (isAm) SafetyOrange else Color.Transparent)
                .clickable {
                    if (!isAm) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAmPmChanged(true)
                    }
                }
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Text(
                text = "AM",
                color = if (isAm) MatteCharcoal else TextMuted,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (!isAm) SafetyOrange else Color.Transparent)
                .clickable {
                    if (isAm) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAmPmChanged(false)
                    }
                }
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Text(
                text = "PM",
                color = if (!isAm) MatteCharcoal else TextMuted,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}
