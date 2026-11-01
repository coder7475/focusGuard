export const requireNativeModule = jest.fn(() => ({
  startFocusLock: jest.fn(() => true),
  stopFocusLock: jest.fn(() => true),
  getPermissionStatus: jest.fn(() => true),
  openSettingsForPermission: jest.fn(() => true),
  openEmergencyDialer: jest.fn(() => true),
}));
