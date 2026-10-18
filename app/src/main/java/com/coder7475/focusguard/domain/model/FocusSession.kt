package com.coder7475.focusguard.domain.model

import java.util.UUID

data class FocusSession(
    val id: String = UUID.randomUUID().toString(),
    val type: SessionType = SessionType.QUICK,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val startTimeEpochMs: Long,
    val endsAtEpochMs: Long,
    val durationMs: Long,
    val interruptionCount: Int = 0
)
