import { FocusSchedule } from './types';

export class ScheduleEngine {
  /**
   * Day of week: 1 = Monday, 2 = Tuesday, ..., 7 = Sunday
   */
  static getDayOfWeek(date: Date): number {
    const day = date.getDay(); // 0 is Sunday
    return day === 0 ? 7 : day;
  }

  static isOvernight(schedule: FocusSchedule): boolean {
    const startMinute = schedule.startHour * 60 + schedule.startMinute;
    const endMinute = schedule.endHour * 60 + schedule.endMinute;
    return endMinute <= startMinute;
  }

  static isDayEnabled(schedule: FocusSchedule, dayOfWeek: number): boolean {
    return schedule.daysOfWeek.includes(dayOfWeek);
  }

  static isInsideScheduleWindow(schedule: FocusSchedule, now: Date = new Date()): boolean {
    if (!schedule.isEnabled) return false;

    const currentMinuteOfDay = now.getHours() * 60 + now.getMinutes();
    const today = this.getDayOfWeek(now);
    const yesterday = today === 1 ? 7 : today - 1;

    const startMinute = schedule.startHour * 60 + schedule.startMinute;
    const endMinute = schedule.endHour * 60 + schedule.endMinute;

    if (!this.isOvernight(schedule)) {
      return (
        this.isDayEnabled(schedule, today) &&
        currentMinuteOfDay >= startMinute &&
        currentMinuteOfDay < endMinute
      );
    } else {
      // Overnight schedule:
      // 1. Started today before midnight
      const startedToday =
        this.isDayEnabled(schedule, today) && currentMinuteOfDay >= startMinute;

      // 2. Started yesterday and running past midnight into today morning
      const startedYesterday =
        this.isDayEnabled(schedule, yesterday) && currentMinuteOfDay < endMinute;

      return startedToday || startedYesterday;
    }
  }

  static computeActiveWindowEnd(schedule: FocusSchedule, now: Date = new Date()): Date | null {
    if (!this.isInsideScheduleWindow(schedule, now)) return null;

    const currentMinuteOfDay = now.getHours() * 60 + now.getMinutes();
    const startMinute = schedule.startHour * 60 + schedule.startMinute;

    const endDate = new Date(now);
    endDate.setHours(schedule.endHour, schedule.endMinute, 0, 0);

    if (this.isOvernight(schedule)) {
      if (currentMinuteOfDay >= startMinute) {
        // Ends tomorrow morning
        endDate.setDate(endDate.getDate() + 1);
      }
    }
    return endDate;
  }

  static computeNextStart(schedule: FocusSchedule, now: Date = new Date()): Date | null {
    if (!schedule.isEnabled) return null;

    for (let dayOffset = 0; dayOffset <= 7; dayOffset++) {
      const candidateDate = new Date(now);
      candidateDate.setDate(candidateDate.getDate() + dayOffset);
      candidateDate.setHours(schedule.startHour, schedule.startMinute, 0, 0);

      const candidateDayOfWeek = this.getDayOfWeek(candidateDate);
      if (this.isDayEnabled(schedule, candidateDayOfWeek)) {
        if (candidateDate.getTime() > now.getTime()) {
          return candidateDate;
        }
      }
    }
    return null;
  }

  static formatTime(hour: number, minute: number): string {
    const period = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour % 12 === 0 ? 12 : hour % 12;
    const displayMinute = minute < 10 ? `0${minute}` : minute;
    return `${displayHour}:${displayMinute} ${period}`;
  }

  static formatRange(schedule: FocusSchedule): string {
    return `${this.formatTime(schedule.startHour, schedule.startMinute)} – ${this.formatTime(
      schedule.endHour,
      schedule.endMinute
    )}`;
  }
}
