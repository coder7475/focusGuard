package com.coder7575.sleepguard.enforcer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NativeDbHelper(context: Context) : SQLiteOpenHelper(context, "focusguard.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {}
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
