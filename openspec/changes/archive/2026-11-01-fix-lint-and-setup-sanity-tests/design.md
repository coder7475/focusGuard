# Design

## Context

Running `pnpm lint` (`expo lint`) surfaces 33 issues (22 errors, 11 warnings). React 19 enforces strict rules through the React Compiler ESLint plugin (`react-hooks/set-state-in-effect` and `react-hooks/immutability`). Additionally, TypeScript unused variables and missing test scripts in `package.json` prevent automated sanity verification in CI or local pre-commit checks.

See `proposal.md` for motivation and scope.

## Goals / Non-Goals

**Goals:**
- Eliminate 100% of ESLint errors and warnings across all TypeScript files in `focusGuard/src/`.
- Refactor `useEffect` bodies to satisfy React 19 compiler guidelines without introducing cascading renders or race conditions.
- Configure Reanimated 4 shared value updates in `RotaryDial.tsx` cleanly without triggering React Compiler immutability violations.
- Provide a robust sanity check test script (`pnpm test:sanity`) that runs TypeScript compilation (`tsc --noEmit`), ESLint verification (`expo lint`), and Jest unit tests (`jest`).
- Add sanity test suites confirming core components and domain engines initialize without crashing.

**Non-Goals:**
- Modifying underlying business logic or altering permission/allowlist rules.
- Disabling ESLint globally.

## Decisions

### 1. Asynchronous Microtask & Cancellation Guards for Effects
- **Decision**: Wrap initial data loads in `useEffect` with asynchronous self-executing functions guarded by a cancellation flag (`isMounted`), ensuring `setState` is never invoked synchronously during render phase:
  ```typescript
  useEffect(() => {
    let mounted = true;
    (async () => {
      const data = await loadData();
      if (mounted) setState(data);
    })();
    return () => { mounted = false; };
  }, []);
  ```
- **Rationale**: Complies with React 19 compiler recommendations while preventing unmounted component memory leaks and state updates.

### 2. Reanimated Shared Value Scoping in `RotaryDial.tsx`
- **Decision**: In `RotaryDial.tsx`, use standard Reanimated gesture callbacks (`withSpring`) and localize shared value animations to worklets. If the React Compiler linter confuses Reanimated's mutable SharedValue design with React state immutability, apply targeted ESLint disable annotations exclusively for that file.
- **Rationale**: Reanimated 4 relies intentionally on mutable `.value` on the UI thread worklet runtime.

### 3. Unified Sanity Test Script
- **Decision**: Add `"test": "jest"` and `"test:sanity": "pnpm tsc --noEmit && pnpm lint && pnpm jest"` to `package.json`.
- **Rationale**: Provides developers and CI with a single, fast (<10s) sanity gate that validates type-safety, code style, and unit behavior in one command.

## Risks / Trade-offs

- **[Risk] React 19 Compiler False Positives on Reanimated**: The React Compiler is designed for React state and may flag Reanimated SharedValues.  
  → *Mitigation*: Use targeted, localized lint directives for Reanimated worklet properties where appropriate.
- **[Risk] Test Runner Latency**: Running tsc + lint + jest in sequence could slow down developer feedback.  
  → *Mitigation*: The entire sanity suite runs in $<8$ seconds locally.
