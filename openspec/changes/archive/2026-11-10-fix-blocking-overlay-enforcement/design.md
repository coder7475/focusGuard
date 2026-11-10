# Design: Blocking Overlay Enforcement Fix

## Context

See `proposal.md` for background and motivation.
In `focusGuard`, the Expo / React Native JavaScript layer writes active session records to `/files/SQLite/focusguard.db` using `expo-sqlite`. Concurrently, the native Android layer runs `FocusAccessibilityService` and `FocusForegroundService` inside the custom Expo module `modules/focus-enforcer/android`. Currently, native SQLite helpers inspect `/databases/focusguard.db`, which is an empty database file, causing native session checks to always return `0L`. In addition, no in-memory session bridge exists between `FocusEnforcerModule` and `FocusAccessibilityService`, and `LockOverlayWindowManager` lacks robust attachment guards and proper window layout flags.

## Goals / Non-Goals

**Goals:**
- Provide zero-latency session state propagation from `FocusEnforcerModule` to `FocusAccessibilityService` in memory.
- Align SQLite persistence queries in `NativeDbHelper` and `FocusDatabaseHelper` to read directly from `File(context.filesDir, "SQLite/focusguard.db")`.
- Harden `LockOverlayWindowManager` with proper `WindowManager.LayoutParams` flags (`TYPE_ACCESSIBILITY_OVERLAY`, `FLAG_LAYOUT_IN_SCREEN`, `FLAG_LAYOUT_NO_LIMITS`, `FLAG_SHOW_WHEN_LOCKED`, `FLAG_KEEP_SCREEN_ON`) and guard against duplicate view additions.
- Filter out self-package events, null targets, and redundant re-evaluations in `FocusAccessibilityService`.

**Non-Goals:**
- Modifying React Native TypeScript repositories or altering the database schema.
- Changing onboarding permission workflows or battery optimization flows.
- Replacing the Android Accessibility Service architecture with alternative polling engines.

## Decisions

### 1. In-Memory Session Synchronization via Companion State
- **Decision**: Introduce `@Volatile var activeSessionEndsAt: Long = 0L` on `FocusAccessibilityService.Companion` and update it immediately in `FocusEnforcerModule.startFocusLock` and `stopFocusLock`.
- **Rationale**: Reading SQLite on every single window state event causes file I/O latency and race conditions during fast window transitions. In-memory check provides instant (<1ms) decision-making.
- **Alternatives Considered**: BroadcastReceivers (adds overhead, prone to background latency) or ContentProviders (excessive complexity for single timestamp).

### 2. File-Path Targeted Native SQLite Helper
- **Decision**: Change `NativeDbHelper` (and `FocusDatabaseHelper`) to resolve SQLite databases explicitly from `File(context.filesDir, "SQLite/focusguard.db")`. Open using `SQLiteDatabase.openDatabase(..., null, SQLiteDatabase.OPEN_READONLY)`.
- **Rationale**: Android's default `SQLiteOpenHelper` strictly targets `context.getDatabasePath(name)` which maps to `/databases/`. Expo SQLite stores files in `files/SQLite/`. Explicit path binding reconciles both worlds without needing custom migration code in JS.
- **Alternatives Considered**: Changing `expo-sqlite` storage path via custom config (brittle across Expo SDK upgrades).

### 3. Safe WindowManager Lifecycle & Window Flags
- **Decision**: In `LockOverlayWindowManager`, check `isVisible` and `overlayView != null` before calling `windowManager.addView(...)`. Wrap layout params with `FLAG_LAYOUT_IN_SCREEN`, `FLAG_LAYOUT_NO_LIMITS`, `FLAG_SHOW_WHEN_LOCKED`, `FLAG_KEEP_SCREEN_ON`, and use `TYPE_ACCESSIBILITY_OVERLAY`. Remove views cleanly inside `hideOverlay()`.
- **Rationale**: Prevents `IllegalStateException: View already added` and ensures modal display on Android 12 through 15 without touch bleed-through.

## Risks / Trade-offs

- **[Risk] Native service killed while app in background**:
  → *Mitigation*: Fallback to `NativeDbHelper.getActiveSessionEndsAt()` directly if in-memory `activeSessionEndsAt` is 0L, reading the persisted timestamp from `files/SQLite/focusguard.db`.
- **[Risk] Database locked during React Native write transaction**:
  → *Mitigation*: Open the database in `OPEN_READONLY` mode and catch `SQLiteDatabaseLockedException`, falling back safely to the in-memory state.
