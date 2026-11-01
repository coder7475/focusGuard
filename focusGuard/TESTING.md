# FocusGuard — Comprehensive Testing & Build Guide

This guide details how to build, run, and test **FocusGuard** across all supported environments: **Web**, **Development Build**, **Preview Build (Standalone APK)**, and **Production (AAB)**, along with specialized native resilience testing procedures.

---

## 1. Build & Environment Overview

FocusGuard is built on **React Native 0.88**, **Expo SDK 58**, and Continuous Native Generation (CNG). Because FocusGuard includes a custom native Android module (`modules/focus-enforcer`) and privileged permissions (`TYPE_ACCESSIBILITY_OVERLAY`, `FOREGROUND_SERVICE`, `BOOT_COMPLETED`), the testing environment dictates what features are accessible:

| Target Environment | Native Module (`focus-enforcer`) | Hot Reload / Metro | Standalone (No PC needed) | Recommended Use Case |
| :--- | :---: | :---: | :---: | :--- |
| **Web Browser** | ❌ (Safe Stubs / Mock) | ✅ | ❌ | Rapid UI layout, theme, typography, routing verification |
| **Expo Go** | ❌ **Unsupported** | ⚠️ Incompatible | ❌ | **Do not use** (missing native Kotlin module) |
| **Development Build** | ✅ | ✅ | ❌ (Requires Metro) | Active daily development, debugging, native module iteration |
| **Preview Build (APK)** | ✅ | ❌ (Bundled JS) | ✅ | QA testing, stakeholder distribution, real-world discipline test |
| **Production Build (AAB)**| ✅ | ❌ (Bundled JS) | ✅ | Google Play Console internal track / release |

> [!WARNING]
> **Expo Go is not compatible with FocusGuard**: Standard Expo Go does not contain custom native Kotlin code ([`modules/focus-enforcer/`](./modules/focus-enforcer/)) or native Android manifest modifications. You must use a **Development Build** or **Preview Build** to test on Android.

---

## 2. Prerequisites & Setup

Ensure the following tools are installed before proceeding:

- **Node.js**: `20.x` or higher
- **pnpm**: `pnpm install` (project uses `pnpm-lock.yaml`)
- **Android Studio & SDK** (for local Android compilation):
  - Android SDK Platform `37` (Android 16 / SDK 37 required by Expo SDK 58 / React Native 0.88)
  - Java Development Kit (JDK 17)
  - Environment variables set in `~/.bashrc` or `~/.zshrc`:
    ```bash
    export ANDROID_HOME=$HOME/Android/Sdk
    export PATH=$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools
    ```
- **EAS CLI** (for cloud builds and internal distribution):
  ```bash
  # Authenticate with Expo Application Services
  eas login
  # or: npx eas-cli login
  ```

---

## 3. Testing on Web

Web testing allows rapid iteration on UI components, navigation flows, and design systems without waiting for Android Gradle compilation.

### WebAssembly SQLite & Headers Support
`expo-sqlite` uses `wa-sqlite` (WebAssembly SQLite) under the hood on web. The project's [`metro.config.js`](./metro.config.js) is preconfigured to:
1. Treat `.wasm` files as assets (`config.resolver.assetExts.push('wasm')`).
2. Serve required Cross-Origin isolation headers (`Cross-Origin-Embedder-Policy: credentialless` and `Cross-Origin-Opener-Policy: same-origin`) so browser `SharedArrayBuffer` can run.

### How to Run:
```bash
# Start the Expo web development server
pnpm web
# or:
npx expo start --web
```

Once running, open [http://localhost:8081](http://localhost:8081) in your browser.

### Recommended Web Testing Workflow:
1. **Toggle Mobile Device Emulation**: Press `F12` in Chrome/Edge, click the device toggle icon (`Ctrl+Shift+M` / `Cmd+Shift+M`), and select **Pixel 7** or **Samsung Galaxy S20** to preview mobile dimensions.
2. **Validate Expo Router Navigation**: Test tab switches, stack pushes, settings screens, and modal dialogs.
3. **Verify Color Schemes & UI Tokens**: Toggle dark and light modes to ensure layout and color contrast adhere to styling tokens.

### Web Limitations to Keep in Mind:
- Native Android services (`FocusEnforcer`, Accessibility overlays, exact alarms, battery optimization, boot receivers) operate as safe stubs.
- Native SQLite database runs within browser IndexedDB/VFS memory.

---

## 4. Testing with Development Build

A **Development Build** pairs a custom native Android binary (including all native modules like `focus-enforcer`) with the Metro bundler. This gives you native Android capabilities alongside Fast Refresh, debugging tools, and console logging.

Both local and EAS profiles generate direct installable `.apk` files (`"android": { "buildType": "apk" }` in [`eas.json`](./eas.json)).

### Option A: Local Development Build (Fastest & Free)

Run this directly on an attached USB device or running Android Virtual Device (AVD):

```bash
# 1. Verify your Android device or emulator is detected
adb devices

# 2. Build and launch the debug app
pnpm android
# or:
npx expo run:android
```

Once compiled and installed, start or reconnect Metro:
```bash
pnpm run start:dev
```

### Option B: Cloud Development Build via EAS

If you don't have a local Android SDK configured or want to share the development build with team members:

```bash
# Cloud EAS build (outputs downloadable development .apk)
pnpm run build:dev
# or: eas build --profile development --platform android

# Or build locally using EAS CLI:
pnpm run build:dev:local
# or: eas build --profile development --platform android --local
```

### Connecting to the Development Client
Once the development `.apk` is installed on your physical device or emulator:
1. Start the Metro bundler:
   ```bash
   pnpm run start:dev
   ```
2. Open the FocusGuard development client on the device.
3. Scan the QR code or select the local Metro URL (`http://<your-ip>:8081`).

### Development Client Testing Features:
- **Fast Refresh**: Save any React Native component file to see updates immediately.
- **Developer Menu**: Shake the physical device or press `Ctrl+M` (or `Cmd+M`) in the emulator to open developer settings, toggle performance monitors, or inspect elements.
- **Logcat Debugging**:
  ```bash
  # View live React Native and FocusGuard logs
  adb logcat *:S ReactNative:V ReactNativeJS:V FocusGuard:V FocusEnforcer:V
  ```
- **When to Rebuild Native**: You only need to rebuild native code when modifying native files (`modules/focus-enforcer/`, `plugins/`, `app.json` plugins, or installing new native libraries). Pure JavaScript/TypeScript edits only require Metro reload (`r` in the terminal).

---

## 5. Testing with Preview Build (Standalone APK)

The **Preview Build** generates a self-contained, standalone `.apk` that bundles all JavaScript code. It runs **completely independent of Metro or your computer**, making it the primary method for:
- QA and manual testing
- Testing persistent boot receivers, deep doze modes, and un-dismissible lock sessions
- Sideloading onto physical test devices

FocusGuard's [`eas.json`](./eas.json) is pre-configured for internal APK generation:
```json
"preview": {
  "distribution": "internal",
  "android": {
    "buildType": "apk"
  }
}
```

### Step 1: Generate the Preview APK

#### Method 1: Cloud Build via EAS (Recommended)
```bash
pnpm run build:preview
# or: eas build --profile preview --platform android
```
When complete, EAS outputs a direct download link and QR code. Scan the QR code on your Android device to install the APK directly.

#### Method 2: Local EAS Build
```bash
eas build --profile preview --platform android --local
```

#### Method 3: Direct Gradle Release Assembly
If building locally without EAS:
```bash
cd android
./gradlew assembleRelease
# The standalone APK will be generated at:
# android/app/build/outputs/apk/release/app-release.apk
cd ..
```

### Step 2: Install APK via ADB (Physical Device / Emulator)

```bash
# Install the generated APK directly
adb install -r path/to/focusguard-preview.apk

# Launch the app
adb shell monkey -p com.coder7575.SleepGuard -c android.intent.category.LAUNCHER 1
```

---

## 6. Testing Production Build

The **Production Build** creates a signed Android App Bundle (`.aab`) optimized for Google Play distribution.

```bash
# Build production bundle
eas build --profile production --platform android
```

### How to Test Production Bundles:
1. Upload the generated `.aab` to **Google Play Console** under **Internal Testing** or **Closed Beta**.
2. Add tester email addresses to the testing track.
3. Testers install FocusGuard directly through the Google Play Store to verify licensing, store metadata, and update delivery via Google Play.

---

## 7. Native Feature & Resilience Test Checklist

Because FocusGuard enforces strict discipline locks, verify the following critical test scenarios on a physical device or emulator using a Development or Preview build:

### Test 1: Permission Flow Verification
- [ ] **Accessibility Service**: Verify the app requests and navigates the user to enable `SleepGuard Focus Service`.
- [ ] **Android 13+ Restricted Settings Guidance**: On Android 13+ devices running sideloaded APKs, verify the restricted setting warning card appears if accessibility is not granted, offering a direct shortcut to App Info to unlock restricted settings.
- [ ] **Overlay Permission (`SYSTEM_ALERT_WINDOW`)**: Verify prompt to enable "Display over other apps".
- [ ] **Exact Alarms (`SCHEDULE_EXACT_ALARM`)**: Verify permission for exact timing across Android 14+.
- [ ] **Ignore Battery Optimization**: Ensure the app prompts to exempt itself from aggressive OEM battery killers.

> [!TIP]
> **Android 13+ ADB Bypass Shortcut:** For automated testing pipelines or emulator environments, you can instantly allow restricted settings via ADB:
> ```bash
> adb shell appops set com.coder7575.SleepGuard ACCESS_RESTRICTED_SETTINGS allow
> ```

### Test 2: Active Session Blocking
- [ ] Start a 5-minute Focus Session.
- [ ] Attempt to press the **Hardware/Gesture Home Button** — overlay must remain visible.
- [ ] Attempt to press the **Back Button** — overlay must consume the gesture without closing.
- [ ] Attempt to switch apps using the **Recent Apps / Overview Screen** — blocker must re-engage immediately.

### Test 3: Emergency Dialer Pass-Through (Safety Verification)
FocusGuard must never block emergency services. Run:
```bash
adb shell am start -a android.intent.action.DIAL -d "tel:911"
```
- [ ] Verify the system dialer opens and the emergency call interface is not blocked.

### Test 4: Device Reboot Survival (`BOOT_COMPLETED`)
- [ ] Start an active session with 10 minutes remaining.
- [ ] Reboot the device via ADB:
  ```bash
  adb reboot
  ```
- [ ] Unlock the device upon reboot.
- [ ] Verify `SleepGuard` re-engages the lock overlay automatically based on the epoch session end time.

### Test 5: Deep Doze & AlarmManager Simulation
Verify the lock stays in sync even when the device enters Android Deep Doze:
```bash
# 1. Unplug virtual battery
adb shell dumpsys battery unplug

# 2. Force device into deep doze
adb shell dumpsys deviceidle force-idle

# 3. Step through idle state
adb shell dumpsys deviceidle step

# 4. Reset battery
adb shell dumpsys battery reset
```

### Automated Resilience Suite
FocusGuard includes an automated verification script:
```bash
# Ensure execution permissions
chmod +x ./scripts/verify-resilience.sh

# Run the resilience test suite
./scripts/verify-resilience.sh
```

---

## 8. Automated Code Verification & Unit Testing

Run these tests before creating builds or submitting pull requests:

```bash
# 1. Run all sanity checks (TypeScript + ESLint + Jest)
pnpm test:sanity

# 2. Run Jest unit tests only
pnpm test

# 3. Check TypeScript types
pnpm tsc --noEmit

# 4. Run ESLint
pnpm lint
```

---

## 9. Troubleshooting & FAQ

### Issue: `Unable to resolve module ./wa-sqlite/wa-sqlite.wasm in web`
- **Cause**: Metro does not recognize `.wasm` files as assets by default.
- **Fix**: Ensure [`metro.config.js`](./metro.config.js) contains `config.resolver.assetExts.push('wasm')` and restarts with cache clear:
  ```bash
  pnpm web -- -c
  ```

### Issue: Gradle error `checkReleaseAarMetadata` (compileSdk mismatch)
- **Error**: `Dependency ':expo-modules-core' requires libraries and applications to compile against version 37 or later of Android APIs. :focus-enforcer is currently compiled against android-35.`
- **Cause**: Custom native module hardcoded an older Android SDK version incompatible with Expo SDK 58.
- **Fix**: In [`modules/focus-enforcer/android/build.gradle`](./modules/focus-enforcer/android/build.gradle), use dynamic SDK resolution from the root project:
  ```groovy
  def safeExtGet(prop, fallback) {
      rootProject.ext.has(prop) ? rootProject.ext.get(prop) : fallback
  }
  android {
      compileSdk safeExtGet('compileSdkVersion', 37)
      defaultConfig {
          minSdk safeExtGet('minSdkVersion', 24)
          targetSdk safeExtGet('targetSdkVersion', 37)
      }
  }
  ```

### Issue: `Cannot find module '@expo/config-plugins'` during EAS build
- **Cause**: Strict package managers like `pnpm` isolate transitive dependencies. If custom plugins in `plugins/` require `@expo/config-plugins`, it must be declared explicitly in dependencies.
- **Fix**: Run `npx expo install @expo/config-plugins`.

### Issue: `"Cannot find native module 'FocusEnforcer'"`
- **Cause**: The app is running in standard Expo Go or a JavaScript bundle was loaded into an older native build that didn't include the module.
- **Fix**: Run `pnpm android` to build and launch the dedicated Development Build.

### Issue: Port 8081 is already in use
```bash
# Terminate conflicting process on port 8081
npx kill-port 8081
pnpm start --clear
```

### Issue: Android build cache or Gradle failure
```bash
# Clean Gradle cache
cd android && ./gradlew clean && cd ..

# Or regenerate the native Android folder cleanly
npx expo prebuild --clean
pnpm android
```

### Issue: ADB device not detected
```bash
adb kill-server
adb start-server
adb devices
```
Ensure **USB Debugging** is toggled ON under **Developer Options** on your physical Android phone.
