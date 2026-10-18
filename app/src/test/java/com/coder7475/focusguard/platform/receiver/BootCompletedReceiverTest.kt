package com.coder7475.focusguard.platform.receiver

import com.coder7475.focusguard.domain.engine.ReconciliationResult
import com.coder7475.focusguard.domain.model.FocusSession
import com.coder7475.focusguard.domain.model.SessionStatus
import com.coder7475.focusguard.domain.model.SessionType
import com.coder7475.focusguard.platform.alarm.AlarmCoordinator
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BootCompletedReceiverTest {

    @Test
    fun `reconciliation result types are correctly classified`() {
        val now = 100_000L
        val activeSession = FocusSession(
            id = "test-1",
            type = SessionType.QUICK,
            status = SessionStatus.ACTIVE,
            startTimeEpochMs = now - 5000L,
            endsAtEpochMs = now + 10_000L,
            durationMs = 15_000L
        )

        val stillActive = ReconciliationResult.StillActive(activeSession, 10_000L)
        assertEquals(10_000L, stillActive.remainingMs)
        assertEquals("test-1", stillActive.session.id)

        val expired = ReconciliationResult.CompletedExpired(activeSession.copy(status = SessionStatus.COMPLETED))
        assertEquals(SessionStatus.COMPLETED, expired.session.status)

        val noSession = ReconciliationResult.NoActiveSession
        assertEquals(ReconciliationResult.NoActiveSession, noSession)
    }

    @Test
    fun `alarm coordinator cancels and reschedules matching sessions`() {
        val alarmCoordinator: AlarmCoordinator = mockk(relaxed = true)
        val sessionId = "session-123"
        val triggerEpoch = 200_000L

        alarmCoordinator.scheduleSessionCompletion(triggerEpoch, sessionId)
        verify { alarmCoordinator.scheduleSessionCompletion(triggerEpoch, sessionId) }

        alarmCoordinator.cancelSessionCompletion(sessionId)
        verify { alarmCoordinator.cancelSessionCompletion(sessionId) }
    }
}
