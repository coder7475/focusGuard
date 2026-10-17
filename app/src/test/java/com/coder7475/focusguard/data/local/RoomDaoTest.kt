package com.coder7475.focusguard.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.coder7475.focusguard.data.local.dao.ScheduleDao
import com.coder7475.focusguard.data.local.dao.SessionDao
import com.coder7475.focusguard.data.local.entity.FocusScheduleEntity
import com.coder7475.focusguard.data.local.entity.FocusSessionEntity
import com.coder7475.focusguard.domain.model.SessionStatus
import com.coder7475.focusguard.domain.model.SessionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class RoomDaoTest {

    private lateinit var database: FocusGuardDatabase
    private lateinit var sessionDao: SessionDao
    private lateinit var scheduleDao: ScheduleDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FocusGuardDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionDao = database.sessionDao()
        scheduleDao = database.scheduleDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndQueryActiveSession() = runBlocking {
        val now = 1_000_000L
        val entity = FocusSessionEntity(
            id = "test-session-1",
            sessionType = SessionType.QUICK.name,
            status = SessionStatus.ACTIVE.name,
            startTimeEpochMs = now,
            endsAtEpochMs = now + 15 * 60 * 1000L,
            durationMs = 15 * 60 * 1000L,
            interruptionCount = 0
        )

        sessionDao.insertSession(entity)

        val active = sessionDao.getActiveSession().first()
        assertNotNull(active)
        assertEquals("test-session-1", active?.id)
        assertEquals(SessionStatus.ACTIVE.name, active?.status)

        val activeSync = sessionDao.getActiveSessionSync()
        assertNotNull(activeSync)
        assertEquals("test-session-1", activeSync?.id)
    }

    @Test
    fun updateSessionStatusAndCountCompleted() = runBlocking {
        val now = 1_000_000L
        val entity = FocusSessionEntity(
            id = "test-session-2",
            sessionType = SessionType.QUICK.name,
            status = SessionStatus.ACTIVE.name,
            startTimeEpochMs = now,
            endsAtEpochMs = now + 15 * 60 * 1000L,
            durationMs = 15 * 60 * 1000L,
            interruptionCount = 0
        )

        sessionDao.insertSession(entity)
        assertEquals(0, sessionDao.getCompletedCount().first())

        sessionDao.updateStatus("test-session-2", SessionStatus.COMPLETED.name)

        val active = sessionDao.getActiveSession().first()
        assertNull(active)

        val completedCount = sessionDao.getCompletedCount().first()
        assertEquals(1, completedCount)
    }

    @Test
    fun scheduleCrudOperations() = runBlocking {
        val schedule1 = FocusScheduleEntity(
            id = "sched-1",
            name = "Work Hours",
            startMinuteOfDay = 9 * 60,
            endMinuteOfDay = 17 * 60,
            daysOfWeekBitmask = 0b0111110, // Mon-Fri
            isEnabled = true
        )
        val schedule2 = FocusScheduleEntity(
            id = "sched-2",
            name = "Night Focus",
            startMinuteOfDay = 21 * 60,
            endMinuteOfDay = 6 * 60,
            daysOfWeekBitmask = 0b1111111,
            isEnabled = false
        )

        scheduleDao.insertSchedule(schedule1)
        scheduleDao.insertSchedule(schedule2)

        val all = scheduleDao.getAllSchedules().first()
        assertEquals(2, all.size)
        assertEquals("sched-1", all[0].id)
        assertEquals("sched-2", all[1].id)

        val enabled = scheduleDao.getEnabledSchedules()
        assertEquals(1, enabled.size)
        assertEquals("sched-1", enabled[0].id)

        scheduleDao.deleteSchedule("sched-1")
        val remaining = scheduleDao.getAllSchedules().first()
        assertEquals(1, remaining.size)
        assertEquals("sched-2", remaining[0].id)
    }
}
