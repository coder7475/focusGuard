## Why

Users need the flexibility to configure custom focus session parameters (such as entering exact minutes, quick duration presets, and customized discipline settings) beyond fixed rotary dial ticks. Simultaneously, the permission onboarding flow currently presents a wall of 7 technical permission cards with high cognitive load and friction—particularly on Android 13+ where restricted accessibility settings confuse users. Streamlining permission onboarding into an intuitive, guided wizard with automated progression and clarifying custom session settings dramatically reduces abandonment and elevates user discipline.

## What Changes

- **Custom Focus Session Settings:**
  - Add quick preset duration chips (e.g., 15m, 25m, 45m, 60m, 120m) and a direct numeric custom minute entry modal to `RotaryDialTimer` and `HomeScreen`.
  - Allow user-defined durations between 1 minute and 1440 minutes (24 hours).
  - Persist the user's last selected session configuration across app launches.
- **Improved Permission Onboarding UX:**
  - Redesign `OnboardingScreen` into prioritized tiers: **Core Enforcers** (Accessibility, Exact Alarms, Notifications) vs. **Background Resilience** (Battery Optimization, Reboot Survival).
  - Add a primary "Grant Next" action button that automatically dispatches the intent for the highest-priority ungranted permission.
  - Integrate an Android 13+ Restricted Settings banner with an explicit "Unlock in App Info" shortcut when sideloaded installations encounter disabled accessibility.
  - Introduce an animated progress bar, contextual rationale cards explaining why each permission is essential, and instant checkmark animations upon return from system settings.

## Capabilities

### New Capabilities
<!-- None: all features enhance existing capabilities -->

### Modified Capabilities
- `permission-onboarding`: Redesign the onboarding flow to provide prioritized permission grouping, one-tap sequential grant routing, Android 13+ sideload guidance, and reduced setup friction.
- `tactile-rotary-timer`: Extend timer configuration with quick preset selection chips, direct custom minute entry, and persistent duration selection.

## Impact

- **UI Layer:** `OnboardingScreen.kt`, `HomeScreen.kt`, `RotaryDialTimer.kt`.
- **Platform Layer:** `PermissionChecker.kt` (enhanced status grouping and priority routing).
- **Data & Preferences:** `FocusPreferences.kt` (persisting custom duration preference).
- **Dependencies:** Uses existing Jetpack Compose Foundation, Material3, and DataStore components.
