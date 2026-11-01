import { Tabs } from 'expo-router';
import React from 'react';
import {
  Platform,
  Pressable,
  StyleSheet,
  type GestureResponderEvent,
} from 'react-native';
import * as Haptics from 'expo-haptics';
import { SymbolView } from 'expo-symbols';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

function NativeTabBarButton(props: any) {
  const handlePress = (e: GestureResponderEvent) => {
    if (Platform.OS === 'ios' || Platform.OS === 'android') {
      try {
        Haptics.selectionAsync();
      } catch {}
    }
    props.onPress?.(e);
  };

  return (
    <Pressable
      {...props}
      onPress={handlePress}
      android_ripple={{
        color: 'rgba(255, 255, 255, 0.12)',
        borderless: true,
      }}
      style={({ pressed }) => [
        props.style,
        Platform.OS === 'ios' && pressed && styles.tabPressedIOS,
      ]}
    />
  );
}

export default function TabLayout() {
  const colorScheme = useColorScheme();
  const isDark = colorScheme === 'dark';
  const colors = Colors[isDark ? 'dark' : 'light'];
  const insets = useSafeAreaInsets();
  const bottomInset = Math.max(insets.bottom, 6);
  const tabHeight = Platform.select({
    ios: 56 + insets.bottom,
    android: 62 + bottomInset,
    default: 60,
  });

  return (
    <Tabs
      screenOptions={{
        tabBarActiveTintColor: '#FF5722',
        tabBarInactiveTintColor: colors.textSecondary,
        tabBarButton: (props) => <NativeTabBarButton {...props} />,
        tabBarStyle: {
          backgroundColor: '#121316',
          borderTopColor: '#2E323E',
          borderTopWidth: StyleSheet.hairlineWidth,
          height: tabHeight,
          paddingBottom: bottomInset,
          paddingTop: 6,
          elevation: 8,
        },
        tabBarLabelStyle: {
          fontSize: 12,
          fontWeight: '700',
          marginTop: Platform.OS === 'android' ? 2 : 0,
        },
        headerShown: false,
      }}>
      <Tabs.Screen
        name="index"
        options={{
          title: 'Focus',
          tabBarIcon: ({ color, focused }) => (
            <SymbolView
              tintColor={color}
              name={{
                ios: focused ? 'timer' : 'timer',
                android: 'timer',
                web: 'timer',
              }}
              size={Platform.OS === 'ios' ? 22 : 24}
            />
          ),
        }}
      />
      <Tabs.Screen
        name="schedules"
        options={{
          title: 'Schedules',
          tabBarIcon: ({ color, focused }) => (
            <SymbolView
              tintColor={color}
              name={{
                ios: focused ? 'clock.badge.checkmark.fill' : 'clock.badge.checkmark',
                android: 'event',
                web: 'event',
              }}
              size={Platform.OS === 'ios' ? 22 : 24}
            />
          ),
        }}
      />
      <Tabs.Screen
        name="explore"
        options={{
          title: 'Explore',
          tabBarIcon: ({ color, focused }) => (
            <SymbolView
              tintColor={color}
              name={{
                ios: focused ? 'shield.fill' : 'shield',
                android: 'security',
                web: 'security',
              }}
              size={Platform.OS === 'ios' ? 22 : 24}
            />
          ),
        }}
      />
    </Tabs>
  );
}

const styles = StyleSheet.create({
  tabPressedIOS: {
    opacity: 0.7,
    transform: [{ scale: 0.94 }],
  },
});
