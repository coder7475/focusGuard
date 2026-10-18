package com.coder7475.focusguard.ui.schedule

import java.util.Locale

object ScheduleTimeFormatter {

    /**
     * Converts a 12-hour AM/PM time specification into total minutes of the day (0..1439).
     *
     * 12:00 AM -> 0 (midnight)
     * 12:00 PM -> 720 (noon)
     */
    fun convert12HourToMinuteOfDay(hour12: Int, minute: Int, isAm: Boolean): Int {
        require(hour12 in 1..12) { "Hour in 12-hour format must be between 1 and 12, got $hour12" }
        require(minute in 0..59) { "Minute must be between 0 and 59, got $minute" }

        val hour24 = when {
            isAm && hour12 == 12 -> 0
            isAm -> hour12
            !isAm && hour12 == 12 -> 12
            else -> hour12 + 12
        }
        return (hour24 * 60 + minute).coerceIn(0, 1439)
    }

    /**
     * Converts a 24-hour total minute-of-day (0..1439) into 12-hour components (hour12, minute, isAm).
     */
    fun convertMinuteOfDayTo12Hour(minuteOfDay: Int): Triple<Int, Int, Boolean> {
        val clamped = minuteOfDay.coerceIn(0, 1439)
        val hour24 = clamped / 60
        val minute = clamped % 60
        val isAm = hour24 < 12
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return Triple(hour12, minute, isAm)
    }

    /**
     * Formats total minute-of-day into standard 12-hour AM/PM string, e.g. "09:00 AM", "12:00 PM".
     */
    fun formatMinuteOfDay(minuteOfDay: Int): String {
        val (hour12, minute, isAm) = convertMinuteOfDayTo12Hour(minuteOfDay)
        val period = if (isAm) "AM" else "PM"
        return String.format(Locale.US, "%02d:%02d %s", hour12, minute, period)
    }
}
