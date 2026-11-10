# App Detection & Allowlist Specification

## Purpose

Enforces application access control during active focus sessions by detecting foreground window transitions via Accessibility Service and permitting or blocking applications according to dynamic and static allowlist policies.

## Requirements

### Requirement: Accessibility Service Window Monitoring
The system SHALL register an Accessibility Service to capture foreground window state changes with sub-100ms response time, zero package content inspection, and robust self-window event suppression.

#### Scenario: Intercept window state transition
- **WHEN** the user switches between applications causing a window state changed accessibility event
- **THEN** the accessibility service MUST intercept the event and extract the target application package name within 100ms

#### Scenario: Suppress view hierarchy inspection
- **WHEN** an accessibility event is processed
- **THEN** the accessibility service MUST operate with content retrieval disabled and evaluate only window-level metadata

#### Scenario: Ignore non-window-state and null package events
- **WHEN** an accessibility event is not a window state change or contains a blank package name
- **THEN** the accessibility service MUST ignore the event without altering overlay visibility state

#### Scenario: Ignore self-package overlay window events
- **WHEN** an accessibility event originates from the enforcer host package and the class name is not MainActivity
- **THEN** the accessibility service MUST immediately ignore the event without invoking overlay dismissal

#### Scenario: Retain visible overlay during active session
- **WHEN** the lock overlay is already visible, the active session timestamp has not elapsed, and an incoming event matches the currently active target or host overlay window
- **THEN** the accessibility service MUST maintain overlay presentation and suppress redundant dismiss or show calls

#### Scenario: Suppress redundant window state evaluations
- **WHEN** a received package matches the previous target package and the overlay is already displayed
- **THEN** the accessibility service MUST suppress re-evaluation to prevent oscillation

#### Scenario: In-memory session synchronization and database fallback
- **WHEN** an active focus session is initiated or terminated
- **THEN** the accessibility service MUST evaluate active enforcement state via real-time in-memory epoch cache and fallback to the verified SQLite database directory

### Requirement: Dynamic Allowlist Resolution
The system SHALL dynamically resolve and permit system-critical applications including the default phone dialer, emergency packages, default SMS, default home launcher, and core platform UI surfaces without requiring `QUERY_ALL_PACKAGES` permission.

#### Scenario: Resolve and allow default phone dialer
- **WHEN** a focus session is active and the foreground package matches the system default dialer package
- **THEN** the enforcer MUST allow the application without triggering the lock overlay

#### Scenario: Resolve and allow default SMS messaging
- **WHEN** a focus session is active and the foreground package matches the system default SMS application
- **THEN** the enforcer MUST allow the application without triggering the lock overlay

#### Scenario: Resolve and allow default home launcher
- **WHEN** a focus session is active and the foreground package matches the resolved default home launcher
- **THEN** the enforcer MUST treat the launcher as allowlisted and prevent lock overlay cycling

#### Scenario: Allow emergency numbers and dialer actions
- **WHEN** the user initiates an emergency call or navigates to the emergency call interface
- **THEN** the enforcer MUST immediately yield and dismiss the lock overlay

#### Scenario: Allow core system UI surfaces
- **WHEN** a focus session is active and the foreground package is "com.android.systemui" or "android" during notification shade or system bar interaction
- **THEN** the enforcer MUST permit the system surface without dismantling the lock overlay

#### Scenario: Allow Transsion and OEM home launchers
- **WHEN** a focus session is active and the foreground package is an OEM launcher including "com.transsion.XOSLauncher" or "com.transsion.hilauncher"
- **THEN** the enforcer MUST treat the launcher as allowlisted and prevent lock overlay cycling

#### Scenario: Package visibility queries for dynamic resolution
- **WHEN** the system queries the package manager for default home launcher or dialer intent handlers on Android 11+ (API 30+)
- **THEN** the application manifest queries declaration MUST allow dynamic resolution of the actual system launcher or dialer package

#### Scenario: Block non-allowlisted third-party application
- **WHEN** a focus session is active and a non-allowlisted package enters the foreground
- **THEN** the enforcer MUST trigger the full-screen lock overlay within 100ms

### Requirement: Allowlist Policy Access Evaluation
The system SHALL evaluate application package names against emergency, system role, default launcher, and own-package allowlists to determine access eligibility.

#### Scenario: Allow phone and dialer interactions
- **WHEN** the foreground package matches the resolved default dialer or OEM emergency dialer
- **THEN** the system MUST evaluate access as ALLOWED

#### Scenario: Allow SMS and messaging interactions
- **WHEN** the foreground package matches the resolved default SMS application
- **THEN** the system MUST evaluate access as ALLOWED

#### Scenario: Allow FocusGuard own application
- **WHEN** the foreground package matches the FocusGuard host package
- **THEN** the system MUST evaluate access as ALLOWED

#### Scenario: Block third-party applications during active session
- **WHEN** the foreground package belongs to a non-allowlisted third-party application during an active session
- **THEN** the system MUST evaluate access as BLOCKED
