# App Detection & Allowlist Specification

## Purpose

Enforces application access control during active focus sessions by detecting foreground window transitions via Accessibility Service and permitting or blocking applications according to dynamic and static allowlist policies.

## Requirements

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

### Requirement: Dynamic Allowlist Resolution
The system SHALL dynamically resolve and permit system-critical applications including the default phone dialer, emergency packages, default SMS, and the default home launcher.

#### Scenario: Resolve and allow default phone dialer
- **WHEN** a focus session is active and the foreground package matches the system default dialer package
- **THEN** the enforcer MUST allow the application without triggering the lock overlay

#### Scenario: Allow emergency numbers and dialer actions
- **WHEN** the user initiates an emergency call or navigates to the emergency call interface
- **THEN** the enforcer MUST immediately yield and dismiss the lock overlay

#### Scenario: Block non-allowlisted third-party application
- **WHEN** a focus session is active and a non-allowlisted package enters the foreground
- **THEN** the enforcer MUST trigger the full-screen lock overlay within 100ms
