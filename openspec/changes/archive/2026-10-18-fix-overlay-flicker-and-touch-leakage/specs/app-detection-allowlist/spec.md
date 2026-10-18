## MODIFIED Requirements

### Requirement: Accessibility Service Window Monitoring
The system SHALL register an Accessibility Service to capture foreground window state changes with sub-100ms response time and zero package content inspection, ignoring hierarchy jitter, structural events without packages, and internal overlay window state changes.

#### Scenario: Intercept window state transition
- **WHEN** the user switches between applications causing a `TYPE_WINDOW_STATE_CHANGED` accessibility event
- **THEN** `FocusGuardAccessibilityService` MUST intercept the event and extract the target application's package name within 100ms

#### Scenario: Suppress view hierarchy inspection
- **WHEN** an accessibility event is processed
- **THEN** `FocusGuardAccessibilityService` MUST operate with `canRetrieveWindowContent = false` and evaluate only window-level metadata

#### Scenario: Ignore non-window-state and null package events
- **WHEN** an accessibility event is of type other than `TYPE_WINDOW_STATE_CHANGED` or contains a null or blank package name
- **THEN** `FocusGuardAccessibilityService` MUST ignore the event without altering overlay visibility state

#### Scenario: Prevent overlay self-dismissal
- **WHEN** an accessibility event is received with `packageName == com.coder7475.focusguard`
- **THEN** `FocusGuardAccessibilityService` MUST only consider it allowed if `className` matches `MainActivity`, ignoring overlay ComposeView window events

#### Scenario: Suppress redundant window state evaluations
- **WHEN** a received package matches the previous target package and the overlay is already displayed
- **THEN** `FocusGuardAccessibilityService` MUST suppress re-evaluation to prevent oscillation
