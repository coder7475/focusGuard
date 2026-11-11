import React, { useState, useEffect, useMemo } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Modal,
  TextInput,
  FlatList,
  Platform,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import * as Haptics from 'expo-haptics';
import { Colors } from '../../constants/colors';
import { bagRepository, BagSlot } from '../../data/repositories/BagRepository';
import { InstalledAppItem } from '../../modules/FocusEnforcer';

export default function BagScreen() {
  const insets = useSafeAreaInsets();
  const [bagSlot, setBagSlot] = useState<BagSlot>({
    packageName: null,
    label: null,
    isPacked: false,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [installedApps, setInstalledApps] = useState<InstalledAppItem[]>([]);

  const loadBagState = async () => {
    try {
      const slot = await bagRepository.getBagSlot();
      setBagSlot(slot);
    } catch {}
  };

  useEffect(() => {
    loadBagState();
  }, []);

  const openPicker = () => {
    try {
      Haptics.selectionAsync();
    } catch {}
    const apps = bagRepository.getInstalledApps();
    setInstalledApps(apps);
    setSearchQuery('');
    setModalVisible(true);
  };

  const handleSelectApp = async (app: InstalledAppItem) => {
    try {
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
    } catch {}
    await bagRepository.setBagApp(app.packageName, app.label);
    setBagSlot({
      packageName: app.packageName,
      label: app.label,
      isPacked: true,
    });
    setModalVisible(false);
  };

  const handleUnpack = async () => {
    try {
      Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    } catch {}
    await bagRepository.clearBagApp();
    setBagSlot({
      packageName: null,
      label: null,
      isPacked: false,
    });
  };

  const filteredApps = useMemo(() => {
    if (!searchQuery.trim()) return installedApps;
    const query = searchQuery.trim().toLowerCase();
    return installedApps.filter(
      (app) =>
        app.label.toLowerCase().includes(query) ||
        app.packageName.toLowerCase().includes(query)
    );
  }, [installedApps, searchQuery]);

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={[
        styles.content,
        {
          paddingTop: Math.max(insets.top + 16, 44),
          paddingBottom: insets.bottom + 24,
        },
      ]}
    >
      {/* Header Bar */}
      <View style={styles.headerRow}>
        <View>
          <Text style={styles.title}>LOCKDOWN BAG</Text>
          <Text style={styles.subtitle}>Pack 1 tool allowed during lockdown</Text>
        </View>
        <View style={styles.slotBadge}>
          <Text style={styles.slotBadgeText}>1 SLOT</Text>
        </View>
      </View>

      {/* Protocol Explanation Card */}
      <View style={styles.card}>
        <Text style={styles.cardHeader}>THE SUITCASE PROTOCOL</Text>
        <Text style={styles.cardBody}>
          During an active focus session or routine lockdown, all non-essential applications are strictly obstructed.
        </Text>
        <Text style={[styles.cardBody, { marginTop: 6 }]}>
          You have exactly <Text style={{ color: Colors.electricCyan, fontWeight: 'bold' }}>one bag slot</Text> to pack a single critical productivity tool (such as Maps, Spotify, Notes, or Kindle) that is granted full bypass through the discipline barrier.
        </Text>
      </View>

      {/* Slot 1 Management Card */}
      <Text style={styles.sectionTitle}>BAG SLOT 1 OF 1</Text>
      <View
        style={[
          styles.slotCard,
          bagSlot.isPacked ? styles.slotCardPacked : styles.slotCardEmpty,
        ]}
      >
        <View style={styles.slotStatusRow}>
          <View style={styles.statusDotRow}>
            <View
              style={[
                styles.statusDot,
                bagSlot.isPacked ? styles.statusDotPacked : styles.statusDotEmpty,
              ]}
            />
            <Text
              style={[
                styles.statusLabel,
                bagSlot.isPacked ? { color: Colors.electricCyan } : { color: Colors.textMuted },
              ]}
            >
              {bagSlot.isPacked ? 'PACKED & READY' : 'EMPTY SLOT'}
            </Text>
          </View>
          {bagSlot.isPacked && (
            <View style={styles.allowedTag}>
              <Text style={styles.allowedTagText}>ALLOWED</Text>
            </View>
          )}
        </View>

        {bagSlot.isPacked ? (
          <View style={styles.slotDetails}>
            <Text style={styles.appName}>{bagSlot.label}</Text>
            <Text style={styles.appPackage}>{bagSlot.packageName}</Text>

            <View style={styles.actionRow}>
              <TouchableOpacity style={styles.changeBtn} onPress={openPicker}>
                <Text style={styles.changeBtnText}>CHANGE APP</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.unpackBtn} onPress={handleUnpack}>
                <Text style={styles.unpackBtnText}>UNPACK</Text>
              </TouchableOpacity>
            </View>
          </View>
        ) : (
          <View style={styles.slotDetails}>
            <Text style={styles.emptyTitle}>No application packed in bag</Text>
            <Text style={styles.emptyDescription}>
              Select 1 app that you can freely launch and use during active focus sessions.
            </Text>
            <TouchableOpacity style={styles.packBtn} onPress={openPicker}>
              <Text style={styles.packBtnText}>+ PACK AN APP</Text>
            </TouchableOpacity>
          </View>
        )}
      </View>

      {/* App Picker Modal */}
      <Modal
        visible={modalVisible}
        animationType="slide"
        transparent={true}
        onRequestClose={() => setModalVisible(false)}
      >
        <View style={styles.modalOverlay}>
          <View
            style={[
              styles.modalContent,
              { paddingBottom: Math.max(insets.bottom + 16, 24) },
            ]}
          >
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>SELECT APP FOR BAG</Text>
              <TouchableOpacity
                onPress={() => setModalVisible(false)}
                style={styles.modalCloseBtn}
              >
                <Text style={styles.modalCloseText}>✕</Text>
              </TouchableOpacity>
            </View>

            <TextInput
              style={styles.searchInput}
              placeholder="Search installed apps..."
              placeholderTextColor={Colors.textMuted}
              value={searchQuery}
              onChangeText={setSearchQuery}
              autoCapitalize="none"
              autoCorrect={false}
            />

            {filteredApps.length === 0 ? (
              <View style={styles.emptySearchContainer}>
                <Text style={styles.emptySearchText}>
                  {installedApps.length === 0
                    ? 'No launchable apps available or querying unsupported on this device.'
                    : 'No apps found matching search.'}
                </Text>
              </View>
            ) : (
              <FlatList
                data={filteredApps}
                keyExtractor={(item) => item.packageName}
                keyboardShouldPersistTaps="handled"
                renderItem={({ item }) => (
                  <TouchableOpacity
                    style={styles.appItemRow}
                    onPress={() => handleSelectApp(item)}
                  >
                    <View style={styles.appAvatar}>
                      <Text style={styles.appAvatarText}>
                        {item.label.charAt(0).toUpperCase()}
                      </Text>
                    </View>
                    <View style={styles.appInfo}>
                      <Text style={styles.appItemLabel}>{item.label}</Text>
                      <Text style={styles.appItemPackage}>{item.packageName}</Text>
                    </View>
                  </TouchableOpacity>
                )}
              />
            )}
          </View>
        </View>
      </Modal>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.matteCharcoal,
  },
  content: {
    paddingHorizontal: 20,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  title: {
    fontSize: 20,
    fontWeight: '900',
    color: Colors.offWhite,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
    letterSpacing: 1.5,
  },
  subtitle: {
    fontSize: 12,
    color: Colors.textMuted,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
    marginTop: 2,
  },
  slotBadge: {
    backgroundColor: Colors.darkGunmetal,
    borderColor: Colors.borderDark,
    borderWidth: 1,
    borderRadius: 6,
    paddingHorizontal: 10,
    paddingVertical: 5,
  },
  slotBadgeText: {
    color: Colors.electricCyan,
    fontSize: 11,
    fontWeight: '700',
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
  },
  card: {
    backgroundColor: Colors.darkGunmetal,
    borderColor: Colors.borderDark,
    borderWidth: 1,
    borderRadius: 12,
    padding: 16,
    marginBottom: 20,
  },
  cardHeader: {
    fontSize: 12,
    fontWeight: '700',
    color: Colors.electricCyan,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
    letterSpacing: 1,
    marginBottom: 6,
  },
  cardBody: {
    fontSize: 12,
    color: Colors.textMuted,
    lineHeight: 18,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
  },
  sectionTitle: {
    fontSize: 11,
    fontWeight: '700',
    color: Colors.textMuted,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
    letterSpacing: 1.5,
    marginBottom: 8,
  },
  slotCard: {
    backgroundColor: Colors.darkGunmetal,
    borderRadius: 14,
    padding: 18,
    borderWidth: 1,
    marginBottom: 24,
  },
  slotCardEmpty: {
    borderColor: Colors.borderDark,
  },
  slotCardPacked: {
    borderColor: Colors.electricCyan,
  },
  slotStatusRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  statusDotRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 8,
  },
  statusDotEmpty: {
    backgroundColor: Colors.borderDark,
  },
  statusDotPacked: {
    backgroundColor: Colors.electricCyan,
  },
  statusLabel: {
    fontSize: 11,
    fontWeight: '700',
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
    letterSpacing: 1,
  },
  allowedTag: {
    backgroundColor: 'rgba(0, 229, 255, 0.15)',
    borderColor: 'rgba(0, 229, 255, 0.4)',
    borderWidth: 1,
    borderRadius: 4,
    paddingHorizontal: 6,
    paddingVertical: 2,
  },
  allowedTagText: {
    color: Colors.electricCyan,
    fontSize: 9,
    fontWeight: '700',
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
  },
  slotDetails: {
    marginTop: 4,
  },
  appName: {
    fontSize: 18,
    fontWeight: '800',
    color: Colors.offWhite,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
  },
  appPackage: {
    fontSize: 11,
    color: Colors.textMuted,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
    marginTop: 2,
  },
  actionRow: {
    flexDirection: 'row',
    marginTop: 18,
    gap: 10,
  },
  changeBtn: {
    flex: 1,
    backgroundColor: 'transparent',
    borderColor: Colors.borderDark,
    borderWidth: 1,
    borderRadius: 8,
    paddingVertical: 12,
    alignItems: 'center',
  },
  changeBtnText: {
    color: Colors.offWhite,
    fontSize: 11,
    fontWeight: '700',
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
  },
  unpackBtn: {
    flex: 1,
    backgroundColor: 'rgba(255, 51, 102, 0.15)',
    borderColor: 'rgba(255, 51, 102, 0.4)',
    borderWidth: 1,
    borderRadius: 8,
    paddingVertical: 12,
    alignItems: 'center',
  },
  unpackBtnText: {
    color: Colors.dangerRed,
    fontSize: 11,
    fontWeight: '700',
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
  },
  emptyTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: Colors.offWhite,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
  },
  emptyDescription: {
    fontSize: 12,
    color: Colors.textMuted,
    lineHeight: 16,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
    marginTop: 4,
    marginBottom: 16,
  },
  packBtn: {
    backgroundColor: Colors.safetyOrange,
    borderRadius: 8,
    paddingVertical: 14,
    alignItems: 'center',
  },
  packBtnText: {
    color: Colors.matteCharcoal,
    fontSize: 12,
    fontWeight: '900',
    letterSpacing: 1,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.75)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    backgroundColor: Colors.darkGunmetal,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    paddingHorizontal: 20,
    paddingTop: 20,
    maxHeight: '80%',
    borderColor: Colors.borderDark,
    borderTopWidth: 1,
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 14,
  },
  modalTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: Colors.offWhite,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
    letterSpacing: 1,
  },
  modalCloseBtn: {
    padding: 6,
  },
  modalCloseText: {
    color: Colors.textMuted,
    fontSize: 16,
    fontWeight: 'bold',
  },
  searchInput: {
    backgroundColor: Colors.matteCharcoal,
    borderColor: Colors.borderDark,
    borderWidth: 1,
    borderRadius: 8,
    color: Colors.offWhite,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
    fontSize: 13,
    marginBottom: 12,
  },
  emptySearchContainer: {
    padding: 24,
    alignItems: 'center',
  },
  emptySearchText: {
    color: Colors.textMuted,
    fontSize: 12,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
    textAlign: 'center',
    lineHeight: 18,
  },
  appItemRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomColor: Colors.borderDark,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  appAvatar: {
    width: 36,
    height: 36,
    borderRadius: 8,
    backgroundColor: Colors.matteCharcoal,
    borderColor: Colors.borderDark,
    borderWidth: 1,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  appAvatarText: {
    color: Colors.electricCyan,
    fontSize: 15,
    fontWeight: '700',
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
  },
  appInfo: {
    flex: 1,
  },
  appItemLabel: {
    color: Colors.offWhite,
    fontSize: 14,
    fontWeight: '700',
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
  },
  appItemPackage: {
    color: Colors.textMuted,
    fontSize: 11,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Menlo',
    marginTop: 2,
  },
});
