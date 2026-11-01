# Spec Delta

## MODIFIED Requirements

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

#### Scenario: Launch application details settings
- **WHEN** user requests navigation to application details settings
- **THEN** the system MUST dispatch an intent targeting application details settings scoped to the application package URI

## ADDED Requirements

### Requirement: Restricted Settings Guidance on Android 13+
The system SHALL present actionable instructions and navigation shortcuts for resolving Android restricted settings when running on Android 13 or higher (API 33+) while accessibility is not yet enabled.

#### Scenario: Display restricted settings guidance for ungranted accessibility
- **WHEN** running on Android 13+ and accessibility permission is not granted
- **THEN** the system MUST display contextual guidance explaining how to allow restricted settings from the system App Info menu

#### Scenario: Trigger application details navigation from restricted settings prompt
- **WHEN** the user invokes the restricted settings shortcut
- **THEN** the system MUST navigate to the application details screen to allow unblocking restricted settings
