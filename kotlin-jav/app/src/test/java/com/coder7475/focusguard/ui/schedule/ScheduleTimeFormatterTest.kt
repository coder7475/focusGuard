package com.coder7475.focusguard.ui.schedule

import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleTimeFormatterTest {

    @Test
    fun convert12HourToMinuteOfDay_midnightIsZero() {
        // 12:00 AM -> 0
        val minutes = ScheduleTimeFormatter.convert12HourToMinuteOfDay(hour12 = 12, minute = 0, isAm = true)
        assertEquals(0, minutes)
    }

    @Test
    fun convert12HourToMinuteOfDay_noonIs720() {
        // 12:00 PM -> 720
        val minutes = ScheduleTimeFormatter.convert12HourToMinuteOfDay(hour12 = 12, minute = 0, isAm = false)
        assertEquals(720, minutes)
    }

    @Test
    fun convert12HourToMinuteOfDay_morningTimes() {
        // 9:00 AM -> 9 * 60 = 540
        assertEquals(540, ScheduleTimeFormatter.convert12HourToMinuteOfDay(hour12 = 9, minute = 0, isAm = true))

        // 1:15 AM -> 1 * 60 + 15 = 75
        assertEquals(75, ScheduleTimeFormatter.convert12HourToMinuteOfDay(hour12 = 1, minute = 15, isAm = true))

        // 12:45 AM -> 45
        assertEquals(45, ScheduleTimeFormatter.convert12HourToMinuteOfDay(hour12 = 12, minute = 45, isAm = true))
    }

    @Test
    fun convert12HourToMinuteOfDay_afternoonAndEveningTimes() {
        // 5:00 PM -> (5 + 12) * 60 = 1020
        assertEquals(1020, ScheduleTimeFormatter.convert12HourToMinuteOfDay(hour12 = 5, minute = 0, isAm = false))

        // 1:30 PM -> (1 + 12) * 60 + 30 = 810
        assertEquals(810, ScheduleTimeFormatter.convert12HourToMinuteOfDay(hour12 = 1, minute = 30, isAm = false))

        // 11:59 PM -> (11 + 12) * 60 + 59 = 1439
        assertEquals(1439, ScheduleTimeFormatter.convert12HourToMinuteOfDay(hour12 = 11, minute = 59, isAm = false))
    }

    @Test
    fun convertMinuteOfDayTo12Hour_roundTripsCorrectly() {
        val testCases = listOf(
            0 to Triple(12, 0, true),
            45 to Triple(12, 45, true),
            540 to Triple(9, 0, true),
            720 to Triple(12, 0, false),
            750 to Triple(12, 30, false),
            1020 to Triple(5, 0, false),
            1439 to Triple(11, 59, false)
        )

        for ((minuteOfDay, expectedTriple) in testCases) {
            val result = ScheduleTimeFormatter.convertMinuteOfDayTo12Hour(minuteOfDay)
            assertEquals("Failed for minuteOfDay=$minuteOfDay", expectedTriple, result)
        }
    }

    @Test
    fun formatMinuteOfDay_formatsCorrectAmPmStrings() {
        assertEquals("12:00 AM", ScheduleTimeFormatter.formatMinuteOfDay(0))
        assertEquals("09:00 AM", ScheduleTimeFormatter.formatMinuteOfDay(540))
        assertEquals("12:00 PM", ScheduleTimeFormatter.formatMinuteOfDay(720))
        assertEquals("05:00 PM", ScheduleTimeFormatter.formatMinuteOfDay(1020))
        assertEquals("10:30 PM", ScheduleTimeFormatter.formatMinuteOfDay(1350))
        assertEquals("11:59 PM", ScheduleTimeFormatter.formatMinuteOfDay(1439))
    }
}
