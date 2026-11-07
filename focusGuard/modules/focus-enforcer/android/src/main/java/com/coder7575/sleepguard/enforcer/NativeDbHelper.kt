package com.coder7575.sleepguard.enforcer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

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
}
