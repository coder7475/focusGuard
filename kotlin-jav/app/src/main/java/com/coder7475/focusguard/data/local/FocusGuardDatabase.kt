package com.coder7475.focusguard.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.coder7475.focusguard.data.local.dao.ScheduleDao
import com.coder7475.focusguard.data.local.dao.SessionDao
import com.coder7475.focusguard.data.local.entity.FocusScheduleEntity
import com.coder7475.focusguard.data.local.entity.FocusSessionEntity

@Database(
    entities = [
        FocusSessionEntity::class,
        FocusScheduleEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class FocusGuardDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        const val DATABASE_NAME = "focusguard.db"

        @Volatile
        private var instance: FocusGuardDatabase? = null

        fun getInstance(context: Context): FocusGuardDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FocusGuardDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
        }
    }
}
