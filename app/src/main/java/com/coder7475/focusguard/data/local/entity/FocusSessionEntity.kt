package com.coder7475.focusguard.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.coder7475.focusguard.domain.model.FocusSession
import com.coder7475.focusguard.domain.model.SessionStatus
import com.coder7475.focusguard.domain.model.SessionType

@Entity(
    tableName = "focus_sessions",
    indices = [
        Index(value = ["status"]),
        Index(value = ["endsAtEpochMs"])
    ]
)
data class FocusSessionEntity(
    @PrimaryKey
    val id: String,
    val sessionType: String,
    val status: String,
    val startTimeEpochMs: Long,
    val endsAtEpochMs: Long,
    val durationMs: Long,
    val interruptionCount: Int = 0
) {
    fun toDomain(): FocusSession = FocusSession(
        id = id,
        type = SessionType.valueOf(sessionType),
        status = SessionStatus.valueOf(status),
        startTimeEpochMs = startTimeEpochMs,
        endsAtEpochMs = endsAtEpochMs,
        durationMs = durationMs,
        interruptionCount = interruptionCount
    )

    companion object {
        fun fromDomain(session: FocusSession): FocusSessionEntity = FocusSessionEntity(
            id = session.id,
            sessionType = session.type.name,
            status = session.status.name,
            startTimeEpochMs = session.startTimeEpochMs,
            endsAtEpochMs = session.endsAtEpochMs,
            durationMs = session.durationMs,
            interruptionCount = session.interruptionCount
        )
    }
}
