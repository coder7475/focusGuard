# Permission & Onboarding Specification

## Purpose

Governs the multi-step permission onboarding wizard, direct system settings intent dispatches, and real-time accessibility tampering detection to ensure all required capabilities are granted and preserved for uninterrupted focus enforcement.

## Requirements

### Requirement: Granular Permission Status Verification
The system SHALL evaluate and verify the grant status of mandatory system permissions without causing unexpected application crashes, and declare battery optimization and overlay permissions in the manifest.

#### Scenario: Check accessibility service status
- **WHEN** permission evaluation is invoked
- **THEN** the system MUST inspect secure settings (`Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES`) to confirm active accessibility service component registration

#### Scenario: Check exact alarm scheduling status
- **WHEN** running on Android 12 or higher (API 31+)
- **THEN** the system MUST query system alarm manager status to verify whether exact alarms are permitted

#### Scenario: Verify battery optimization exemption declaration
- **WHEN** battery optimization exemption is requested
- **THEN** the permission MUST be declared in the application manifest to prevent runtime security exceptions

#### Scenario: Check display overlay permission
- **WHEN** overlay permission status is queried on Android 6.0+
- **THEN** the system MUST query window manager overlay permissions to confirm whether overlay drawing is allowed

### Requirement: Direct System Settings Intent Dispatch
The system SHALL dispatch explicit system intents to direct the user directly to the relevant settings configuration screens.

#### Scenario: Launch accessibility settings
- **WHEN** user requests navigation to accessibility settings
- **THEN** the system MUST dispatch an action intent targeting accessibility settings with new task flags

#### Scenario: Launch overlay management settings
- **WHEN** user requests overlay permission grant
- **THEN** the system MUST launch the manage overlay intent scoped to the application package URI

#### Scenario: Request battery optimization ignore
- **WHEN** user requests battery optimization exemption
- **THEN** the system MUST dispatch the request ignore battery optimizations intent targeting the application package URI

#### Scenario: Launch alarm and reminder settings
- **WHEN** user requests exact alarm permissions on Android 12+
- **THEN** the system MUST launch the request schedule exact alarm intent with application package data URI

### Requirement: Guided 7-Step Permission Onboarding Flow
The system SHALL guide users through a prioritized, tiered setup wizard with sequential smart routing and contextual guidance before allowing activation of focus sessions.

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

### Requirement: Onboarding Completion State Persistence
The system SHALL persist onboarding completion state locally to prevent re-displaying completed onboarding flows across application restarts.

#### Scenario: Persist onboarding completion flag
- **WHEN** the user completes all required permission steps and finishes onboarding
- **THEN** the system MUST store the completion flag in persistent storage

#### Scenario: Route based on onboarding status
- **WHEN** the application starts up
- **THEN** the router MUST navigate to the main dashboard if onboarding is completed, or to the onboarding wizard if incomplete

### Requirement: Accessibility Tamper Detection
The system SHALL detect when the user disables the accessibility service during an active focus session and trigger an immediate warning.

#### Scenario: Detect accessibility disabled during active session
- **WHEN** a session is active and `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` changes to exclude FocusGuard
- **THEN** the system MUST dispatch a high-priority warning notification directing the user to re-enable the service
