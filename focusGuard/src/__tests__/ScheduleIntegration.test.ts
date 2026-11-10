import FocusEnforcer from '../../modules/focus-enforcer';
import { ScheduleEngine } from '../domain/ScheduleEngine';
import { FocusSchedule } from '../domain/types';

describe('Schedule Integration and Native Module Contract', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('FocusEnforcer exposes all native enforcer methods with correct types', () => {
    expect(typeof FocusEnforcer.startFocusLock).toBe('function');
    expect(typeof FocusEnforcer.stopFocusLock).toBe('function');
    expect(typeof FocusEnforcer.rescheduleRoutines).toBe('function');
    expect(typeof FocusEnforcer.getPermissionStatus).toBe('function');
    expect(typeof FocusEnforcer.openSettingsForPermission).toBe('function');
    expect(typeof FocusEnforcer.openEmergencyDialer).toBe('function');
  });

  test('rescheduleRoutines returns true in test/fallback environment', () => {
    const result = FocusEnforcer.rescheduleRoutines();
    expect(result).toBe(true);
  });

  test('evaluates upcoming schedule sequence without negative duration', () => {
    const schedule: FocusSchedule = {
      id: 'sched_night_lock',
      name: 'Night Lock',
      startHour: 23,
      startMinute: 0,
      endHour: 6,
      endMinute: 30,
      daysOfWeek: [1, 2, 3, 4, 5, 6, 7],
      isEnabled: true,
      createdAtEpochMs: Date.now(),
    };

    // Sunday 22:00 -> not inside window yet
    const preWindow = new Date(2026, 10, 8, 22, 0); // Sunday
    expect(ScheduleEngine.isInsideScheduleWindow(schedule, preWindow)).toBe(false);

    // Compute next start from 22:00 -> should be today at 23:00
    const nextStart = ScheduleEngine.computeNextStart(schedule, preWindow);
    expect(nextStart).not.toBeNull();
    expect(nextStart?.getHours()).toBe(23);
    expect(nextStart?.getMinutes()).toBe(0);

    // Inside window at 23:30 Sunday night
    const insideWindow = new Date(2026, 10, 8, 23, 30);
    expect(ScheduleEngine.isInsideScheduleWindow(schedule, insideWindow)).toBe(true);

    const windowEnd = ScheduleEngine.computeActiveWindowEnd(schedule, insideWindow);
    expect(windowEnd).not.toBeNull();
    expect(windowEnd?.getDate()).toBe(9); // Monday morning
    expect(windowEnd?.getHours()).toBe(6);
    expect(windowEnd?.getMinutes()).toBe(30);

    const durationRemaining = (windowEnd?.getTime() ?? 0) - insideWindow.getTime();
    expect(durationRemaining).toBeGreaterThan(0);
    expect(durationRemaining).toBe((6 * 60 + 30 + 30) * 60 * 1000); // 7 hours in ms
  });

  test('disabled schedule never returns active window or next start', () => {
    const disabledSchedule: FocusSchedule = {
      id: 'disabled_sched',
      name: 'Disabled Routine',
      startHour: 9,
      startMinute: 0,
      endHour: 17,
      endMinute: 0,
      daysOfWeek: [1, 2, 3, 4, 5],
      isEnabled: false,
      createdAtEpochMs: Date.now(),
    };

    const daytime = new Date(2026, 10, 2, 11, 0); // Monday morning
    expect(ScheduleEngine.isInsideScheduleWindow(disabledSchedule, daytime)).toBe(false);
    expect(ScheduleEngine.computeActiveWindowEnd(disabledSchedule, daytime)).toBeNull();
    expect(ScheduleEngine.computeNextStart(disabledSchedule, daytime)).toBeNull();
  });
});
