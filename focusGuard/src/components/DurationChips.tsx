import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Modal,
  TextInput,
} from 'react-native';
import { Colors } from '../constants/colors';

interface DurationChipsProps {
  selectedMinutes: number;
  onSelectMinutes: (minutes: number) => void;
  disabled?: boolean;
}

const PRESETS = [15, 25, 45, 60];

export const DurationChips: React.FC<DurationChipsProps> = ({
  selectedMinutes,
  onSelectMinutes,
  disabled = false,
}) => {
  const [modalVisible, setModalVisible] = useState(false);
  const [customInput, setCustomInput] = useState('');
  const [warning, setWarning] = useState('');

  const handleCustomSubmit = () => {
    const val = parseInt(customInput, 10);
    if (isNaN(val) || val < 5) return;
    if (val > 180) {
      setWarning('⚠️ Extreme Focus: Sessions over 3 hours cannot be cancelled easily.');
    } else {
      setWarning('');
    }
    onSelectMinutes(val);
    setModalVisible(false);
    setCustomInput('');
  };

  return (
    <View style={styles.container}>
      {PRESETS.map((minutes) => {
        const isSelected = selectedMinutes === minutes;
        return (
          <TouchableOpacity
            key={minutes}
            style={[
              styles.chip,
              isSelected && styles.chipSelected,
              disabled && styles.chipDisabled,
            ]}
            disabled={disabled}
            onPress={() => onSelectMinutes(minutes)}
          >
            <Text
              style={[
                styles.chipText,
                isSelected && styles.chipTextSelected,
              ]}
            >
              {minutes}m
            </Text>
          </TouchableOpacity>
        );
      })}

      <TouchableOpacity
        style={[
          styles.chip,
          !PRESETS.includes(selectedMinutes) && styles.chipSelected,
          disabled && styles.chipDisabled,
        ]}
        disabled={disabled}
        onPress={() => setModalVisible(true)}
      >
        <Text
          style={[
            styles.chipText,
            !PRESETS.includes(selectedMinutes) && styles.chipTextSelected,
          ]}
        >
          Custom
        </Text>
      </TouchableOpacity>

      {/* Custom Duration Dialog */}
      <Modal visible={modalVisible} transparent animationType="fade">
        <View style={styles.modalBackdrop}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>CUSTOM FOCUS DURATION</Text>
            <Text style={styles.modalSubtitle}>Enter session length in minutes (5 – 360):</Text>

            <TextInput
              style={styles.input}
              keyboardType="number-pad"
              value={customInput}
              onChangeText={(text) => {
                setCustomInput(text);
                const n = parseInt(text, 10);
                if (!isNaN(n) && n > 180) {
                  setWarning('⚠️ Long session warning: Over 180 minutes requires high discipline.');
                } else {
                  setWarning('');
                }
              }}
              placeholder="e.g. 90"
              placeholderTextColor={Colors.textMuted}
            />

            {warning.length > 0 && <Text style={styles.warningText}>{warning}</Text>}

            <View style={styles.modalButtons}>
              <TouchableOpacity
                style={styles.cancelBtn}
                onPress={() => {
                  setModalVisible(false);
                  setWarning('');
                }}
              >
                <Text style={styles.cancelBtnText}>CANCEL</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={styles.confirmBtn}
                onPress={handleCustomSubmit}
              >
                <Text style={styles.confirmBtnText}>SET DURATION</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
};

export default DurationChips;

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 8,
    marginVertical: 16,
  },
  chip: {
    paddingVertical: 8,
    paddingHorizontal: 16,
    borderRadius: 20,
    backgroundColor: Colors.cardSurface,
    borderWidth: 1,
    borderColor: Colors.borderDark,
  },
  chipSelected: {
    backgroundColor: Colors.safetyOrange,
    borderColor: Colors.safetyOrange,
  },
  chipDisabled: {
    opacity: 0.5,
  },
  chipText: {
    fontSize: 14,
    fontWeight: '700',
    color: Colors.offWhite,
  },
  chipTextSelected: {
    color: Colors.matteCharcoal,
  },
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.75)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  modalCard: {
    width: '100%',
    maxWidth: 340,
    backgroundColor: Colors.cardSurface,
    borderRadius: 16,
    padding: 24,
    borderWidth: 1,
    borderColor: Colors.borderDark,
  },
  modalTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: Colors.offWhite,
    marginBottom: 8,
  },
  modalSubtitle: {
    fontSize: 13,
    color: Colors.textMuted,
    marginBottom: 16,
  },
  input: {
    backgroundColor: Colors.matteCharcoal,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: Colors.borderDark,
    padding: 12,
    fontSize: 20,
    fontWeight: 'bold',
    color: Colors.offWhite,
    textAlign: 'center',
    marginBottom: 12,
  },
  warningText: {
    fontSize: 12,
    color: '#FFB300',
    marginBottom: 16,
    lineHeight: 16,
  },
  modalButtons: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    gap: 12,
    marginTop: 8,
  },
  cancelBtn: {
    paddingVertical: 10,
    paddingHorizontal: 16,
  },
  cancelBtnText: {
    color: Colors.textMuted,
    fontWeight: '700',
  },
  confirmBtn: {
    backgroundColor: Colors.safetyOrange,
    paddingVertical: 10,
    paddingHorizontal: 18,
    borderRadius: 8,
  },
  confirmBtnText: {
    color: Colors.matteCharcoal,
    fontWeight: '800',
  },
});
