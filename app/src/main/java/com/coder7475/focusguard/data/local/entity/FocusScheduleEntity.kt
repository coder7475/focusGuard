package com.coder7475.focusguard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.coder7475.focusguard.domain.model.FocusSchedule

@Entity(tableName = "focus_schedules")
data class FocusScheduleEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val daysOfWeekBitmask: Int,
    val isEnabled: Boolean
) {
    fun toDomain(): FocusSchedule = FocusSchedule(
        id = id,
        name = name,
        startMinuteOfDay = startMinuteOfDay,
        endMinuteOfDay = endMinuteOfDay,
        daysOfWeekBitmask = daysOfWeekBitmask,
        isEnabled = isEnabled
    )

    companion object {
        fun fromDomain(schedule: FocusSchedule): FocusScheduleEntity = FocusScheduleEntity(
            id = schedule.id,
            name = schedule.name,
            startMinuteOfDay = schedule.startMinuteOfDay,
            endMinuteOfDay = schedule.endMinuteOfDay,
            daysOfWeekBitmask = schedule.daysOfWeekBitmask,
            isEnabled = schedule.isEnabled
        )
    }
}
