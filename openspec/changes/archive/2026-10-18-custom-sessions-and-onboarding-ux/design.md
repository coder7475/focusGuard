## Context

FocusGuard currently restricts session duration selection to a circular rotary dial constrained to 5–120 minutes with 5-minute ticks, preventing users from entering precise durations (e.g., 10m, 35m, 90m, or multi-hour sessions like 180m). Furthermore, the onboarding experience presents a flat, unprioritized list of 7 technical permission cards with high cognitive load. On physical devices (particularly Android 13+), users struggle to navigate system settings and frequently miss restricted setting unlocks.

## Goals / Non-Goals

**Goals:**
- Provide intuitive custom session duration configuration via quick preset chips (`15m`, `25m`, `45m`, `60m`) and a direct numeric input dialog (1 to 1440 minutes).
- Persist the user's preferred session duration across app launches using DataStore.
- Redesign `OnboardingScreen` into prioritized tiers: **Core Enforcers** (Accessibility, Exact Alarms, Notifications) and **Background Durability** (Battery Optimization, Reboot Survival).
- Implement a sequential "Grant Next" action that automatically navigates to the next required permission intent.
- Provide explicit Android 13+ sideload guidance with a direct shortcut to App Info for restricted accessibility unlocking.

**Non-Goals:**
- Bypassing Android's platform security policies without user consent.
- Introducing complex cloud-synced account profiles.
- Allowing active focus sessions to be modified while locked.

## Decisions

### 1. Dual Duration Input (Rotary Dial + Presets + Custom Numeric Entry)
* **Choice:** Keep the tactile rotary dial as the primary physical interaction, but add quick preset chips and a custom numeric modal dialog.
* **Rationale:** The rotary dial provides strong tactile feedback for standard Pomodoro intervals (25m), while preset chips and direct numeric entry cater to users needing quick switching or extended sessions (e.g. 180m study blocks).
* **Alternatives Considered:** Replacing the rotary dial entirely with a standard slider. Rejected because the rotary dial is a core brand aesthetic and delivers superior tactile engagement.

### 2. Prioritized Onboarding Tiers with Smart Sequential Routing
* **Choice:** Separate permissions into two distinct sections: **Core Enforcers** (Mandatory for lock enforcement) and **Background Durability** (Prevents OS termination). Provide a prominent "Grant Next" floating/bottom button that automatically launches the highest-priority ungranted permission.
* **Rationale:** Eliminates the cognitive fatigue of analyzing 7 separate cards. The user can simply tap the primary button sequentially until setup is complete.
* **Alternatives Considered:** A multi-page swipeable carousel (1 permission per screen). Rejected because carousels hide overall progress and make returning to partially granted permissions difficult.

### 3. Sideload Detection & One-Tap App Info Navigation
* **Choice:** On Android 13+ (API 33+), if Accessibility is not granted, display an amber callout explaining how to unlock Restricted Settings with a one-tap button opening `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`.
* **Rationale:** Android does not permit apps to programmatically unlock Restricted Settings. Clear visual guidance and direct intent dispatch to the exact App Info screen removes 90% of user drop-off during testing.

## Risks / Trade-offs

- **[Risk] Extreme custom durations (e.g. 1440 minutes / 24 hours):** Could trap a user in an un-bypassable lock for too long if selected accidentally.
  $\rightarrow$ **Mitigation:** Impose a confirmation prompt if duration exceeds 180 minutes, clearly warning the user of the hardware discipline lock.
- **[Risk] Rotary dial visual sync with custom values:** If the user enters a custom value like 73 minutes or 300 minutes, a 120-minute dial scale might appear inaccurate.
  $\rightarrow$ **Mitigation:** Scale the dial indicator proportionally or display an "EXTENDED" badge when duration exceeds 120 minutes.
