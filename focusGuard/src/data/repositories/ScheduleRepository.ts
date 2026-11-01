import { openAppDatabase } from '../db';
import { FocusSchedule } from '../../domain/types';

export class ScheduleRepository {
  async createSchedule(schedule: FocusSchedule): Promise<void> {
    const db = await openAppDatabase();
    await db.runAsync(
      `INSERT INTO focus_schedules (id, name, startHour, startMinute, endHour, endMinute, daysOfWeek, isEnabled, createdAtEpochMs)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        schedule.id,
        schedule.name,
        schedule.startHour,
        schedule.startMinute,
        schedule.endHour,
        schedule.endMinute,
        JSON.stringify(schedule.daysOfWeek),
        schedule.isEnabled ? 1 : 0,
        schedule.createdAtEpochMs,
      ]
    );
  }

  async getAllSchedules(): Promise<FocusSchedule[]> {
    const db = await openAppDatabase();
    const rows = await db.getAllAsync<any>(
      `SELECT * FROM focus_schedules ORDER BY createdAtEpochMs ASC`
    );
    return rows.map((r) => ({
      id: r.id,
      name: r.name,
      startHour: Number(r.startHour),
      startMinute: Number(r.startMinute),
      endHour: Number(r.endHour),
      endMinute: Number(r.endMinute),
      daysOfWeek: JSON.parse(r.daysOfWeek || '[]'),
      isEnabled: Boolean(r.isEnabled),
      createdAtEpochMs: Number(r.createdAtEpochMs),
    }));
  }

  async toggleSchedule(id: string, isEnabled: boolean): Promise<void> {
    const db = await openAppDatabase();
    await db.runAsync(
      `UPDATE focus_schedules SET isEnabled = ? WHERE id = ?`,
      [isEnabled ? 1 : 0, id]
    );
  }

  async deleteSchedule(id: string): Promise<void> {
    const db = await openAppDatabase();
    await db.runAsync(`DELETE FROM focus_schedules WHERE id = ?`, [id]);
  }
}

export const scheduleRepository = new ScheduleRepository();
