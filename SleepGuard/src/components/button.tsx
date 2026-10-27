import React from 'react';
import {
  ActivityIndicator,
  Platform,
  Pressable,
  StyleProp,
  StyleSheet,
  View,
  ViewStyle,
} from 'react-native';
import * as Haptics from 'expo-haptics';
import { SymbolView, type SymbolViewProps } from 'expo-symbols';

import { ThemedText } from './themed-text';
import { Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

export type ButtonTheme = 'primary' | 'secondary' | 'outline' | 'danger';

export type ButtonSymbolName = SymbolViewProps['name'];

export interface ButtonProps {
  label: string;
  theme?: ButtonTheme;
  icon?: ButtonSymbolName;
  onPress?: () => void;
  disabled?: boolean;
  loading?: boolean;
  style?: StyleProp<ViewStyle>;
  testID?: string;
}

export default function Button({
  label,
  theme = 'secondary',
  icon,
  onPress,
  disabled = false,
  loading = false,
  style,
  testID,
}: ButtonProps) {
  const currentTheme = useTheme();

  const handlePress = () => {
    if (disabled || loading) return;

    if (Platform.OS === 'ios' || Platform.OS === 'android') {
      Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    }

    onPress?.();
  };

  const isPrimary = theme === 'primary';
  const isDanger = theme === 'danger';
  const isOutline = theme === 'outline';

  // Dynamic theme colors
  let backgroundColor: string;
  let textColor: string;
  let borderColor: string | undefined;

  if (isPrimary) {
    backgroundColor = '#208AEF'; // Expo / Focus Primary Blue
    textColor = '#ffffff';
  } else if (isDanger) {
    backgroundColor = '#E53935';
    textColor = '#ffffff';
  } else if (isOutline) {
    backgroundColor = 'transparent';
    textColor = currentTheme.text;
    borderColor = currentTheme.backgroundSelected;
  } else {
    // Secondary
    backgroundColor = currentTheme.backgroundElement;
    textColor = currentTheme.text;
  }

  const rippleColor = isPrimary || isDanger
    ? 'rgba(255, 255, 255, 0.25)'
    : 'rgba(0, 0, 0, 0.12)';

  return (
    <View style={[styles.outerContainer, style]}>
      <Pressable
        testID={testID}
        disabled={disabled || loading}
        onPress={handlePress}
        android_ripple={{
          color: rippleColor,
          foreground: true,
        }}
        style={({ pressed }) => [
          styles.button,
          {
            backgroundColor,
            borderColor,
            borderWidth: borderColor ? 1.5 : 0,
          },
          Platform.OS === 'ios' && pressed && styles.pressedIOS,
          disabled && styles.disabled,
          isPrimary && styles.primaryShadow,
        ]}>
        {loading ? (
          <ActivityIndicator color={textColor} size="small" />
        ) : (
          <View style={styles.contentRow}>
            {icon && (
              <SymbolView
                name={icon as any}
                tintColor={textColor}
                size={18}
                style={styles.icon}
              />
            )}
            <ThemedText
              type="default"
              style={[styles.label, { color: textColor }]}>
              {label}
            </ThemedText>
          </View>
        )}
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  outerContainer: {
    width: '100%',
    marginVertical: Spacing.one,
    borderRadius: 14,
    overflow: Platform.select({ android: 'hidden', default: 'visible' }),
  },
  button: {
    width: '100%',
    minHeight: 52,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: Spacing.two,
    paddingHorizontal: Spacing.four,
    ...Platform.select({
      android: {
        elevation: 2,
      },
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.12,
        shadowRadius: 4,
      },
    }),
  },
  primaryShadow: {
    ...Platform.select({
      android: {
        elevation: 4,
      },
      ios: {
        shadowColor: '#208AEF',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.25,
        shadowRadius: 8,
      },
    }),
  },
  pressedIOS: {
    opacity: 0.82,
    transform: [{ scale: 0.985 }],
  },
  disabled: {
    opacity: 0.5,
  },
  contentRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: Spacing.two,
  },
  icon: {
    marginRight: Spacing.one,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    letterSpacing: 0.3,
  },
});
