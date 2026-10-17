# FocusGuard

FocusGuard is an Android app that helps you stay on task and reduce phone addiction by locking the device down on your terms.

## How it works

You choose when the phone should be focused:

- **Quick sessions** — pick a duration such as 15 min, 45 min, or 1 hr and start it on demand.
- **Schedules** — set recurring windows, e.g. 8:00 PM to 8:00 AM.

During an active session, the phone is locked except for the essentials: **calls** and **messages**. FocusGuard runs as a device-level service with priority over other apps, so the lock cannot simply be dismissed by switching apps.

## Features

- [x] Focus session timer (15 min / 45 min / 1 hr presets)
- [x] Custom schedules with start and end times & overnight midnight-crossing support
- [x] Device-wide lock overlay with emergency calls and messages allowed
- [x] Accessibility overlay service (`TYPE_ACCESSIBILITY_OVERLAY`) with priority over blocked apps
- [x] Un-dismissible discipline lock (swallows back/home gestures, persists through reboot)
- [x] 7-Step Permission Onboarding Wizard with direct settings intent dispatchers
- [x] OEM Battery Killer mitigation dialogs (Samsung, Xiaomi, OnePlus)
- [x] Exact wall-clock alarm reconciliation surviving deep Doze
- [x] Foreground Service (`specialUse`) with continuous countdown notification ticker

## Tech stack

| Layer | Choice |
| --- | --- |
| Language | Kotlin 2.2 |
| UI | Jetpack Compose + Material 3 |
| Build | Gradle Kotlin DSL, Android Gradle Plugin 9.4.1 |
| SDK | `minSdk` 24, `targetSdk` 37 (Java 17 Toolchain + Core Desugaring) |
| Architecture | Clean Architecture (Domain, Data, Service, Platform, UI) |
| Storage | Room Database 2.8.4 + Preferences DataStore 1.1.1 |
| Package | `com.coder7475.focusguard` |

## Documentation

- **[Developer, Testing & Deployment Guide](doc/developer/DEVELOPMENT_TESTING_DEPLOYMENT_GUIDE.md)** — Complete step-by-step manual for Compose previews, automated unit tests, emulator/physical device testing, ADB quick commands, and Google Play Store deployment.
- **[Smoke Test Checklist](doc/qa/SMOKE_TEST_CHECKLIST.md)** — End-to-end verification scenarios (quick lock, dialer pass-through, reboot survival, tamper detection).
- **[Play Store Disclosures](doc/play/PLAY_STORE_DISCLOSURES.md)** — Prominent disclosure copy and video submission guidelines for Accessibility Service and Foreground Service (`specialUse`).
- **[Architecture Decision Records (ADRs)](doc/design/adrs/)** — System architecture decisions for locking, lifecycles, and alarm management.

## Getting started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1+) or newer
- **JDK 17**: Automatically configured via Gradle JVM Toolchain
- **Android Device / Emulator**: Running API 24 (Nougat) to API 35 (Android 15)

### Android Studio Workflow (Recommended)

1. Open Android Studio > **File > Open** > select the `focusGuard` repository.
2. Click **Sync Project with Gradle Files** (elephant icon) on the toolbar.
3. **Compose Previews**: Open [`HomeScreen.kt`](app/src/main/java/com/coder7475/focusguard/ui/home/HomeScreen.kt) and click **Split** to view and interact with the UI live.
4. **Run on Device/Emulator**: Select your emulator or connected device in the top toolbar dropdown and click the green **Run 'app'** button (`Shift+F10`).
5. **Inspect Database**: Open **App Inspection > Database Inspector** to view Room SQLite session tables live.
6. **Generate Release Bundle**: Navigate to **Build > Generate Signed Bundle / APK...** to export `.aab` for Google Play.

For in-depth walkthroughs, see the **[Developer, Testing & Deployment Guide](doc/developer/DEVELOPMENT_TESTING_DEPLOYMENT_GUIDE.md)**.

### Command-Line (CLI) Alternative

```bash
# Compile and run JVM unit tests
./gradlew testDebugUnitTest

# Run Android Lint audit
./gradlew lintDebug

# Assemble debug APK
./gradlew assembleDebug

# Build release Android App Bundle (AAB)
./gradlew bundleRelease
```

## Project structure

```
app/
├── src/main/java/com/coder7475/focusguard/
│   ├── MainActivity.kt          # Entry point and current Compose UI
│   └── ui/theme/                # Material 3 theme (Color, Type, Theme)
├── src/main/AndroidManifest.xml
└── build.gradle.kts
```

## Roadmap

The full MVP plan — scope, architecture, milestones, risks, skills, and references —
lives in **[doc/mvp/MVP_PLAN.md](doc/mvp/MVP_PLAN.md)**.

1. **MVP** — session timer, schedule editor, lock overlay with calls/messages allowed.
2. **Hardening** — prevent early unlock, survive reboot, handle permission revocation.
3. **Insights** — daily focus stats, streaks, and reminders.

## License

TBD.
