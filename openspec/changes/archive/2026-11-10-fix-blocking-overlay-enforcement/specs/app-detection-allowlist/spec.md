# Spec Delta: App Detection & Allowlist

## MODIFIED Requirements

### Requirement: Accessibility Service Window Monitoring
The system SHALL register an Accessibility Service to capture foreground window state changes with sub-100ms response time and zero package content inspection.

#### Scenario: Intercept window state transition
- **WHEN** the user switches between applications causing a window state changed accessibility event
- **THEN** the accessibility service MUST intercept the event and extract the target application package name within 100ms

#### Scenario: Suppress view hierarchy inspection
- **WHEN** an accessibility event is processed
- **THEN** the accessibility service MUST operate with content retrieval disabled and evaluate only window-level metadata

#### Scenario: Ignore non-window-state and null package events
- **WHEN** an accessibility event is not a window state change or contains a blank package name
- **THEN** the accessibility service MUST ignore the event without altering overlay visibility state

#### Scenario: In-memory session synchronization and database fallback
- **WHEN** an active focus session is initiated or terminated
- **THEN** the accessibility service MUST evaluate active enforcement state via real-time in-memory epoch cache and fallback to the verified SQLite database directory
