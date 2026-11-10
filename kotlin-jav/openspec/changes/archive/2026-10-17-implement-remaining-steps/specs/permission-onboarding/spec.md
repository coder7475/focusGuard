## ADDED Requirements

### Requirement: Granular Permission Status Verification
The system SHALL evaluate and verify the grant status of mandatory system permissions without causing unexpected application crashes.

#### Scenario: Check accessibility service status
- **WHEN** `PermissionChecker.isAccessibilityServiceEnabled()` is invoked
- **THEN** it MUST inspect `AccessibilityManager.getEnabledAccessibilityServiceList()` to confirm active binding

#### Scenario: Check exact alarm scheduling status
- **WHEN** running on Android 12+ (API 31+)
- **THEN** `PermissionChecker.canScheduleExactAlarms()` MUST query `AlarmManager.canScheduleExactAlarms()`

### Requirement: Guided 7-Step Permission Onboarding Flow
The system SHALL guide users through a multi-step Jetpack Compose setup wizard before allowing activation of focus sessions.

#### Scenario: Complete onboarding wizard
- **WHEN** the user grants all required permissions in the wizard
- **THEN** the system MUST persist `KEY_ONBOARDING_COMPLETED = true` in Preferences DataStore and transition to the Home screen

#### Scenario: Launch specific system settings intent
- **WHEN** the user taps the action button for a permission step
- **THEN** the system MUST dispatch the direct intent targeting that permission's system configuration page

### Requirement: Accessibility Tamper Detection
The system SHALL detect when the user disables the accessibility service during an active focus session and trigger an immediate warning.

#### Scenario: Detect accessibility disabled during active session
- **WHEN** a session is active and `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` changes to exclude FocusGuard
- **THEN** the system MUST dispatch a high-priority warning notification directing the user to re-enable the service
