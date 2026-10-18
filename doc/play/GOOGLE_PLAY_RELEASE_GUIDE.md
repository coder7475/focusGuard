# Google Play Store Release Engineering Guide: FocusGuard

> **Role & Authority:** Mobile Release Engineer  
> **Package:** `com.coder7475.focusguard`  
> **Target OS:** Android 7.0 (API 24) to Android 15+ (API 35+)  
> **Target Architecture:** Modern Android (Kotlin, Jetpack Compose, Room, DataStore, Foreground Services, Accessibility)

---

## 1. Release Philosophy & Non-Negotiable Rules

Releasing an Android app to Google Play is a **one-way door**. Once an Android App Bundle (AAB) is published and distributed to production devices:
1. **You cannot un-ship or rollback a binary.** There is no `git revert` or server rollback for installed client code. If a critical bug escapes, you can only roll a fix forward through Google Play's review queue, which takes hours to days.
2. **Signing identity is critical infrastructure, not a laptop file.** Never keep your production keystore exclusively on a local developer machine or email it. Losing your keystore when self-signing means you can never update the app again.
3. **Always enroll in Google Play App Signing.** Google manages and protects the actual app signing key in secure Google infrastructure. You only manage an **Upload Key**. If your upload key is lost or compromised, Google can reset it for you without breaking existing installs.
4. **Test the exact release artifact (AAB), never a debug build.** Minification, R8 code shrinking, resource stripping, and release keystores introduce runtime behavioral differences that debug builds do not expose.
5. **Never launch straight to 100% in production.** Always use a phased staged rollout (`1% -> 5% -> 20% -> 50% -> 100%`) with predefined crash/ANR halt criteria.
6. **Ship deobfuscation mapping files with every build.** A crash report without `mapping.txt` is an indecipherable stack of hex addresses.

---

## 2. Phase 1: Cryptographic Identity & Signing Infrastructure

### Step 1.1: Generate the Production Upload Keystore
Generate a dedicated Java KeyStore (JKS) for your upload key using `keytool`. Use RSA 4096-bit encryption with a 25-year validity period:

```bash
keytool -genkeypair -v \
  -keystore focusguard-upload.jks \
  -alias focusguard-upload \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -storetype PKCS12
```

> [!CAUTION]
> **Backup and Security:**
> - Store `focusguard-upload.jks` in a secure, encrypted secrets vault (e.g., 1Password, AWS Secrets Manager, HashiCorp Vault, or encrypted KeePass).
> - Never commit `.jks`, `.keystore`, or passwords to Git. The project `.gitignore` already ignores `*.jks` and `*.keystore`.

### Step 1.2: Configure Gradle Keystore Injection
Create a `keystore.properties` file in the project root for local builds (already ignored by `.gitignore`):

```properties
# keystore.properties (DO NOT COMMIT TO GIT)
storeFile=/absolute/path/to/focusguard-upload.jks
storePassword=YOUR_SECURE_STORE_PASSWORD
keyAlias=focusguard-upload
keyPassword=YOUR_SECURE_KEY_PASSWORD
```

On CI/CD runners (e.g., GitHub Actions), inject these values via Base64 environment variables instead of disk files:
- `KEYSTORE_BASE64`: `base64 -w 0 focusguard-upload.jks`
- `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`

Update `app/build.gradle.kts` to securely load signing configurations:

```kotlin
import java.io.FileInputStream
import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

android {
    ...
    signingConfigs {
        create("release") {
            val isCi = System.getenv("CI") == "true"
            if (isCi) {
                storeFile = file(System.getenv("KEYSTORE_PATH") ?: "upload.jks")
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            } else if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

---

## 3. Phase 2: Build Engineering, R8 & Optimization

### Step 2.1: Monotonic Versioning
Google Play strictly requires a strictly increasing integer `versionCode`.
- `versionCode`: Integer (e.g., `1`, `2`, `3`). Each upload must be greater than the previous.
- `versionName`: SemVer string displayed to users (e.g., `1.0.0`, `1.0.1`).

In `app/build.gradle.kts`:
```kotlin
defaultConfig {
    applicationId = "com.coder7475.focusguard"
    minSdk = 24
    targetSdk = 35 // Ensure targetSdk meets Google Play's current API requirement
    versionCode = 1
    versionName = "1.0.0"
}
```

### Step 2.2: ProGuard / R8 Rules for FocusGuard
FocusGuard relies on Room, DataStore, and Android Services. Create `app/proguard-rules.pro` to prevent R8 from stripping needed reflection or room metadata:

```proguard
# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep DataStore & Kotlin Serialization models if any
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Services and BroadcastReceivers referenced in Manifest
-keep public class com.coder7475.focusguard.service.** extends android.app.Service
-keep public class com.coder7475.focusguard.platform.** extends android.content.BroadcastReceiver
```

### Step 2.3: Build the Production Android App Bundle (AAB)
Run the clean release bundle build:

```bash
./gradlew clean bundleRelease
```

The output bundle and mapping file are produced at:
- **AAB:** `app/build/outputs/bundle/release/app-release.aab`
- **Mapping (Symbols):** `app/build/outputs/mapping/release/mapping.txt`

### Step 2.4: Smoke-Testing the Release Artifact
Never test on users. Test the actual release AAB locally using `bundletool` before uploading:

```bash
# 1. Generate universal APK from AAB
bundletool build-apks \
  --bundle=app/build/outputs/bundle/release/app-release.aab \
  --output=app-release.apks \
  --mode=universal \
  --ks=focusguard-upload.jks \
  --ks-pass=pass:YOUR_PASSWORD \
  --ks-key-alias=focusguard-upload \
  --key-pass=pass:YOUR_PASSWORD

# 2. Install to connected test device
bundletool install-apks --apks=app-release.apks
```

Verify that:
1. App launches with no crashes.
2. Accessibility service activates and window changes are detected.
3. Foreground service starts with its countdown notification.
4. Exact alarms trigger accurately.

---

## 4. Phase 3: Automated Release Pipeline with Fastlane

Avoid manual point-and-click uploads via the Play Console web UI. Automate AAB generation, mapping file uploads, and changelog management with Fastlane.

### Step 3.1: Service Account Setup in Google Cloud & Play Console
1. In Google Cloud Console, create a Service Account with **Google Play Android Developer** role.
2. Generate a JSON private key and save as `play-api-key.json` (encrypted in CI secrets).
3. In Google Play Console -> **API access**, invite the service account email and grant **Admin** or **Releases** permission.

### Step 3.2: Fastlane Configuration
Initialize fastlane or create `fastlane/Fastfile`:

```ruby
default_platform(:android)

platform :android do
  desc "Build signed AAB, upload mapping symbols, and deploy to Play Store Internal Track"
  lane :internal do
    gradle(
      task: "clean bundle",
      build_type: "Release"
    )

    upload_to_play_store(
      package_name: "com.coder7475.focusguard",
      track: "internal",
      aab: "app/build/outputs/bundle/release/app-release.aab",
      mapping_paths: ["app/build/outputs/mapping/release/mapping.txt"],
      json_key: ENV["PLAY_STORE_JSON_KEY"] || "play-api-key.json",
      release_status: "completed"
    )
  end

  desc "Promote Internal build to Production with 5% staged rollout"
  lane :promote_to_production do |options|
    rollout = options[:rollout] || "0.05" # 5% default
    upload_to_play_store(
      package_name: "com.coder7475.focusguard",
      track: "internal",
      track_promote_to: "production",
      rollout: rollout,
      json_key: ENV["PLAY_STORE_JSON_KEY"] || "play-api-key.json",
      release_status: "inProgress"
    )
  end
end
```

---

## 5. Phase 4: Google Play Console Policies & Sensitive Permissions

FocusGuard declares several sensitive Android permissions. Google Play strictly scrutinizes these. Failure to provide exact declarations and demonstrations will trigger an immediate rejection.

### 5.1 Personal Account Prerequisite: 20-Tester 14-Day Closed Testing
> [!IMPORTANT]
> If your Google Play developer account is a **personal account registered after November 13, 2023**, Google requires:
> - Minimum **20 testers** opted in to a **Closed Testing** track.
> - Testers must remain opted-in continuously for at least **14 days**.
> - Only after fulfilling this requirement does the Play Console unlock the button to apply for Production publishing.

### 5.2 Accessibility API Declaration (`BIND_ACCESSIBILITY_SERVICE`)
- **Review Vulnerability:** Google terminates or rejects apps that abuse Accessibility APIs for background tracking or ad blocking.
- **Requirements:**
  1. **In-App Prominent Disclosure:** Must display before the user is redirected to Android Accessibility Settings. Must disclose what data is accessed (window state), why (focus session enforcement), that no keystrokes/passwords/screen contents are read (`canRetrieveWindowContent = "false"`), and that zero data is sent off-device.
  2. **Play Console Video Demonstration:** Provide a publicly viewable YouTube link (can be Unlisted) demonstrating:
     - The in-app disclosure dialog showing to the user.
     - User turning on the accessibility service in system settings.
     - The user initiating a focus lock.
     - A blocked app opening and FocusGuard successfully displaying the blocking overlay.
  3. **Console Declaration Text:** Reference `doc/play/PLAY_STORE_DISCLOSURES.md`.

### 5.3 Foreground Service `specialUse` Declaration (Android 14+)
- **Manifest property:** `android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE` = `"Focus lock countdown ticker and state preservation"`.
- **Review Requirement:** Submit justification explaining that WorkManager cannot maintain per-second notification countdown updates, and system Doze mode would terminate the active session state prematurely.

### 5.4 Exact Alarm (`SCHEDULE_EXACT_ALARM`) Declaration
- **Category:** Timer & Clock / Strict Focus Enforcement.
- **Justification:** Exact epoch wakeups are required to terminate the session lock. Inexact alarms introduce 5-15 minute delays, leaving users unjustly locked out after their timer expires.

### 5.5 Data Safety & Privacy Form
- **Data Collection:** **No data collected.** FocusGuard operates 100% locally.
- **Data Sharing:** **No data shared** with third parties.
- **Encryption in transit:** Not applicable (app makes zero external network calls).
- **Privacy Policy URL:** Host a clear privacy policy (e.g., GitHub Pages or public domain) stating that no personal data, telemetry, or window contents leave the user's device.

### 5.6 Store Listing Assets Checklist
- [ ] **App Icon:** 512 x 512 px, 32-bit PNG with alpha.
- [ ] **Feature Graphic:** 1024 x 500 px, JPG or 24-bit PNG (no alpha).
- [ ] **Phone Screenshots:** At least 4 screenshots (minimum 1080 x 1920 px or 16:9 / 9:16 aspect ratio).
- [ ] **Tablet Screenshots:** 7-inch and 10-inch screenshots (recommended for wider catalog reach).
- [ ] **Short Description:** Up to 80 characters (e.g., *"Strict focus session timer with distraction blocking and habit discipline."*).
- [ ] **Full Description:** Up to 4000 characters detailing features, how focus locking works, and privacy guarantees.

---

## 6. Phase 5: Staged Rollout Strategy & Health Telemetry

### 6.1 Promotion Tracks
```
[Local Release AAB]
        │
        ▼
[Internal Testing]  ──>  Fast automated distribution to core dev/QA team (instant approval)
        │
        ▼
[Closed Testing]    ──>  20+ testers for 14 continuous days (Google Play requirement)
        │
        ▼
[Production Staged Rollout]
   ├── Day 1: 1%
   ├── Day 2: 5%
   ├── Day 3: 20%
   ├── Day 4: 50%
   └── Day 5: 100%
```

### 6.2 Go / No-Go Halt Criteria
During production rollout, monitor the **Android Vitals** dashboard and crash reporter continuously:

| Metric | Target / Healthy | Halt & Investigate Threshold |
| :--- | :--- | :--- |
| **Crash-Free Sessions** | $\ge 99.5\%$ | $< 99.0\%$ |
| **User-Perceived Crash Rate** | $< 1.09\%$ (Google bad behavior threshold) | $\ge 1.09\%$ |
| **User-Perceived ANR Rate** | $< 0.47\%$ (Google bad behavior threshold) | $\ge 0.47\%$ |
| **P0 Functional Regression** | 0 | Any report of permanent lockup or dialer obstruction |

### 6.3 Incident Response: Halt vs. Roll-Forward
- **If a red signal occurs:**
  1. Go to **Play Console -> Production -> Releases -> Edit release -> Halt rollout**.
  2. The halted rollout stops new downloads immediately; existing users stay on the build.
  3. Formulate the fix on a hotfix branch.
  4. Bump `versionCode` (e.g., from `1` to `2`) and `versionName` (e.g., `1.0.1`).
  5. Build, sign, test, and release the hotfix directly to a new staged rollout.

---

## 7. Pre-Submission Checklist (Release-Blocking Gate)

Before promoting any build to Public Production, verify every item below:

```markdown
### Binary & Build Configuration
- [ ] `versionCode` bumped monotonically and strictly greater than current store version.
- [ ] `versionName` updated to match intended release tag (e.g., `v1.0.0`).
- [ ] `targetSdk` satisfies latest Google Play requirement (currently API 34/35).
- [ ] Built with `./gradlew bundleRelease` using the official upload keystore.
- [ ] ProGuard / R8 enabled with `isMinifyEnabled = true` and `isShrinkResources = true`.
- [ ] `mapping.txt` exported and archived with release tag.
- [ ] Universal APK tested on physical hardware via `bundletool`.

### Permissions & Policy Declarations
- [ ] Accessibility prominent disclosure dialog verified in clean app install.
- [ ] Unlisted YouTube demo video URL added to Accessibility declaration in Play Console.
- [ ] Foreground Service `specialUse` subtype and justification submitted in Console.
- [ ] Exact Alarm (`SCHEDULE_EXACT_ALARM`) declaration completed.
- [ ] Data Safety form verified (zero data collected, zero shared).
- [ ] Privacy policy link active and reachable via HTTPS.

### Store Listing & Testing
- [ ] Store metadata (Title, Short Description, Full Description) proofread.
- [ ] Hi-res icon (512x512) and Feature Graphic (1024x500) uploaded.
- [ ] At least 4 high-quality screenshots uploaded.
- [ ] Closed testing track completed (20 testers for 14 days if personal account).
- [ ] Rollout initialized at 1% or 5% (NEVER 100% on first release).
- [ ] On-call release engineer assigned to monitor Android Vitals for first 48 hours.
```
