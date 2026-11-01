import { AllowlistPolicy } from '../AllowlistPolicy';

describe('AllowlistPolicy', () => {
  const policy = new AllowlistPolicy('com.coder7575.SleepGuard');

  test('allows own package unconditionally', () => {
    expect(policy.evaluateAccess('com.coder7575.SleepGuard')).toBe('ALLOWED');
  });

  test('allows system default dialer', () => {
    expect(
      policy.evaluateAccess(
        'com.custom.dialer',
        'com.custom.dialer', // resolved default dialer
        null,
        null
      )
    ).toBe('ALLOWED');
  });

  test('allows OEM dialers and in-call UI', () => {
    expect(policy.evaluateAccess('com.google.android.dialer')).toBe('ALLOWED');
    expect(policy.evaluateAccess('com.samsung.android.dialer')).toBe('ALLOWED');
    expect(policy.evaluateAccess('com.android.incallui')).toBe('ALLOWED');
    expect(policy.evaluateAccess('com.google.android.incallui')).toBe('ALLOWED');
  });

  test('allows emergency packages unconditionally', () => {
    expect(policy.evaluateAccess('com.android.phone.EmergencyDialer')).toBe('ALLOWED');
    expect(policy.evaluateAccess('com.android.phone')).toBe('ALLOWED');
    expect(policy.evaluateAccess('com.samsung.android.incallui')).toBe('ALLOWED');
  });

  test('allows default SMS and OEM SMS packages', () => {
    expect(
      policy.evaluateAccess(
        'com.custom.sms',
        null,
        'com.custom.sms', // resolved default SMS
        null
      )
    ).toBe('ALLOWED');
    expect(policy.evaluateAccess('com.google.android.apps.messaging')).toBe('ALLOWED');
  });

  test('allows default home launcher', () => {
    expect(
      policy.evaluateAccess(
        'com.teslacoilsw.launcher',
        null,
        null,
        'com.teslacoilsw.launcher' // resolved default launcher
      )
    ).toBe('ALLOWED');
    expect(policy.evaluateAccess('com.miui.home')).toBe('ALLOWED');
    expect(policy.evaluateAccess('com.sec.android.app.launcher')).toBe('ALLOWED');
  });

  test('blocks non-allowlisted social media and games', () => {
    expect(policy.evaluateAccess('com.instagram.android')).toBe('BLOCKED');
    expect(policy.evaluateAccess('com.zhiliaoapp.musically')).toBe('BLOCKED');
    expect(policy.evaluateAccess('com.twitter.android')).toBe('BLOCKED');
    expect(policy.evaluateAccess('com.facebook.katana')).toBe('BLOCKED');
    expect(policy.evaluateAccess('com.google.android.youtube')).toBe('BLOCKED');
  });

  test('allows null, empty, or whitespace packages without crash', () => {
    expect(policy.evaluateAccess(null)).toBe('ALLOWED');
    expect(policy.evaluateAccess('')).toBe('ALLOWED');
    expect(policy.evaluateAccess('   ')).toBe('ALLOWED');
  });
});
