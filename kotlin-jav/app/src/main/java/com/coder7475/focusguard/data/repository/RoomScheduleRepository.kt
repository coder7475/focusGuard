package com.coder7475.focusguard.data.repository

import com.coder7475.focusguard.data.local.dao.ScheduleDao
import com.coder7475.focusguard.data.local.entity.FocusScheduleEntity
import com.coder7475.focusguard.domain.model.FocusSchedule
import com.coder7475.focusguard.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomScheduleRepository(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override fun observeAllSchedules(): Flow<List<FocusSchedule>> {
        return scheduleDao.getAllSchedules().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getEnabledSchedules(): List<FocusSchedule> {
        return scheduleDao.getEnabledSchedules().map { it.toDomain() }
    }

    override suspend fun getScheduleById(id: String): FocusSchedule? {
        return scheduleDao.getScheduleById(id)?.toDomain()
    }

    override suspend fun saveSchedule(schedule: FocusSchedule) {
        scheduleDao.insertSchedule(FocusScheduleEntity.fromDomain(schedule))
    }

    override suspend fun updateSchedule(schedule: FocusSchedule) {
        scheduleDao.updateSchedule(FocusScheduleEntity.fromDomain(schedule))
    }

    override suspend fun deleteSchedule(id: String) {
        scheduleDao.deleteSchedule(id)
    }
}
