# How to Build a Developer Release APK (Production-Grade Behavior)

> **Audience:** Android Developers, QA Engineers, and Hardware Field Testers  
> **Target:** FocusGuard (`com.coder7475.focusguard`)  
> **OS Compatibility:** Android 12 through Android 15 (API 31–35)

---

## 1. Overview: Debug vs. Developer Release Builds

When testing FocusGuard on physical devices, standard `debug` builds introduce significant performance overhead and trigger security restrictions on modern Android versions.

| Characteristic | Debug Build (`assembleDebug`) | Developer Release Build (`assembleRelease`) |
| :--- | :--- | :--- |
| **Compose Performance** | Unoptimized JIT, layout inspector hooks, animation stutter | Full AOT compilation, R8 optimization, smooth 60/120 FPS |
| **Android 13+ Restricted Settings** | Flagged as untrusted sideload $\rightarrow$ Accessibility disabled | Can be signed with production certificate or unlocked via ADB/App Info |
| **Signing Status** | Automatically signed with local `debug.keystore` | Requires explicit signing config in Gradle to avoid `unsigned.apk` |
| **Process Inspection** | `android:debuggable="true"` (vulnerable to memory dumps) | `android:debuggable="false"` (strictly production-hardened) |

---

## 2. Step 1: Configure Gradle for Signed Release Builds

By default, executing `./gradlew assembleRelease` outputs `app-release-unsigned.apk`. Android devices reject unsigned packages with:
```text
Failure [INSTALL_PARSE_FAILED_NO_CERTIFICATES]
```

To automatically generate a signed, directly-installable release APK, configure [`app/build.gradle.kts`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/build.gradle.kts):

### Option A: Fast Local Testing (Sign Release with Debug Keystore)
This is ideal for fast physical device testing without managing private keystore files:

```kotlin
// app/build.gradle.kts
android {
    ...
    buildTypes {
        release {
            // Sign the release build with the local machine's debug key
            signingConfig = signingConfigs.getByName("debug")
            
            // Production flags
            isDebuggable = false
            
            optimization {
                enable = false // Set to true when ready for full ProGuard/R8 shrinking
            }
        }
    }
}
```

### Option B: Professional Release Keystore (Field Testing & Distribution)
To create a dedicated certificate for distribution outside of Google Play:

1. Generate a keystore using Java's `keytool`:
   ```bash
   keytool -genkeypair -v \
     -keystore $HOME/keystores/focusguard-dev.jks \
     -alias focusguard \
     -keyalg RSA \
     -keysize 2048 \
     -validity 10000 \
     -storetype JKS
   ```

2. Reference the keystore in `app/build.gradle.kts`:
   ```kotlin
   android {
       signingConfigs {
           create("developerRelease") {
               storeFile = file("${System.getProperty("user.home")}/keystores/focusguard-dev.jks")
               storePassword = "your_keystore_password"
               keyAlias = "focusguard"
               keyPassword = "your_key_password"
           }
       }
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("developerRelease")
               isDebuggable = false
           }
       }
   }
   ```

---

## 3. Step 2: Build the Release APK

### Via Command Line (Recommended)
Open a terminal in the project root and run:

```bash
./gradlew assembleRelease
```

Once compilation finishes, verify the generated binary:
```bash
ls -lh app/build/outputs/apk/release/
```
*Expected Output:*
```text
app-release.apk
```

### Via Android Studio GUI
1. Open the **Build Variants** tool window (bottom-left corner).
2. Switch `:app` from `debug` to `release`.
3. Go to the top menu: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Click **Locate** in the bottom-right notification when the build completes.

---

## 4. Step 3: Installing & Bypassing Android 13/14/15 "Restricted Settings"

On Android 13 (API 33), Android 14 (API 34), and Android 15 (API 35), the operating system flags all sideloaded applications as potentially harmful and grays out the **Accessibility Service** toggle with:
> *"Restricted setting: For your security, this setting is currently unavailable."*

Because FocusGuard requires the Accessibility Service to detect foreground window switches, you must unlock this restriction.

### Method 1: Instant ADB Unlock (Fastest for Developers)

Connect your device via USB with USB Debugging enabled, then run:

```bash
# 1. Install the signed release APK
adb install -r app/build/outputs/apk/release/app-release.apk

# 2. Grant unrestricted settings privilege via AppOps
adb shell appops set com.coder7475.focusguard ACCESS_RESTRICTED_SETTINGS allow
```

> [!TIP]
> Running the `appops` command immediately unlocks the grayed-out switch in **Settings > Accessibility > FocusGuard**, allowing you to enable the service with one tap.

### Method 2: On-Device Manual Unlock (No Computer Required)

If you are sharing the APK with QA or field testers who do not have access to a computer:

1. Send and install `app-release.apk` on the physical device.
2. Open phone **Settings** $\rightarrow$ **Apps** $\rightarrow$ **FocusGuard**.
3. Tap the **three-dot icon (⋮)** in the top-right corner of the screen.
4. Tap **"Allow restricted settings"**.
5. Authenticate with device lock (PIN, pattern, or fingerprint).
6. Open **Settings** $\rightarrow$ **Accessibility** $\rightarrow$ **FocusGuard** and turn the switch **ON**.

---

## 5. Step 4: Verification & Runtime Inspection

### 1. Verify Non-Debuggable Production Flags
Confirm the installed build runs without debugging overhead:
```bash
adb shell dumpsys package com.coder7475.focusguard | grep -E "flags=|versionName="
```
*Verification:* The `DEBUGGABLE` flag must **not** be present in the output.

### 2. Verify Accessibility & Overlay Operation
Launch FocusGuard and start a test focus session:
```bash
adb shell am start -n com.coder7475.focusguard/.MainActivity
```
1. Select a duration on the Rotary Dial (e.g. 5 minutes).
2. Tap **ENGAGE FOCUS LOCK**.
3. Open a blocked app (e.g., YouTube, Chrome, or Social Media).
4. Monitor system logs:
   ```bash
   adb logcat -s FocusGuardA11y LockOverlayManager
   ```
5. Confirm that the **HARDWARE LOCK ACTIVE** overlay appears and all background touch inputs are consumed.

---

## 6. Common Pitfalls & Troubleshooting

### Error: `INSTALL_PARSE_FAILED_NO_CERTIFICATES`
* **Cause:** The APK was built without signing configurations (`app-release-unsigned.apk`).
* **Fix:** Add `signingConfig = signingConfigs.getByName("debug")` under `buildTypes { release { ... } }` in [`app/build.gradle.kts`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/build.gradle.kts).

### Error: Accessibility Switch is Grayed Out
* **Cause:** Android 13+ Restricted Settings security policy on non-Play Store installs.
* **Fix:** Run `adb shell appops set com.coder7475.focusguard ACCESS_RESTRICTED_SETTINGS allow` or unlock via **App Info > 3 dots > Allow restricted settings**.

### Error: `SecurityException: Neither user ... nor current process has android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
* **Cause:** Missing manifest declaration for battery optimization exemption.
* **Fix:** Ensure `<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />` is declared in [`app/src/main/AndroidManifest.xml`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/src/main/AndroidManifest.xml).
