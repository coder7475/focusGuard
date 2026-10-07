# FocusGuard

FocusGuard is an Android app that helps you stay on task and reduce phone addiction by locking the device down on your terms.

## How it works

You choose when the phone should be focused:

- **Quick sessions** — pick a duration such as 15 min, 45 min, or 1 hr and start it on demand.
- **Schedules** — set recurring windows, e.g. 8:00 PM to 8:00 AM.

During an active session, the phone is locked except for the essentials: **calls** and **messages**. FocusGuard runs as a device-level service with priority over other apps, so the lock cannot simply be dismissed by switching apps.

## Planned features

- [ ] Focus session timer (15 min / 45 min / 1 hr presets)
- [ ] Custom schedules with start and end times
- [ ] Device-wide lock overlay with calls and messages allowed
- [ ] Accessibility / overlay service with priority over other apps
- [ ] Unlock policy (e.g. require waiting out the session, no early exit)
- [ ] Usage stats and focus streaks
- [ ] Notifications for session start and end

## Tech stack

| Layer | Choice |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Build | Gradle Kotlin DSL, Android Gradle Plugin 9.4.1 |
| SDK | `minSdk` 24, `targetSdk` 37 |
| Package | `com.coder7475.focusguard` |

Planned for the locking mechanism: an Accessibility Service (or device admin) plus a full-screen overlay window, foreground service for the timer, and `NotificationManager` / usage access where needed.

## Project status

Early scaffold. The app currently launches a single Compose screen; none of the focus features are implemented yet. The checklist above is the roadmap.

## Getting started

### Prerequisites

- Android Studio (Ladybug or newer recommended)
- JDK 11+
- An Android device or emulator running API 24+

### Build and run

```bash
# Debug build
./gradlew assembleDebug

# Install on a connected device or running emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (device/emulator required)
./gradlew connectedAndroidTest
```

Or open the project in Android Studio and press **Run**.

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
