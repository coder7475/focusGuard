package com.coder7475.focusguard.domain.engine

import com.coder7475.focusguard.domain.model.FocusSchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

class ScheduleEngineTest {

    private lateinit var engine: ScheduleEngine
    private val zoneId = ZoneId.of("UTC")

    @Before
    fun setUp() {
        engine = ScheduleEngine()
    }

    @Test
    fun `daytime schedule identifies inside and outside window`() {
        // Monday - Friday 09:00 (540) to 17:00 (1020)
        val schedule = FocusSchedule(
            name = "Work",
            startMinuteOfDay = 9 * 60,
            endMinuteOfDay = 17 * 60,
            daysOfWeekBitmask = ScheduleEngine.WEEKDAYS_MASK,
            isEnabled = true
        )

        // Wednesday at 10:30 AM -> inside
        val wednesdayInside = ZonedDateTime.of(2026, 10, 14, 10, 30, 0, 0, zoneId)
        assertTrue(engine.isInsideScheduleWindow(schedule, wednesdayInside))

        // Wednesday at 18:00 -> outside
        val wednesdayAfter = ZonedDateTime.of(2026, 10, 14, 18, 0, 0, 0, zoneId)
        assertFalse(engine.isInsideScheduleWindow(schedule, wednesdayAfter))

        // Saturday at 10:30 AM -> outside (weekend)
        val saturday = ZonedDateTime.of(2026, 10, 17, 10, 30, 0, 0, zoneId)
        assertFalse(engine.isInsideScheduleWindow(schedule, saturday))
    }

    @Test
    fun `overnight schedule identifies active window across midnight`() {
        // Monday - Friday 20:00 (1200) to 08:00 (480)
        val overnight = FocusSchedule(
            name = "Night Focus",
            startMinuteOfDay = 20 * 60,
            endMinuteOfDay = 8 * 60,
            daysOfWeekBitmask = ScheduleEngine.WEEKDAYS_MASK,
            isEnabled = true
        )
        assertTrue(engine.isOvernight(overnight))

        // Tuesday 21:00 -> inside (started Tuesday evening)
        val tuesdayEvening = ZonedDateTime.of(2026, 10, 13, 21, 0, 0, 0, zoneId)
        assertTrue(engine.isInsideScheduleWindow(overnight, tuesdayEvening))

        // Wednesday 06:30 -> inside (started Tuesday evening, ends Wednesday 08:00)
        val wednesdayMorning = ZonedDateTime.of(2026, 10, 14, 6, 30, 0, 0, zoneId)
        assertTrue(engine.isInsideScheduleWindow(overnight, wednesdayMorning))

        // Wednesday 12:00 -> outside
        val wednesdayNoon = ZonedDateTime.of(2026, 10, 14, 12, 0, 0, 0, zoneId)
        assertFalse(engine.isInsideScheduleWindow(overnight, wednesdayNoon))
    }

    @Test
    fun `computeActiveWindowEnd returns next day for overnight schedule evening`() {
        val overnight = FocusSchedule(
            name = "Night Focus",
            startMinuteOfDay = 20 * 60,
            endMinuteOfDay = 8 * 60,
            daysOfWeekBitmask = ScheduleEngine.WEEKDAYS_MASK,
            isEnabled = true
        )

        val tuesdayEvening = ZonedDateTime.of(2026, 10, 13, 22, 0, 0, 0, zoneId)
        val end = engine.computeActiveWindowEnd(overnight, tuesdayEvening)
        assertNotNull(end)
        assertEquals(2026, end!!.year)
        assertEquals(10, end.monthValue)
        assertEquals(14, end.dayOfMonth)
        assertEquals(8, end.hour)
        assertEquals(0, end.minute)
    }

    @Test
    fun `computeNextStart schedules next trigger correctly`() {
        val schedule = FocusSchedule(
            name = "Daily",
            startMinuteOfDay = 14 * 60, // 14:00
            endMinuteOfDay = 16 * 60,
            daysOfWeekBitmask = ScheduleEngine.ALL_DAYS_MASK,
            isEnabled = true
        )

        // Today at 10:00 -> next is today at 14:00
        val morning = ZonedDateTime.of(2026, 10, 17, 10, 0, 0, 0, zoneId)
        val nextStart = engine.computeNextStart(schedule, morning)
        assertNotNull(nextStart)
        assertEquals(17, nextStart!!.dayOfMonth)
        assertEquals(14, nextStart.hour)

        // Today at 15:00 -> next is tomorrow at 14:00
        val afternoon = ZonedDateTime.of(2026, 10, 17, 15, 0, 0, 0, zoneId)
        val tomorrowStart = engine.computeNextStart(schedule, afternoon)
        assertNotNull(tomorrowStart)
        assertEquals(18, tomorrowStart!!.dayOfMonth)
        assertEquals(14, tomorrowStart.hour)
    }
}
