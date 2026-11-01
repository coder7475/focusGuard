import React, { useEffect, useState, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Alert,
} from 'react-native';
import { RotaryDial } from '../../components/RotaryDial';
import { DurationChips } from '../../components/DurationChips';
import { Colors } from '../../constants/colors';
import { sessionEngine } from '../../domain/SessionEngine';
import { sessionRepository } from '../../data/repositories/SessionRepository';
import FocusEnforcer from '../../modules/FocusEnforcer';
import { FocusSession } from '../../domain/types';
import { OemBatteryGuideModal } from '../../components/OemBatteryGuideModal';

export default function FocusDashboard() {
  const [selectedMinutes, setSelectedMinutes] = useState(25);
  const [activeSession, setActiveSession] = useState<FocusSession | null>(null);
  const [remainingMs, setRemainingMs] = useState(0);
  const [stats, setStats] = useState({ completedCount: 0, totalFocusMinutes: 0 });
  const [oemModalVisible, setOemModalVisible] = useState(false);
  const timerRef = useRef<any>(null);

  const checkState = async () => {
    try {
      const rec = await sessionEngine.reconcileState();
      if (rec.type === 'STILL_ACTIVE') {
        setActiveSession(rec.session);
        setRemainingMs(rec.remainingMs);
      } else {
        setActiveSession(null);
        setRemainingMs(0);
      }
      const s = await sessionRepository.getStats();
      setStats(s);
    } catch {}
  };

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const rec = await sessionEngine.reconcileState();
        if (mounted) {
          if (rec.type === 'STILL_ACTIVE') {
            setActiveSession(rec.session);
            setRemainingMs(rec.remainingMs);
          } else {
            setActiveSession(null);
            setRemainingMs(0);
          }
          const s = await sessionRepository.getStats();
          setStats(s);
        }
      } catch {}
    })();

    timerRef.current = setInterval(() => {
      setRemainingMs((prev) => {
        if (prev <= 1000) {
          checkState();
          return 0;
        }
        return prev - 1000;
      });
    }, 1000);

    return () => {
      mounted = false;
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, []);

  const handleStartLock = async () => {
    const isA11yReady = FocusEnforcer.getPermissionStatus('ACCESSIBILITY');
    if (!isA11yReady) {
      Alert.alert(
        'Accessibility Required',
        'Please enable the SleepGuard Accessibility Service in System Settings before starting a focus lock.',
        [
          { text: 'Cancel', style: 'cancel' },
          {
            text: 'Open Settings',
            onPress: () => FocusEnforcer.openSettingsForPermission('ACCESSIBILITY'),
          },
        ]
      );
      return;
    }

    const durationMs = selectedMinutes * 60 * 1000;
    const res = await sessionEngine.startQuickSession(durationMs);
    if (res.success && res.session) {
      setActiveSession(res.session);
      setRemainingMs(durationMs);
    } else {
      Alert.alert('Error', res.error || 'Failed to start session');
    }
  };

  const formatCountdown = (ms: number) => {
    const totalSec = Math.floor(ms / 1000);
    const s = totalSec % 60;
    const m = Math.floor(totalSec / 60) % 60;
    const h = Math.floor(totalSec / 3600);
    const pad = (n: number) => String(n).padStart(2, '0');
    return h > 0 ? `${pad(h)}:${pad(m)}:${pad(s)}` : `${pad(m)}:${pad(s)}`;
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {/* Top Status Bar */}
      <View style={styles.statusBar}>
        <View style={styles.statusDotRow}>
          <View
            style={[
              styles.statusDot,
              activeSession ? styles.statusDotActive : styles.statusDotIdle,
            ]}
          />
          <Text style={styles.statusText}>
            {activeSession ? 'FOCUS LOCK ACTIVE' : 'SYSTEM READY'}
          </Text>
        </View>
        <TouchableOpacity onPress={() => setOemModalVisible(true)}>
          <Text style={styles.guideLink}>OEM Guide ℹ️</Text>
        </TouchableOpacity>
      </View>

      {/* Main Focus Dial / Active Timer Display */}
      {activeSession ? (
        <View style={styles.activeTimerCard}>
          <Text style={styles.activeLabel}>REMAINING FOCUS TIME</Text>
          <Text style={styles.countdownBig}>{formatCountdown(remainingMs)}</Text>
          <View style={styles.activeBadge}>
            <Text style={styles.activeBadgeText}>🔒 ALL DISTRACTIONS BLOCKED</Text>
          </View>
          <Text style={styles.lockNotice}>
            Switching apps is strictly prevented by the Accessibility Overlay.
          </Text>
        </View>
      ) : (
        <View style={styles.dialWrapper}>
          <RotaryDial
            value={selectedMinutes}
            onChange={setSelectedMinutes}
            size={280}
          />
          <DurationChips
            selectedMinutes={selectedMinutes}
            onSelectMinutes={setSelectedMinutes}
          />
        </View>
      )}

      {/* Primary Action Button */}
      {!activeSession ? (
        <TouchableOpacity
          style={styles.startButton}
          onPress={handleStartLock}
        >
          <Text style={styles.startButtonText}>
            ENGAGE FOCUS LOCK ({selectedMinutes} MIN)
          </Text>
        </TouchableOpacity>
      ) : (
        <TouchableOpacity
          style={styles.emergencyDialerBtn}
          onPress={() => FocusEnforcer.openEmergencyDialer()}
        >
          <Text style={styles.emergencyDialerText}>📞 EMERGENCY DIALER</Text>
        </TouchableOpacity>
      )}

      {/* Quick Statistics Banner */}
      <View style={styles.statsContainer}>
        <View style={styles.statCard}>
          <Text style={styles.statNumber}>{stats.completedCount}</Text>
          <Text style={styles.statLabel}>SESSIONS FINISHED</Text>
        </View>
        <View style={styles.statCard}>
          <Text style={styles.statNumber}>{stats.totalFocusMinutes}m</Text>
          <Text style={styles.statLabel}>TOTAL TIME</Text>
        </View>
      </View>

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
    padding: 20,
    paddingTop: 56,
    paddingBottom: 40,
    alignItems: 'center',
  },
  statusBar: {
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  statusDotRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  statusDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
  },
  statusDotActive: {
    backgroundColor: Colors.safetyOrange,
    shadowColor: Colors.safetyOrange,
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.9,
    shadowRadius: 5,
    elevation: 5,
  },
  statusDotIdle: {
    backgroundColor: Colors.successGreen,
  },
  statusText: {
    fontSize: 12,
    fontWeight: '800',
    color: Colors.offWhite,
    letterSpacing: 1,
  },
  guideLink: {
    fontSize: 12,
    color: Colors.electricCyan,
    fontWeight: '700',
  },
  dialWrapper: {
    alignItems: 'center',
    marginVertical: 12,
  },
  activeTimerCard: {
    width: '100%',
    backgroundColor: Colors.cardSurface,
    borderRadius: 20,
    borderWidth: 2,
    borderColor: Colors.safetyOrange,
    padding: 32,
    alignItems: 'center',
    marginVertical: 24,
  },
  activeLabel: {
    fontSize: 12,
    fontWeight: '800',
    color: Colors.textMuted,
    letterSpacing: 2,
  },
  countdownBig: {
    fontSize: 56,
    fontWeight: '800',
    color: Colors.offWhite,
    marginVertical: 16,
    letterSpacing: -1,
  },
  activeBadge: {
    backgroundColor: Colors.safetyOrangeDim,
    paddingVertical: 6,
    paddingHorizontal: 12,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: Colors.safetyOrange,
    marginBottom: 12,
  },
  activeBadgeText: {
    color: Colors.safetyOrange,
    fontSize: 12,
    fontWeight: '800',
  },
  lockNotice: {
    fontSize: 12,
    color: Colors.textMuted,
    textAlign: 'center',
    lineHeight: 16,
  },
  startButton: {
    width: '100%',
    backgroundColor: Colors.safetyOrange,
    paddingVertical: 18,
    borderRadius: 14,
    alignItems: 'center',
    shadowColor: Colors.safetyOrange,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.35,
    shadowRadius: 8,
    elevation: 8,
    marginVertical: 16,
  },
  startButtonText: {
    color: Colors.matteCharcoal,
    fontSize: 15,
    fontWeight: '800',
    letterSpacing: 0.5,
  },
  emergencyDialerBtn: {
    width: '100%',
    backgroundColor: 'rgba(255, 51, 102, 0.15)',
    borderWidth: 1,
    borderColor: Colors.dangerRed,
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: 'center',
    marginVertical: 16,
  },
  emergencyDialerText: {
    color: Colors.dangerRed,
    fontSize: 14,
    fontWeight: '800',
  },
  statsContainer: {
    width: '100%',
    flexDirection: 'row',
    gap: 12,
    marginTop: 8,
  },
  statCard: {
    flex: 1,
    backgroundColor: Colors.cardSurface,
    borderRadius: 14,
    padding: 16,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: Colors.borderDark,
  },
  statNumber: {
    fontSize: 24,
    fontWeight: '800',
    color: Colors.offWhite,
  },
  statLabel: {
    fontSize: 10,
    fontWeight: '700',
    color: Colors.textMuted,
    letterSpacing: 1,
    marginTop: 4,
  },
});
