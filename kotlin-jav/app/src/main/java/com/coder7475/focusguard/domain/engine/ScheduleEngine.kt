package com.coder7475.focusguard.domain.engine

import com.coder7475.focusguard.domain.model.FocusSchedule
import java.time.DayOfWeek
import java.time.ZonedDateTime

class ScheduleEngine {

    fun isDayEnabled(daysOfWeekBitmask: Int, dayOfWeek: DayOfWeek): Boolean {
        val mask = 1 shl (dayOfWeek.value - 1)
        return (daysOfWeekBitmask and mask) != 0
    }

    fun isOvernight(schedule: FocusSchedule): Boolean {
        return schedule.endMinuteOfDay <= schedule.startMinuteOfDay
    }

    fun isInsideScheduleWindow(
        schedule: FocusSchedule,
        now: ZonedDateTime = ZonedDateTime.now()
    ): Boolean {
        if (!schedule.isEnabled) return false

        val currentMinuteOfDay = now.hour * 60 + now.minute
        val today = now.dayOfWeek
        val yesterday = today.minus(1)

        return if (!isOvernight(schedule)) {
            isDayEnabled(schedule.daysOfWeekBitmask, today) &&
                currentMinuteOfDay >= schedule.startMinuteOfDay &&
                currentMinuteOfDay < schedule.endMinuteOfDay
        } else {
            // Started today and running past midnight
            val startedToday = isDayEnabled(schedule.daysOfWeekBitmask, today) &&
                currentMinuteOfDay >= schedule.startMinuteOfDay

            // Started yesterday and concluding this morning
            val startedYesterday = isDayEnabled(schedule.daysOfWeekBitmask, yesterday) &&
                currentMinuteOfDay < schedule.endMinuteOfDay

            startedToday || startedYesterday
        }
    }

    fun computeActiveWindowEnd(
        schedule: FocusSchedule,
        now: ZonedDateTime = ZonedDateTime.now()
    ): ZonedDateTime? {
        if (!isInsideScheduleWindow(schedule, now)) return null

        val currentMinuteOfDay = now.hour * 60 + now.minute
        val endHour = schedule.endMinuteOfDay / 60
        val endMinute = schedule.endMinuteOfDay % 60

        return if (!isOvernight(schedule)) {
            now.withHour(endHour).withMinute(endMinute).withSecond(0).withNano(0)
        } else {
            if (currentMinuteOfDay >= schedule.startMinuteOfDay) {
                now.plusDays(1).withHour(endHour).withMinute(endMinute).withSecond(0).withNano(0)
            } else {
                now.withHour(endHour).withMinute(endMinute).withSecond(0).withNano(0)
            }
        }
    }

    fun computeNextStart(
        schedule: FocusSchedule,
        now: ZonedDateTime = ZonedDateTime.now()
    ): ZonedDateTime? {
        if (!schedule.isEnabled) return null

        val startHour = schedule.startMinuteOfDay / 60
        val startMinute = schedule.startMinuteOfDay % 60

        for (dayOffset in 0L..7L) {
            val candidateDate = now.plusDays(dayOffset)
            if (isDayEnabled(schedule.daysOfWeekBitmask, candidateDate.dayOfWeek)) {
                val candidateStart = candidateDate
                    .withHour(startHour)
                    .withMinute(startMinute)
                    .withSecond(0)
                    .withNano(0)

                if (candidateStart.isAfter(now)) {
                    return candidateStart
                }
            }
        }
        return null
    }

    companion object {
        fun createWeekdayBitmask(vararg days: DayOfWeek): Int {
            var mask = 0
            for (day in days) {
                mask = mask or (1 shl (day.value - 1))
            }
            return mask
        }

        val WEEKDAYS_MASK = createWeekdayBitmask(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )

        val ALL_DAYS_MASK = createWeekdayBitmask(*DayOfWeek.entries.toTypedArray())
    }
}
