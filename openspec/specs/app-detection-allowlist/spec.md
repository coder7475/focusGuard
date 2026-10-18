# App Detection & Allowlist Specification

## Purpose
Enforces application access control during active focus sessions by detecting foreground window transitions via Accessibility Service and permitting or blocking applications according to dynamic and static allowlist policies.

## Requirements

### Requirement: Accessibility Service Window Monitoring
The system SHALL register an Accessibility Service to capture foreground window state changes with sub-100ms response time and zero package content inspection.

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

### Requirement: Dynamic System Role Resolution
The system SHALL dynamically resolve default phone dialer and SMS packages at runtime without requiring `QUERY_ALL_PACKAGES` permission.

#### Scenario: Resolve system dialer role
- **WHEN** `SystemRoleResolver.getDefaultDialerPackage()` is invoked
- **THEN** the system MUST retrieve the package name returned by `TelecomManager.getDefaultDialerPackage()` or fallback to known OEM dialer packages

#### Scenario: Resolve system SMS role
- **WHEN** `SystemRoleResolver.getDefaultSmsPackage()` is invoked
- **THEN** the system MUST retrieve the package name returned by `Telephony.Sms.getDefaultSmsPackage()` or fallback to known OEM messaging packages

### Requirement: Allowlist Policy Enforcement
The system SHALL evaluate application package names against emergency, system role, and own-package allowlists to determine access eligibility.

#### Scenario: Allow phone and dialer interactions
- **WHEN** the foreground package matches the resolved default dialer or OEM emergency dialer
- **THEN** `AllowlistPolicy.evaluateAccess(packageName)` MUST return `AccessDecision.ALLOWED`

#### Scenario: Allow SMS and messaging interactions
- **WHEN** the foreground package matches the resolved default SMS application
- **THEN** `AllowlistPolicy.evaluateAccess(packageName)` MUST return `AccessDecision.ALLOWED`

#### Scenario: Allow FocusGuard own application
- **WHEN** the foreground package matches `com.coder7475.focusguard`
- **THEN** `AllowlistPolicy.evaluateAccess(packageName)` MUST return `AccessDecision.ALLOWED`

#### Scenario: Block third-party applications during active session
- **WHEN** the foreground package belongs to a third-party application (e.g., social media or games)
- **THEN** `AllowlistPolicy.evaluateAccess(packageName)` MUST return `AccessDecision.BLOCKED`
