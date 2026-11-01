export type SessionStatus = 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
export type SessionType = 'QUICK' | 'SCHEDULED' | 'CUSTOM';

export interface FocusSession {
  id: string;
  startedAtEpochMs: number;
  endsAtEpochMs: number;
  durationMs: number;
  type: SessionType;
  status: SessionStatus;
}

export interface FocusSchedule {
  id: string;
  name: string;
  startHour: number;
  startMinute: number;
  endHour: number;
  endMinute: number;
  daysOfWeek: number[]; // 1 = Monday, 7 = Sunday (or 0 = Sunday)
  isEnabled: boolean;
  createdAtEpochMs: number;
}

export interface PermissionStatus {
  accessibility: boolean;
  exactAlarm: boolean;
  batteryOptimization: boolean;
  overlay: boolean;
}
