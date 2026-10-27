This is an Expo/React Native mobile application. Prioritize mobile-first patterns, performance, and cross-platform compatibility.

## Expo has changed — do not trust your training data

Expo ships breaking changes every SDK release. APIs you remember are likely renamed, moved, or removed. Before writing any code that touches an Expo, EAS, or React Native API:

1. Read the major version of the `expo` package in `package.json`.
2. Fetch the matching versioned docs: `https://docs.expo.dev/versions/v<major>.0.0/`
3. For anything else, fetch https://docs.expo.dev/llms.txt — an index of all Expo docs with corrections to common LLM misconceptions. Follow its links to the specific page you need; never answer from memory.

## Commands

Use `bunx` instead of `npx` if the project uses bun (`bun.lock` present).

```bash
npx expo install <package>  # ALWAYS use instead of npm/yarn/pnpm/bun add — resolves SDK-compatible versions
npx expo start              # start the dev server
npx expo lint               # lint
npx tsc --noEmit            # typecheck
npx expo-doctor             # diagnose dependency and config issues
npx expo install --fix      # fix incompatible package versions
```

Run lint and typecheck before declaring any task done.

## Navigation & Routing

- Use **Expo Router** for all navigation. Routes live in `src/app/` — every file there is a screen, `_layout.tsx` files define navigators. Keep non-route code (components, hooks, utils) outside `src/app/`.
- Import `Link`, `router`, and `useLocalSearchParams` from `expo-router`.
- Docs: https://docs.expo.dev/router/introduction.md

## Building with EAS

Use EAS to build, sign, and submit the app in the cloud (`eas build`, `eas submit`) and to ship over-the-air updates (`eas update`) — no local Xcode or Android Studio required. Run EAS CLI as `bunx eas-cli <command>` in Bun projects, or `npx eas-cli@latest <command>` otherwise; substitute that for bare `eas` in docs examples.
Docs: https://docs.expo.dev/eas/index.md

## Rules

- If `ios/` and `android/` directories do not exist, they are generated (Continuous Native Generation). Never create or edit them by hand — configure native behavior in `app.json` and config plugins.
- Expo Go only includes its bundled native modules. After adding a library with native code, the app needs a development build: `npx expo run:ios|android` locally, or `eas build --profile development`.
- Prefer recommended Expo modules over third-party libraries, and check your available skills before adding dependencies. Docs: https://docs.expo.dev/versions/latest/index.md

## Performance & Lag Reduction Checklist

Follow the guidelines from [Best Practices for Reducing Lag in Expo Apps](https://expo.dev/blog/best-practices-for-reducing-lag-in-expo-apps):

### 1. Architecture & Thread Management
- [ ] **Respect Thread Separation**: Keep heavy computation off the JavaScript thread. UI/Main thread handles layout and drawing; blocking JS freezes state updates and responsiveness.
- [ ] **Use the Platform (Native over JS)**: Use `react-native-gesture-handler` instead of `PanResponder` so gestures run on the native UI thread. Use native storage/caching (e.g. `expo-sqlite`) over heavy pure-JS abstractions.
- [ ] **Optimized Native Media & Assets**: Use `expo-image` for performant native decoding, disk/memory caching, and progressive loading instead of standard React Native `Image`.

### 2. Code Quality & Modern Compilation
- [ ] **Step 1: Strict TypeScript**: Catch structural and type issues at compile time (`npx tsc --noEmit`) to avoid runtime overhead.
- [ ] **Step 2: Static JavaScript Features**: Prefer static, analyzable code structures that Hermes and Metro can optimize into bytecode.
- [ ] **Step 3: Enforce Linting**: Run `npx expo lint` to catch anti-patterns, memory leaks, and redundant renders.
- [ ] **Step 4: Enable React Compiler**: Let the React Compiler handle automatic memoization (`useMemo`, `useCallback`) to eliminate re-renders without manual boilerplate.
- [ ] **Step 5: Leverage Modern React 19 Patterns**: Use React 19 primitives (such as the `use` API and Suspense) for async data flow and non-blocking state updates.
- [ ] **Step 6: Multi-thread Heavy Tasks with Worklets**: Offload CPU-intensive operations (heavy calculations, transformations) from the main JS thread using background threads and Reanimated worklets (`react-native-worklets`).

### 3. Profiling & Diagnostics
- [ ] **Connect Chrome DevTools**: Press `J` in the Expo CLI to connect Chrome DevTools directly to the Hermes engine.
- [ ] **Highlight Renders**: In Profiler settings, enable *"Highlight updates when components render"* to spot persistent re-render loops and wasted render passes.
- [ ] **Profile First**: Always measure and profile JS thread bottlenecks before introducing premature optimizations.

