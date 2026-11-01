## Context

FocusGuard is a phone-lock discipline application targeting native Android (minSdk 24, targetSdk 37). Baseline persistence (Room entities, DAOs, and DataStore preferences) is established. The remaining system must implement active window detection, an un-bypassable lock overlay, wall-clock alarm reconciliation, foreground service countdown notifications, a 7-step permission onboarding wizard, and comprehensive testing.

All architecture decisions adhere to the project's Architectural Decision Records (ADRs):
- **ADR-001**: Accessibility Service Window Monitoring & `TYPE_ACCESSIBILITY_OVERLAY`
- **ADR-002**: Persistent Wall-Clock Epoch Timestamps in Room SQLite
- **ADR-003**: `AlarmManager.setExactAndAllowWhileIdle()` over WorkManager for session completion
- **ADR-004**: Synthetic `OverlayLifecycleOwner` hosting Jetpack Compose in an Accessibility Service
- **ADR-005**: Dynamic `TelecomManager` & `Telephony.Sms` role resolution without `QUERY_ALL_PACKAGES`

## Goals / Non-Goals

**Goals:**
- Sub-100ms detection and overlay response upon switching to non-allowlisted apps.
- Zero obstruction of system dialer, incoming/outgoing calls, emergency dialer, and SMS.
- Complete state preservation across device reboots and background process termination using exact alarms and epoch reconciliation.
- Jetpack Compose-driven lock overlay with live countdown and emergency action.
- Guided 7-step permission onboarding flow with live verification and tamper detection.
- Full compliance with Google Play Store accessibility and package visibility policies.

**Non-Goals:**
- Root, Device-Owner, or MDM enforcement (consumer Play Store focus).
- View hierarchy inspection or keylogging (`canRetrieveWindowContent = false`).
- Broad package queries via `QUERY_ALL_PACKAGES`.
- Relative thread sleep or in-memory countdown timing.

## Decisions

### 1. Window Event Interception via Accessibility Service
- **Choice**: Use `FocusGuardAccessibilityService` with `typeWindowStateChanged|typeWindowsChanged` and `canRetrieveWindowContent="false"`.
- **Alternatives Considered**: Polling `UsageStatsManager` (rejected: 500ms–2s latency, high battery drain, delayed sync on Android 14+); MDM DevicePolicyManager (rejected: requires factory reset/enterprise provisioning).
- **Rationale**: Immediate callback directly from `WindowManagerService` when foreground activity transitions occur, with negligible battery overhead and full privacy compliance.

### 2. Lock Overlay via `TYPE_ACCESSIBILITY_OVERLAY` & Synthetic Lifecycle
- **Choice**: Attach a full-screen window via `WindowManager` inside `FocusGuardAccessibilityService` using `TYPE_ACCESSIBILITY_OVERLAY`, `FLAG_LAYOUT_IN_SCREEN`, `FLAG_NOT_TOUCH_MODAL`, and `FLAG_SHOW_WHEN_LOCKED`. Host Jetpack Compose via custom `OverlayLifecycleOwner` implementing `LifecycleOwner`, `ViewModelStoreOwner`, and `SavedStateRegistryOwner`.
- **Alternatives Considered**: Standard `TYPE_APPLICATION_OVERLAY` via `SYSTEM_ALERT_WINDOW` (rejected: can be drawn over or delayed, requires extra runtime permission); full-screen Activity intent launch (rejected: easily bypassed with Home/Recents gestures and slow to launch).
- **Rationale**: Accessibility overlay renders above third-party applications, swallows hardware Back and Home gestures, and requires no separate `SYSTEM_ALERT_WINDOW` permission. Reusing the view with `View.VISIBLE`/`View.GONE` avoids window churn during rapid switching.

### 3. Wall-Clock Epoch Math & Exact Alarms
- **Choice**: Persist `endsAtEpochMs = System.currentTimeMillis() + durationMs` in Room SQLite. Schedule exact alarms with `AlarmManager.setExactAndAllowWhileIdle()`.
- **Alternatives Considered**: Kotlin Coroutines `delay()`, Java Timer, or Foreground Service ticker alone (rejected: desyncs or gets terminated during Doze or OEM task killing); WorkManager (rejected: minimum 15-minute intervals for periodic work, cannot guarantee exact second execution).
- **Rationale**: Survives process death, Doze mode, and device reboots. Upon boot, `BootCompletedReceiver` queries Room and either re-arms the alarm or finalizes the expired session.

### 4. Dynamic Allowlist Resolution Without `QUERY_ALL_PACKAGES`
- **Choice**: Query `TelecomManager.getDefaultDialerPackage()` and `Telephony.Sms.getDefaultSmsPackage(context)` at runtime, combined with static fallback lists for standard OEM phone and emergency packages.
- **Alternatives Considered**: Requesting `QUERY_ALL_PACKAGES` permission (rejected: immediate Play Store rejection for non-launcher/search apps); hardcoding a single dialer package (rejected: breaks across Samsung, Xiaomi, Google Pixel, etc.).
- **Rationale**: Dynamically detects the user's active dialer and SMS app with zero Play Store policy risk.

### 5. Onboarding Wizard & Tamper Detection
- **Choice**: Sequential 7-step wizard tracking granular permission states, paired with a `ContentObserver` on `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` to trigger high-priority alert notifications if accessibility is toggled off during an active session.
- **Rationale**: Educates the user on required permissions, reduces drop-off, and deters impulsive disabling mid-session.

## Risks / Trade-offs

- **[Risk] OEM aggressive battery managers terminate Foreground Service**  
  → *Mitigation*: Provide OEM-specific guidance cards (Samsung Never Sleeping Apps, Xiaomi AutoStart & No Restrictions, OnePlus Battery Optimization Off). Persistent epoch in Room and exact alarms guarantee session state is restored upon next wake or app launch.
- **[Risk] User enters dialer and navigates away into other apps**  
  → *Mitigation*: The Accessibility Service continuously monitors foreground package changes. As soon as the user transitions away from dialer/SMS back to an un-allowlisted package, the lock overlay immediately re-attaches.
- **[Risk] ComposeView in Service memory leak or lifecycle crash**  
  → *Mitigation*: `OverlayLifecycleOwner` properly transitions through `CREATED`, `STARTED`, and `RESUMED` on window attach, and dispatches `DESTROYED` when service unbinds. The Compose view is created once and toggled via visibility rather than repeated attachment cycles.

## Migration & Rollout Plan

1. Step 1: Implement `SystemRoleResolver`, `AllowlistPolicy`, and `FocusGuardAccessibilityService`.
2. Step 2: Implement `OverlayLifecycleOwner`, `LockOverlayWindowManager`, and `LockOverlayScreen`.
3. Step 3: Implement `SessionEngine`, `ScheduleEngine`, `AlarmCoordinator`, `SessionAlarmReceiver`, `FocusGuardService`, and `BootCompletedReceiver`.
4. Step 4: Implement `PermissionChecker`, 7-step Onboarding Wizard, tamper detection, and OEM guidance.
5. Step 5: Execute JVM and Android unit/instrumented tests and verify release build.
