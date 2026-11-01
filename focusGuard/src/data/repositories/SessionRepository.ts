import { openAppDatabase } from '../db';
import { FocusSession } from '../../domain/types';

export class SessionRepository {
  async createSession(session: FocusSession): Promise<void> {
    const db = await openAppDatabase();
    await db.runAsync(
      `INSERT INTO focus_sessions (id, startedAtEpochMs, endsAtEpochMs, durationMs, type, status)
       VALUES (?, ?, ?, ?, ?, ?)`,
      [
        session.id,
        session.startedAtEpochMs,
        session.endsAtEpochMs,
        session.durationMs,
        session.type,
        session.status,
      ]
    );
  }

  async getActiveSession(): Promise<FocusSession | null> {
    const db = await openAppDatabase();
    const row = await db.getFirstAsync<any>(
      `SELECT * FROM focus_sessions WHERE status = 'ACTIVE' ORDER BY startedAtEpochMs DESC LIMIT 1`
    );
    if (!row) return null;
    return {
      id: row.id,
      startedAtEpochMs: Number(row.startedAtEpochMs),
      endsAtEpochMs: Number(row.endsAtEpochMs),
      durationMs: Number(row.durationMs),
      type: row.type,
      status: row.status,
    };
  }

  async completeSession(sessionId: string): Promise<void> {
    const db = await openAppDatabase();
    await db.runAsync(
      `UPDATE focus_sessions SET status = 'COMPLETED' WHERE id = ?`,
      [sessionId]
    );
  }

  async cancelSession(sessionId: string): Promise<void> {
    const db = await openAppDatabase();
    await db.runAsync(
      `UPDATE focus_sessions SET status = 'CANCELLED' WHERE id = ?`,
      [sessionId]
    );
  }

  async getRecentSessions(limit: number = 10): Promise<FocusSession[]> {
    const db = await openAppDatabase();
    const rows = await db.getAllAsync<any>(
      `SELECT * FROM focus_sessions ORDER BY startedAtEpochMs DESC LIMIT ?`,
      [limit]
    );
    return rows.map((r) => ({
      id: r.id,
      startedAtEpochMs: Number(r.startedAtEpochMs),
      endsAtEpochMs: Number(r.endsAtEpochMs),
      durationMs: Number(r.durationMs),
      type: r.type,
      status: r.status,
    }));
  }

  async getStats(): Promise<{ completedCount: number; totalFocusMinutes: number }> {
    const db = await openAppDatabase();
    const countRow = await db.getFirstAsync<any>(
      `SELECT COUNT(*) as cnt, SUM(durationMs) as totalMs FROM focus_sessions WHERE status = 'COMPLETED'`
    );
    const count = countRow?.cnt || 0;
    const totalMs = countRow?.totalMs || 0;
    return {
      completedCount: count,
      totalFocusMinutes: Math.round(totalMs / 60000),
    };
  }
}

export const sessionRepository = new SessionRepository();
