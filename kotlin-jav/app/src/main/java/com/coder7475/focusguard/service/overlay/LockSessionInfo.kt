package com.coder7475.focusguard.service.overlay

import com.coder7475.focusguard.domain.model.SessionType

/**
 * Encapsulates the contextual metadata required to render an informative lock overlay.
 */
data class LockSessionInfo(
    val endsAtEpochMs: Long = 0L,
    val sessionType: SessionType = SessionType.QUICK,
    val routineName: String? = null,
    val timeRangeFormatted: String? = null,
    val totalDurationMs: Long = 0L
)
