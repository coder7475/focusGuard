import { Colors, BrandColors } from '../constants/colors';
import { DURATION_STEPS, PRESET_DURATIONS } from '../constants/timer';
import { allowlistPolicy, AllowlistPolicy } from '../domain/AllowlistPolicy';
import { sessionEngine } from '../domain/SessionEngine';
import { ScheduleEngine } from '../domain/ScheduleEngine';
import { DB_NAME } from '../data/db';
import FocusEnforcer from '../modules/FocusEnforcer';

describe('Sanity Check Suite', () => {
  test('brand theme colors are defined with high contrast tokens', () => {
    expect(Colors.matteCharcoal).toBe('#121316');
    expect(Colors.safetyOrange).toBe('#FF5722');
    expect(Colors.electricCyan).toBe('#00E5FF');
    expect(BrandColors).toBe(Colors);
  });

  test('rotary dial duration steps and presets contain discrete intervals', () => {
    expect(DURATION_STEPS).toEqual([15, 25, 45, 60, 90, 120, 180, 240]);
    expect(PRESET_DURATIONS).toEqual([15, 25, 45, 60]);
  });

  test('allowlist policy has complete fallback OEM package sets', () => {
    expect(AllowlistPolicy.OEM_DIALER_PACKAGES.has('com.google.android.dialer')).toBe(true);
    expect(AllowlistPolicy.OEM_EMERGENCY_PACKAGES.has('com.android.phone.EmergencyDialer')).toBe(true);
    expect(allowlistPolicy.evaluateAccess('com.android.phone')).toBe('ALLOWED');
  });

  test('domain engine singletons are properly instantiated', () => {
    expect(sessionEngine).toBeDefined();
    expect(typeof sessionEngine.startQuickSession).toBe('function');
    expect(typeof ScheduleEngine.isInsideScheduleWindow).toBe('function');
  });

  test('database configuration targets focusguard.db', () => {
    expect(DB_NAME).toBe('focusguard.db');
  });

  test('focus enforcer interface exposes all permission types including APP_DETAILS', () => {
    expect(FocusEnforcer).toBeDefined();
    expect(typeof FocusEnforcer.openSettingsForPermission).toBe('function');
    expect(FocusEnforcer.openSettingsForPermission('APP_DETAILS')).toBe(true);
    expect(FocusEnforcer.openSettingsForPermission('ACCESSIBILITY')).toBe(true);
  });
});
