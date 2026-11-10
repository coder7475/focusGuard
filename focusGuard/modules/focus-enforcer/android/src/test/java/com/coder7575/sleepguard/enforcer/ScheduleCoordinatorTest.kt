package com.coder7575.sleepguard.enforcer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ScheduleCoordinatorTest {

    private fun createCalendar(year: Int, month: Int, day: Int, hour: Int, minute: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    @Test
    fun testDayOfWeekMapping() {
        // 2026-11-01 is Sunday -> should map to 7
        val sunday = createCalendar(2026, Calendar.NOVEMBER, 1, 12, 0)
        assertEquals(7, ScheduleCoordinator.getDayOfWeek(sunday))

        // 2026-11-02 is Monday -> should map to 1
        val monday = createCalendar(2026, Calendar.NOVEMBER, 2, 12, 0)
        assertEquals(1, ScheduleCoordinator.getDayOfWeek(monday))

        // 2026-11-07 is Saturday -> should map to 6
        val saturday = createCalendar(2026, Calendar.NOVEMBER, 7, 12, 0)
        assertEquals(6, ScheduleCoordinator.getDayOfWeek(saturday))
    }

    @Test
    fun testIsOvernight() {
        val intraday = NativeSchedule(
            id = "s1",
            name = "Work",
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            daysOfWeek = listOf(1, 2, 3, 4, 5),
            isEnabled = true
        )
        assertFalse(ScheduleCoordinator.isOvernight(intraday))

        val overnight = NativeSchedule(
            id = "s2",
            name = "Bedtime",
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
            isEnabled = true
        )
        assertTrue(ScheduleCoordinator.isOvernight(overnight))
    }

    @Test
    fun testIntradayScheduleWindow() {
        val sched = NativeSchedule(
            id = "s1",
            name = "Work",
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            daysOfWeek = listOf(1, 2, 3, 4, 5), // Mon-Fri
            isEnabled = true
        )

        // Monday 10:30 AM (Inside)
        val inside = createCalendar(2026, Calendar.NOVEMBER, 2, 10, 30)
        assertTrue(ScheduleCoordinator.isInsideScheduleWindow(sched, inside))

        // Monday 18:00 (Outside)
        val outside = createCalendar(2026, Calendar.NOVEMBER, 2, 18, 0)
        assertFalse(ScheduleCoordinator.isInsideScheduleWindow(sched, outside))

        // Sunday 11:00 AM (Disabled day)
        val disabledDay = createCalendar(2026, Calendar.NOVEMBER, 1, 11, 0)
        assertFalse(ScheduleCoordinator.isInsideScheduleWindow(sched, disabledDay))
    }

    @Test
    fun testOvernightScheduleWindow() {
        val sched = NativeSchedule(
            id = "s2",
            name = "Night Lock",
            startHour = 22,
            startMinute = 30,
            endHour = 6,
            endMinute = 30,
            daysOfWeek = listOf(1, 2, 3, 4, 5), // Mon-Fri nights
            isEnabled = true
        )

        // Monday 23:00 (Evening onset)
        val mondayNight = createCalendar(2026, Calendar.NOVEMBER, 2, 23, 0)
        assertTrue(ScheduleCoordinator.isInsideScheduleWindow(sched, mondayNight))

        // Tuesday 04:00 AM (Post-midnight morning)
        val tuesdayMorning = createCalendar(2026, Calendar.NOVEMBER, 3, 4, 0)
        assertTrue(ScheduleCoordinator.isInsideScheduleWindow(sched, tuesdayMorning))

        // Tuesday 07:00 AM (Window closed)
        val tuesdayPast = createCalendar(2026, Calendar.NOVEMBER, 3, 7, 0)
        assertFalse(ScheduleCoordinator.isInsideScheduleWindow(sched, tuesdayPast))
    }

    @Test
    fun testComputeActiveWindowEnd() {
        val sched = NativeSchedule(
            id = "s2",
            name = "Night Lock",
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            daysOfWeek = listOf(1, 2, 3, 4, 5),
            isEnabled = true
        )

        // Monday 23:00 -> window end should be Tuesday 07:00
        val mondayNight = createCalendar(2026, Calendar.NOVEMBER, 2, 23, 0)
        val endMs = ScheduleCoordinator.computeActiveWindowEnd(sched, mondayNight)
        assertNotNull(endMs)

        val endCal = Calendar.getInstance().apply { timeInMillis = endMs!! }
        assertEquals(3, endCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(7, endCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, endCal.get(Calendar.MINUTE))
    }

    @Test
    fun testComputeNextStart() {
        val sched = NativeSchedule(
            id = "s1",
            name = "Work",
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            daysOfWeek = listOf(1), // Only Monday
            isEnabled = true
        )

        // Sunday 14:00 -> Next start should be Monday 09:00
        val sundayAfternoon = createCalendar(2026, Calendar.NOVEMBER, 1, 14, 0)
        val nextStartMs = ScheduleCoordinator.computeNextStart(sched, sundayAfternoon)
        assertNotNull(nextStartMs)

        val nextStartCal = Calendar.getInstance().apply { timeInMillis = nextStartMs!! }
        assertEquals(2, nextStartCal.get(Calendar.DAY_OF_MONTH)) // Monday
        assertEquals(9, nextStartCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, nextStartCal.get(Calendar.MINUTE))
    }
}
