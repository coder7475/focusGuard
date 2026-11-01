import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  Switch,
  TouchableOpacity,
  Modal,
  TextInput,
} from 'react-native';
import { Colors } from '../../constants/colors';
import { FocusSchedule } from '../../domain/types';
import { scheduleRepository } from '../../data/repositories/ScheduleRepository';
import { ScheduleEngine } from '../../domain/ScheduleEngine';

const DAYS = [
  { label: 'M', value: 1 },
  { label: 'T', value: 2 },
  { label: 'W', value: 3 },
  { label: 'T', value: 4 },
  { label: 'F', value: 5 },
  { label: 'S', value: 6 },
  { label: 'S', value: 7 },
];

export default function SchedulesScreen() {
  const [schedules, setSchedules] = useState<FocusSchedule[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [name, setName] = useState('');
  const [startHour, setStartHour] = useState('22');
  const [startMinute, setStartMinute] = useState('00');
  const [endHour, setEndHour] = useState('07');
  const [endMinute, setEndMinute] = useState('00');
  const [selectedDays, setSelectedDays] = useState<number[]>([1, 2, 3, 4, 5, 6, 7]);

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const all = await scheduleRepository.getAllSchedules();
        if (mounted) {
          if (all.length === 0) {
            const defaultSchedule: FocusSchedule = {
              id: 'routine_default_sleep',
              name: 'Night Sleep Lock',
              startHour: 22,
              startMinute: 0,
              endHour: 7,
              endMinute: 0,
              daysOfWeek: [1, 2, 3, 4, 5, 6, 7],
              isEnabled: true,
              createdAtEpochMs: Date.now(),
            };
            await scheduleRepository.createSchedule(defaultSchedule);
            setSchedules([defaultSchedule]);
          } else {
            setSchedules(all);
          }
        }
      } catch {}
    })();
    return () => {
      mounted = false;
    };
  }, []);

  const handleToggle = async (id: string, currentState: boolean) => {
    await scheduleRepository.toggleSchedule(id, !currentState);
    setSchedules((prev) =>
      prev.map((s) => (s.id === id ? { ...s, isEnabled: !currentState } : s))
    );
  };

  const handleDelete = async (id: string) => {
    await scheduleRepository.deleteSchedule(id);
    setSchedules((prev) => prev.filter((s) => s.id !== id));
  };

  const handleCreate = async () => {
    if (!name.trim()) return;
    const sH = parseInt(startHour, 10) || 0;
    const sM = parseInt(startMinute, 10) || 0;
    const eH = parseInt(endHour, 10) || 0;
    const eM = parseInt(endMinute, 10) || 0;

    const newSched: FocusSchedule = {
      id: `sched_${Date.now()}`,
      name: name.trim(),
      startHour: sH,
      startMinute: sM,
      endHour: eH,
      endMinute: eM,
      daysOfWeek: selectedDays,
      isEnabled: true,
      createdAtEpochMs: Date.now(),
    };

    await scheduleRepository.createSchedule(newSched);
    setSchedules((prev) => [...prev, newSched]);
    setModalVisible(false);
    setName('');
  };

  const toggleDay = (day: number) => {
    setSelectedDays((prev) =>
      prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day].sort()
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.headerRow}>
        <View>
          <Text style={styles.header}>BEDTIME & FOCUS ROUTINES</Text>
          <Text style={styles.subtitle}>Recurring automated focus windows</Text>
        </View>
        <TouchableOpacity
          style={styles.addButton}
          onPress={() => setModalVisible(true)}
        >
          <Text style={styles.addButtonText}>+ NEW</Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={schedules}
        keyExtractor={(item) => item.id}
        contentContainerStyle={{ paddingBottom: 32 }}
        renderItem={({ item }) => {
          const isOvernight = ScheduleEngine.isOvernight(item);
          return (
            <View style={styles.card}>
              <View style={styles.cardHeader}>
                <View>
                  <Text style={styles.name}>{item.name}</Text>
                  <Text style={styles.time}>{ScheduleEngine.formatRange(item)}</Text>
                </View>
                <Switch
                  value={item.isEnabled}
                  onValueChange={() => handleToggle(item.id, item.isEnabled)}
                  thumbColor={item.isEnabled ? Colors.safetyOrange : Colors.textMuted}
                  trackColor={{ false: Colors.borderDark, true: Colors.safetyOrangeDim }}
                />
              </View>

              <View style={styles.badgeRow}>
                {isOvernight && (
                  <View style={styles.overnightBadge}>
                    <Text style={styles.overnightBadgeText}>🌙 OVERNIGHT LOCK</Text>
                  </View>
                )}
                <View style={styles.daysRow}>
                  {DAYS.map((d, index) => {
                    const active = item.daysOfWeek.includes(d.value);
                    return (
                      <View
                        key={index}
                        style={[styles.dayDot, active && styles.dayDotActive]}
                      >
                        <Text
                          style={[styles.dayDotText, active && styles.dayDotTextActive]}
                        >
                          {d.label}
                        </Text>
                      </View>
                    );
                  })}
                </View>
                <TouchableOpacity
                  style={styles.deleteBtn}
                  onPress={() => handleDelete(item.id)}
                >
                  <Text style={styles.deleteBtnText}>Delete</Text>
                </TouchableOpacity>
              </View>
            </View>
          );
        }}
      />

      {/* Add Routine Modal */}
      <Modal visible={modalVisible} transparent animationType="slide">
        <View style={styles.modalBackdrop}>
          <View style={styles.modalSheet}>
            <Text style={styles.modalTitle}>CREATE ROUTINE</Text>

            <Text style={styles.inputLabel}>Routine Name</Text>
            <TextInput
              style={styles.textInput}
              value={name}
              onChangeText={setName}
              placeholder="e.g. Deep Sleep Lock"
              placeholderTextColor={Colors.textMuted}
            />

            <View style={styles.timeRow}>
              <View style={styles.timeBox}>
                <Text style={styles.inputLabel}>Start (24h)</Text>
                <View style={styles.timeInputs}>
                  <TextInput
                    style={styles.timeInput}
                    value={startHour}
                    onChangeText={setStartHour}
                    keyboardType="number-pad"
                    maxLength={2}
                  />
                  <Text style={styles.timeColon}>:</Text>
                  <TextInput
                    style={styles.timeInput}
                    value={startMinute}
                    onChangeText={setStartMinute}
                    keyboardType="number-pad"
                    maxLength={2}
                  />
                </View>
              </View>

              <View style={styles.timeBox}>
                <Text style={styles.inputLabel}>End (24h)</Text>
                <View style={styles.timeInputs}>
                  <TextInput
                    style={styles.timeInput}
                    value={endHour}
                    onChangeText={setEndHour}
                    keyboardType="number-pad"
                    maxLength={2}
                  />
                  <Text style={styles.timeColon}>:</Text>
                  <TextInput
                    style={styles.timeInput}
                    value={endMinute}
                    onChangeText={setEndMinute}
                    keyboardType="number-pad"
                    maxLength={2}
                  />
                </View>
              </View>
            </View>

            <Text style={styles.inputLabel}>Active Weekdays</Text>
            <View style={styles.daysPicker}>
              {DAYS.map((d, index) => {
                const selected = selectedDays.includes(d.value);
                return (
                  <TouchableOpacity
                    key={index}
                    style={[styles.dayChip, selected && styles.dayChipSelected]}
                    onPress={() => toggleDay(d.value)}
                  >
                    <Text
                      style={[styles.dayChipText, selected && styles.dayChipTextSelected]}
                    >
                      {d.label}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>

            <View style={styles.modalActions}>
              <TouchableOpacity
                style={styles.modalCancel}
                onPress={() => setModalVisible(false)}
              >
                <Text style={styles.modalCancelText}>CANCEL</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={styles.modalSave}
                onPress={handleCreate}
              >
                <Text style={styles.modalSaveText}>SAVE ROUTINE</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.matteCharcoal,
    padding: 20,
    paddingTop: 56,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 24,
  },
  header: {
    fontSize: 18,
    fontWeight: '800',
    color: Colors.offWhite,
    letterSpacing: 0.5,
  },
  subtitle: {
    fontSize: 12,
    color: Colors.textMuted,
    marginTop: 2,
  },
  addButton: {
    backgroundColor: Colors.safetyOrange,
    paddingVertical: 8,
    paddingHorizontal: 16,
    borderRadius: 8,
  },
  addButtonText: {
    color: Colors.matteCharcoal,
    fontWeight: '800',
    fontSize: 13,
  },
  card: {
    backgroundColor: Colors.cardSurface,
    padding: 16,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: Colors.borderDark,
    marginBottom: 14,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  name: {
    fontSize: 16,
    fontWeight: '700',
    color: Colors.offWhite,
  },
  time: {
    fontSize: 15,
    fontWeight: '800',
    color: Colors.safetyOrange,
    marginTop: 4,
  },
  badgeRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 12,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: Colors.borderDark,
  },
  overnightBadge: {
    backgroundColor: 'rgba(0, 229, 255, 0.12)',
    paddingVertical: 4,
    paddingHorizontal: 8,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: 'rgba(0, 229, 255, 0.3)',
  },
  overnightBadgeText: {
    color: Colors.electricCyan,
    fontSize: 10,
    fontWeight: '800',
  },
  daysRow: {
    flexDirection: 'row',
    gap: 4,
  },
  dayDot: {
    width: 22,
    height: 22,
    borderRadius: 11,
    backgroundColor: Colors.matteCharcoal,
    alignItems: 'center',
    justifyContent: 'center',
  },
  dayDotActive: {
    backgroundColor: Colors.safetyOrange,
  },
  dayDotText: {
    fontSize: 10,
    color: Colors.textMuted,
    fontWeight: '700',
  },
  dayDotTextActive: {
    color: Colors.matteCharcoal,
  },
  deleteBtn: {
    padding: 4,
  },
  deleteBtnText: {
    color: Colors.dangerRed,
    fontSize: 12,
    fontWeight: '600',
  },
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.7)',
    justifyContent: 'flex-end',
  },
  modalSheet: {
    backgroundColor: Colors.cardSurface,
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 24,
    borderWidth: 1,
    borderColor: Colors.borderDark,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: Colors.offWhite,
    marginBottom: 20,
  },
  inputLabel: {
    fontSize: 12,
    fontWeight: '700',
    color: Colors.textMuted,
    marginBottom: 6,
  },
  textInput: {
    backgroundColor: Colors.matteCharcoal,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: Colors.borderDark,
    padding: 12,
    color: Colors.offWhite,
    fontSize: 14,
    marginBottom: 16,
  },
  timeRow: {
    flexDirection: 'row',
    gap: 16,
    marginBottom: 16,
  },
  timeBox: {
    flex: 1,
  },
  timeInputs: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  timeInput: {
    flex: 1,
    backgroundColor: Colors.matteCharcoal,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: Colors.borderDark,
    padding: 10,
    color: Colors.offWhite,
    fontSize: 18,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  timeColon: {
    color: Colors.offWhite,
    fontSize: 18,
    fontWeight: 'bold',
    marginHorizontal: 4,
  },
  daysPicker: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 24,
  },
  dayChip: {
    width: 38,
    height: 38,
    borderRadius: 19,
    backgroundColor: Colors.matteCharcoal,
    borderWidth: 1,
    borderColor: Colors.borderDark,
    alignItems: 'center',
    justifyContent: 'center',
  },
  dayChipSelected: {
    backgroundColor: Colors.safetyOrange,
    borderColor: Colors.safetyOrange,
  },
  dayChipText: {
    color: Colors.textMuted,
    fontWeight: 'bold',
  },
  dayChipTextSelected: {
    color: Colors.matteCharcoal,
  },
  modalActions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    gap: 12,
  },
  modalCancel: {
    paddingVertical: 12,
    paddingHorizontal: 16,
  },
  modalCancelText: {
    color: Colors.textMuted,
    fontWeight: 'bold',
  },
  modalSave: {
    backgroundColor: Colors.safetyOrange,
    paddingVertical: 12,
    paddingHorizontal: 20,
    borderRadius: 8,
  },
  modalSaveText: {
    color: Colors.matteCharcoal,
    fontWeight: 'bold',
  },
});
