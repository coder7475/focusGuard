import * as SQLite from 'expo-sqlite';

export const DB_NAME = 'focusguard.db';

let dbInstance: SQLite.SQLiteDatabase | null = null;

export async function openAppDatabase(): Promise<SQLite.SQLiteDatabase> {
  if (dbInstance) return dbInstance;

  const db = await SQLite.openDatabaseAsync(DB_NAME);
  await db.execAsync(`
    PRAGMA journal_mode = WAL;

    CREATE TABLE IF NOT EXISTS focus_sessions (
      id TEXT PRIMARY KEY NOT NULL,
      startedAtEpochMs INTEGER NOT NULL,
      endsAtEpochMs INTEGER NOT NULL,
      durationMs INTEGER NOT NULL,
      type TEXT NOT NULL,
      status TEXT NOT NULL
    );

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
    );
  `);

  dbInstance = db;
  return db;
}
