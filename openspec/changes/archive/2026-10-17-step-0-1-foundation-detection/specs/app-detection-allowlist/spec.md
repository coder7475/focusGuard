## ADDED Requirements

### Requirement: Accessibility Service Window Monitoring
The system SHALL register an Accessibility Service to capture foreground window state changes with sub-100ms response time and zero package content inspection.

#### Scenario: Intercept window state transition
- **WHEN** the user switches between applications causing a `TYPE_WINDOW_STATE_CHANGED` accessibility event
- **THEN** `FocusGuardAccessibilityService` MUST intercept the event and extract the target application's package name within 100ms

#### Scenario: Suppress view hierarchy inspection
- **WHEN** an accessibility event is processed
- **THEN** `FocusGuardAccessibilityService` MUST operate with `canRetrieveWindowContent = false` and evaluate only window-level metadata

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
