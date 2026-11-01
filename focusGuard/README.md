# FocusGuard (React Native & Expo)

> A privacy-first, un-dismissible focus and sleep discipline lock built with React Native 0.88, Expo SDK 58, and Continuous Native Generation (CNG).

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com)
[![Expo](https://img.shields.io/badge/Expo-SDK%2058-black.svg?style=flat&logo=expo)](https://expo.dev)
[![React Native](https://img.shields.io/badge/React%20Native-0.88-61DAFB.svg?style=flat&logo=react)](https://reactnative.dev)
[![React](https://img.shields.io/badge/React-19.3-61DAFB.svg?style=flat&logo=react)](https://react.dev)
[![TypeScript](https://img.shields.io/badge/TypeScript-6.0-3178C6.svg?style=flat&logo=typescript)](https://www.typescriptlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](../LICENSE)

---

## Why FocusGuard Exists

Most screen-time limiters and app blockers fail when willpower breaks. Standard timers can be swiped away with a simple back gesture, bypassed through the recent apps task switcher, or silently aborted by OEM background killers (Samsung, Xiaomi, OnePlus).

**FocusGuard is engineered for uncompromised discipline:**

- **Un-Dismissible Full-Screen Overlay**: Uses Android's `TYPE_ACCESSIBILITY_OVERLAY` to cover blocked apps and consume all hardware back, home, and touch gestures.
- **Deep Doze & Reboot Resilience**: Tracks focus sessions via absolute wall-clock epoch time with exact alarms (`AlarmManager.setExactAndAllowWhileIdle`) and native boot receivers (`BOOT_COMPLETED`).
- **Safety by Design**: System dialers, emergency numbers, and incoming calls are dynamically whitelisted without invasive permissions.
- **Zero Network & 100% Offline**: Zero analytics, zero external network permissions in the manifest, and zero screen inspection (`canRetrieveWindowContent = false`).

---

## Quick Start

### Prerequisites

- **Node.js**: `20.x` or higher
- **Package Manager**: `pnpm` (recommended), `npm`, or `yarn`
- **Android Development**: Android Studio, Android SDK (`compileSdk 35+`, `minSdk 24`), and an Android device or emulator with USB debugging enabled

### 1. Install Dependencies

```bash
cd focusGuard
pnpm install
```

### 2. Run Sanity Checks

Validate TypeScript types, ESLint rules, and Jest unit tests in a single command:

```bash
pnpm test:sanity
```

### 3. Run on Android Device / Emulator

Build and run the native debug application with the custom `focus-enforcer` module compiled:

```bash
pnpm android
# or: npx expo run:android
```

> **Note**: Because FocusGuard relies on native Android Accessibility, Foreground Service, and Overlay APIs via Continuous Native Generation (CNG), you must run a **development build** (`pnpm android`), not Expo Go.

### 4. Start the Development Server

To restart or connect Metro after building:

```bash
pnpm start
```

---

## Architecture & How It Works

FocusGuard pairs a high-performance React Native declarative UI with an autonomous, low-overhead native Android enforcement engine:

```mermaid
flowchart TD
    subgraph UI ["React Native Layer (Expo SDK 58)"]
        Dial["RotaryDial (Reanimated 4)"] --> SessionEngine["SessionEngine (Domain)"]
        Schedules["ScheduleManager"] --> ScheduleEngine["ScheduleEngine (Domain)"]
        SessionEngine --> DB["SQLite Database (WAL Mode)"]
        ScheduleEngine --> DB
    end

    subgraph NativeBridge ["Expo Module Bridge"]
        SessionEngine -- "startFocusLock(epochMs)" --> FocusEnforcerNative["FocusEnforcerModule"]
    end

    subgraph AndroidCore ["Native Android Layer (Kotlin)"]
        FocusEnforcerNative --> FGS["FocusForegroundService (specialUse Ticker)"]
        FocusEnforcerNative --> Alarm["AlarmCoordinator (Exact Alarms)"]
        AccessService["FocusAccessibilityService"] --> Event{"Window Changed?"}
        Event -- "Allowlisted?" --> Pass["Allow Package"]
        Event -- "Blocked?" --> Overlay["LockOverlayManager (TYPE_ACCESSIBILITY_OVERLAY)"]
        BootReceiver["BootReceiver (BOOT_COMPLETED)"] --> Reconcile["Re-arm Active Session"]
    end
```

### Key Modules

1. **Continuous Native Generation (CNG)**:
   - Config plugin at [`plugins/withFocusEnforcer.js`](./plugins/withFocusEnforcer.js) injects required Android permissions, service declarations, and `@xml/accessibility_service_config` automatically during prebuild.
   - Preserves 100% offline security without checking in ephemeral `android/` folders.

2. **Native Focus Enforcer (`modules/focus-enforcer`)**:
   - High-speed native Kotlin module delivering sub-100ms window detection, foreground countdown service, and un-dismissible window overlays.
   - For full API documentation, see the [Focus Enforcer README](./modules/focus-enforcer/README.md).

3. **Persistent SQLite Storage (`src/data/db.ts`)**:
   - Uses `expo-sqlite` configured with **Write-Ahead Logging (`journal_mode = WAL`)** for thread-safe concurrent reads and writes between the React Native JavaScript thread and background native services.

4. **Pure Domain Engines**:
   - [`SessionEngine.ts`](./src/domain/SessionEngine.ts): Manages session lifecycle, active state reconciliation, and persistence.
   - [`ScheduleEngine.ts`](./src/domain/ScheduleEngine.ts): Evaluates complex recurring schedules, including midnight-crossing routines (e.g., 22:00 to 07:00).
   - [`AllowlistPolicy.ts`](./src/domain/AllowlistPolicy.ts): Enforces safety-first access control with fallback sets for Samsung, Google, Xiaomi, and OnePlus dialers.

5. **Tactile Industrial UI**:
   - Engineered with custom colors in [`src/constants/colors.ts`](./src/constants/colors.ts) (Matte Charcoal, Safety Orange, Electric Cyan).
   - Circular gesture rotary dial built with `react-native-reanimated` (4.x) and `react-native-gesture-handler` for mechanical detent haptics.

---

## Directory Structure

```
focusGuard/
├── app.json                       # Expo configuration & plugin declarations
├── package.json                   # Dependency tree, build scripts, test suites
├── plugins/
│   └── withFocusEnforcer.js       # Expo config plugin for native permissions & services
├── modules/
│   └── focus-enforcer/            # Custom Expo native module for Android enforcement
│       ├── android/               # Native Kotlin services, receivers, and overlay manager
│       ├── expo-module.config.json# Autolinking specification
│       └── src/                   # TypeScript native module interface & web fallbacks
└── src/
    ├── __tests__/                 # Automated sanity check test suite
    ├── app/                       # Expo Router v5 file-based navigation
    │   ├── _layout.tsx            # Root navigation stack & theme provider
    │   ├── onboarding.tsx         # 4-step permission setup wizard
    │   └── (tabs)/
    │       ├── index.tsx          # Focus Dashboard & Rotary Dial timer
    │       ├── schedules.tsx      # Recurring schedule manager
    │       └── explore.tsx        # Diagnostics & permission health checks
    ├── components/                # Reusable UI components (RotaryDial, Modals, Cards)
    ├── constants/                 # Theme tokens (colors.ts) and timer intervals (timer.ts)
    ├── data/                      # Database schema, migrations, and repository access
    └── domain/                    # Clean Architecture domain models & business engines
```

---

## Available Scripts

| Script | Command | Purpose |
| :--- | :--- | :--- |
| **`pnpm start`** | `expo start` | Starts the Metro development server. |
| **`pnpm android`** | `expo run:android` | Compiles native code, autolinks modules, and launches on Android. |
| **`pnpm test`** | `jest` | Runs the Jest unit test suite across domain engines and models. |
| **`pnpm lint`** | `expo lint` | Audits code against React 19 compiler rules and ESLint standard. |
| **`pnpm test:sanity`** | `pnpm tsc --noEmit && pnpm lint && pnpm jest` | **Full CI sanity gate**: checks types, lint, and unit tests. |

---

## Android Permissions & Setup

To enforce focus locks reliably, FocusGuard requires four Android system privileges configured via the onboarding wizard ([`src/app/onboarding.tsx`](./src/app/onboarding.tsx)):

1. **Accessibility Service (`FocusAccessibilityService`)**:
   - Required to intercept foreground window transitions (`TYPE_WINDOW_STATE_CHANGED`).
   - Configured with `canRetrieveWindowContent = false` (no screen inspection, keystroke reading, or content snooping).
2. **Display Over Other Apps (`SYSTEM_ALERT_WINDOW`)**:
   - Permits rendering the full-screen lock overlay when a non-allowlisted application enters the foreground.
3. **Exact Alarms (`SCHEDULE_EXACT_ALARM`)**:
   - Guarantees precise session wakeups and timer completion even in Android deep Doze power saving.
4. **Ignore Battery Optimizations (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`)**:
   - Prevents aggressive OEM task killers (Samsung, Xiaomi MIUI/HyperOS, OnePlus) from prematurely terminating background session tickers.

---

## Verification & Testing

FocusGuard enforces a strict **zero-warning policy**:

```bash
# 1. Typecheck
pnpm tsc --noEmit

# 2. Lint
pnpm lint

# 3. Jest unit tests
pnpm test
```

All 22 unit tests across `SessionEngine`, `ScheduleEngine`, `AllowlistPolicy`, and `sanity.test.ts` must pass before code is merged.

---

## Security & Privacy Policy

- **100% Offline**: The app contains zero network permissions (`android.permission.INTERNET` is omitted). No data is transmitted to remote servers.
- **On-Device Storage**: Sessions, schedules, and preferences are stored exclusively in your device's local SQLite database.
- **Emergency Priority**: Emergency dialer shortcuts and system dialers are dynamically recognized and granted immediate pass-through.

---

## License

FocusGuard is open-source software licensed under the [Apache License, Version 2.0](../LICENSE).
