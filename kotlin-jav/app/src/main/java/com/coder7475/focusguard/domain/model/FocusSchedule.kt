package com.coder7475.focusguard.domain.model

import java.util.UUID

data class FocusSchedule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val daysOfWeekBitmask: Int,
    val isEnabled: Boolean = true
)
