package com.coder7575.sleepguard.enforcer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FocusDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "focusguard.db", null, 1) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.enableWriteAheadLogging()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS focus_sessions (
                id TEXT PRIMARY KEY NOT NULL,
                startedAtEpochMs INTEGER NOT NULL,
                endsAtEpochMs INTEGER NOT NULL,
                durationMs INTEGER NOT NULL,
                type TEXT NOT NULL,
                status TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS focus_schedules (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                startHour INTEGER NOT NULL,
                startMinute INTEGER NOT NULL,
                endHour INTEGER NOT NULL,
                endMinute INTEGER NOT NULL,
                daysOfWeek TEXT NOT NULL,
                isEnabled INTEGER NOT NULL,
                createdAtEpochMs INTEGER NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun getActiveSessionEndsAt(): Long {
        return try {
            val db = readableDatabase
            val cursor = db.rawQuery(
                "SELECT endsAtEpochMs FROM focus_sessions WHERE status = 'ACTIVE' LIMIT 1",
                null
            )
            var targetEpoch = 0L
            if (cursor.moveToFirst()) {
                targetEpoch = cursor.getLong(0)
            }
            cursor.close()
            targetEpoch
        } catch (_: Exception) {
            0L
        }
    }
}
