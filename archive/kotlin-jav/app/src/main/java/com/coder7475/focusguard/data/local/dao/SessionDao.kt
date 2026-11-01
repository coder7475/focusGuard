package com.coder7475.focusguard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.coder7475.focusguard.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM focus_sessions WHERE status = 'ACTIVE' LIMIT 1")
    fun getActiveSession(): Flow<FocusSessionEntity?>

    @Query("SELECT * FROM focus_sessions WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveSessionSync(): FocusSessionEntity?

    @Query("SELECT * FROM focus_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): FocusSessionEntity?

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE status = 'COMPLETED'")
    fun getCompletedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity)

    @Update
    suspend fun updateSession(session: FocusSessionEntity)

    @Query("UPDATE focus_sessions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String): Int

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAll(): Int
}
