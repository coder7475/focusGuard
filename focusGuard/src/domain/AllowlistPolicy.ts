export type AccessDecision = 'ALLOWED' | 'BLOCKED';

export class AllowlistPolicy {
  private ownPackageName: string;

  constructor(ownPackageName: string = 'com.coder7575.SleepGuard') {
    this.ownPackageName = ownPackageName;
  }

  evaluateAccess(
    packageName: string | null | undefined,
    resolvedDefaultDialer?: string | null,
    resolvedDefaultSms?: string | null,
    resolvedDefaultLauncher?: string | null,
    bagAllowedPackage?: string | null
  ): AccessDecision {
    if (!packageName || packageName.trim().length === 0) {
      return 'ALLOWED';
    }

    // 1. Own package is always allowed
    if (packageName === this.ownPackageName) {
      return 'ALLOWED';
    }

    // 2. Bag slot allowed app (user packed app during lockdown)
    if (bagAllowedPackage && packageName === bagAllowedPackage) {
      return 'ALLOWED';
    }

    // 2. Default dialer
    if (resolvedDefaultDialer && packageName === resolvedDefaultDialer) {
      return 'ALLOWED';
    }

    // 3. Default SMS
    if (resolvedDefaultSms && packageName === resolvedDefaultSms) {
      return 'ALLOWED';
    }

    // 4. Default home launcher
    if (resolvedDefaultLauncher && packageName === resolvedDefaultLauncher) {
      return 'ALLOWED';
    }

    // 5. Fallback OEM dialers & in-call UI
    if (
      AllowlistPolicy.OEM_DIALER_PACKAGES.has(packageName) ||
      packageName.includes('dialer') ||
      packageName.includes('incall')
    ) {
      return 'ALLOWED';
    }

    // 6. Emergency dialers
    if (AllowlistPolicy.OEM_EMERGENCY_PACKAGES.has(packageName)) {
      return 'ALLOWED';
    }

    // 7. Fallback OEM SMS apps
    if (AllowlistPolicy.OEM_SMS_PACKAGES.has(packageName)) {
      return 'ALLOWED';
    }

    // 8. Fallback OEM launchers
    if (AllowlistPolicy.OEM_LAUNCHER_PACKAGES.has(packageName)) {
      return 'ALLOWED';
    }

    return 'BLOCKED';
  }

  static readonly OEM_DIALER_PACKAGES = new Set([
    'com.google.android.dialer',
    'com.android.dialer',
    'com.samsung.android.dialer',
    'com.android.server.telecom',
    'com.android.incallui',
    'com.google.android.incallui',
    'com.oneplus.dialer',
    'com.miui.home',
  ]);

  static readonly OEM_EMERGENCY_PACKAGES = new Set([
    'com.android.phone.EmergencyDialer',
    'com.android.phone',
    'com.sec.android.app.easylauncher',
    'com.samsung.android.incallui',
  ]);

  static readonly OEM_SMS_PACKAGES = new Set([
    'com.google.android.apps.messaging',
    'com.android.mms',
    'com.samsung.android.messaging',
  ]);

  static readonly OEM_LAUNCHER_PACKAGES = new Set([
    'com.google.android.apps.nexuslauncher',
    'com.sec.android.app.launcher',
    'com.miui.home',
    'com.huawei.android.launcher',
    'com.oppo.launcher',
    'com.bbk.launcher2',
  ]);
}

export const allowlistPolicy = new AllowlistPolicy();
