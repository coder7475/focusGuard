# FocusGuard: APK & AAB Build and Release Guide

> **Audience**: Android Engineers, QA Engineers, and Release Managers  
> **Scope**: Building `.apk` binaries for local emulator/device testing and `.aab` (Android App Bundle) archives for Google Play Store deployment.

---

## Table of Contents
1. [Overview: APK vs. AAB](#1-overview-apk-vs-aab)
2. [Prerequisites & Environment Setup](#2-prerequisites--environment-setup)
3. [Building `.apk` for Local Testing](#3-building-apk-for-local-testing)
   - [Method A: Gradle CLI (`assembleDebug`)](#method-a-gradle-cli-assembledebug)
   - [Method B: Android Studio GUI](#method-b-android-studio-gui)
   - [Installing & Testing on Device via ADB](#installing--testing-on-device-via-adb)
   - [Generating a Signed Release APK for Hardware Field Testing](#generating-a-signed-release-apk-for-hardware-field-testing)
4. [Building `.aab` for Google Play Store Release](#4-building-aab-for-google-play-store-release)
   - [Step 1: Generate a Release Keystore](#step-1-generate-a-release-keystore)
   - [Step 2: Configure Keystore Secrets Safely](#step-2-configure-keystore-secrets-safely)
   - [Step 3: Update Version Code and Version Name](#step-3-update-version-code-and-version-name)
   - [Method A: Build Signed AAB via Android Studio GUI](#method-a-build-signed-aab-via-android-studio-gui)
   - [Method B: Build Signed AAB via Gradle CLI (`bundleRelease`)](#method-b-build-signed-aab-via-gradle-cli-bundlerelease)
5. [Verifying & Inspecting Your Artifacts](#5-verifying--inspecting-your-artifacts)
   - [Inspect Manifest and Permissions with APK Analyzer](#inspect-manifest-and-permissions-with-apk-analyzer)
   - [Extract and Test Device-Specific APKs from AAB with `bundletool`](#extract-and-test-device-specific-apks-from-aab-with-bundletool)
6. [Deploying to Google Play Console](#6-deploying-to-google-play-console)
7. [Troubleshooting & Common Pitfalls](#7-troubleshooting--common-pitfalls)

---

## 1. Overview: APK vs. AAB

| Feature | Android Package (`.apk`) | Android App Bundle (`.aab`) |
| :--- | :--- | :--- |
| **Primary Target** | Local development, QA testing, internal side-loading | Google Play Store distribution (Mandatory since August 2021) |
| **Contents** | Complete compiled binary with all screen densities, ABIs, and localized language assets bundled together. | Publishing format containing all compiled code and resources; Google Play generates optimized split APKs per user device. |
| **Direct Device Installation** | **Yes** — Installable via `adb install` or file transfer. | **No** — Cannot be installed directly on a phone without conversion via `bundletool`. |
| **Signing Requirements** | Debug keystore (`~/.android/debug.keystore`) or custom release key. | Upload keystore + Google Play App Signing key. |

---

## 2. Prerequisites & Environment Setup

Verify the following tools are available in your shell:

```bash
# Verify Java JDK 17
java -version

# Verify Android Debug Bridge (ADB)
adb --version
```

If `adb` is not recognized, add the Android SDK `platform-tools` directory to your shell path:
```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

---

## 3. Building `.apk` for Local Testing

### Method A: Gradle CLI (`assembleDebug`)

The Gradle CLI build compiles FocusGuard using the debug signing configuration, which utilizes your local machine's default debug key.

1. Open your terminal in the project root:
   ```bash
   cd /home/fahad/AndroidStudioProjects/focusGuard
   ```

2. Run the `assembleDebug` task:
   ```bash
   ./gradlew assembleDebug
   ```

3. Locate the output APK:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Method B: Android Studio GUI

1. Open FocusGuard in **Android Studio**.
2. In the top toolbar, ensure the **Build Variant** is set to `debug`:
   - Open the **Build Variants** tool window (bottom-left margin or `View > Tool Windows > Build Variants`).
   - Confirm `:app` is set to `debug`.
3. From the main menu, select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. When compilation completes, a popup appears at the bottom-right:
   > *"APK(s) generated successfully for 1 module: Locate | Analyze"*
5. Click **Locate** to open the folder containing `app-debug.apk` in your file manager.

---

### Installing & Testing on Device via ADB

Connect your physical device via USB (or start an Android Virtual Device / Emulator).

1. Verify the device is connected and authorized:
   ```bash
   adb devices
   ```
   *Expected output:*
   ```text
   List of devices attached
   emulator-5554    device
   # OR
   R58N10XXXXX     device
   ```

2. Install the debug APK:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
   > `-r` flag reinstalls the application while preserving local SQLite database (`focusguard_db`) and DataStore preferences.

3. Launch FocusGuard directly from the terminal:
   ```bash
   adb shell am start -n com.coder7475.focusguard/.MainActivity
   ```

---

### Generating a Signed Release APK for Hardware Field Testing

When running field tests with external testers who do not have access to Google Play Internal Testing, build an optimized, signed release APK.

1. Execute the `assembleRelease` Gradle task:
   ```bash
   ./gradlew assembleRelease
   ```
2. The generated binary will be located at:
   ```
   app/build/outputs/apk/release/app-release.apk
   # Or app-release-unsigned.apk if no release signing credentials are provided
   ```

---

## 4. Building `.aab` for Google Play Store Release

Google Play requires all new production submissions to be uploaded as **Android App Bundles (`.aab`)**.

### Step 1: Generate a Release Keystore

If you do not already have an upload keystore for FocusGuard, generate one using `keytool` (included in JDK 17).

> [!CAUTION]
> **Store your keystore and passwords in a secure password manager.** If you lose this key and do not have Play App Signing enabled, you will not be able to update your app on Google Play.

Run the following command to generate `focusguard-release.jks`:

```bash
keytool -genkeypair -v \
  -keystore $HOME/keystores/focusguard-release.jks \
  -alias focusguard \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storetype JKS
```

You will be prompted to enter:
1. Keystore password (choose a strong passphrase)
2. Your name, organization, city, state, and two-letter country code
3. Key alias password (or press Enter to use the same password)

---

### Step 2: Configure Keystore Secrets Safely

FocusGuard's `.gitignore` already ignores `keystore.properties` and `*.jks`. Do not commit credentials to source control.

Create `keystore.properties` in the project root directory:

```properties
# keystore.properties (Ignored by Git)
RELEASE_STORE_FILE=/home/fahad/keystores/focusguard-release.jks
RELEASE_STORE_PASSWORD=YourStorePasswordHere
RELEASE_KEY_ALIAS=focusguard
RELEASE_KEY_PASSWORD=YourKeyPasswordHere
```

To enable automated CLI signing, configure `app/build.gradle.kts`:

```kotlin
// app/build.gradle.kts
import java.io.FileInputStream
import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    ...
    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties["RELEASE_STORE_FILE"] as String)
                storePassword = keystoreProperties["RELEASE_STORE_PASSWORD"] as String
                keyAlias = keystoreProperties["RELEASE_KEY_ALIAS"] as String
                keyPassword = keystoreProperties["RELEASE_KEY_PASSWORD"] as String
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false // Set to true with ProGuard rules if code shrinking is desired
        }
    }
}
```

---

### Step 3: Update Version Code and Version Name

Before every production release, increment `versionCode` and update `versionName` in [`app/build.gradle.kts`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/build.gradle.kts#L17-L18):

```kotlin
defaultConfig {
    applicationId = "com.coder7475.focusguard"
    minSdk = 24
    targetSdk = 37
    versionCode = 2     // Must be incremented by at least +1 on every release
    versionName = "1.1" // SemVer identifier displayed to end users
    ...
}
```

---

### Method A: Build Signed AAB via Android Studio GUI

1. Open FocusGuard in **Android Studio**.
2. From the main menu, navigate to **Build > Generate Signed Bundle / APK...**.
3. Select **Android App Bundle** and click **Next**.
4. In the dialog:
   - **Key store path**: Click **Choose existing...** and browse to `focusguard-release.jks`.
   - **Key store password**: Enter your keystore password.
   - **Key alias**: Select or enter `focusguard`.
   - **Key password**: Enter the key alias password.
   - *(Optional)* Check **Remember passwords**.
5. Click **Next**.
6. Set **Destination Folder** to `app/release` (or leave default).
7. Select **Build Variants**: `release`.
8. Click **Create**.
9. When the build finishes, click **Locate** on the completion notification.
   - Output file: `app-release.aab`

---

### Method B: Build Signed AAB via Gradle CLI (`bundleRelease`)

If you configured `keystore.properties` (or for headless CI/CD runners):

1. Execute the `bundleRelease` task:
   ```bash
   ./gradlew bundleRelease
   ```

2. Locate the generated bundle archive:
   ```
   app/build/outputs/bundle/release/app-release.aab
   ```

---

## 5. Verifying & Inspecting Your Artifacts

### Inspect Manifest and Permissions with APK Analyzer

Before submitting to Google Play, verify that no unintended permissions or large assets leaked into the bundle:

1. In Android Studio, go to **Build > Analyze APK...**.
2. Select `app/build/outputs/bundle/release/app-release.aab` (or your `.apk`).
3. Expand `AndroidManifest.xml` and verify:
   - `PACKAGE_USAGE_STATS` is present.
   - `SYSTEM_ALERT_WINDOW` is present.
   - `BIND_ACCESSIBILITY_SERVICE` is properly attached to [`FocusGuardAccessibilityService`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/src/main/java/com/coder7475/focusguard/service/accessibility/FocusGuardAccessibilityService.kt).
   - **`QUERY_ALL_PACKAGES` is NOT present** (protects against Play Store policy violation).
4. Inspect total download and raw uncompressed size.

---

### Extract and Test Device-Specific APKs from AAB with `bundletool`

Google Play uses Google's open-source [`bundletool`](https://github.com/google/bundletool/releases) to generate split APKs from an `.aab`. You can test this locally:

1. Download `bundletool.jar`:
   ```bash
   curl -L -o bundletool.jar https://github.com/google/bundletool/releases/download/1.18.0/bundletool-all-1.18.0.jar
   ```

2. Generate an `.apks` archive from your `.aab`:
   ```bash
   java -jar bundletool.jar build-apks \
     --bundle=app/build/outputs/bundle/release/app-release.aab \
     --output=app/build/outputs/bundle/release/focusguard.apks \
     --mode=default
   ```

3. Install the generated split APKs onto your connected test device:
   ```bash
   java -jar bundletool.jar install-apks \
     --apks=app/build/outputs/bundle/release/focusguard.apks
   ```

---

## 6. Deploying to Google Play Console

1. Navigate to the [Google Play Console](https://play.google.com/console).
2. Select **FocusGuard**.
3. Under the **Release** section in the left navigation bar, select **Testing > Internal testing**.
   > [!TIP]
   > Always publish to **Internal Testing** first. Internal test tracks bypass standard Google Play review queues, allowing installation within 5-10 minutes.
4. Click **Create new release** (top right).
5. Drag and drop `app-release.aab` into the **App bundles** drop zone.
6. Verify:
   - Version name and version code match your intended release.
   - Google Play App Signing key is acknowledged.
7. Enter Release notes (e.g. `FocusGuard 1.0 initial release`).
8. Click **Next** > **Save and publish**.
9. Invite testers via email under the **Testers** tab and verify the install on physical hardware.

---

## 7. Troubleshooting & Common Pitfalls

### Issue 1: `INSTALL_FAILED_UPDATE_INCOMPATIBLE`
- **Cause**: The device has a version installed that was signed with a different key (e.g. attempting to install a `release` APK over a `debug` APK).
- **Solution**: Uninstall the existing app before installing:
  ```bash
  adb uninstall com.coder7475.focusguard
  adb install -r app/build/outputs/apk/debug/app-debug.apk
  ```

### Issue 2: `Version code X has already been used`
- **Cause**: Google Play Console requires every uploaded `.aab` to have a strictly higher `versionCode` than prior uploads.
- **Solution**: Increment `versionCode` in `app/build.gradle.kts` (e.g., `1` -> `2`) and rebuild.

### Issue 3: `Keystore was tampered with, or password was incorrect`
- **Cause**: Password typo or mismatch between `storePassword` and `keyPassword`.
- **Solution**: Test keystore access using `keytool`:
  ```bash
  keytool -list -v -keystore /path/to/focusguard-release.jks -alias focusguard
  ```

### Issue 4: LintVitalRelease Failure During `bundleRelease`
- **Cause**: Android Gradle Plugin runs strict `lintVitalRelease` on production bundles. Unused resources or deprecation warnings marked as fatal will fail the build.
- **Solution**: Inspect the report at `app/build/reports/lint-results-release.html` to address reported fatal issues.
