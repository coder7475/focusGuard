# ADR-001: Enforcement Primitive via Accessibility Service and Overlay

## Status
Accepted

## Context
FocusGuard's primary value proposition is to make the Android device unusable for non-essential tasks while a focus session is active. To deliver on this guarantee, the app must:
1. Detect when the user opens or switches to a non-allowlisted application.
2. Intercept and block access with near-zero latency (<100 ms).
3. Prevent the user from bypassing the lock via back gestures, home buttons, or quick app switching.

On Android, there are four technical primitives traditionally considered for app blocking:
1. **Device Owner / Lock Task Mode:** Intended for enterprise kiosk devices via provisioning; not available to general consumer apps distributed on Google Play.
2. **`UsageStatsManager` Polling:** Polling `UsageEvents.Event.ACTIVITY_RESUMED` requires a background polling loop or timer. Events on Android 14+ have noticeable delays (500 ms – 2 s), allowing users to peek or interact with blocked apps before the blocker triggers, while continuously consuming battery.
3. **`SYSTEM_ALERT_WINDOW` Alone:** Displays overlay windows, but lacks foreground app switch detection; furthermore, Android 14/15 restricts launching foreground activities and alert windows from the background without exemptions.
4. **`AccessibilityService` + `TYPE_ACCESSIBILITY_OVERLAY`:** Receives immediate system callbacks (`TYPE_WINDOW_STATE_CHANGED`, `TYPE_WINDOWS_CHANGED`) and can attach a `TYPE_ACCESSIBILITY_OVERLAY` window directly to `WindowManager` that sits above other apps (including Settings) without requiring separate `SYSTEM_ALERT_WINDOW` permission.

## Decision
We adopt **`AccessibilityService` with a `TYPE_ACCESSIBILITY_OVERLAY` window** as the primary enforcement mechanism for FocusGuard MVP.

`UsageStatsManager` will be retained strictly as a secondary fallback detector and audit data source.

## Consequences
### Positive (What becomes easier):
- **Zero-Latency Enforcement:** Window change events are delivered synchronously from Android's window server, allowing the overlay to intercept app launches before user interaction occurs.
- **Superior Window Elevation:** `TYPE_ACCESSIBILITY_OVERLAY` windows have a window layer priority higher than standard application windows, preventing standard touch pass-through.
- **Battery Efficiency:** Eliminates continuous background polling loops; enforcement logic is completely event-driven.
- **Unified Permission Surface:** A single accessibility grant provides both window inspection capability and overlay drawing capability.

### Negative / Trade-Offs (What becomes harder):
- **Play Store Scrutiny:** Google Play enforces strict policies on accessibility APIs. FocusGuard must declare assistive focus enforcement as its core feature in its store listing and provide clear user-facing disclosures.
- **User Grant Friction:** Enabling an Accessibility Service requires navigating through system Settings with multi-step confirmation dialogs. A guided onboarding wizard is strictly mandatory.
- **Service Disable Vulnerability:** Users can manually disable the accessibility service in system Settings to escape a session. The system must monitor accessibility status and trigger alerts if disabled mid-session.
