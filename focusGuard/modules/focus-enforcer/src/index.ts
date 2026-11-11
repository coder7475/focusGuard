import { requireNativeModule } from 'expo';

export type PermissionType = 'ACCESSIBILITY' | 'EXACT_ALARM' | 'BATTERY' | 'OVERLAY' | 'APP_DETAILS';

export interface InstalledAppItem {
  packageName: string;
  label: string;
}

export interface FocusEnforcerNative {
  startFocusLock(endsAtEpochMs: number): boolean;
  stopFocusLock(): boolean;
  rescheduleRoutines(): boolean;
  getPermissionStatus(permissionName: PermissionType): boolean;
  openSettingsForPermission(permissionName: PermissionType): boolean;
  openEmergencyDialer(): boolean;
  getInstalledApps(): InstalledAppItem[];
  setBagApp(packageName: string, label: string): boolean;
  getBagApp(): InstalledAppItem | null;
  clearBagApp(): boolean;
  openApp(packageName: string): boolean;
}

let nativeModule: FocusEnforcerNative | null = null;

try {
  nativeModule = requireNativeModule<FocusEnforcerNative>('FocusEnforcer');
} catch (e) {
  // Fallback for non-Android / development simulators
  nativeModule = {
    startFocusLock: () => true,
    stopFocusLock: () => true,
    rescheduleRoutines: () => true,
    getPermissionStatus: () => true,
    openSettingsForPermission: () => true,
    openEmergencyDialer: () => true,
    getInstalledApps: () => [],
    setBagApp: () => true,
    getBagApp: () => null,
    clearBagApp: () => true,
    openApp: () => true,
  };
}

const FocusEnforcer = nativeModule!;

export default FocusEnforcer;
