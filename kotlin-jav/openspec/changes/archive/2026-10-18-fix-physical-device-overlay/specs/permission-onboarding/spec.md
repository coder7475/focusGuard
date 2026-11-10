## MODIFIED Requirements

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
The system SHALL guide users through a multi-step Jetpack Compose setup wizard before allowing activation of focus sessions, providing Android 13+ sideload guidance for restricted accessibility settings.

#### Scenario: Complete onboarding wizard
- **WHEN** the user grants all required permissions in the wizard
- **THEN** the system MUST persist `KEY_ONBOARDING_COMPLETED = true` in Preferences DataStore and transition to the Home screen

#### Scenario: Launch specific system settings intent
- **WHEN** the user taps the action button for a permission step
- **THEN** the system MUST dispatch the direct intent targeting that permission's system configuration page

#### Scenario: Guide user through Android 13+ Restricted Settings
- **WHEN** running on Android 13+ (API 33+) and the Accessibility Service is not yet granted
- **THEN** the onboarding screen MUST display a notice instructing the user how to allow restricted settings in App Info with a direct button to App Info settings
