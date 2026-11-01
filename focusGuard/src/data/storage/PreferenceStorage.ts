import * as SecureStore from 'expo-secure-store';

const KEY_ONBOARDING_COMPLETED = 'sleepguard_onboarding_completed';
const KEY_DEFAULT_DURATION = 'sleepguard_default_duration_min';

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
}

export const preferenceStorage = new PreferenceStorage();
