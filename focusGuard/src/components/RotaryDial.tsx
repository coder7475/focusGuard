/* eslint-disable react-hooks/immutability, react-hooks/exhaustive-deps */
import React, { useEffect } from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { Gesture, GestureDetector } from 'react-native-gesture-handler';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  runOnJS,
  withSpring,
} from 'react-native-reanimated';
import * as Haptics from 'expo-haptics';
import Svg, { Circle } from 'react-native-svg';
import { Colors } from '../constants/colors';
import { DURATION_STEPS } from '../constants/timer';

export { DURATION_STEPS };

interface RotaryDialProps {
  value: number; // in minutes
  onChange: (minutes: number) => void;
  size?: number;
  disabled?: boolean;
}

export const RotaryDial: React.FC<RotaryDialProps> = ({
  value,
  onChange,
  size = 280,
  disabled = false,
}) => {
  // Convert minutes to an angle in 0..300 degrees
  const minutesToDeg = (mins: number) => {
    const minVal = 15;
    const maxVal = 240;
    const clamped = Math.max(minVal, Math.min(maxVal, mins));
    return ((clamped - minVal) / (maxVal - minVal)) * 280;
  };

  const degToMinutes = (deg: number) => {
    const minVal = 15;
    const maxVal = 240;
    const rawMins = minVal + (deg / 280) * (maxVal - minVal);
    // Find closest discrete step
    let closest = DURATION_STEPS[0];
    let minDiff = Math.abs(rawMins - closest);
    for (const step of DURATION_STEPS) {
      const diff = Math.abs(rawMins - step);
      if (diff < minDiff) {
        minDiff = diff;
        closest = step;
      }
    }
    return closest;
  };

  const angle = useSharedValue(minutesToDeg(value));
  const lastReportedStep = useSharedValue(value);

  useEffect(() => {
    angle.value = withSpring(minutesToDeg(value));
    lastReportedStep.value = value;
  }, [value]);

  const triggerHaptic = () => {
    try {
      Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    } catch {}
  };

  const panGesture = Gesture.Pan()
    .enabled(!disabled)
    .onUpdate((event) => {
      const center = size / 2;
      const x = event.x - center;
      const y = event.y - center;
      let rad = Math.atan2(y, x) + Math.PI / 2;
      if (rad < 0) rad += 2 * Math.PI;

      const deg = (rad * 180) / Math.PI;
      if (deg <= 290) {
        angle.value = deg;
        const step = degToMinutes(deg);
        if (step !== lastReportedStep.value) {
          lastReportedStep.value = step;
          runOnJS(triggerHaptic)();
          runOnJS(onChange)(step);
        }
      }
    })
    .onEnd(() => {
      const snapDeg = minutesToDeg(lastReportedStep.value);
      angle.value = withSpring(snapDeg);
    });

  const pointerStyle = useAnimatedStyle(() => ({
    transform: [{ rotate: `${angle.value}deg` }],
  }));

  const radius = size / 2 - 24;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (circumference * (angle.value / 360));

  return (
    <GestureDetector gesture={panGesture}>
      <View style={[styles.container, { width: size, height: size }]}>
        <Svg width={size} height={size}>
          {/* Background Outer Ring */}
          <Circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke={Colors.darkGunmetal}
            strokeWidth={14}
            fill={Colors.matteCharcoal}
          />
          {/* Active Accent Arc */}
          <Circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke={Colors.safetyOrange}
            strokeWidth={14}
            strokeDasharray={`${circumference}`}
            strokeDashoffset={strokeDashoffset}
            strokeLinecap="round"
            fill="transparent"
            rotation="-90"
            origin={`${size / 2}, ${size / 2}`}
          />
        </Svg>

        {/* Rotating Needle / Knob */}
        <Animated.View style={[styles.needleTrack, pointerStyle]}>
          <View style={styles.knob} />
        </Animated.View>

        {/* Center Display */}
        <View style={styles.centerContainer}>
          <Text style={styles.durationText}>{value}</Text>
          <Text style={styles.unitText}>MINUTES</Text>
        </View>
      </View>
    </GestureDetector>
  );
};

// Also export default for flexibility
export default RotaryDial;

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
  },
  needleTrack: {
    ...StyleSheet.absoluteFill,
    alignItems: 'center',
  },
  knob: {
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: Colors.safetyOrange,
    marginTop: 14,
    borderWidth: 3,
    borderColor: Colors.offWhite,
    shadowColor: Colors.safetyOrange,
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.8,
    shadowRadius: 6,
    elevation: 8,
  },
  centerContainer: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
  },
  durationText: {
    fontSize: 52,
    fontWeight: '800',
    color: Colors.offWhite,
    letterSpacing: -1,
  },
  unitText: {
    fontSize: 12,
    fontWeight: '700',
    color: Colors.textMuted,
    letterSpacing: 2,
    marginTop: 2,
  },
});
