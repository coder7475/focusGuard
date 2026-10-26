package com.coder7475.focusguard.domain.repository

import com.coder7475.focusguard.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeActiveSession(): Flow<FocusSession?>
    suspend fun getActiveSession(): FocusSession?
    fun observeCompletedSessionCount(): Flow<Int>
    suspend fun saveSession(session: FocusSession)
    suspend fun updateSession(session: FocusSession)
    suspend fun getSessionById(id: String): FocusSession?
}
