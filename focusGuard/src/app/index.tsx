import React, { useEffect, useState } from 'react';
import { View, StyleSheet, ActivityIndicator } from 'react-native';
import { Redirect } from 'expo-router';
import { Image } from 'expo-image';
import { preferenceStorage } from '../data/storage/PreferenceStorage';
import { Colors } from '../constants/colors';

export default function Index() {
  const [target, setTarget] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    preferenceStorage.isOnboardingCompleted().then((completed) => {
      if (mounted) {
        setTarget(completed ? '/(tabs)' : '/onboarding');
      }
    }).catch(() => {
      if (mounted) {
        setTarget('/onboarding');
      }
    });
    return () => {
      mounted = false;
    };
  }, []);

  if (!target) {
    return (
      <View style={styles.splash}>
        <Image
          source={require('../../assets/images/icon.png')}
          style={styles.logo}
          contentFit="contain"
        />
        <ActivityIndicator size="small" color={Colors.safetyOrange} style={styles.spinner} />
      </View>
    );
  }

  return <Redirect href={target as any} />;
}

const styles = StyleSheet.create({
  splash: {
    flex: 1,
    backgroundColor: Colors.matteCharcoal,
    alignItems: 'center',
    justifyContent: 'center',
  },
  logo: {
    width: 96,
    height: 96,
    borderRadius: 22,
  },
  spinner: {
    marginTop: 24,
  },
});
