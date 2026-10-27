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

import { Colors, Spacing } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

function NativeTabBarButton(props: any) {
  const handlePress = (e: GestureResponderEvent) => {
    if (Platform.OS === 'ios' || Platform.OS === 'android') {
      Haptics.selectionAsync();
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

  return (
    <Tabs
      screenOptions={{
        tabBarActiveTintColor: colors.text,
        tabBarInactiveTintColor: colors.textSecondary,
        tabBarButton: (props) => <NativeTabBarButton {...props} />,
        tabBarStyle: {
          backgroundColor: colors.background,
          borderTopColor: colors.backgroundElement,
          borderTopWidth: StyleSheet.hairlineWidth,
          ...Platform.select({
            android: {
              height: 64,
              paddingBottom: Spacing.two,
              paddingTop: Spacing.one,
              elevation: 8,
            },
            ios: {
              height: 84,
              paddingBottom: Spacing.four,
              paddingTop: Spacing.one,
              shadowColor: '#000',
              shadowOffset: { width: 0, height: -2 },
              shadowOpacity: isDark ? 0.3 : 0.06,
              shadowRadius: 4,
            },
            web: {
              height: 60,
              paddingBottom: Spacing.one,
              paddingTop: Spacing.one,
            },
          }),
        },
        tabBarLabelStyle: {
          fontSize: 12,
          fontWeight: '600',
          marginTop: Platform.OS === 'android' ? 2 : 0,
        },
        headerStyle: {
          backgroundColor: colors.background,
        },
        headerTintColor: colors.text,
        headerShown: false,
      }}>
      <Tabs.Screen
        name="index"
        options={{
          title: 'Home',
          tabBarIcon: ({ color, focused }) => (
            <SymbolView
              tintColor={color}
              name={{
                ios: focused ? 'house.fill' : 'house',
                android: 'home',
                web: 'home',
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
                ios: focused ? 'paperplane.fill' : 'paperplane',
                android: 'explore',
                web: 'explore',
              }}
              size={Platform.OS === 'ios' ? 22 : 24}
            />
          ),
        }}
      />
      <Tabs.Screen
        name="about"
        options={{
          title: 'About',
          tabBarIcon: ({ color, focused }) => (
            <SymbolView
              tintColor={color}
              name={{
                ios: focused ? 'info.circle.fill' : 'info.circle',
                android: 'info',
                web: 'info',
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
