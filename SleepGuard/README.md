# SleepGuard 🌙

> Modern, tactile focus lock and sleep guard mobile application built with React Native and Expo (SDK 58).

SleepGuard brings the uncompromised discipline and focus lock capabilities of FocusGuard to a cross-platform React Native architecture with Expo Continuous Native Generation (CNG).

---

## 🚀 Getting Started

### Prerequisites

- [Node.js](https://nodejs.org/) (LTS recommended)
- [pnpm](https://pnpm.io/) or npm / yarn
- Android Studio / Android SDK (for Android builds)
- [Expo Go](https://expo.dev/go) or an Android emulator/device

### Installation

```bash
# Install dependencies
pnpm install
```

### Running the App

```bash
# Start Expo development server
pnpm start
# or
npx expo start
```

Press `a` in the terminal to launch the Android emulator, or scan the QR code with Expo Go / custom development build.

### Running Native Builds

```bash
# Android
npx expo run:android

# iOS (macOS only)
npx expo run:ios
```

---

## 📁 Project Structure

```
SleepGuard/
├── src/
│   ├── app/           # Expo Router file-based pages and layouts
│   ├── components/    # Reusable UI components (dial timer, buttons, cards)
│   ├── constants/     # Colors, typography, and theme definitions
│   └── hooks/         # Custom React hooks (theme, orientation, color-scheme)
├── assets/            # App icons, splash screens, and images
├── app.json           # Expo project configuration
└── package.json       # Project dependencies and scripts
```

---

## 📖 Documentation & Guides

- [Migration Plan](./MIGRATION_PLAN.md): Architectural roadmap migrating FocusGuard native features to Expo.
- [Beginner Step-by-Step Guide](./BEGINNER_STEP_BY_STEP_GUIDE.md): Complete setup and onboarding guide.
- [Engineering Team Plan](./ENGINEERING_TEAM_PLAN.md): Engineering specs and milestones.

---

## 📄 License

Licensed under the [Apache License, Version 2.0](./LICENSE).
