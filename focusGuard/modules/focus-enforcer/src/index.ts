import { requireNativeModule } from 'expo-modules-core';

export type PermissionType = 'ACCESSIBILITY' | 'EXACT_ALARM' | 'BATTERY' | 'OVERLAY';

export interface FocusEnforcerNative {
  startFocusLock(endsAtEpochMs: number): boolean;
  stopFocusLock(): boolean;
  getPermissionStatus(permissionName: PermissionType): boolean;
  openSettingsForPermission(permissionName: PermissionType): boolean;
  openEmergencyDialer(): boolean;
}

let nativeModule: FocusEnforcerNative | null = null;

try {
  nativeModule = requireNativeModule<FocusEnforcerNative>('FocusEnforcer');
} catch (e) {
  // Fallback for non-Android / development simulators
  nativeModule = {
    startFocusLock: () => true,
    stopFocusLock: () => true,
    getPermissionStatus: () => true,
    openSettingsForPermission: () => true,
    openEmergencyDialer: () => true,
  };
}

const FocusEnforcer = nativeModule!;

export default FocusEnforcer;
