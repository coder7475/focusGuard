package com.coder7475.focusguard.data.repository

import com.coder7475.focusguard.data.local.dao.SessionDao
import com.coder7475.focusguard.data.local.entity.FocusSessionEntity
import com.coder7475.focusguard.domain.model.FocusSession
import com.coder7475.focusguard.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSessionRepository(
    private val sessionDao: SessionDao
) : SessionRepository {

    override fun observeActiveSession(): Flow<FocusSession?> {
        return sessionDao.getActiveSession().map { it?.toDomain() }
    }

    override suspend fun getActiveSession(): FocusSession? {
        return sessionDao.getActiveSessionSync()?.toDomain()
    }

    override fun observeCompletedSessionCount(): Flow<Int> {
        return sessionDao.getCompletedCount()
    }

    override suspend fun saveSession(session: FocusSession) {
        sessionDao.insertSession(FocusSessionEntity.fromDomain(session))
    }

    override suspend fun updateSession(session: FocusSession) {
        sessionDao.updateSession(FocusSessionEntity.fromDomain(session))
    }

    override suspend fun getSessionById(id: String): FocusSession? {
        return sessionDao.getSessionById(id)?.toDomain()
    }
}
