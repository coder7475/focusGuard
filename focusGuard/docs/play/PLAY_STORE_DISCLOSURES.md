# Google Play Store Policy Declarations & Disclosures — SleepGuard

## 1. Accessibility API Prominent Disclosure

### In-App Prominent Disclosure Dialog & Onboarding Copy
> **Title:** Accessibility Service Disclosure for SleepGuard (`com.coder7575.SleepGuard`)  
> **Body:** SleepGuard uses Android's AccessibilityService API exclusively for the purpose of enforcing active focus and bedtime locks. When enabled, SleepGuard listens for foreground application state changes (`TYPE_WINDOW_STATE_CHANGED`) to detect when a non-allowlisted application is launched during a focus lock.
> 
> **Privacy Guarantees:**
> - SleepGuard operates with `canRetrieveWindowContent = "false"`.
> - SleepGuard does **NOT** read, inspect, or log text or images on your screen.
> - SleepGuard does **NOT** collect keystrokes, personal communications, passwords, or payment information.
> - SleepGuard does **NOT** transmit any accessibility or usage data to external servers; all evaluation occurs 100% offline on your device.
> - Essential emergency services and phone dialers are dynamically allowlisted and never obstructed.

### Play Console Form Response
- **Question:** Does your app use the AccessibilityService API?  
  **Answer:** Yes.
- **Purpose:** Focus lock discipline & Bedtime Digital Wellbeing enforcement.
- **Target Audience:** Consumer productivity and wellness.

---

## 2. Foreground Service `specialUse` Policy Declaration (Android 14+)

### Manifest Property Subtype
`android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE`:
```
"Focus lock countdown ticker and state preservation"
```

### Policy Justification Statement for Play Console Reviewers
> **Justification Statement:**  
> SleepGuard provides an uncompromising commitment lock during focus sessions. The foreground service is essential to maintain a continuous, user-visible countdown ticker notification and ensure the session state is preserved against aggressive OEM battery management termination.
> 
> Standard background tasks (such as WorkManager) cannot guarantee per-second notification countdown updates, and Doze mode termination would destroy the user's active session state. The foreground service is only active while a focus session is in progress and immediately terminates upon completion.

---

## 3. Exact Alarm (`SCHEDULE_EXACT_ALARM`) Declaration

### Policy Category
- **Category:** Timer & Clock / Strict Focus Enforcement.

### Justification Statement
> **Justification Statement:**  
> SleepGuard requires `SCHEDULE_EXACT_ALARM` to wake the device from deep Doze at the exact epoch timestamp when a focus session concludes. Inexact alarms introduce delays between 5 to 15 minutes, leaving the user locked out of their phone after their scheduled focus period has ended. Exact alarms guarantee that session completion, overlay dismissal, and completion alerts occur with sub-second accuracy.

---

## 4. Package Visibility Compliance (`QUERY_ALL_PACKAGES` Zero Exemption)

- SleepGuard does **NOT** declare `android.permission.QUERY_ALL_PACKAGES`.
- Application access control is evaluated dynamically using `TelecomManager.getDefaultDialerPackage()` and `Telephony.Sms.getDefaultSmsPackage()`, paired with static fallback OEM packages for system emergency phone apps. This design eliminates any requirement for broad package inventory visibility.
