# Security and Privacy Policy

FocusGuard takes user security, device safety, and data privacy with utmost seriousness. Because FocusGuard operates device-level enforcement features (Accessibility Service and system overlay windows), this policy outlines our commitments, security design principles, and instructions for reporting vulnerabilities.

---

## 1. Privacy Principles & Guarantees

FocusGuard is designed as an offline-first, zero-telemetry application:

- **100% On-Device Processing**: FocusGuard contains zero network dependencies, analytics SDKs, crash trackers, or external APIs. All focus data, schedules, and preferences remain solely on your device.
- **Zero Content Inspection (`canRetrieveWindowContent = false`)**:
  FocusGuard's Accessibility Service configuration explicitly sets `android:canRetrieveWindowContent="false"`. FocusGuard cannot inspect, read, copy, or log texts, passwords, credit cards, messages, or screen contents.
- **Minimal Privilege Package Detection**:
  FocusGuard does **not** declare or use `android.permission.QUERY_ALL_PACKAGES`. It dynamically inspects only foreground window state transitions (`TYPE_WINDOW_STATE_CHANGED`) to cross-reference against Android's default dialer and SMS roles (`TelecomManager` / `Telephony.Sms`).
- **Emergency Safety Guarantee**:
  FocusGuard never interferes with or obstructs emergency calls, default phone dialer operations, or critical system dispatchers.

---

## 2. Supported Versions

Security updates and bug fixes are prioritized for the following versions:

| Version | Supported | Notes |
| :--- | :---: | :--- |
| `1.x.x` (Current Main) | :white_check_mark: | Active support and maintenance |
| `< 1.0.0` (Pre-release) | :x: | Unsupported; please upgrade to latest release |

---

## 3. Reporting a Security Vulnerability

If you discover a security vulnerability, an unintended permission bypass, or an unexpected data handling issue in FocusGuard, please disclose it responsibly.

**Do NOT report security vulnerabilities through public GitHub issues.**

### How to Report:
1. Send an encrypted email or private disclosure message to the maintainer:
   - **Email**: [robiulhossain7475@gmail.com](mailto:robiulhossain7475@gmail.com)
   - **Subject**: `[SECURITY VULNERABILITY] FocusGuard - <Brief Description>`
2. Include the following details in your report:
   - Device model, manufacturer, and Android version tested (e.g. Pixel 8, Android 15; Samsung Galaxy S24, One UI 6).
   - Step-by-step reproduction steps or Proof of Concept (PoC).
   - Expected behavior vs. observed security or safety impact.
   - Any suggested mitigations or patches if available.

### Response Timeline:
- **Initial Response**: Within 48 hours acknowledging receipt of your report.
- **Assessment & Triage**: Within 5 business days with a severity evaluation and remediation plan.
- **Patch & Advisory**: Coordinated release of the fix alongside credit to the reporting researcher.
