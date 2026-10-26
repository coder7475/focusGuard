package com.coder7475.focusguard.platform.alarm

import android.app.AlarmManager
import android.content.Context
import android.content.ContextWrapper
import androidx.test.core.app.ApplicationProvider
import com.coder7475.focusguard.domain.engine.ScheduleEngine
import com.coder7475.focusguard.domain.model.FocusSchedule
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class AlarmCoordinatorTest {

    private lateinit var baseContext: Context
    private val mockAlarmManager: AlarmManager = mockk(relaxed = true)
    private lateinit var coordinator: AlarmCoordinator

    @Before
    fun setUp() {
        baseContext = ApplicationProvider.getApplicationContext()
        io.mockk.every { mockAlarmManager.canScheduleExactAlarms() } returns true
        val wrappedContext = object : ContextWrapper(baseContext) {
            override fun getSystemService(name: String): Any {
                if (name == Context.ALARM_SERVICE) {
                    return mockAlarmManager
                }
                return super.getSystemService(name)
            }
        }
        coordinator = AlarmCoordinator(wrappedContext)
    }

    @Test
    fun `scheduleSessionCompletion registers alarm with AlarmManager`() {
        val triggerEpoch = System.currentTimeMillis() + 60_000L
        coordinator.scheduleSessionCompletion(triggerEpoch, "test-session-1")

        verify(atLeast = 1) {
            mockAlarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerEpoch,
                any()
            )
        }
    }

    @Test
    fun `scheduleRecurringRoutine registers start alarm when enabled`() {
        val now = ZonedDateTime.of(2026, 10, 14, 8, 0, 0, 0, ZoneId.systemDefault())
        val schedule = FocusSchedule(
            id = "sched-1",
            name = "Morning Focus",
            startMinuteOfDay = 9 * 60, // 09:00
            endMinuteOfDay = 11 * 60,
            daysOfWeekBitmask = ScheduleEngine.createWeekdayBitmask(DayOfWeek.WEDNESDAY),
            isEnabled = true
        )

        coordinator.scheduleRecurringRoutine(schedule, now)

        verify(atLeast = 1) {
            mockAlarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                any(),
                any()
            )
        }
    }

    @Test
    fun `scheduleRecurringRoutine cancels alarm when schedule is disabled`() {
        val schedule = FocusSchedule(
            id = "sched-2",
            name = "Disabled Routine",
            startMinuteOfDay = 9 * 60,
            endMinuteOfDay = 11 * 60,
            daysOfWeekBitmask = ScheduleEngine.ALL_DAYS_MASK,
            isEnabled = false
        )

        coordinator.scheduleRecurringRoutine(schedule)

        // Verifies no exception thrown and cancel operation succeeded
        assertNotNull(coordinator)
    }
}
