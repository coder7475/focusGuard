# Permission Onboarding Specification

## Purpose
Governs the multi-step permission onboarding wizard, direct system settings intent dispatches, and real-time accessibility tampering detection to ensure all required capabilities are granted and preserved for uninterrupted focus enforcement.

## Requirements

### Requirement: Granular Permission Status Verification
The system SHALL evaluate and verify the grant status of mandatory system permissions without causing unexpected application crashes, and declare battery optimization and overlay permissions in the manifest.

#### Scenario: Check accessibility service status
- **WHEN** `PermissionChecker.isAccessibilityServiceEnabled()` is invoked
- **THEN** it MUST inspect `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` to confirm active service component registration

#### Scenario: Check exact alarm scheduling status
- **WHEN** running on Android 12+ (API 31+)
- **THEN** `PermissionChecker.canScheduleExactAlarms()` MUST query `AlarmManager.canScheduleExactAlarms()`

#### Scenario: Verify battery optimization exemption permission declaration
- **WHEN** `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` is requested
- **THEN** the permission MUST be declared in `AndroidManifest.xml` to prevent security crashes on OEM devices

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

### Requirement: Accessibility Tamper Detection
The system SHALL detect when the user disables the accessibility service during an active focus session and trigger an immediate warning.

#### Scenario: Detect accessibility disabled during active session
- **WHEN** a session is active and `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` changes to exclude FocusGuard
- **THEN** the system MUST dispatch a high-priority warning notification directing the user to re-enable the service
