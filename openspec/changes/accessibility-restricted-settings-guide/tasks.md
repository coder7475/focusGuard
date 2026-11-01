# Implementation Tasks

- [x] 1. Native Intent Dispatch for Application Details (`APP_DETAILS`)
  - [x] 1.1 Update `FocusEnforcerModule.kt` to handle `"APP_DETAILS"` targeting `ACTION_APPLICATION_DETAILS_SETTINGS` with package data URI.
  - [x] 1.2 Update TypeScript module interface `modules/focus-enforcer/src/index.ts` to include `'APP_DETAILS'` in `PermissionType`.
- [x] 2. UI Guidance for Android 13+ Restricted Settings
  - [x] 2.1 Update `src/app/onboarding.tsx` to detect Android 13+ (`Platform.OS === 'android' && Platform.Version >= 33`).
  - [x] 2.2 Add contextual warning and step-by-step instructions when accessibility permission is ungranted.
  - [x] 2.3 Add direct action button to trigger `openSettingsForPermission('APP_DETAILS')`.
- [x] 3. Documentation & Verification
  - [x] 3.1 Update `TESTING.md` with restricted setting verification steps and ADB bypass commands.
  - [x] 3.2 Add test assertions to `src/__tests__/sanity.test.ts` for `'APP_DETAILS'`.
  - [x] 3.3 Validate with `pnpm test:sanity` (TypeScript, ESLint, Jest) and `npx expo-doctor`.
