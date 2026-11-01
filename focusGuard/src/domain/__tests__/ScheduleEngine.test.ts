import { ScheduleEngine } from '../ScheduleEngine';
import { FocusSchedule } from '../types';

describe('ScheduleEngine', () => {
  const normalSchedule: FocusSchedule = {
    id: 'sched_1',
    name: 'Work Focus',
    startHour: 9,
    startMinute: 0,
    endHour: 17,
    endMinute: 0,
    daysOfWeek: [1, 2, 3, 4, 5], // Mon - Fri
    isEnabled: true,
    createdAtEpochMs: 1000,
  };

  const overnightSchedule: FocusSchedule = {
    id: 'sched_2',
    name: 'Sleep Guard',
    startHour: 22,
    startMinute: 0,
    endHour: 7,
    endMinute: 0,
    daysOfWeek: [1, 2, 3, 4, 5], // Mon - Fri evening
    isEnabled: true,
    createdAtEpochMs: 2000,
  };

  test('identifies overnight schedule correctly', () => {
    expect(ScheduleEngine.isOvernight(normalSchedule)).toBe(false);
    expect(ScheduleEngine.isOvernight(overnightSchedule)).toBe(true);
  });

  test('normal intraday schedule is active during daytime on active days', () => {
    // A Monday at 10:30 AM
    // 2026-11-02 is a Monday
    const mondayMorning = new Date(2026, 10, 2, 10, 30);
    expect(ScheduleEngine.isInsideScheduleWindow(normalSchedule, mondayMorning)).toBe(true);

    // A Monday at 18:30 (outside window)
    const mondayEvening = new Date(2026, 10, 2, 18, 30);
    expect(ScheduleEngine.isInsideScheduleWindow(normalSchedule, mondayEvening)).toBe(false);

    // A Sunday at 10:30 AM (disabled day)
    // 2026-11-01 is a Sunday
    const sundayMorning = new Date(2026, 10, 1, 10, 30);
    expect(ScheduleEngine.isInsideScheduleWindow(normalSchedule, sundayMorning)).toBe(false);
  });

  test('overnight schedule is active in the evening and post-midnight morning', () => {
    // Monday at 23:15 (started today before midnight)
    const mondayNight = new Date(2026, 10, 2, 23, 15);
    expect(ScheduleEngine.isInsideScheduleWindow(overnightSchedule, mondayNight)).toBe(true);

    // Tuesday at 04:30 AM (started Monday night, rolling into Tuesday morning)
    const tuesdayEarlyMorning = new Date(2026, 10, 3, 4, 30);
    expect(ScheduleEngine.isInsideScheduleWindow(overnightSchedule, tuesdayEarlyMorning)).toBe(true);

    // Tuesday at 08:30 AM (past end window)
    const tuesdayMorning = new Date(2026, 10, 3, 8, 30);
    expect(ScheduleEngine.isInsideScheduleWindow(overnightSchedule, tuesdayMorning)).toBe(false);
  });

  test('formats 12-hour time with AM/PM', () => {
    expect(ScheduleEngine.formatTime(9, 0)).toBe('9:00 AM');
    expect(ScheduleEngine.formatTime(12, 30)).toBe('12:30 PM');
    expect(ScheduleEngine.formatTime(22, 5)).toBe('10:05 PM');
    expect(ScheduleEngine.formatTime(0, 0)).toBe('12:00 AM');
  });

  test('computes active window end date correctly for overnight routine', () => {
    const mondayNight = new Date(2026, 10, 2, 23, 15);
    const end = ScheduleEngine.computeActiveWindowEnd(overnightSchedule, mondayNight);
    expect(end).not.toBeNull();
    expect(end?.getDate()).toBe(3); // Ends next day (Tuesday)
    expect(end?.getHours()).toBe(7);
    expect(end?.getMinutes()).toBe(0);
  });
});
