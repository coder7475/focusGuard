import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Platform,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { router } from 'expo-router';
import FocusEnforcer from '../modules/FocusEnforcer';
import { Colors } from '../constants/colors';
import { preferenceStorage } from '../data/storage/PreferenceStorage';
import { OemBatteryGuideModal } from '../components/OemBatteryGuideModal';

export default function OnboardingScreen() {
  const insets = useSafeAreaInsets();
  const [a11yGranted, setA11yGranted] = useState(false);
  const [overlayGranted, setOverlayGranted] = useState(false);
  const [alarmGranted, setAlarmGranted] = useState(false);
  const [batteryGranted, setBatteryGranted] = useState(false);
  const [oemModalVisible, setOemModalVisible] = useState(false);

  const isAndroid13Plus =
    Platform.OS === 'android' &&
    (typeof Platform.Version === 'number'
      ? Platform.Version >= 33
      : parseInt(String(Platform.Version), 10) >= 33);

  const checkPermissions = () => {
    try {
      setA11yGranted(FocusEnforcer.getPermissionStatus('ACCESSIBILITY'));
      setOverlayGranted(FocusEnforcer.getPermissionStatus('OVERLAY'));
      setAlarmGranted(FocusEnforcer.getPermissionStatus('EXACT_ALARM'));
      setBatteryGranted(FocusEnforcer.getPermissionStatus('BATTERY'));
    } catch {
      // Fallback
    }
  };

  useEffect(() => {
    let mounted = true;
    Promise.resolve().then(() => {
      if (mounted) checkPermissions();
    });
    const interval = setInterval(() => {
      if (mounted) checkPermissions();
    }, 2000);
    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, []);

  const handleFinish = async () => {
    await preferenceStorage.setOnboardingCompleted(true);
    router.replace('/(tabs)' as any);
  };

  const coreReady = a11yGranted && alarmGranted;
  const allPermissionsGranted = a11yGranted && overlayGranted && alarmGranted && batteryGranted;

  // Auto-advancement when all permissions are confirmed granted
  useEffect(() => {
    if (allPermissionsGranted) {
      const timer = setTimeout(() => {
        handleFinish();
      }, 1200);
      return () => clearTimeout(timer);
    }
  }, [allPermissionsGranted]);

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={[
        styles.content,
        {
          paddingTop: Math.max(insets.top + 16, 44),
          paddingBottom: insets.bottom + 32,
        },
      ]}
    >
      <Text style={styles.title}>SYSTEM DIAGNOSTICS & ONBOARDING</Text>
      <Text style={styles.subtitle}>
        FocusGuard operates as an uncompromising hardware lock. Grant permissions below to establish OS-level focus protection.
      </Text>

      {/* 1. Accessibility Service */}
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <Text style={styles.cardTitle}>1. Accessibility Service</Text>
          <Text style={[styles.badge, a11yGranted ? styles.badgeReady : styles.badgeReq]}>
            {a11yGranted ? '✓ GRANTED' : 'REQUIRED'}
          </Text>
        </View>
        <Text style={styles.cardDesc}>
          Intercepts foreground app transitions without inspecting content (`canRetrieveWindowContent = false`).
        </Text>
        <TouchableOpacity
          style={styles.actionButton}
          onPress={() => FocusEnforcer.openSettingsForPermission('ACCESSIBILITY')}
        >
          <Text style={styles.actionButtonText}>CONFIGURE ACCESSIBILITY</Text>
        </TouchableOpacity>

        {isAndroid13Plus && !a11yGranted && (
          <View style={styles.restrictedNotice}>
            <Text style={styles.restrictedTitle}>⚠️ Android 13+ Restricted Setting Notice</Text>
            <Text style={styles.restrictedText}>
              If the Accessibility toggle is grayed out (&quot;Restricted setting&quot;):
              {'\n'}1. Tap &apos;OPEN APP INFO&apos; below.
              {'\n'}2. Tap the ⋮ (three dots menu) in the top-right corner.
              {'\n'}3. Tap &apos;Allow restricted settings&apos; & confirm with device PIN/biometrics.
              {'\n'}4. Return here and tap &apos;CONFIGURE ACCESSIBILITY&apos;.
            </Text>
            <TouchableOpacity
              style={styles.restrictedButton}
              onPress={() => FocusEnforcer.openSettingsForPermission('APP_DETAILS')}
            >
              <Text style={styles.restrictedButtonText}>OPEN APP INFO (ALLOW RESTRICTED)</Text>
            </TouchableOpacity>
          </View>
        )}
      </View>

      {/* 2. Display Over Other Apps */}
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <Text style={styles.cardTitle}>2. Display Over Other Apps</Text>
          <Text style={[styles.badge, overlayGranted ? styles.badgeReady : styles.badgeReq]}>
            {overlayGranted ? '✓ GRANTED' : 'RECOMMENDED'}
          </Text>
        </View>
        <Text style={styles.cardDesc}>
          Renders the un-dismissible lock screen countdown when distracting applications enter foreground.
        </Text>
        <TouchableOpacity
          style={styles.actionButton}
          onPress={() => FocusEnforcer.openSettingsForPermission('OVERLAY')}
        >
          <Text style={styles.actionButtonText}>GRANT OVERLAY PERMISSION</Text>
        </TouchableOpacity>
      </View>

      {/* 3. Exact Alarm Permission */}
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <Text style={styles.cardTitle}>3. Exact Alarms</Text>
          <Text style={[styles.badge, alarmGranted ? styles.badgeReady : styles.badgeReq]}>
            {alarmGranted ? '✓ GRANTED' : 'REQUIRED'}
          </Text>
        </View>
        <Text style={styles.cardDesc}>
          Wakes up device exactly on time at session end even in deep Android Doze power-saving states.
        </Text>
        <TouchableOpacity
          style={styles.actionButton}
          onPress={() => FocusEnforcer.openSettingsForPermission('EXACT_ALARM')}
        >
          <Text style={styles.actionButtonText}>ENABLE EXACT ALARMS</Text>
        </TouchableOpacity>
      </View>

      {/* 4. Battery Optimization */}
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <Text style={styles.cardTitle}>4. Unrestricted Battery</Text>
          <Text style={[styles.badge, batteryGranted ? styles.badgeReady : styles.badgeReq]}>
            {batteryGranted ? '✓ GRANTED' : 'RECOMMENDED'}
          </Text>
        </View>
        <Text style={styles.cardDesc}>
          Exempts SleepGuard from OS background process killers that prematurely kill focus sessions.
        </Text>
        <TouchableOpacity
          style={styles.actionButton}
          onPress={() => FocusEnforcer.openSettingsForPermission('BATTERY')}
        >
          <Text style={styles.actionButtonText}>EXEMPT BATTERY SAVER</Text>
        </TouchableOpacity>
      </View>

      {/* 5. OEM Guide Link */}
      <TouchableOpacity
        style={styles.oemGuideButton}
        onPress={() => setOemModalVisible(true)}
      >
        <Text style={styles.oemGuideText}>📱 OEM Battery Killer Guide (Samsung / Xiaomi / OnePlus)</Text>
      </TouchableOpacity>

      {/* Continue */}
      <TouchableOpacity
        style={[styles.continueButton, !coreReady && styles.continueButtonDisabled]}
        onPress={handleFinish}
      >
        <Text style={styles.continueText}>
          {allPermissionsGranted
            ? '✓ ALL GRANTED • ADVANCING...'
            : coreReady
            ? 'ENTER FOCUSGUARD →'
            : 'CONTINUE (PERMISSIONS PENDING)'}
        </Text>
      </TouchableOpacity>

      <OemBatteryGuideModal
        visible={oemModalVisible}
        onClose={() => setOemModalVisible(false)}
      />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.matteCharcoal,
  },
  content: {
    padding: 24,
    paddingTop: 56,
    paddingBottom: 48,
  },
  title: {
    fontSize: 22,
    fontWeight: '800',
    color: Colors.safetyOrange,
    letterSpacing: 0.5,
  },
  subtitle: {
    fontSize: 13,
    color: Colors.textMuted,
    lineHeight: 18,
    marginTop: 6,
    marginBottom: 24,
  },
  card: {
    backgroundColor: Colors.cardSurface,
    padding: 16,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: Colors.borderDark,
    marginBottom: 16,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  cardTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: Colors.offWhite,
  },
  cardDesc: {
    fontSize: 12,
    color: Colors.textMuted,
    lineHeight: 16,
    marginBottom: 12,
  },
  badge: {
    fontSize: 10,
    fontWeight: '800',
    paddingVertical: 3,
    paddingHorizontal: 8,
    borderRadius: 4,
  },
  badgeReady: {
    backgroundColor: 'rgba(0, 230, 118, 0.15)',
    color: Colors.successGreen,
  },
  badgeReq: {
    backgroundColor: 'rgba(255, 51, 102, 0.15)',
    color: Colors.dangerRed,
  },
  actionButton: {
    backgroundColor: Colors.darkGunmetal,
    paddingVertical: 10,
    borderRadius: 8,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: Colors.borderDark,
  },
  actionButtonText: {
    color: Colors.electricCyan,
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
  oemGuideButton: {
    padding: 14,
    borderRadius: 10,
    backgroundColor: 'rgba(0, 229, 255, 0.08)',
    borderWidth: 1,
    borderColor: 'rgba(0, 229, 255, 0.25)',
    alignItems: 'center',
    marginVertical: 12,
  },
  oemGuideText: {
    color: Colors.electricCyan,
    fontSize: 12,
    fontWeight: '700',
  },
  continueButton: {
    backgroundColor: Colors.safetyOrange,
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
    marginTop: 16,
  },
  continueButtonDisabled: {
    opacity: 0.6,
  },
  continueText: {
    color: Colors.matteCharcoal,
    fontSize: 15,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
  restrictedNotice: {
    marginTop: 12,
    padding: 12,
    backgroundColor: 'rgba(255, 87, 34, 0.1)',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: 'rgba(255, 87, 34, 0.3)',
  },
  restrictedTitle: {
    fontSize: 12,
    fontWeight: '700',
    color: Colors.safetyOrange,
    marginBottom: 6,
  },
  restrictedText: {
    fontSize: 11,
    color: Colors.offWhite,
    lineHeight: 16,
    marginBottom: 10,
  },
  restrictedButton: {
    backgroundColor: Colors.safetyOrange,
    paddingVertical: 8,
    borderRadius: 6,
    alignItems: 'center',
  },
  restrictedButtonText: {
    color: Colors.matteCharcoal,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
});
