package com.coder7475.focusguard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.coder7475.focusguard.data.local.entity.FocusScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM focus_schedules ORDER BY startMinuteOfDay ASC")
    fun getAllSchedules(): Flow<List<FocusScheduleEntity>>

    @Query("SELECT * FROM focus_schedules WHERE isEnabled = 1")
    suspend fun getEnabledSchedules(): List<FocusScheduleEntity>

    @Query("SELECT * FROM focus_schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: String): FocusScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: FocusScheduleEntity)

    @Update
    suspend fun updateSchedule(schedule: FocusScheduleEntity)

    @Query("DELETE FROM focus_schedules WHERE id = :id")
    suspend fun deleteSchedule(id: String): Int

    @Query("DELETE FROM focus_schedules")
    suspend fun clearAll(): Int
}
