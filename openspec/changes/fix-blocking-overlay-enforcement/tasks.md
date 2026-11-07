# Tasks

## 1. Native Database Path & Fallback Persistence

- [x] 1.1 Update `NativeDbHelper.kt` to target `File(context.filesDir, "SQLite/focusguard.db")` using read-only database connections, and verify it handles non-existent or locked database files safely.
- [x] 1.2 Update `FocusDatabaseHelper.kt` to target `File(context.filesDir, "SQLite/focusguard.db")` for background queries, and verify path alignment.

## 2. In-Memory Session Synchronization

- [x] 2.1 Add `@Volatile var activeSessionEndsAt: Long = 0L` to `FocusAccessibilityService.Companion` and integrate it into `onAccessibilityEvent` session expiration checks.
- [x] 2.2 Update `FocusEnforcerModule.kt` to broadcast `endsAtEpochMs` directly to `FocusAccessibilityService.activeSessionEndsAt` in `startFocusLock` and reset to `0L` in `stopFocusLock`.

## 3. Overlay Window Lifecycle & Layout Hardening

- [x] 3.1 Update `LockOverlayWindowManager.kt` layout flags to include `FLAG_LAYOUT_IN_SCREEN`, `FLAG_LAYOUT_NO_LIMITS`, `FLAG_SHOW_WHEN_LOCKED`, `FLAG_NOT_TOUCH_MODAL`, and `FLAG_KEEP_SCREEN_ON` with `TYPE_ACCESSIBILITY_OVERLAY`.
- [x] 3.2 Add safe attachment checks (`isVisible` and `overlayView != null`) before invoking `windowManager.addView(...)` and `windowManager.removeView(...)` to eliminate `IllegalStateException` crashes.

## 4. Accessibility Event Filtering & Debouncing

- [x] 4.1 In `FocusAccessibilityService.kt`, add filters for null/blank packages, own-package events, and redundant target package re-evaluations to eliminate overlay flickering.

## 5. Verification & Testing

- [x] 5.1 Run project lint and TypeScript tests (`pnpm test`) to ensure no regressions in TypeScript or domain models.
- [ ] 5.2 Build Android debug package or verify module compilation with `./gradlew assembleDebug` in `focusGuard/android`.
