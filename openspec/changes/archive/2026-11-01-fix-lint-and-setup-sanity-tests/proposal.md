# Proposal

## Why

The React 19 compiler and React Hooks ESLint rules report 33 lint issues (22 errors, 11 warnings) across the codebase, primarily due to synchronous `setState` in `useEffect`, Reanimated shared value mutations inside gesture hooks, and unused variable declarations. Establishing zero-warning clean linting and an automated test sanity check suite ensures continuous code quality, prevents regressions, and validates application readiness for production builds.

## What Changes

- **Resolve React Hooks & React Compiler Errors**:
  - Refactor asynchronous state hydration in `src/app/(tabs)/explore.tsx`, `src/app/(tabs)/index.tsx`, `src/app/(tabs)/schedules.tsx`, and `src/hooks/use-color-scheme.web.ts` to avoid synchronous cascading render warnings.
  - Refactor `RotaryDial.tsx` shared value mutations and Reanimated 4 gesture handlers to comply with React 19 immutability guidelines.
  - Remove all unused imports and unhandled catch variables in `SessionEngine.ts`, `schedules.tsx`, and `_layout.tsx`.
- **Configure Comprehensive Test Sanity Suite**:
  - Add a unified `test:sanity` npm script in `package.json` that runs TypeScript validation (`tsc --noEmit`), ESLint verification (`expo lint`), and Jest unit tests (`jest`).
  - Add sanity unit tests validating end-to-end component rendering and domain engine contracts.

## Capabilities

### New Capabilities
<!-- None: Pure tooling, lint remediation, and test infrastructure setup. skip_specs: true is set in .openspec.yaml. -->

### Modified Capabilities
<!-- None: No behavioral specification changes. -->

## Impact

- **Affected Code**: `src/app/(tabs)/explore.tsx`, `src/app/(tabs)/index.tsx`, `src/app/(tabs)/schedules.tsx`, `src/app/_layout.tsx`, `src/components/RotaryDial.tsx`, `src/domain/SessionEngine.ts`, `src/hooks/use-color-scheme.web.ts`.
- **Scripts**: `package.json` (`test`, `test:sanity`, `lint`).
- **Dependencies**: No external runtime dependencies added.
