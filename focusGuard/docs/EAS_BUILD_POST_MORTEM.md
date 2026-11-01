# Post-Mortem & Summary: EAS Build Failures & Resolution

**Project:** focusGuard (`com.coder7575.SleepGuard`)  
**Platform:** Android (Expo SDK 58, React Native 0.88.1, EAS CLI)  
**Date:** November 1, 2026  
**Status:** Resolved & Verified  

---

## 1. Executive Summary

During development and preview build generation via EAS (Expo Application Services), all Android builds failed during native Gradle compilation. 

Through log retrieval, decompression of Brotli-encoded EAS build streams, and source code tracing across Expo SDK 58 autolinking internals, we diagnosed two consecutive failure stages:
1. **Initial failure (`7e20a389...`)**: AAR metadata check failure caused by `compileSdkVersion` set to 35 while `expo-modules-core` required 37.
2. **Subsequent failures (`717787ab...` and `6f0f84b1...`)**: Kotlin compilation failures caused by an obsolete `ModuleProvider` implementation in the local `focus-enforcer` native module.

All issues were systematically resolved by migrating `focus-enforcer` to Expo SDK 58's native module autolinking standards, removing legacy package classes, cleaning up transitive dependency imports, and verifying the entire stack with automated tests and `expo-doctor`.

---

## 2. What Was Failing

### Build Failure Timeline & Matrix

| Build ID | Profile | Git Commit | Failing Gradle Task | Error Category |
| :--- | :--- | :--- | :--- | :--- |
| `7e20a389-4d9c-4a63-b3aa-b39d0b60e8e8` | `preview` | `6ff2c7e` | `:focus-enforcer:checkReleaseAarMetadata` | AAR compileSdk version mismatch |
| `717787ab-5adc-4f47-aa01-238d41ec1acd` | `development` | `6ff2c7e` | `:focus-enforcer:compileDebugKotlin` | Kotlin unresolved reference |
| `6f0f84b1-ef46-4319-95c2-3a21dd8a37db` | `preview` | `eb7fbb3` | `:focus-enforcer:compileReleaseKotlin` | Kotlin unresolved reference |

---

### Failure 1: AAR Metadata Check (`7e20a389...`)

In the initial preview build, Gradle failed during dependency metadata verification:

```text
> Task :focus-enforcer:checkReleaseAarMetadata FAILED
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':focus-enforcer:checkReleaseAarMetadata'.
> A failure occurred while executing com.android.build.gradle.internal.tasks.CheckAarMetadataWorkAction
   > 6 issues were found when checking AAR metadata:
     1. Dependency ':expo-modules-core' requires libraries and applications that
        depend on it to compile against version 37 or later of the Android APIs.
```

**Why it happened:**
`modules/focus-enforcer/android/build.gradle` had hardcoded `compileSdkVersion 35`. In Expo SDK 58, `expo-modules-core` targets and requires Android SDK 37 (Android 16).

---

### Failure 2 & 3: Kotlin Compilation Errors (`717787ab...` and `6f0f84b1...`)

Once `compileSdk` was updated to 37 in commit `eb7fbb3`, both development and preview builds progressed past metadata checks, but crashed during Kotlin compilation:

```text
> Task :focus-enforcer:compileReleaseKotlin FAILED (and :focus-enforcer:compileDebugKotlin FAILED)
e: file:///home/expo/workingdir/build/focusGuard/modules/focus-enforcer/android/src/main/java/com/coder7575/sleepguard/enforcer/FocusEnforcerPackage.kt:6:36 Unresolved reference 'ModuleProvider'.
e: file:///home/expo/workingdir/build/focusGuard/modules/focus-enforcer/android/src/main/java/com/coder7575/sleepguard/enforcer/FocusEnforcerPackage.kt:8:39 Unresolved reference 'ModuleProvider'.
e: file:///home/expo/workingdir/build/focusGuard/modules/focus-enforcer/android/src/main/java/com/coder7575/sleepguard/enforcer/FocusEnforcerPackage.kt:9:5 'getModules' overrides nothing.

FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':focus-enforcer:compileReleaseKotlin'.
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction
   > Compilation error. See log for more details
```

**Why it happened:**
`FocusEnforcerPackage.kt` attempted to import and implement `expo.modules.kotlin.modules.ModuleProvider`:
```kotlin
package com.coder7575.sleepguard.enforcer

import android.content.Context
import expo.modules.core.interfaces.Package
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleProvider // <-- Does not exist in SDK 58

class FocusEnforcerPackage : Package, ModuleProvider {
    override fun getModules(context: Context): List<Class<out Module>> {
        return listOf(FocusEnforcerModule::class.java)
    }
}
```
In Expo SDK 58 (Expo Modules API), `ModuleProvider` was deprecated and eliminated. Modules no longer need a companion `Package` or `ModuleProvider` class.

---

## 3. How It Was Debugged

### Step 1: Querying EAS Build History Non-Interactively
Using the EAS CLI with JSON output mode:
```bash
npx --yes eas-cli build:list --limit 10 --json
```
This returned structured JSON metadata for the last 10 builds, identifying build IDs, git commits, build profiles, and signed Google Cloud Storage URLs for the raw build logs.

### Step 2: Decompressing Brotli Build Logs
Direct inspection of the downloaded raw log bytes revealed binary data with HTTP response headers:
```http
content-type: text/plain;charset=utf-8
content-encoding: br
```
Google Cloud Storage stored the logs with Brotli compression (`.br`). Using Node.js's built-in `zlib.brotliDecompressSync`, we decompressed the log stream into memory and parsed the individual JSON log lines from EAS workers to isolate the exact Gradle task failure points and compiler stack traces.

### Step 3: Tracing Expo Autolinking Mechanics
We analyzed the autolinking system located in `node_modules/expo-modules-autolinking/build/platforms/android/android.js`:
- The autolinker scans for packages importing `expo.modules.core.interfaces.Package`.
- In `modules/focus-enforcer/expo-module.config.json`, the configuration was:
  ```json
  {
    "name": "focus-enforcer",
    "platforms": ["android"],
    "android": {
      "package": "com.coder7575.sleepguard.enforcer.FocusEnforcerPackage"
    }
  }
  ```
  Because `"package"` was declared and `"modules"` was absent, Expo treated the module as an old unimodule-style package.
- In contrast, modern Expo modules declare their module classes directly under `"modules"`:
  ```json
  {
    "name": "focus-enforcer",
    "platforms": ["android"],
    "android": {
      "modules": ["com.coder7575.sleepguard.enforcer.FocusEnforcerModule"]
    }
  }
  ```
  When `"modules"` is specified, Expo's Gradle plugin generates `ExpoModulesPackageList.kt` which directly instantiates and registers `FocusEnforcerModule`, completely bypassing the need for `FocusEnforcerPackage.kt`.

### Step 4: Full Project Audit with `expo-doctor`
Running `npx expo-doctor` revealed secondary dependency issues:
- `expo-modules-core` should not be a direct dependency in root `package.json` (sub-exported by `expo`).
- `@expo/config-plugins` should not be a direct dependency in root `package.json` (sub-exported by `expo/config-plugins`).

---

## 4. How It Was Resolved

### 1. Updated `expo-module.config.json`
Modified [modules/focus-enforcer/expo-module.config.json](file:///home/fahad/AndroidStudioProjects/focusGuard/focusGuard/modules/focus-enforcer/expo-module.config.json) to declare `modules` instead of `package`:
```diff
 {
   "name": "focus-enforcer",
   "platforms": ["android"],
   "android": {
-    "package": "com.coder7575.sleepguard.enforcer.FocusEnforcerPackage"
+    "modules": [
+      "com.coder7575.sleepguard.enforcer.FocusEnforcerModule"
+    ]
   }
 }
```

### 2. Removed Obsolete `FocusEnforcerPackage.kt`
Deleted `modules/focus-enforcer/android/src/main/java/com/coder7575/sleepguard/enforcer/FocusEnforcerPackage.kt`. `FocusEnforcerModule.kt` is now the sole native entry point.

### 3. Updated TypeScript Native Module Import
In [modules/focus-enforcer/src/index.ts](file:///home/fahad/AndroidStudioProjects/focusGuard/focusGuard/modules/focus-enforcer/src/index.ts):
```diff
-import { requireNativeModule } from 'expo-modules-core';
+import { requireNativeModule } from 'expo';
```

### 4. Standardized Config Plugin Import
In [plugins/withFocusEnforcer.js](file:///home/fahad/AndroidStudioProjects/focusGuard/focusGuard/plugins/withFocusEnforcer.js):
```diff
-const { withAndroidManifest, withDangerousMod, withStringsXml } = require('@expo/config-plugins');
+const { withAndroidManifest, withDangerousMod, withStringsXml } = require('expo/config-plugins');
```

### 5. Removed Redundant Dependencies from `package.json`
Removed `@expo/config-plugins` and `expo-modules-core` from direct `package.json` dependencies, running `pnpm install` to update the lockfile.

### 6. Configured Jest Mappings for Expo
Added [src/data/\_\_mocks\_\_/expo.ts](file:///home/fahad/AndroidStudioProjects/focusGuard/focusGuard/src/data/__mocks__/expo.ts) and mapped `'^expo$'` in [jest.config.js](file:///home/fahad/AndroidStudioProjects/focusGuard/focusGuard/jest.config.js) to maintain green unit test runs without ESM resolution conflicts in Node environment.

---

## 5. Verification Results

| Verification Check | Command | Result |
| :--- | :--- | :--- |
| **Expo Autolinking** | `npx expo-modules-autolinking resolve -p android` | **PASS** — `FocusEnforcerModule` registered under `modules`, `packages: []` |
| **Prebuild Config** | `npx expo config --type prebuild` | **PASS** — All plugins evaluate cleanly; `focus-enforcer` in `autolinkedModules` |
| **Typecheck** | `pnpm tsc --noEmit` | **PASS** — 0 errors |
| **Linter** | `expo lint` | **PASS** — Clean |
| **Unit Tests** | `pnpm jest` | **PASS** — 4 suites passed, 22/22 tests passed |
| **Sanity Suite** | `pnpm run test:sanity` | **PASS** — End-to-end local sanity pass |
| **Expo Doctor** | `npx expo-doctor` | **PASS** — 20/20 checks passed without warnings |

---

## 6. How to Build Going Forward

The codebase is ready for clean cloud and local builds:

```bash
# Trigger Android Development Build on EAS
pnpm run build:dev

# Trigger Android Preview APK on EAS
pnpm run build:preview

# Local Development Build (if local Android SDK/NDK is configured)
pnpm run build:dev:local
```
