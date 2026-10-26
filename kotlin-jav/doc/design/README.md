# FocusGuard — Technical Architecture & Design Documentation

This directory contains the system architecture designs, domain specifications, and Architecture Decision Records (ADRs) for FocusGuard.

## Core Documents

- **[System Architecture Document](SYSTEM_ARCHITECTURE.md)**: Complete system design, bounded contexts, C4 models (Context, Container, Component), subsystem specifications, failure modes ("What happens when X fails?"), persistence schemas, and testing strategy.
- **[MVP Plan & Product Requirements](../mvp/MVP_PLAN.md)**: Foundational product requirements, user stories, milestone delivery, and research references.

## Architecture Decision Records (ADRs)

| ADR | Title | Status | Scope |
| :--- | :--- | :--- | :--- |
| **[ADR-001](adrs/ADR-001-accessibility-overlay-enforcement.md)** | Enforcement Primitive via Accessibility Service and Overlay | Accepted | Interception mechanism, zero-latency detection, window hierarchy |
| **[ADR-002](adrs/ADR-002-persistent-epoch-state-reconciliation.md)** | Persistent Wall-Clock Epoch State and Boot Reconciliation | Accepted | Single source of truth, crash/reboot survival, time calculations |
| **[ADR-003](adrs/ADR-003-alarmmanager-over-workmanager-for-session-timing.md)** | Timing Engine via AlarmManager Exact Alarms vs WorkManager | Accepted | Session boundaries, exact wakeup timing, Doze survival |
| **[ADR-004](adrs/ADR-004-composeview-in-accessibility-overlay.md)** | Jetpack Compose Rendering Inside Accessibility Overlay Window | Accepted | UI architecture, synthetic lifecycle management in service |
| **[ADR-005](adrs/ADR-005-system-role-based-allowlist-resolution.md)** | System Role-Based Allowlist Resolution vs QUERY_ALL_PACKAGES | Accepted | Play Store policy compliance, emergency dialer & SMS safety |
