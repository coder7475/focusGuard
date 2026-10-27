import { useState } from 'react';
import { Platform, StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { router } from 'expo-router';

import Button from '@/components/button';
import { AnimatedIcon } from '@/components/animated-icon';
import { HintRow } from '@/components/hint-row';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { WebBadge } from '@/components/web-badge';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';

export default function HomeScreen() {
  const [sessionActive, setSessionActive] = useState(false);
  const [duration, setDuration] = useState(25);
  const [showDetails, setShowDetails] = useState(false);

  const toggleSession = () => {
    setSessionActive((prev) => !prev);
  };

  const cycleDuration = () => {
    setDuration((prev) => {
      if (prev === 15) return 25;
      if (prev === 25) return 45;
      if (prev === 45) return 60;
      return 15;
    });
  };

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        {/* Hero Section matching Expo Tutorial imageContainer */}
        <ThemedView style={styles.heroSection}>
          <AnimatedIcon />
          <ThemedText type="title" style={styles.title}>
            SleepGuard Focus
          </ThemedText>
          <ThemedText type="small" themeColor="textSecondary" style={styles.subtitle}>
            {sessionActive
              ? `Lock engaged • ${duration} min target window`
              : 'Select duration and start focus enforcement'}
          </ThemedText>
        </ThemedView>

        {/* Action Button Container matching Expo Tutorial footerContainer */}
        <View style={styles.footerContainer}>
          <Button
            theme={sessionActive ? 'danger' : 'primary'}
            label={
              sessionActive
                ? `Abort Active Session (${duration}m)`
                : `Start Focus Session (${duration}m)`
            }
            icon={
              sessionActive
                ? { ios: 'stop.fill', android: 'stop', web: 'stop' }
                : { ios: 'play.fill', android: 'play_arrow', web: 'play_arrow' }
            }
            onPress={toggleSession}
            testID="home-primary-button"
          />

          <Button
            theme="secondary"
            label={`Change Duration (${duration} min)`}
            icon={{ ios: 'clock.fill', android: 'schedule', web: 'schedule' }}
            onPress={cycleDuration}
            testID="home-duration-button"
          />

          <Button
            theme="outline"
            label={showDetails ? 'Hide Diagnostics Info' : 'Show Diagnostics Info'}
            icon={{ ios: 'info.circle', android: 'info', web: 'info' }}
            onPress={() => setShowDetails((prev) => !prev)}
            testID="home-details-button"
          />
        </View>

        {showDetails && (
          <ThemedView type="backgroundElement" style={styles.stepContainer}>
            <HintRow
              title="Current layout"
              hint={<ThemedText type="code">src/app/(tabs)/index.tsx</ThemedText>}
            />
            <HintRow
              title="Native buttons"
              hint={<ThemedText type="code">src/components/button.tsx</ThemedText>}
            />
            <HintRow
              title="About screen"
              hint={<ThemedText type="code">src/app/(tabs)/about.tsx</ThemedText>}
            />
          </ThemedView>
        )}

        {Platform.OS === 'web' && <WebBadge />}
      </SafeAreaView>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  safeArea: {
    flex: 1,
    width: '100%',
    maxWidth: MaxContentWidth,
    paddingHorizontal: Spacing.four,
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingBottom: BottomTabInset + Spacing.three,
    paddingTop: Spacing.two,
  },
  heroSection: {
    alignItems: 'center',
    justifyContent: 'center',
    flex: 1,
    paddingHorizontal: Spacing.four,
    gap: Spacing.three,
  },
  title: {
    textAlign: 'center',
    fontSize: 26,
    letterSpacing: 0.5,
  },
  subtitle: {
    textAlign: 'center',
    paddingHorizontal: Spacing.three,
  },
  footerContainer: {
    width: '100%',
    alignItems: 'center',
    gap: Spacing.one,
    paddingVertical: Spacing.two,
  },
  stepContainer: {
    gap: Spacing.two,
    width: '100%',
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.three,
    borderRadius: Spacing.three,
    marginTop: Spacing.two,
  },
});
