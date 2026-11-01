import React from 'react';
import { View, Text, Modal, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import { Colors } from '../constants/colors';

interface Props {
  visible: boolean;
  onClose: () => void;
}

export const OemBatteryGuideModal: React.FC<Props> = ({ visible, onClose }) => {
  return (
    <Modal visible={visible} animationType="slide" transparent>
      <View style={styles.backdrop}>
        <View style={styles.sheet}>
          <Text style={styles.title}>OEM BATTERY KILLER MITIGATION</Text>
          <Text style={styles.intro}>
            Aggressive OEM operating systems kill background accessibility services to save battery. Follow these steps to ensure uninterrupted protection:
          </Text>

          <ScrollView style={{ maxHeight: 360 }}>
            <View style={styles.vendorCard}>
              <Text style={styles.vendor}>Samsung (One UI)</Text>
              <Text style={styles.body}>
                Settings → Apps → SleepGuard → Battery → Choose &apos;Unrestricted&apos;.
                Also remove from &apos;Deep Sleeping Apps&apos; in Device Care.
              </Text>
            </View>

            <View style={styles.vendorCard}>
              <Text style={styles.vendor}>Xiaomi (HyperOS / MIUI)</Text>
              <Text style={styles.body}>
                App Info → Battery Saver → Select &apos;No Restrictions&apos;.
                Enable &apos;Autostart&apos;. In Recents screen, long press SleepGuard and tap the Lock icon.
              </Text>
            </View>

            <View style={styles.vendorCard}>
              <Text style={styles.vendor}>OnePlus / OPPO / Realme (ColorOS / OxygenOS)</Text>
              <Text style={styles.body}>
                App Info → Battery Usage → Enable &apos;Allow background activity&apos; and &apos;Allow auto-launch&apos;.
              </Text>
            </View>

            <View style={styles.vendorCard}>
              <Text style={styles.vendor}>Google Pixel (Stock Android)</Text>
              <Text style={styles.body}>
                Settings → Apps → SleepGuard → App battery usage → Select &apos;Unrestricted&apos;.
              </Text>
            </View>
          </ScrollView>

          <TouchableOpacity style={styles.closeBtn} onPress={onClose}>
            <Text style={styles.closeText}>GOT IT</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
};

export default OemBatteryGuideModal;

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.75)',
    justifyContent: 'flex-end',
  },
  sheet: {
    backgroundColor: Colors.cardSurface,
    padding: 24,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    borderWidth: 1,
    borderColor: Colors.borderDark,
  },
  title: {
    fontSize: 17,
    fontWeight: '800',
    color: Colors.safetyOrange,
    letterSpacing: 0.5,
    marginBottom: 6,
  },
  intro: {
    fontSize: 12,
    color: Colors.textMuted,
    lineHeight: 16,
    marginBottom: 16,
  },
  vendorCard: {
    backgroundColor: Colors.matteCharcoal,
    borderRadius: 10,
    padding: 12,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: Colors.borderDark,
  },
  vendor: {
    fontSize: 14,
    fontWeight: 'bold',
    color: Colors.electricCyan,
    marginBottom: 4,
  },
  body: {
    fontSize: 12,
    color: Colors.offWhite,
    lineHeight: 16,
  },
  closeBtn: {
    backgroundColor: Colors.safetyOrange,
    padding: 14,
    borderRadius: 10,
    marginTop: 16,
    alignItems: 'center',
  },
  closeText: {
    color: Colors.matteCharcoal,
    fontWeight: '800',
    fontSize: 14,
  },
});
