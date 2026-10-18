## MODIFIED Requirements

### Requirement: Dynamic System Role Resolution
The system SHALL dynamically resolve default phone dialer, SMS, and default launcher packages at runtime without requiring `QUERY_ALL_PACKAGES` permission.

#### Scenario: Resolve system dialer role
- **WHEN** `SystemRoleResolver.getDefaultDialerPackage()` is invoked
- **THEN** the system MUST retrieve the package name returned by `TelecomManager.getDefaultDialerPackage()` or fallback to known OEM dialer packages

#### Scenario: Resolve system SMS role
- **WHEN** `SystemRoleResolver.getDefaultSmsPackage()` is invoked
- **THEN** the system MUST retrieve the package name returned by `Telephony.Sms.getDefaultSmsPackage()` or fallback to known OEM messaging packages

#### Scenario: Resolve default home launcher role
- **WHEN** `SystemRoleResolver.getDefaultLauncherPackage()` is invoked
- **THEN** the system MUST query `PackageManager` with an `Intent.ACTION_MAIN` and `Intent.CATEGORY_HOME` intent and return the active launcher package name

### Requirement: Allowlist Policy Enforcement
The system SHALL evaluate application package names against emergency, system role, default launcher, and own-package allowlists to determine access eligibility.

#### Scenario: Allow phone and dialer interactions
- **WHEN** the foreground package matches the resolved default dialer or OEM emergency dialer
- **THEN** `AllowlistPolicy.evaluateAccess(packageName)` MUST return `AccessDecision.ALLOWED`

#### Scenario: Allow SMS and messaging interactions
- **WHEN** the foreground package matches the resolved default SMS application
- **THEN** `AllowlistPolicy.evaluateAccess(packageName)` MUST return `AccessDecision.ALLOWED`

#### Scenario: Allow FocusGuard own application
- **WHEN** the foreground package matches `com.coder7475.focusguard`
- **THEN** `AllowlistPolicy.evaluateAccess(packageName)` MUST return `AccessDecision.ALLOWED`

#### Scenario: Evaluate default home launcher
- **WHEN** the foreground package matches the resolved default home launcher
- **THEN** `AllowlistPolicy.evaluateAccess(packageName)` MUST return `AccessDecision.ALLOWED`

#### Scenario: Block third-party applications during active session
- **WHEN** the foreground package belongs to a non-allowlisted third-party application
- **THEN** `AllowlistPolicy.evaluateAccess(packageName)` MUST return `AccessDecision.BLOCKED`
