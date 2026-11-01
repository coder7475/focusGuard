# Proposal

## Why

When preview and sideloaded builds (e.g. EAS preview APKs or QA distribution APKs) are installed on Android 13+ (API level 33+), the OS enforces security restrictions that disable the Accessibility Service toggle ("Restricted setting: For your security, this setting is currently unavailable"). Testers and users are blocked from activating the focus enforcer service without clear guidance on how to allow restricted settings in the system App Info menu.

## What Changes

- Add native intent support in `FocusEnforcerModule` for `ACTION_APPLICATION_DETAILS_SETTINGS` (`APP_DETAILS` permission name) so users can be routed directly to the app info page where restricted settings can be unblocked.
- Enhance the onboarding UI (`src/app/onboarding.tsx`) with an Android 13+ detection banner and an explicit step-by-step guide explaining how to unlock grayed-out accessibility toggles.
- Add a direct shortcut button in the onboarding wizard to open App Info when accessibility permissions remain ungranted on Android 13+.
- Document manual device steps and developer ADB unlock commands in project testing documentation.

## Capabilities

### New Capabilities
*(None)*

### Modified Capabilities
- `permission-onboarding`: Add requirements for dispatching application details settings intents and presenting contextual guidance for Android 13+ restricted settings.

## Impact

- Native Android module (`FocusEnforcerModule.kt`): Updated `openSettingsForPermission` to support `APP_DETAILS`.
- TypeScript definition (`modules/focus-enforcer/src/index.ts`): Updated types for permission setting targets.
- Frontend onboarding (`src/app/onboarding.tsx`): Updated UI with conditional Android 13+ guidance and helper button.
- Documentation: Testing documentation updated with bypass commands and instructions.
