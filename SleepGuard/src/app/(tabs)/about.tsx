import { StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { router } from 'expo-router';
import * as WebBrowser from 'expo-web-browser';

import Button from '@/components/button';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';

export default function AboutScreen() {
  const openDocs = async () => {
    await WebBrowser.openBrowserAsync('https://docs.expo.dev');
  };

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <View style={styles.content}>
          <ThemedText type="title" style={styles.title}>
            About SleepGuard
          </ThemedText>
          <ThemedText themeColor="textSecondary" style={styles.description}>
            SleepGuard is a high-performance focus and sleep enforcement platform built
            with Expo SDK 58 and React Native.
          </ThemedText>

          <ThemedView type="backgroundElement" style={styles.card}>
            <ThemedText type="subtitle">Platform Native Features</ThemedText>
            <ThemedText type="small" themeColor="textSecondary">
              • Android Material 3 tabs, ripple effects & accessibility enforcement
            </ThemedText>
            <ThemedText type="small" themeColor="textSecondary">
              • iOS SF Symbols, tactile haptic detents & smooth gesture transitions
            </ThemedText>
            <ThemedText type="small" themeColor="textSecondary">
              • Shared SQLite persistence & wall-clock alarms
            </ThemedText>
          </ThemedView>
        </View>

        <View style={styles.footerContainer}>
          <Button
            theme="primary"
            label="Explore Features"
            icon={{ ios: 'paperplane.fill', android: 'explore', web: 'explore' }}
            onPress={() => router.push('/explore')}
            testID="about-explore-button"
          />

          <Button
            theme="outline"
            label="Open Documentation"
            icon={{ ios: 'arrow.up.right.square', android: 'link', web: 'link' }}
            onPress={openDocs}
            testID="about-docs-button"
          />
        </View>
      </SafeAreaView>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  safeArea: {
    flex: 1,
    width: '100%',
    maxWidth: MaxContentWidth,
    paddingHorizontal: Spacing.four,
    justifyContent: 'space-between',
    paddingBottom: BottomTabInset + Spacing.three,
    paddingTop: Spacing.two,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    gap: Spacing.three,
  },
  title: {
    textAlign: 'center',
    fontSize: 26,
  },
  description: {
    textAlign: 'center',
    paddingHorizontal: Spacing.three,
    lineHeight: 22,
  },
  card: {
    padding: Spacing.four,
    borderRadius: Spacing.three,
    gap: Spacing.two,
    width: '100%',
    marginTop: Spacing.three,
  },
  footerContainer: {
    width: '100%',
    gap: Spacing.one,
    paddingVertical: Spacing.two,
  },
});
