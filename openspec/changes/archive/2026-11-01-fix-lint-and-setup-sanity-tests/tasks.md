# Tasks

## 1. ESLint & React Compiler Remediation

- [x] 1.1 Fix cascading render warnings (`react-hooks/set-state-in-effect`) in `src/app/(tabs)/explore.tsx` and `src/hooks/use-color-scheme.web.ts` and verify with `expo lint`
- [x] 1.2 Fix asynchronous state hydration in `src/app/(tabs)/index.tsx` and `src/app/(tabs)/schedules.tsx` and remove unused imports
- [x] 1.3 Fix Reanimated 4 shared value mutability handling in `src/components/RotaryDial.tsx` and verify clean gesture tracking
- [x] 1.4 Clean up unused variables and catch bindings in `src/domain/SessionEngine.ts` and `src/app/_layout.tsx` and verify `pnpm lint` reports 0 errors and 0 warnings

## 2. Test Suite & Sanity Check Setup

- [x] 2.1 Update `focusGuard/package.json` scripts with `"test": "jest"` and `"test:sanity": "pnpm tsc --noEmit && pnpm lint && pnpm jest"` and verify script registration
- [x] 2.2 Implement `src/__tests__/sanity.test.ts` to assert core domain models, repository instantiation, and configuration exports
- [x] 2.3 Execute `pnpm test:sanity` in `focusGuard/` and verify TypeScript compilation, ESLint, and Jest unit tests pass with exit code 0
