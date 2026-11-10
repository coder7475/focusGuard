# FocusGuard: Build & Release Engineering Guide

> **Audience**: Android Engineers, QA Engineers, and Release Managers  
> **Scope**: Building test APKs, generating signed production App Bundles (AAB), configuring signing keys, and deploying through Google Play Console release tracks.

---

## 1. Build Artifact Overview

| Artifact | Format | Primary Use Case | Sideloadable? |
| :--- | :--- | :--- | :--- |
| **Debug APK** | `app-debug.apk` | Fast local testing, automated CI testing | Yes (`adb install`) |
| **Developer Release APK** | `app-release.apk` | Hardware field testing, full R8/performance validation | Yes (`adb install -r`) |
| **App Bundle (AAB)** | `app-release.aab` | Google Play Store distribution (Mandatory for Play) | No (Requires `bundletool`) |

---

## 2. Building APKs for Local & Hardware Testing

### Method A: Debug APK (Fast Iteration)
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Install to connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Method B: Signed Release APK (Hardware Field Testing)
Release builds execute R8 code shrinking and omit debug hooks, giving exact production performance:
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

#### Android 13+ Restricted Settings Workaround for Sideloaded APKs
Android 13+ blocks Accessibility services on sideloaded release APKs with a *"Restricted Setting"* dialog. To bypass this on hardware test devices:
```bash
# Unlock restricted settings via ADB for your test device
adb shell appops set com.coder7475.focusguard ACCESS_RESTRICTED_SETTINGS allow
```
*Manual alternative on device: Settings > Apps > FocusGuard > tap the 3-dot menu (top-right) > "Allow restricted settings".*

---

## 3. Production Signing & Keystore Configuration

### 3.1 Generate an Upload Keystore
Generate a dedicated Java KeyStore using `keytool` (included in JDK 17):
```bash
keytool -genkeypair -v \
  -keystore $HOME/keystores/focusguard-upload.jks \
  -alias focusguard-upload \
  -keyalg RSA \
  -keysize 4096 \
  -validity 9125 \
  -storetype JKS
```

> [!CAUTION]
> Store your keystore file and passwords in a secure team credential vault. Never commit `.jks` files or passwords to version control.

### 3.2 Secure Credentials Configuration
Create `keystore.properties` in the project root (ignored by `.gitignore`):
```properties
RELEASE_STORE_FILE=/path/to/focusguard-upload.jks
RELEASE_STORE_PASSWORD=YourStorePassword
RELEASE_KEY_ALIAS=focusguard-upload
RELEASE_KEY_PASSWORD=YourKeyPassword
```

`app/build.gradle.kts` automatically loads these properties when present:
```kotlin
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
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
            isMinifyEnabled = false
        }
    }
}
```

---

## 4. Building Production App Bundles (AAB)

### Version Code & Name Increment
Before each release, update [`app/build.gradle.kts`](file:///home/fahad/AndroidStudioProjects/focusGuard/app/build.gradle.kts#L17-L18):
```kotlin
defaultConfig {
    versionCode = 2     // Increment by +1 for every Play Console upload
    versionName = "1.0.1" // Public SemVer string
}
```

### Build via Gradle CLI
```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

### Build via Android Studio GUI
1. Navigate to **Build > Generate Signed Bundle / APK...**.
2. Select **Android App Bundle** > **Next**.
3. Select your keystore path, enter passwords, and choose alias `focusguard-upload`.
4. Choose build variant `release` and click **Create**.

---

## 5. Artifact Verification

### 5.1 Manifest & Policy Audit (APK Analyzer)
1. In Android Studio, go to **Build > Analyze APK...** and select `app-release.aab`.
2. Inspect `AndroidManifest.xml` and verify:
   - `BIND_ACCESSIBILITY_SERVICE` is attached to `FocusGuardAccessibilityService`.
   - `FOREGROUND_SERVICE` with `FOREGROUND_SERVICE_SPECIAL_USE` is declared.
   - **`QUERY_ALL_PACKAGES` is NOT present** (protects against Play rejection).

### 5.2 Split APK Local Testing (`bundletool`)
```bash
# Generate device-specific APKs from AAB
java -jar bundletool.jar build-apks \
  --bundle=app/build/outputs/bundle/release/app-release.aab \
  --output=app/build/outputs/bundle/release/focusguard.apks \
  --mode=default

# Install onto connected test device
java -jar bundletool.jar install-apks \
  --apks=app/build/outputs/bundle/release/focusguard.apks
```

---

## 6. Google Play Console Deployment

### 6.1 Release Tracks Strategy
1. **Internal Testing**: Distribute to internal engineering & QA first (bypasses full Google review; available in ~10 minutes).
2. **Closed Testing (Alpha/Beta)**: Distribute to trusted field users across various OEM hardware (Samsung, Xiaomi, OnePlus, Pixel).
3. **Production Staged Rollout**: Release gradually to catch unexpected OEM battery bugs:
   $$\text{Day 1: } 1\% \longrightarrow \text{Day 2: } 5\% \longrightarrow \text{Day 3: } 20\% \longrightarrow \text{Day 5: } 50\% \longrightarrow \text{Day 7: } 100\%$$

### 6.2 Mandatory Console Declarations
Before submitting your first release, configure declarations in **Play Console > App Content**:
- **Accessibility Tool**: Declare FocusGuard as a self-control tool that blocks distracting apps via foreground window detection. Refer to [`PLAY_STORE_DISCLOSURES.md`](file:///home/fahad/AndroidStudioProjects/focusGuard/doc/play/PLAY_STORE_DISCLOSURES.md) for required declaration copy and video requirements.
- **Foreground Service (`specialUse`)**: Justify continuous countdown ticker and anti-tamper monitoring.
- **Exact Alarms (`SCHEDULE_EXACT_ALARM`)**: Select "Alarms and timers" for exact session boundary enforcement.
- **Data Safety**: Declare **No data collected or shared**. FocusGuard is 100% offline.

---

## 7. Troubleshooting

| Error | Root Cause | Resolution |
| :--- | :--- | :--- |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Existing installed package signed with a different key | Run `adb uninstall com.coder7475.focusguard` before reinstalling |
| `Version code X has already been used` | Play Console requires monotonically increasing `versionCode` | Increment `versionCode` in `app/build.gradle.kts` and re-bundle |
| `Keystore was tampered with, or password was incorrect` | Typo in store/key password or alias | Verify with `keytool -list -v -keystore <path> -alias <alias>` |
| `LintVitalRelease` failure | Fatal lint error encountered during release bundling | Check report at `app/build/reports/lint-results-release.html` |
