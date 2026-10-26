# FocusGuard / SleepGuard

> A privacy-first, uncompromising focus and sleep discipline lock that eliminates digital distractions and restores your productivity.

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20(WIP)-3DDC84.svg?style=flat)](https://www.android.com)
[![Expo](https://img.shields.io/badge/Expo-SDK%2058-black.svg?style=flat&logo=expo)](https://expo.dev)
[![React Native](https://img.shields.io/badge/React%20Native-0.88-61DAFB.svg?style=flat&logo=react)](https://reactnative.dev)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## 📂 Repository Structure

This repository contains both the native Android implementation and the React Native / Expo hybrid version:

```
focusGuard/
├── SleepGuard/     # 📱 React Native / Expo implementation (Expo SDK 58, Expo Router v5)
└── kotlin-jav/     # 🤖 Native Android implementation (Kotlin 2.2, Jetpack Compose, Material 3)
```

| Project | Stack | Description | Status |
| :--- | :--- | :--- | :--- |
| [**SleepGuard**](./SleepGuard) | React Native 0.88, Expo SDK 58, Expo Router v5, TypeScript | Hybrid core app with tactile rotary dial UI, animated themes, and native Android enforcement modules via Expo CNG. | 🚀 Active Migration / Feature Dev |
| [**kotlin-jav**](./kotlin-jav) | Native Kotlin 2.2, Jetpack Compose, Room, Android 15/16 ready | Production-ready native Android focus lock with un-dismissible accessibility overlay, boot resilience, and zero network calls. | 🛡️ Stable Native Reference |

---

## ⚡ Quick Start

### 1. SleepGuard (React Native / Expo)

Navigate to the `SleepGuard` directory:

```bash
cd SleepGuard

# Install dependencies
pnpm install # or npm install

# Start the Expo development server
npx expo start
```

For development builds with custom native modules:
```bash
# Run on Android device or emulator
npx expo run:android
```

For more details, see the [SleepGuard Documentation](./SleepGuard/README.md) and [Migration Plan](./SleepGuard/MIGRATION_PLAN.md).

### 2. FocusGuard Native Android (`kotlin-jav`)

Navigate to `kotlin-jav/` or open it directly in Android Studio:

```bash
cd kotlin-jav

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```

For more details on building and releasing, see [kotlin-jav/README.md](./kotlin-jav/README.md).

---

## 🛡️ Core Philosophy

- **Un-Dismissible Enforcement**: Active focus commitments cannot be casually swiped away, bypassed via recent apps, or killed by aggressive background process managers.
- **Privacy First & 100% Offline**: Zero tracking, zero analytics, zero external network access, and zero screen inspection.
- **Safety by Design**: Emergency calls and system dialers are dynamically whitelisted so vital communications remain accessible.

---

## 📄 License

Licensed under the [Apache License, Version 2.0](kotlin-jav/LICENSE).
