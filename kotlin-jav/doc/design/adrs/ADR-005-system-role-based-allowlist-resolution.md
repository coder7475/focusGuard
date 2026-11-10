# ADR-005: System Role-Based Allowlist Resolution vs QUERY_ALL_PACKAGES

## Status
Accepted

## Context
Goal G4 mandates: "Keep calls, SMS/messaging, and the emergency dialer usable at all times."

Android devices feature hundreds of different phone dialers and SMS apps depending on manufacturer and user configuration (e.g. Google Phone, Samsung Phone, Xiaomi Contacts and Dialer, Textra, Signal, WhatsApp).

To identify which apps should bypass the lock, two strategies exist:
1. **Broad Package Scanning (`QUERY_ALL_PACKAGES`):** Query every installed application on the device, present a selection list to the user, or heuristically inspect package names and intent filters.
   - *Problem:* Google Play strictly forbids `QUERY_ALL_PACKAGES` unless an app's primary purpose is an antivirus or device file manager. Misuse results in immediate Play rejection.
2. **System Role Resolution (`TelecomManager` & `Telephony.Sms`):** Programmatically query the Android OS for the current default dialer and default SMS application, combined with an immutable fallback catalog of known OEM phone packages and emergency intents.

## Decision
We adopt **System Role-Based Allowlist Resolution with deterministic OEM fallbacks**, eliminating the dependency on `QUERY_ALL_PACKAGES` for the MVP release.

1. **Dynamic Resolution:**
   - Default Dialer: Query `telecomManager.defaultDialerPackage`.
   - Default SMS: Query `Telephony.Sms.getDefaultSmsPackage(context)`.
2. **Immutable System Fallbacks:**
   - Emergency UI: `com.android.phone.EmergencyDialer`, `com.android.phone`, and actions `ACTION_EMERGENCY_ASSIST`, `ACTION_DIAL`.
   - Major OEM In-Call Packages: `com.google.android.dialer`, `com.android.dialer`, `com.samsung.android.dialer`, `com.android.incallui`, `com.google.android.incallui`.
   - Major OEM Messaging Packages: `com.google.android.apps.messaging`, `com.android.mms`, `com.samsung.android.messaging`.
3. **Strict Non-Customizability in MVP:**
   - In MVP, users cannot add arbitrary apps (e.g., Slack or Instagram) to the allowlist. Only verified communication tools pass through.

## Consequences
### Positive (What becomes easier):
- **Play Store Safety:** Completely avoids requesting `QUERY_ALL_PACKAGES`, eliminating a primary cause of Play Console rejection.
- **Fail-Safe Emergency Operation:** Guarantees that emergency calling and incoming phone calls always succeed across Samsung, Pixel, Xiaomi, Motorola, and stock Android devices.
- **Simplified Scope:** Removes the need to build a complex app-picker UI, package icon loading, and persistent user allowlist storage in MVP.

### Negative / Trade-Offs (What becomes harder):
- **User Customization Limitations:** Users who rely on third-party messaging apps (e.g., WhatsApp or Telegram) as their primary communication tool cannot access them during MVP focus sessions unless SMS/calls are used.
  - *Post-MVP Evolution:* In Phase 2, a user-selectable allowlist can be introduced using `Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)` resolution, which does not require `QUERY_ALL_PACKAGES`.
