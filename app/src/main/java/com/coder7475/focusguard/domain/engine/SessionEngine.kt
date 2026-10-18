package com.coder7475.focusguard.domain.engine

import com.coder7475.focusguard.domain.model.FocusSession
import com.coder7475.focusguard.domain.model.SessionStatus
import com.coder7475.focusguard.domain.model.SessionType
import com.coder7475.focusguard.domain.repository.SessionRepository
import kotlin.math.max

sealed interface ReconciliationResult {
    data object NoActiveSession : ReconciliationResult
    data class StillActive(val session: FocusSession, val remainingMs: Long) : ReconciliationResult
    data class CompletedExpired(val session: FocusSession) : ReconciliationResult
}

class SessionEngine(
    private val sessionRepository: SessionRepository
) {
    suspend fun startQuickSession(
        durationMs: Long,
        nowEpochMs: Long = System.currentTimeMillis()
    ): Result<FocusSession> {
        val existingActive = sessionRepository.getActiveSession()
        if (existingActive != null && existingActive.status == SessionStatus.ACTIVE) {
            return Result.failure(IllegalStateException("An active session is already in progress"))
        }

        val session = FocusSession(
            type = SessionType.QUICK,
            status = SessionStatus.ACTIVE,
            startTimeEpochMs = nowEpochMs,
            endsAtEpochMs = nowEpochMs + durationMs,
            durationMs = durationMs
        )

        sessionRepository.saveSession(session)
        return Result.success(session)
    }

    suspend fun completeActiveSession(
        sessionId: String? = null,
        nowEpochMs: Long = System.currentTimeMillis()
    ): Result<FocusSession> {
        val session = if (sessionId != null) {
            sessionRepository.getSessionById(sessionId)
        } else {
            sessionRepository.getActiveSession()
        } ?: return Result.failure(IllegalArgumentException("No active session to complete"))

        val completed = session.copy(status = SessionStatus.COMPLETED)
        sessionRepository.updateSession(completed)
        return Result.success(completed)
    }

    suspend fun reconcileState(
        nowEpochMs: Long = System.currentTimeMillis()
    ): ReconciliationResult {
        val session = sessionRepository.getActiveSession() ?: return ReconciliationResult.NoActiveSession
        if (session.status != SessionStatus.ACTIVE) {
            return ReconciliationResult.NoActiveSession
        }

        return if (nowEpochMs >= session.endsAtEpochMs) {
            val completed = session.copy(status = SessionStatus.COMPLETED)
            sessionRepository.updateSession(completed)
            ReconciliationResult.CompletedExpired(completed)
        } else {
            val remainingMs = session.endsAtEpochMs - nowEpochMs
            ReconciliationResult.StillActive(session, remainingMs)
        }
    }

    fun calculateRemainingMs(
        session: FocusSession,
        nowEpochMs: Long = System.currentTimeMillis()
    ): Long {
        if (session.status != SessionStatus.ACTIVE) return 0L
        return max(0L, session.endsAtEpochMs - nowEpochMs)
    }
}
