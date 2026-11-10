package com.coder7575.sleepguard.enforcer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

data class NativeSchedule(
    val id: String,
    val name: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: List<Int>,
    val isEnabled: Boolean
)

class NativeDbHelper(private val context: Context) {

    private fun getReadableDatabase(): SQLiteDatabase? {
        val dbFile = File(context.filesDir, "SQLite/focusguard.db")
        if (!dbFile.exists()) return null
        return try {
            SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun getWritableDatabase(): SQLiteDatabase? {
        val dbFile = File(context.filesDir, "SQLite/focusguard.db")
        if (!dbFile.exists()) return null
        return try {
            SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.NO_LOCALIZED_COLLATORS
            )
        } catch (_: Exception) {
            null
        }
    }

    fun getActiveSessionEndsAt(): Long {
        val db = getReadableDatabase() ?: return 0L
        return try {
            val cursor = db.rawQuery(
                "SELECT endsAtEpochMs FROM focus_sessions WHERE status = 'ACTIVE' ORDER BY startedAtEpochMs DESC LIMIT 1",
                null
            )
            var targetEpoch = 0L
            if (cursor.moveToFirst()) {
                targetEpoch = cursor.getLong(0)
            }
            cursor.close()
            db.close()
            targetEpoch
        } catch (_: Exception) {
            try { db.close() } catch (_: Exception) {}
            0L
        }
    }

    fun getEnabledSchedules(): List<NativeSchedule> {
        val db = getReadableDatabase() ?: return emptyList()
        val schedules = mutableListOf<NativeSchedule>()
        try {
            val cursor = db.rawQuery(
                "SELECT id, name, startHour, startMinute, endHour, endMinute, daysOfWeek, isEnabled FROM focus_schedules WHERE isEnabled = 1",
                null
            )
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val name = cursor.getString(1)
                val startHour = cursor.getInt(2)
                val startMinute = cursor.getInt(3)
                val endHour = cursor.getInt(4)
                val endMinute = cursor.getInt(5)
                val daysJson = cursor.getString(6) ?: "[]"
                val isEnabled = cursor.getInt(7) == 1

                val daysList = parseDaysOfWeek(daysJson)
                schedules.add(
                    NativeSchedule(
                        id = id,
                        name = name,
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute,
                        daysOfWeek = daysList,
                        isEnabled = isEnabled
                    )
                )
            }
            cursor.close()
            db.close()
        } catch (_: Exception) {
            try { db.close() } catch (_: Exception) {}
        }
        return schedules
    }

    fun recordSessionActive(
        id: String,
        startedAtMs: Long,
        endsAtMs: Long,
        durationMs: Long,
        type: String
    ) {
        val db = getWritableDatabase() ?: return
        try {
            db.execSQL(
                "INSERT OR REPLACE INTO focus_sessions (id, startedAtEpochMs, endsAtEpochMs, durationMs, type, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')",
                arrayOf(id, startedAtMs, endsAtMs, durationMs, type)
            )
            db.close()
        } catch (_: Exception) {
            try { db.close() } catch (_: Exception) {}
        }
    }

    fun markActiveSessionsCompleted() {
        val db = getWritableDatabase() ?: return
        try {
            db.execSQL(
                "UPDATE focus_sessions SET status = 'COMPLETED' WHERE status = 'ACTIVE'"
            )
            db.close()
        } catch (_: Exception) {
            try { db.close() } catch (_: Exception) {}
        }
    }

    private fun parseDaysOfWeek(json: String): List<Int> {
        return try {
            json.trim('[', ']', ' ')
                .split(',')
                .mapNotNull { it.trim().toIntOrNull() }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
