# Contributing to FocusGuard

Thank you for your interest in contributing to FocusGuard! We welcome contributions that help improve focus discipline, app stability, accessibility compliance, and developer experience.

---

## Code of Conduct

FocusGuard aims to provide an inclusive, welcoming, and harassment-free environment. Please treat all contributors and maintainers with empathy, respect, and professionalism.

---

## Getting Started

### 1. Prerequisites

Before setting up the project, make sure you have:

- **Android Studio**: Ladybug (2024.2.1+) or newer
- **JDK 17**: Eclipse Adoptium Temurin 17 (automatically managed by the Gradle JVM Toolchain)
- **Android SDK Platform**: API 35 / API 34 (`compileSdk 37`, `minSdk 24`, `targetSdk 37`)
- **Android Device / Emulator**: Running Android 7.0 (API 24) to Android 15 (API 35)

### 2. Fork & Clone

```bash
git clone https://github.com/your-username/focusGuard.git
cd focusGuard
```

### 3. Build & Verify

Verify your local environment by running the test suite:

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run Android Lint audit
./gradlew lintDebug
```

---

## Development Workflow

### Branching Strategy

- Always create feature or bugfix branches from the `main` branch:
  ```bash
  git checkout -b feat/your-feature-name
  # or
  git checkout -b fix/issue-description
  ```

### Conventional Commits

We follow [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

Common types:
- `feat`: A new user-facing feature or domain capability
- `fix`: A bug fix
- `docs`: Documentation updates or additions
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `test`: Adding or correcting tests
- `chore`: Tooling, build config, or dependency version updates

Example:
```bash
git commit -m "feat(overlay): add tactile vibration feedback to emergency dialer tap"
```

---

## Architectural Principles

When writing or reviewing code in FocusGuard, uphold the following architectural principles:

1. **Clean Architecture Separation**:
   - `domain`: Pure Kotlin business logic (`SessionEngine`, `ScheduleEngine`, models, policies). Zero Android framework imports.
   - `data`: Database entities, DAOs, repositories, and preferences (`FocusPreferences`, Room).
   - `platform`: Android system interactions (`AlarmCoordinator`, `PermissionChecker`, `SystemRoleResolver`, receivers).
   - `service`: Foreground countdown service and Accessibility overlay lifecycle.
   - `ui`: 100% Jetpack Compose screens, components, and Material 3 design tokens.

2. **Privacy by Design**:
   - Never request `QUERY_ALL_PACKAGES`. Dynamic allowlisting must resolve through standard system roles (`TelecomManager`, `Telephony.Sms`).
   - The Accessibility Service must maintain `canRetrieveWindowContent = false`. Do not read or intercept on-screen user text.
   - Zero network transmission: FocusGuard is 100% offline. Do not add remote analytics, tracking, or network clients.

3. **Resilience & Battery Discipline**:
   - Time calculations must rely on wall-clock epoch timestamps (`System.currentTimeMillis()`), never on relative elapsed timers that drift during deep Doze.
   - Foreground service notifications must gracefully terminate when sessions conclude.

---

## Submitting Pull Requests

1. **Keep Pull Requests Focused**: Target a single feature, bug fix, or documentation enhancement. Avoid bundling unrelated refactors.
2. **Include Automated Tests**: Any change in domain logic, scheduling algorithms, or state management must include corresponding unit tests in `app/src/test/`.
3. **Verify Code Quality**:
   ```bash
   ./gradlew testDebugUnitTest
   ./gradlew lintDebug
   ```
4. **Update Documentation**: If your change modifies system behavior, permissions, or user workflows, update the relevant documentation in `doc/` and `README.md`.
5. **Open PR with Context**: Provide a clear summary of what changed, why the change was made, and how to verify it manually or via automated tests.

---

## Questions and Support

For questions, architectural discussions, or bug reports, feel free to open a [GitHub Issue](https://github.com/coder7475/focusGuard/issues) or consult the [Developer Guide](doc/developer/DEVELOPMENT_TESTING_DEPLOYMENT_GUIDE.md).
