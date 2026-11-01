import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { Colors } from '../../constants/colors';
import FocusEnforcer from '../../modules/FocusEnforcer';
import { router } from 'expo-router';

export default function ExploreScreen() {
  const [a11yStatus, setA11yStatus] = useState(false);
  const [alarmStatus, setAlarmStatus] = useState(false);
  const [batteryStatus, setBatteryStatus] = useState(false);

  useEffect(() => {
    let mounted = true;
    Promise.resolve().then(() => {
      if (mounted) {
        try {
          setA11yStatus(FocusEnforcer.getPermissionStatus('ACCESSIBILITY'));
          setAlarmStatus(FocusEnforcer.getPermissionStatus('EXACT_ALARM'));
          setBatteryStatus(FocusEnforcer.getPermissionStatus('BATTERY'));
        } catch {}
      }
    });
    return () => {
      mounted = false;
    };
  }, []);

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>SYSTEM ARCHITECTURE</Text>
      <Text style={styles.subtitle}>
        FocusGuard operates on a zero-network, 100% offline security model with Continuous Native Generation (CNG).
      </Text>

      {/* Security Status Card */}
      <View style={styles.card}>
        <Text style={styles.cardHeader}>PRIVACY & PERMISSION STATUS</Text>
        <View style={styles.row}>
          <Text style={styles.label}>Accessibility Enforcer:</Text>
          <Text style={{ color: a11yStatus ? Colors.successGreen : Colors.dangerRed, fontWeight: 'bold' }}>
            {a11yStatus ? 'ACTIVE' : 'DISABLED'}
          </Text>
        </View>
        <View style={styles.row}>
          <Text style={styles.label}>Exact Alarms (Doze Proof):</Text>
          <Text style={{ color: alarmStatus ? Colors.successGreen : Colors.dangerRed, fontWeight: 'bold' }}>
            {alarmStatus ? 'ACTIVE' : 'DISABLED'}
          </Text>
        </View>
        <View style={styles.row}>
          <Text style={styles.label}>Battery Killer Exemption:</Text>
          <Text style={{ color: batteryStatus ? Colors.successGreen : Colors.dangerRed, fontWeight: 'bold' }}>
            {batteryStatus ? 'EXEMPTED' : 'PENDING'}
          </Text>
        </View>
        <TouchableOpacity
          style={styles.actionBtn}
          onPress={() => router.push('/onboarding' as any)}
        >
          <Text style={styles.actionBtnText}>RUN ONBOARDING DIAGNOSTICS</Text>
        </TouchableOpacity>
      </View>

      {/* Emergency Allowlist Card */}
      <View style={styles.card}>
        <Text style={styles.cardHeader}>DYNAMIC EMERGENCY ALLOWLIST</Text>
        <Text style={styles.body}>
          The following packages are dynamically detected and NEVER blocked during active focus locks:
        </Text>
        <View style={styles.bulletItem}>
          <Text style={styles.bulletTitle}>• System Default Phone Dialer</Text>
          <Text style={styles.bulletDesc}>Resolved dynamically via TelecomManager</Text>
        </View>
        <View style={styles.bulletItem}>
          <Text style={styles.bulletTitle}>• Emergency Numbers (911, 112, 999)</Text>
          <Text style={styles.bulletDesc}>Direct bypass with one-touch button on lock overlay</Text>
        </View>
        <View style={styles.bulletItem}>
          <Text style={styles.bulletTitle}>• Default SMS Handler</Text>
          <Text style={styles.bulletDesc}>Allows essential two-factor authentication and carrier texts</Text>
        </View>
        <View style={styles.bulletItem}>
          <Text style={styles.bulletTitle}>• System Home Launcher</Text>
          <Text style={styles.bulletDesc}>Permits returning home without allowing blocked app launching</Text>
        </View>
      </View>

      {/* Offline Commitment Card */}
      <View style={styles.card}>
        <Text style={styles.cardHeader}>ZERO-TELEMETRY GUARANTEE</Text>
        <Text style={styles.body}>
          • Zero internet permissions in manifest (`android.permission.INTERNET` is not used for analytics).
          {'\n'}• Zero screen content inspection (`canRetrieveWindowContent = false`).
          {'\n'}• All sessions and schedules stored locally in encrypted SQLite storage.
        </Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.matteCharcoal,
  },
  content: {
    padding: 20,
    paddingTop: 56,
    paddingBottom: 40,
  },
  title: {
    fontSize: 20,
    fontWeight: '800',
    color: Colors.offWhite,
    letterSpacing: 0.5,
  },
  subtitle: {
    fontSize: 13,
    color: Colors.textMuted,
    lineHeight: 18,
    marginTop: 6,
    marginBottom: 20,
  },
  card: {
    backgroundColor: Colors.cardSurface,
    borderRadius: 14,
    padding: 16,
    borderWidth: 1,
    borderColor: Colors.borderDark,
    marginBottom: 16,
  },
  cardHeader: {
    fontSize: 14,
    fontWeight: '800',
    color: Colors.safetyOrange,
    letterSpacing: 1,
    marginBottom: 12,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  label: {
    fontSize: 13,
    color: Colors.offWhite,
  },
  body: {
    fontSize: 13,
    color: Colors.textMuted,
    lineHeight: 18,
    marginBottom: 10,
  },
  bulletItem: {
    marginBottom: 8,
  },
  bulletTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: Colors.offWhite,
  },
  bulletDesc: {
    fontSize: 11,
    color: Colors.textMuted,
    marginLeft: 10,
  },
  actionBtn: {
    backgroundColor: Colors.darkGunmetal,
    paddingVertical: 10,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 12,
    borderWidth: 1,
    borderColor: Colors.borderDark,
  },
  actionBtnText: {
    color: Colors.electricCyan,
    fontSize: 11,
    fontWeight: '800',
  },
});
