# Google Play Store Policy Declarations & Disclosures

## 1. Accessibility API Prominent Disclosure

### In-App Prominent Disclosure Dialog & Onboarding Copy
> **Title:** Accessibility Service Disclosure for FocusGuard  
> **Body:** FocusGuard uses Android's AccessibilityService API exclusively for the purpose of enforcing your active focus sessions. When enabled, FocusGuard listens for foreground application state changes (`TYPE_WINDOW_STATE_CHANGED`) to detect when a non-allowlisted application is launched during a focus lock.
> 
> **Privacy Guarantees:**
> - FocusGuard operates with `canRetrieveWindowContent = "false"`.
> - FocusGuard does **NOT** read, inspect, or log text on your screen.
> - FocusGuard does **NOT** collect keystrokes, personal communications, passwords, or payment information.
> - FocusGuard does **NOT** share any accessibility or usage data with external servers; all evaluation occurs 100% locally on your device.
> - Essential emergency and phone dialer access is never obstructed.

### Play Console Form Response
- **Question:** Does your app use the AccessibilityService API?  
  **Answer:** Yes.
- **Purpose:** Focus lock discipline & Digital Wellbeing app blocking.
- **Target Audience:** Consumer productivity.

---

## 2. Foreground Service `specialUse` Policy Declaration (Android 14+)

### Manifest Property Subtype
`android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE`:
```
"Focus lock countdown ticker and state preservation"
```

### Policy Justification Statement for Play Console Reviewers
> **Justification Statement:**  
> FocusGuard provides an un-bypassable commitment lock during focus sessions. The foreground service is essential to maintain a continuous, user-visible countdown ticker notification and ensure the session state is preserved against aggressive OEM battery management termination.
> 
> Standard background tasks (such as WorkManager) cannot guarantee per-second notification countdown updates, and Doze mode termination would destroy the user's active session state. The foreground service is only active while a focus session is in progress and immediately terminates upon completion.

---

## 3. Exact Alarm (`SCHEDULE_EXACT_ALARM`) Declaration

### Policy Category
- **Category:** Timer & Clock / Strict Focus Enforcement.

### Justification Statement
> **Justification Statement:**  
> FocusGuard requires `SCHEDULE_EXACT_ALARM` to wake the device from deep Doze at the exact epoch timestamp when a focus session concludes. Inexact alarms introduce delays between 5 to 15 minutes, leaving the user locked out of their phone after their scheduled focus period has ended. Exact alarms guarantee that session completion, overlay dismissal, and completion alerts occur with sub-second accuracy.

---

## 4. Package Visibility Compliance (`QUERY_ALL_PACKAGES` Zero Exemption)

- FocusGuard does **NOT** declare `android.permission.QUERY_ALL_PACKAGES`.
- Application access control is evaluated dynamically using `TelecomManager.getDefaultDialerPackage()` and `Telephony.Sms.getDefaultSmsPackage()`, paired with static fallback OEM packages for system emergency phone apps. This design eliminates any requirement for broad package inventory visibility.
