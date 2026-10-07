# ADR-004: Jetpack Compose Rendering Inside Accessibility Overlay Window

## Status
Accepted

## Context
FocusGuard's full-screen lock screen must display dynamic UI: a running countdown timer (mm:ss), motivational messaging, active session metadata, and emergency call shortcuts.

Traditionally, system overlays inflated legacy Android XML views via `LayoutInflater`. However:
1. The rest of FocusGuard is built natively with modern Jetpack Compose and Material 3 design tokens.
2. Building the overlay in XML would require maintaining duplicate styling, string resources, color definitions, and manually imperatively updating `TextView` elements on each tick.
3. Jetpack Compose provides declarative state bindings (`collectAsState()`), fluid animations, and unified accessibility semantics.

However, Jetpack Compose requires a `LifecycleOwner`, a `ViewModelStoreOwner`, and a `SavedStateRegistryOwner` attached to the view tree. By default, an `AccessibilityService` is a `ContextWrapper` / `Service`, not a lifecycle-aware component like `ComponentActivity`.

## Decision
We decide to **render the lock overlay using Jetpack Compose (`ComposeView`) inside the `AccessibilityService` `WindowManager`**.

We provide the required Compose lifecycle environment by:
1. Creating a custom `OverlayLifecycleOwner` implementing `LifecycleOwner`, `ViewModelStoreOwner`, and `SavedStateRegistryOwner`.
2. Initializing `ViewTreeLifecycleOwner.set(composeView, lifecycleOwner)`, `ViewTreeViewModelStoreOwner.set(composeView, lifecycleOwner)`, and `ViewTreeSavedStateRegistryOwner.set(composeView, lifecycleOwner)` before attaching the view to `WindowManager`.
3. Toggling visibility using window flags or `View.GONE` / `View.VISIBLE` rather than destroying and reconstructing the Compose composition on every window switch.

## Consequences
### Positive (What becomes easier):
- **Unified Design System:** The lock overlay uses the exact same `FocusGuardTheme`, typography, and color palette as the main app.
- **Declarative Flow Integration:** Countdown and state updates stream directly into Compose composables via Kotlin Coroutine `StateFlow`.
- **Maintainability:** Zero legacy Android XML layout files to maintain.

### Negative / Trade-Offs (What becomes harder):
- **Boilerplate Plumbing:** Requires careful implementation of synthetic lifecycle owners attached to the service. Failure to properly trigger lifecycle events (`ON_CREATE`, `ON_START`, `ON_RESUME`, `ON_DESTROY`) causes Compose crashes.
- **Initial Composition Overhead:** The first layout pass of a Compose hierarchy inside a service window takes slightly longer (~20–40 ms) than an empty Android View. We mitigate this by pre-inflating and keeping the view attached in a hidden state while a session is active.
