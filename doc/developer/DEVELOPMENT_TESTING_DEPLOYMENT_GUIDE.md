# FocusGuard: Android Studio Development, Testing & Deployment Guide

> A comprehensive, step-by-step visual and technical guide for developing, previewing, testing, debugging, and deploying FocusGuard using **Android Studio** (Ladybug/Koala/Meerkat) and Android SDK tools.

---

## Table of Contents
1. [Android Studio Environment Setup](#1-android-studio-environment-setup)
2. [Developing & Inspecting UI with Jetpack Compose Previews](#2-developing--inspecting-ui-with-jetpack-compose-previews)
3. [Running Automated Tests in Android Studio](#3-running-automated-tests-in-android-studio)
4. [Running on Virtual Devices (Android Emulator)](#4-running-on-virtual-devices-android-emulator)
5. [Running on Physical Hardware Devices (USB & Wi-Fi)](#5-running-on-physical-hardware-devices-usb--wi-fi)
6. [Real-Time Debugging & Inspection Tools](#6-real-time-debugging--inspection-tools)
7. [Manual Functional Verification Matrix](#7-manual-functional-verification-matrix)
8. [Building Signed App Bundles via Android Studio](#8-building-signed-app-bundles-via-android-studio)
9. [Google Play Console Deployment & Policy Compliance](#9-google-play-console-deployment--policy-compliance)

---

## 1. Android Studio Environment Setup

### 1.1 Recommended Versions & System Configuration
| Component | Supported Version | Notes |
|-----------|-------------------|-------|
| **Android Studio** | Ladybug (2024.2.1+) or newer | Supports Kotlin 2.2, Compose Preview Interactive Mode, and Layout Inspector |
| **Gradle JDK** | JDK 17 (Eclipse Adoptium Temurin 17) | Automatically provisioned via Gradle JVM Toolchain (`jvmToolchain(17)`) |
| **Android SDK Platform** | API 35 / API 34 | `compileSdk 37`, `minSdk 24`, `targetSdk 37` |
| **Core Desugaring** | Enabled (`desugar_jdk_libs:2.1.4`) | Supports `java.time` back to Android 7.0 (API 24) |

### 1.2 Opening the Project in Android Studio
1. Launch **Android Studio**.
2. On the Welcome Screen, click **Open** (or go to **File > Open...** from the main menu).
3. Navigate to and select the root directory: `/home/fahad/AndroidStudioProjects/focusGuard`.
4. Click **OK**. Android Studio will initialize and open the project.

### 1.3 Syncing Project with Gradle Files
1. Whenever Gradle configuration files change (`build.gradle.kts`, `libs.versions.toml`), Android Studio will prompt with a yellow bar: *"Gradle files have changed: Sync Now"*.
2. Click **Sync Now**, or click the **Sync Project with Gradle Files** icon (elephant with blue downward arrow) on the top toolbar.
3. Alternatively, use the menu: **File > Sync Project with Gradle Files**.
4. Confirm in the bottom **Build** tool window (`Alt+4` / `Cmd+4`) that the sync completes with: `BUILD SUCCESSFUL`.

### 1.4 Verifying the Gradle JDK Setting
To ensure Android Studio doesn't clash with external Java versions:
1. Open **Settings** (`Ctrl+Alt+S` on Linux/Windows, `Cmd+,` on macOS).
2. Navigate to **Build, Execution, Deployment > Build Tools > Gradle**.
3. Under **Gradle Projects**, find **Gradle JDK**.
4. Ensure it is set to **Java 17** (or **Project SDK / Embedded JDK 17**).
5. Click **Apply** and **OK**.

---

## 2. Developing & Inspecting UI with Jetpack Compose Previews

FocusGuard's UI is written 100% in declarative Jetpack Compose. You do not need to boot an emulator or compile the full application just to design, test, or tweak screens.

### 2.1 Available Previews in FocusGuard
| Composable File | Preview Name | UI State Displayed |
|-----------------|--------------|---------------------|
| [`HomeScreen.kt`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/src/main/java/com/coder7475/focusguard/ui/home/HomeScreen.kt) | `HomeScreenIdlePreview` | Idle state with quick duration chips (15m, 45m, 60m), schedule shortcuts, and Start Focus button |
| [`HomeScreen.kt`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/src/main/java/com/coder7475/focusguard/ui/home/HomeScreen.kt) | `HomeScreenActivePreview` | Active lock session state with remaining countdown timer, motivational quote, and Cancel Session button |
| [`OnboardingScreen.kt`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/src/main/java/com/coder7475/focusguard/ui/onboarding/OnboardingScreen.kt) | `OnboardingScreenPreview` | 7-step permission progress tracker, status badges, description copy, and direct settings launch buttons |
| [`LockOverlayScreen.kt`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/src/main/java/com/coder7475/focusguard/service/overlay/LockOverlayScreen.kt) | `LockOverlayScreenPreview` | Full-screen un-dismissible lock interface, giant MM:SS countdown, motivational copy, and Emergency phone dialer action |

### 2.2 Using Compose Previews in Android Studio
1. Open any of the files listed above in the editor (e.g. `HomeScreen.kt`).
2. In the top-right corner of the editor tab, choose your preferred view:
   - **Code** (editor only)
   - **Split** (code on left, live preview on right — **Recommended**)
   - **Design** (full canvas preview)
3. Android Studio will render the composable in real time.

```
+------------------------------------+---------------------------------------+
|  HomeScreen.kt (Code)              |  Compose Preview Canvas               |
|                                    |                                       |
|  @Preview(showBackground = true)   |  +---------------------------------+  |
|  @Composable                       |  |  🛡️ FocusGuard                  |  |
|  fun HomeScreenIdlePreview() {     |  |                                 |  |
|      ...                           |  |  Quick Lock: [15m] [45m] [60m]  |  |
|  }                                 |  |  [ START FOCUS SESSION ]        |  |
|                                    |  +---------------------------------+  |
+------------------------------------+---------------------------------------+
```

### 2.3 Interactive Preview Mode
Want to test clicking buttons, toggling dialogs, or scrolling lists without running the app?
1. Hover over the top bar of the preview card in the Preview canvas.
2. Click the **Start Interactive Mode** icon (small hand/finger icon, or press `Ctrl+Shift+F5`).
3. You can now tap buttons, trigger ripples, and interact with the UI directly inside the editor canvas!
4. Click the small **Stop** icon (`x`) in the preview header to exit interactive mode.

### 2.4 Deploying a Single Composable Preview to Device
If you want to test how a specific composable feels on your actual phone screen without navigating through the entire app:
1. Hover over the preview card.
2. Click the **Run Preview** icon (lightning bolt over device icon).
3. Android Studio instantly builds and deploys an isolated preview activity to your connected phone or emulator in under 3 seconds.

### 2.5 Live Edit for Jetpack Compose
1. Enable Live Edit in **Settings > Editor > Live Edit > Enable Live Edit**.
2. Run FocusGuard on your device or emulator.
3. Edit colors, padding, typography, or text inside any `@Composable` function.
4. Watch the running device update **immediately** without restarting or re-deploying!

---

## 3. Running Automated Tests in Android Studio

You can run, debug, and profile tests directly within Android Studio's graphical test runner.

### 3.1 Running Tests from the Editor Gutter
1. Open any test file, such as [`SessionEngineTest.kt`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/src/test/java/com/coder7475/focusguard/domain/engine/SessionEngineTest.kt).
2. Look at the left gutter next to line numbers:
   - Next to `class SessionEngineTest`: Click the double green arrow icon to run the **entire test class**.
   - Next to `@Test fun startQuickSession_createsActiveSession()`: Click the single green arrow icon to run that **specific test**.
3. Select **Run 'startQuickSession_createsActiveSession()'** or **Debug '...'** (to set breakpoints).

### 3.2 Running the Entire Test Suite via Project Window
1. Open the **Project** tool window (`Alt+1` / `Cmd+1`).
2. Switch to the **Project** or **Android** view.
3. Expand `app > src > test > java`.
4. Right-click the package `com.coder7475.focusguard` (or the `test/java` directory).
5. Click **Run 'Tests in 'com.coder7475...''** (or press `Ctrl+Shift+F10` / `Ctrl+Shift+R`).

### 3.3 Graphical Test Runner Window
When tests run, Android Studio opens the **Run** tool window at the bottom:
- **Left pane**: Tree view of all passing (`✓` green checkmark) and failing (`✗` red cross) test suites.
- **Right pane**: Detailed test output, execution time, and stack traces.
- **Toolbar buttons**:
  - **Rerun Failed Tests**: Retries only tests that failed in the last run.
  - **Toggle Auto-test**: Automatically re-runs tests whenever you save a code change.
  - **Export Test Results**: Exports test reports in HTML or XML format.

### 3.4 Running Tests with Code Coverage
1. Right-click the `app/src/test/java` folder.
2. Select **More Run/Debug > Run 'Tests in 'java'' with Coverage**.
3. Android Studio runs the suite and highlights lines of code in your editor:
   - **Green margin**: 100% covered.
   - **Yellow margin**: Partially covered branches.
   - **Red margin**: Uncovered code.

---

## 4. Running on Virtual Devices (Android Emulator)

The Android Emulator (AVD) provides isolated sandboxes to test Android versions from API 24 (Nougat) through API 35 (Android 15).

### 4.1 Creating a Virtual Device in Device Manager
1. Open the **Device Manager** tool window:
   - Click the **Device Manager** tab on the right sidebar (phone icon with Android robot).
   - Or from the menu: **Tools > Device Manager**.
2. Click **+** (or **Create Device**).
3. **Select Hardware**: Choose **Phone > Pixel 8** (or **Pixel 7**). Click **Next**.
4. **Select System Image**:
   - Tab: **Recommended**.
   - Download and select **VanillaIceCream (API 35)** or **UpsideDownCake (API 34)** with Google APIs (x86_64).
   - Click **Next**.
5. **Verify Configuration**:
   - AVD Name: `Pixel_8_API_35`.
   - Graphics: **Automatic** or **Hardware - GLES 2.0**.
   - Click **Show Advanced Settings** > set **Internal Storage** to `4096 MB` and **RAM** to `2048 MB`.
6. Click **Finish**.

### 4.2 Launching the Emulator & Running FocusGuard
1. In the **Device Manager**, click the green **Play** button next to your virtual device.
2. The emulator starts up (either docked in the Android Studio **Emulator** tool window or as a floating window).
3. On the main top toolbar of Android Studio:
   - Device selector dropdown: Select your running emulator (e.g. `Pixel 8 API 35`).
   - Run configuration: Select `app`.
4. Click the green **Run 'app'** button (`Shift+F10`), or click **Debug 'app'** (`Shift+F9`).
5. FocusGuard compiles, installs, and launches automatically on the emulator screen!

### 4.3 Fast-Track Setup via Android Studio Terminal
Android Studio includes a built-in terminal (`Alt+F12` / `Cmd+F12` or click **Terminal** on the bottom bar). You can grant all 7 required permissions to the emulator in one step:

```bash
# 1. Enable FocusGuard Accessibility Service
adb shell settings put secure enabled_accessibility_services com.coder7475.focusguard/com.coder7475.focusguard.service.accessibility.FocusGuardAccessibilityService
adb shell settings put secure accessibility_enabled 1

# 2. Grant Notification Permission (Android 13+)
adb shell pm grant com.coder7475.focusguard android.permission.POST_NOTIFICATIONS

# 3. Grant Exact Alarm Permission (Android 12+)
adb shell appops set com.coder7475.focusguard SCHEDULE_EXACT_ALARM allow

# 4. Whitelist from Battery Optimization (Doze bypass)
adb shell dumpsys deviceidle whitelist +com.coder7475.focusguard

# 5. Grant Usage Stats Access
adb shell appops set com.coder7475.focusguard GET_USAGE_STATS allow
```

### 4.4 Simulating Doze & Alarms using Emulator Controls
1. On the emulator side toolbar, click the **...** (Extended Controls) button.
2. **Battery**: Set Battery status to **Discharging** and AC charger to **None**.
3. In the Android Studio Terminal, force deep Doze mode:
   ```bash
   adb shell dumpsys deviceidle force-idle
   ```
4. Observe that FocusGuard's exact wall-clock alarm fires cleanly and unlocks the phone when the timer expires!
5. Restore power:
   ```bash
   adb shell dumpsys deviceidle unforce
   adb shell dumpsys battery reset
   ```

---

## 5. Running on Physical Hardware Devices (USB & Wi-Fi)

Physical hardware testing is essential to verify OEM battery management (Samsung OneUI, Xiaomi HyperOS, OnePlus OxygenOS).

### 5.1 Connecting via USB Cable
1. On your phone:
   - Go to **Settings > About Phone**.
   - Tap **Build Number** 7 times until you see *"You are now a developer!"*.
   - Go back to **Settings > System / Developer Options**.
   - Enable **USB Debugging**.
2. Connect your phone to your computer with a USB cable.
3. Your phone will show a dialog: *"Allow USB debugging?"*. Check *"Always allow from this computer"* and tap **Allow**.
4. In Android Studio, your phone will now appear in the device selector dropdown on the top toolbar.
5. Click **Run 'app'** (`Shift+F10`).

### 5.2 Connecting via Wireless Debugging (Wi-Fi, Android 11+)
No cable needed! You can pair and deploy directly over local Wi-Fi:
1. Ensure your computer and phone are connected to the **same Wi-Fi network**.
2. In Android Studio, open **Device Manager** and click **Pair Devices Using Wi-Fi** (or click the device dropdown > **Pair device using Wi-Fi**).
3. A modal with a QR Code appears on your computer screen.
4. On your phone:
   - Open **Settings > Developer Options > Wireless debugging**.
   - Toggle **Wireless debugging** to **ON**.
   - Tap **Pair device with QR code**.
   - Point your phone's camera at the QR code on your computer screen.
5. Android Studio will beep and show: *"Device successfully paired"*.
6. Your phone is now wirelessly connected! Select it from the device dropdown and press **Run**.

---

## 6. Real-Time Debugging & Inspection Tools

Android Studio includes powerful built-in inspection windows specifically useful for FocusGuard's architecture:

### 6.1 Logcat Tool Window (`Alt+6` / `Cmd+6`)
FocusGuard outputs structured diagnostic logs for window state changes and lock enforcement.
1. Open the **Logcat** tool window at the bottom of Android Studio.
2. Ensure your connected device and `com.coder7475.focusguard` are selected.
3. Enter this query in the search bar:
   ```
   package:mine (tag:FocusGuard | tag:FocusGuardAccessibilityService | tag:FocusGuardService | tag:LockOverlayWindowManager)
   ```
4. You will see real-time output whenever you switch apps:
   - `AllowlistPolicy: Package com.instagram.android is BLOCKED -> presenting lock overlay`
   - `AllowlistPolicy: Package com.google.android.dialer is ALLOWED -> dismissing overlay`

### 6.2 App Inspection (Database Inspector)
FocusGuard uses an embedded Room SQLite database (`focusguard_db`). You can inspect and modify database rows live without writing SQL commands or pulling files:
1. Open **App Inspection** from the bottom tool window bar (or **View > Tool Windows > App Inspection**).
2. Select the **Database Inspector** tab.
3. Select your running device and the `com.coder7475.focusguard` process.
4. In the left pane, expand `focusguard_db`:
   - Double-click **`focus_sessions`**: View rows for active and completed sessions (`sessionType`, `status`, `startedAtEpochMs`, `endsAtEpochMs`).
   - Double-click **`recurring_schedules`**: View configured schedules, start/end minutes, and day-of-week bitmasks.
5. Check the **Live updates** checkbox. Start a session in the app and watch the status column update in real time from `IDLE` to `ACTIVE`!

```
+---------------------------------------------------------------------------------------------------+
| App Inspection: Database Inspector (focusguard_db)                             [x] Live updates   |
+---------------------------------------------------------------------------------------------------+
| id | sessionType | status | startedAtEpochMs | endsAtEpochMs | targetDurationMs | completedAtMs   |
|----+-------------+--------+------------------+---------------+------------------+-----------------|
| 1  | QUICK       | ACTIVE | 1729188000000    | 1729188900000 | 900000           | NULL            |
+---------------------------------------------------------------------------------------------------+
```

### 6.3 Layout Inspector
To inspect the un-dismissible Compose overlay window while it is on screen:
1. Trigger a lock session so the overlay is visible.
2. In Android Studio, go to **View > Tool Windows > Layout Inspector**.
3. Select your device and process.
4. You will see a 3D structural breakdown of the Jetpack Compose tree, confirming that `TYPE_ACCESSIBILITY_OVERLAY` correctly covers the display edge-to-edge.

### 6.4 Device File Explorer
Inspect DataStore preferences on a test device:
1. Open **View > Tool Windows > Device File Explorer**.
2. Navigate to: `/data/data/com.coder7475.focusguard/files/datastore/`.
3. Right-click `focus_preferences.preferences_pb` > **Save As...** to inspect local key-value state.

---

## 7. Manual Functional Verification Matrix

Execute these 6 core scenarios before deploying any release:

| Test ID | Scenario | Android Studio & Device Steps | Expected Outcome |
|---------|----------|-------------------------------|------------------|
| **ST-01** | **Quick Focus Lock** | Select 15m preset on Home Screen > tap **Start Focus Session** > switch to Chrome or Instagram | `LockOverlayWindowManager` presents full-screen dark overlay with live MM:SS countdown within 100ms. |
| **ST-02** | **Dialer Pass-Through** | While overlay is displayed, tap the **Emergency** button (or open Phone app) | System dialer opens immediately; overlay is dismissed; calls can be placed without obstruction. |
| **ST-03** | **SMS Pass-Through** | While overlay is active, open Google Messages or Samsung Messages | Messaging app is allowlisted; overlay does not trigger; incoming/outgoing SMS works normally. |
| **ST-04** | **Overlay Gesture Guard** | While overlay is displayed over blocked app, perform back gesture and home gesture | Overlay window flags swallow inputs; background app remains inaccessible. |
| **ST-05** | **Reboot Survival** | Start 15m session > execute `adb reboot` in Terminal > wait for boot | `BootCompletedReceiver` wakes; foreground notification resumes ticker; lock overlay re-arms. |
| **ST-06** | **Anti-Tampering Alert** | During active session, go to Settings > disable FocusGuard Accessibility Service | `FocusGuardService` ContentObserver triggers high-priority heads-up notification directing user to re-enable. |

---

## 8. Building Signed App Bundles via Android Studio

Google Play requires the **Android App Bundle (.aab)** format. You can generate a signed bundle directly via Android Studio's graphical wizard.

### 8.1 Generating a Signed AAB via the Android Studio GUI
1. From the top menu, go to **Build > Generate Signed Bundle / APK...**.
2. Select **Android App Bundle** and click **Next**.
3. **Key store path**:
   - If you already have a keystore: Click **Choose existing...** and select your `.jks` file.
   - If you need a new keystore: Click **Create new...**:
     - *Key store path*: Browse to a secure location (e.g. `~/keystores/focusguard.jks`).
     - *Password*: Enter a strong password.
     - *Alias*: `focusguard`.
     - *Validity (years)*: `25` or higher.
     - *Certificate*: Fill in First and Last Name (e.g. `FocusGuard Team`).
     - Click **OK**.
4. Enter your **Key store password**, **Key alias**, and **Key password**.
5. Check **Remember passwords** (optional) and click **Next**.
6. **Destination Folder**: Select where the bundle will be saved (e.g. `app/release`).
7. **Build Variants**: Select **`release`**.
8. Click **Create** (or **Finish**).

```
+--------------------------------------------------------------------------+
|  Generate Signed Bundle or APK                                           |
|                                                                          |
|  (•) Android App Bundle  ( ) APK                                         |
|                                                                          |
|  Key store path: [/home/fahad/keystores/focusguard.jks] [Create new...]  |
|  Key store password: [••••••••••••]                                      |
|  Key alias: [focusguard]                                                 |
|  Key password: [••••••••••••]                                            |
|                                                                          |
|  Build Variant: [release        ▼]                                       |
|                                                                          |
|                                        [ Cancel ]  [ Back ]  [ Create ]  |
+--------------------------------------------------------------------------+
```

9. Android Studio compiles the bundle with full optimization and desugaring.
10. When finished, a notification popup will appear at the bottom right:
    *"App bundle(s) generated successfully: [Locate]"*.
11. Click **Locate** to open the folder containing `app-release.aab`.

### 8.2 Inspecting the Bundle with "Analyze APK / Bundle"
Before uploading to Google Play, verify the bundle inside Android Studio:
1. From the menu, select **Build > Analyze APK...**.
2. Select your generated `app-release.aab`.
3. Android Studio displays the internal contents:
   - Click `AndroidManifest.xml`: Verify that **`QUERY_ALL_PACKAGES` is NOT present**.
   - Check `classes.dex`: Confirm core library desugaring classes are embedded for API 24 compatibility.
   - Check total download size (typically ~5-7 MB compressed).

---

## 9. Google Play Console Deployment & Policy Compliance

### 9.1 Required Play Store Disclosures & Forms
Because FocusGuard is a discipline and focus enforcement tool, specific policy declarations are required on the [Google Play Console](https://play.google.com/console). Reference [`PLAY_STORE_DISCLOSURES.md`](file:///home/fahad/AndroidStudioProjects/focusGuard/doc/play/PLAY_STORE_DISCLOSURES.md) for full disclosure text.

1. **Accessibility Service Declaration**:
   - In Play Console, open **App Content > Accessibility tool**.
   - Declare: *"FocusGuard uses AccessibilityService to detect when a blocked app is brought to the foreground during an active lock session and present a focus screen. FocusGuard does NOT read, inspect, store, or transmit screen text or keystrokes (`canRetrieveWindowContent=false`)."*
   - Provide an unlisted YouTube video showing the in-app onboarding disclosure, turning on the service in settings, and starting a lock session.
2. **Foreground Service Declaration (`specialUse`)**:
   - In **App Content > Foreground service permissions**, select `specialUse`.
   - Provide description: *"FocusGuard maintains an active wall-clock countdown notification ticker and guards session integrity against background termination."*
3. **Exact Alarm Permission (`USE_EXACT_ALARM`)**:
   - In **App Content > Exact alarms**, select *"Alarms and timers"*.
   - Justify: *"To guarantee that focus lock sessions conclude at the exact wall-clock second, even in deep Doze sleep."*
4. **Data Safety**:
   - Declare: **No user data collected, no user data shared with third parties**.

### 9.2 Release Track Deployment Steps
1. In Google Play Console, go to **Testing > Internal testing**.
2. Click **Create new release**.
3. Drag and drop `app-release.aab` into the upload zone.
4. Enter Release notes (e.g. *"FocusGuard 1.0 initial release — focus lock discipline and scheduling"*).
5. Click **Next** > **Save and publish**.
6. Add your Google email to the internal tester list. Within 5 minutes, you can install the production build directly from Google Play on your phone!
