## MODIFIED Requirements

### Requirement: Guided 7-Step Permission Onboarding Flow
The system SHALL guide users through a prioritized, tiered Jetpack Compose setup wizard with sequential smart routing and contextual guidance before allowing activation of focus sessions.

#### Scenario: Prioritized permission grouping
- **WHEN** the onboarding screen is displayed
- **THEN** permissions MUST be visually partitioned into "Core Enforcers" (Accessibility, Notifications, Exact Alarms) and "Background Durability" (Battery Optimization, Reboot Survival)

#### Scenario: Sequential smart grant action
- **WHEN** user taps the primary "Grant Next" action button
- **THEN** the system MUST automatically resolve the highest-priority ungranted mandatory permission and launch its respective system settings intent

#### Scenario: Android 13+ sideload guidance callout
- **WHEN** running on Android 13+ (API 33+) and the Accessibility Service permission is ungranted
- **THEN** the onboarding screen MUST display an explicit guidance banner explaining how to unlock Restricted Settings with a one-tap shortcut to App Info

#### Scenario: Complete onboarding wizard
- **WHEN** the user grants all required permissions in the wizard
- **THEN** the primary button MUST transition to "Enter FocusGuard" in Safety Orange, persist `KEY_ONBOARDING_COMPLETED = true`, and navigate to the Home screen

#### Scenario: Launch specific system settings intent
- **WHEN** the user taps the action button for an individual permission step card
- **THEN** the system MUST dispatch the direct intent targeting that permission's system configuration page
