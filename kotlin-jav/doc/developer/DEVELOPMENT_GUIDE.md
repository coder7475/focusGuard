# FocusGuard: Developer Guide

> **Audience**: Android Engineers and Contributors  
> **Scope**: Local environment setup, Compose UI previews, test execution, emulator & physical device debugging, and diagnostic tools.

---

## 1. Environment Setup

### System Requirements
| Component | Requirement | Notes |
| :--- | :--- | :--- |
| **Android Studio** | Ladybug (2024.2.1+) or newer | Jetpack Compose interactive previews & Layout Inspector support |
| **JDK** | JDK 17 (Eclipse Adoptium Temurin) | Provisioned automatically via Gradle JVM Toolchain (`jvmToolchain(17)`) |
| **Android SDK** | `compileSdk 37`, `minSdk 24`, `targetSdk 37` | Core desugaring (`desugar_jdk_libs:2.1.4`) enabled for `java.time` back to API 24 |

### Initial Project Setup
1. Launch Android Studio and open `/home/fahad/AndroidStudioProjects/focusGuard`.
2. Sync Gradle by clicking **Sync Project with Gradle Files** or via **File > Sync Project with Gradle Files**.
3. Verify Gradle JDK in **Settings > Build, Execution, Deployment > Build Tools > Gradle** is set to **JDK 17**.

---

## 2. Jetpack Compose UI Previews

FocusGuard's UI is written 100% in Jetpack Compose. You can design, inspect, and test UI components without booting an emulator.

### Key Composable Previews
| Composable File | Preview Function | Purpose |
| :--- | :--- | :--- |
| [`HomeScreen.kt`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/src/main/java/com/coder7475/focusguard/ui/home/HomeScreen.kt) | `HomeScreenIdlePreview` | Idle state with duration chips (15m, 45m, 60m), schedule triggers, and Start button |
| [`HomeScreen.kt`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/src/main/java/com/coder7475/focusguard/ui/home/HomeScreen.kt) | `HomeScreenActivePreview` | Active lock session with remaining countdown timer and Cancel action |
| [`OnboardingScreen.kt`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/src/main/java/com/coder7475/focusguard/ui/onboarding/OnboardingScreen.kt) | `OnboardingScreenPreview` | 7-step permission progress tracker, status badges, and settings deep-links |
| [`LockOverlayScreen.kt`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/src/main/java/com/coder7475/focusguard/service/overlay/LockOverlayScreen.kt) | `LockOverlayScreenPreview` | Full-screen un-dismissible lock interface, countdown display, and Emergency action |

### Preview Features
- **Split / Design Mode**: Use the top-right toggle in the editor to view code alongside the rendered canvas.
- **Interactive Preview (`Ctrl+Shift+F5`)**: Click the hand icon above any preview card to test clicks, ripple effects, and internal state without deploying to a device.
- **Deploy Preview to Device**: Click the **Run Preview** icon (lightning bolt over device) in the preview header to deploy an isolated composable activity in under 3 seconds.
- **Live Edit**: Enable in **Settings > Editor > Live Edit**. Edit colors, padding, and text inside composables with immediate on-device updates.

---

## 3. Running Automated Tests

### Command Line
```bash
# Run JVM unit tests across all domain engines and repositories
./gradlew test

# Run tests with continuous build (auto-reruns on file save)
./gradlew test --continuous
```

### Android Studio Graphical Test Runner
- **Run a specific test**: Click the green arrow in the editor gutter next to any `@Test` method.
- **Run a test class**: Click the double green arrow next to the class definition.
- **Run all tests**: Right-click `app/src/test/java` in the Project view and select **Run 'Tests in 'java''** (`Ctrl+Shift+F10`).
- **Code Coverage**: Right-click `app/src/test/java` and select **More Run/Debug > Run Tests with Coverage**.

---

## 4. Running on Devices & Fast-Track Setup

### Android Virtual Device (Emulator)
1. Open **Tools > Device Manager** and launch a device running **API 34 or 35** (e.g. Pixel 8).
2. Select the device in the toolbar dropdown and press **Run 'app'** (`Shift+F10`).

### Physical Hardware (USB & Wi-Fi)
- **USB**: Enable **Developer Options > USB Debugging** on the target device, connect via cable, accept the host authorization prompt, and press **Run**.
- **Wireless Debugging (Android 11+)**: Open **Device Manager > Pair Devices Using Wi-Fi** in Android Studio and scan the pairing QR code from the phone's **Developer Options > Wireless debugging**.

### Fast-Track ADB Permission Grant
FocusGuard requires system-level permissions to enforce focus locks. Use this script in the Android Studio Terminal (`Alt+F12`) to grant all permissions immediately on a test device:

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

---

## 5. Debugging & Real-Time Inspection

### Logcat Filter Query
Open the **Logcat** tool window (`Alt+6`) and filter by FocusGuard tags:
```text
package:mine (tag:FocusGuard | tag:FocusGuardAccessibilityService | tag:FocusGuardService | tag:LockOverlayWindowManager | tag:AllowlistPolicy)
```

### Database Inspector (App Inspection)
FocusGuard stores session state and recurring schedules in Room SQLite (`focusguard_db`):
1. Open **View > Tool Windows > App Inspection** and select the **Database Inspector** tab.
2. Select the `com.coder7475.focusguard` process.
3. Inspect `focus_sessions` and `recurring_schedules`. Check **Live updates** to observe status transitions in real time.

### Layout Inspector
To verify that `TYPE_ACCESSIBILITY_OVERLAY` covers the screen edge-to-edge:
1. Start an active focus session and switch to a non-allowlisted app.
2. Open **View > Tool Windows > Layout Inspector** to inspect the 3D compose hierarchy of the active overlay.

### Simulating Doze & Midnight Transitions
```bash
# Force device into deep Doze mode
adb shell dumpsys deviceidle force-idle

# Restore device idle state
adb shell dumpsys deviceidle unforce
adb shell dumpsys battery reset
```
