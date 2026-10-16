package com.coder7475.focusguard.domain.engine

import com.coder7475.focusguard.domain.model.FocusSession
import com.coder7475.focusguard.domain.model.SessionStatus
import com.coder7475.focusguard.domain.model.SessionType
import com.coder7475.focusguard.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeSessionRepository : SessionRepository {
    private val sessions = mutableMapOf<String, FocusSession>()
    private var activeSession: FocusSession? = null

    override fun observeActiveSession(): Flow<FocusSession?> = flowOf(activeSession)

    override suspend fun getActiveSession(): FocusSession? = activeSession

    override fun observeCompletedSessionCount(): Flow<Int> {
        val count = sessions.values.count { it.status == SessionStatus.COMPLETED }
        return flowOf(count)
    }

    override suspend fun saveSession(session: FocusSession) {
        sessions[session.id] = session
        if (session.status == SessionStatus.ACTIVE) {
            activeSession = session
        }
    }

    override suspend fun updateSession(session: FocusSession) {
        sessions[session.id] = session
        if (session.id == activeSession?.id) {
            activeSession = if (session.status == SessionStatus.ACTIVE) session else null
        }
    }

    override suspend fun getSessionById(id: String): FocusSession? = sessions[id]
}

class SessionEngineTest {

    private lateinit var repository: FakeSessionRepository
    private lateinit var engine: SessionEngine

    @Before
    fun setUp() {
        repository = FakeSessionRepository()
        engine = SessionEngine(repository)
    }

    @Test
    fun `startQuickSession creates active session with exact end time`() = runBlocking {
        val now = 100_000L
        val duration = 15 * 60 * 1000L // 15 mins

        val result = engine.startQuickSession(duration, now)
        assertTrue(result.isSuccess)

        val session = result.getOrThrow()
        assertEquals(SessionStatus.ACTIVE, session.status)
        assertEquals(SessionType.QUICK, session.type)
        assertEquals(now, session.startTimeEpochMs)
        assertEquals(now + duration, session.endsAtEpochMs)
        assertEquals(duration, session.durationMs)
    }

    @Test
    fun `startQuickSession rejects duplicate when active session exists`() = runBlocking {
        val now = 100_000L
        engine.startQuickSession(15 * 60 * 1000L, now)

        val secondResult = engine.startQuickSession(30 * 60 * 1000L, now + 1000L)
        assertFalse(secondResult.isSuccess)
    }

    @Test
    fun `completeActiveSession transitions status to COMPLETED`() = runBlocking {
        val now = 100_000L
        val session = engine.startQuickSession(15 * 60 * 1000L, now).getOrThrow()

        val completeResult = engine.completeActiveSession(session.id, now + 15 * 60 * 1000L)
        assertTrue(completeResult.isSuccess)

        val completed = completeResult.getOrThrow()
        assertEquals(SessionStatus.COMPLETED, completed.status)
    }

    @Test
    fun `calculateRemainingMs returns zero when session completed or expired`() {
        val now = 100_000L
        val expiredSession = FocusSession(
            id = "1",
            type = SessionType.QUICK,
            status = SessionStatus.ACTIVE,
            startTimeEpochMs = now - 20_000L,
            endsAtEpochMs = now - 5_000L,
            durationMs = 15_000L
        )

        val remaining = engine.calculateRemainingMs(expiredSession, now)
        assertEquals(0L, remaining)
    }
}
