# FocusGuard

> A privacy-first, uncompromising focus and sleep discipline lock that eliminates digital distractions, defeats background process killers, and restores your productivity.

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20(WIP)-3DDC84.svg?style=flat&logo=android)](https://www.android.com)
[![Expo](https://img.shields.io/badge/Expo-SDK%2058-black.svg?style=flat&logo=expo)](https://expo.dev)
[![React Native](https://img.shields.io/badge/React%20Native-0.88-61DAFB.svg?style=flat&logo=react)](https://reactnative.dev)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-6.0-3178C6.svg?style=flat&logo=typescript)](https://www.typescriptlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## 📂 Repository Structure

This monorepo contains the cross-platform React Native / Expo application alongside the native Kotlin reference implementation:

```
focusGuard/
├── focusGuard/     # 📱 React Native / Expo application (Expo SDK 58, Expo Router v5, TypeScript)
├── kotlin-jav/     # 🤖 Native Android reference (Kotlin 2.2, Jetpack Compose, Room, Android 15/16)
└── openspec/       # 📐 OpenSpec formal specifications, architectural designs, and change archives
```

| Project | Tech Stack | Description | Status |
| :--- | :--- | :--- | :--- |
| [**focusGuard**](./focusGuard) | React Native 0.88, Expo SDK 58, Expo Router v5, Reanimated 4, TypeScript 6 | Universal mobile application featuring tactile rotary dial controls, SQLite WAL storage, and native Android enforcement via Continuous Native Generation (CNG). | 🚀 **Primary Application** |
| [**kotlin-jav**](./kotlin-jav) | Pure Kotlin 2.2, Jetpack Compose, Room 2.8, Android 15/16 ready | Battle-tested native Android focus lock reference architecture with un-dismissible accessibility overlays, boot resilience, and zero network calls. | 🛡️ **Native Reference** |

---

## ⚡ Quick Start

### 1. focusGuard (React Native / Expo)

Prerequisites: Node.js 20+, pnpm 9+, Android Studio / Android SDK (for native Android builds).

```bash
# Navigate to the React Native app directory
cd focusGuard

# Install dependencies
pnpm install

# Run automated sanity check (TypeScript + ESLint + Jest)
pnpm test:sanity

# Start Expo interactive development server
pnpm start

# Run on connected Android device / emulator with native enforcer module
pnpm android
```

For complete setup instructions, component architecture, and native module documentation, see [focusGuard/README.md](./focusGuard/README.md).

### 2. FocusGuard Native Android (`kotlin-jav`)

Prerequisites: Android Studio Ladybug (2024.2.1+) or JDK 17.

```bash
# Navigate to the native Android directory
cd kotlin-jav

# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK for sideloading
./gradlew assembleDebug

# Install on connected device via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

For full architecture diagrams, Room schemas, and Play Store guidelines, see [kotlin-jav/README.md](./kotlin-jav/README.md).

---

## 🛡️ Core Philosophy

- **Un-Dismissible Enforcement**: Active focus commitments cannot be casually swiped away, bypassed via recent apps, or killed by aggressive background process managers.
- **Privacy First & 100% Offline**: Zero analytics, zero telemetry, zero external network requests, and zero on-screen content inspection (`canRetrieveWindowContent = false`).
- **Safety by Design**: Emergency calls, incoming rings, and system dialers are dynamically whitelisted so vital communications remain accessible at all times.
- **Doze & Reboot Resilience**: Sessions are reconciled against absolute wall-clock epoch timestamps, surviving deep power-saving Doze mode and device restarts.

---

## 📄 License

FocusGuard is open-source software licensed under the [Apache License, Version 2.0](LICENSE).
