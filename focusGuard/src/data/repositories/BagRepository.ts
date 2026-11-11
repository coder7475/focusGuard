import { preferenceStorage } from '../storage/PreferenceStorage';
import FocusEnforcer, { InstalledAppItem } from '../../modules/FocusEnforcer';

export interface BagSlot {
  packageName: string | null;
  label: string | null;
  isPacked: boolean;
}

export class BagRepository {
  async getBagSlot(): Promise<BagSlot> {
    try {
      // Check native first if available
      const native = FocusEnforcer.getBagApp();
      if (native && native.packageName) {
        return {
          packageName: native.packageName,
          label: native.label || native.packageName,
          isPacked: true,
        };
      }
    } catch {}

    const stored = await preferenceStorage.getBagApp();
    if (stored) {
      // Sync to native if missing
      try {
        FocusEnforcer.setBagApp(stored.packageName, stored.label);
      } catch {}
      return {
        packageName: stored.packageName,
        label: stored.label,
        isPacked: true,
      };
    }

    return {
      packageName: null,
      label: null,
      isPacked: false,
    };
  }

  async setBagApp(packageName: string, label: string): Promise<void> {
    await preferenceStorage.setBagApp(packageName, label);
    try {
      FocusEnforcer.setBagApp(packageName, label);
    } catch {}
  }

  async clearBagApp(): Promise<void> {
    await preferenceStorage.clearBagApp();
    try {
      FocusEnforcer.clearBagApp();
    } catch {}
  }

  getInstalledApps(): InstalledAppItem[] {
    try {
      return FocusEnforcer.getInstalledApps() || [];
    } catch {
      return [];
    }
  }

  openApp(packageName: string): boolean {
    try {
      return FocusEnforcer.openApp(packageName);
    } catch {
      return false;
    }
  }
}

export const bagRepository = new BagRepository();
