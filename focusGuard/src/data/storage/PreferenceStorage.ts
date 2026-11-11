import * as SecureStore from 'expo-secure-store';

const KEY_ONBOARDING_COMPLETED = 'sleepguard_onboarding_completed';
const KEY_DEFAULT_DURATION = 'sleepguard_default_duration_min';
const KEY_BAG_PACKAGE = 'sleepguard_bag_package';
const KEY_BAG_LABEL = 'sleepguard_bag_label';

export class PreferenceStorage {
  async isOnboardingCompleted(): Promise<boolean> {
    try {
      const val = await SecureStore.getItemAsync(KEY_ONBOARDING_COMPLETED);
      return val === 'true';
    } catch {
      return false;
    }
  }

  async setOnboardingCompleted(completed: boolean): Promise<void> {
    try {
      await SecureStore.setItemAsync(KEY_ONBOARDING_COMPLETED, completed ? 'true' : 'false');
    } catch {}
  }

  async getDefaultDurationMinutes(): Promise<number> {
    try {
      const val = await SecureStore.getItemAsync(KEY_DEFAULT_DURATION);
      return val ? parseInt(val, 10) : 25;
    } catch {
      return 25;
    }
  }

  async setDefaultDurationMinutes(minutes: number): Promise<void> {
    try {
      await SecureStore.setItemAsync(KEY_DEFAULT_DURATION, minutes.toString());
    } catch {}
  }

  async getBagApp(): Promise<{ packageName: string; label: string } | null> {
    try {
      const pkg = await SecureStore.getItemAsync(KEY_BAG_PACKAGE);
      const label = await SecureStore.getItemAsync(KEY_BAG_LABEL);
      if (pkg && pkg.trim().length > 0) {
        return { packageName: pkg, label: label || pkg };
      }
      return null;
    } catch {
      return null;
    }
  }

  async setBagApp(packageName: string, label: string): Promise<void> {
    try {
      await SecureStore.setItemAsync(KEY_BAG_PACKAGE, packageName);
      await SecureStore.setItemAsync(KEY_BAG_LABEL, label);
    } catch {}
  }

  async clearBagApp(): Promise<void> {
    try {
      await SecureStore.deleteItemAsync(KEY_BAG_PACKAGE);
      await SecureStore.deleteItemAsync(KEY_BAG_LABEL);
    } catch {}
  }
}

export const preferenceStorage = new PreferenceStorage();
