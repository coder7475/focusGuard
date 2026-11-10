package com.coder7475.focusguard.domain.repository

import com.coder7475.focusguard.domain.model.FocusSchedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun observeAllSchedules(): Flow<List<FocusSchedule>>
    suspend fun getEnabledSchedules(): List<FocusSchedule>
    suspend fun getScheduleById(id: String): FocusSchedule?
    suspend fun saveSchedule(schedule: FocusSchedule)
    suspend fun updateSchedule(schedule: FocusSchedule)
    suspend fun deleteSchedule(id: String)
}
