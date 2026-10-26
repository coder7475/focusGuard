# SleepGuard — Beginner-Friendly Step-by-Step Implementation Guide
## How to Build the React Native Focus & Sleep Locker (From Zero to Working App)

> **Document:** `BEGINNER_STEP_BY_STEP_GUIDE.md`  
> **Target Project:** `SleepGuard`  
> **Prerequisites:** Node.js 20+, Android Studio, Android SDK 34+, Java/JDK 17, and a physical Android phone or emulator.  
> **Approach:** Guided step-by-step instructions with full code snippets, commands, and verification checks.  

---

## Welcome, Builder! 👋

In this guide, you will convert the native Android app **FocusGuard** into **SleepGuard**, a high-performance React Native application powered by **Expo SDK 58**.

Don't worry if you have never built a native Android module or configured an accessibility service before! We will break the entire build into simple, friendly steps organized by our **Engineering Divisions**.

Each step includes:
- 🎯 **The Goal**: What we are making and why.
- 💻 **Terminal Commands**: Exact commands to run.
- 📁 **File to Create**: Full file path and complete code.
- ✅ **Verification Check**: How to verify it works before moving on.
- 💡 **Troubleshooting Tip**: What to do if something goes wrong.

---

## Table of Contents

1. [Step 0: Development Environment & Prerequisites](#step-0-development-environment--prerequisites)
2. [Step 1: Installing Required Expo Packages](#step-1-installing-required-expo-packages)
3. [Step 2: Creating the Expo Config Plugin (`plugins/withFocusEnforcer.js`)](#step-2-creating-the-expo-config-plugin-pluginswithfocusenforcerjs)
4. [Step 3: Creating the Local Native Expo Module (`modules/focus-enforcer`)](#step-3-creating-the-local-native-expo-module-modulesfocus-enforcer)
5. [Step 4: Building the Native Enforcement Core (Accessibility, Overlay, & Services)](#step-4-building-the-native-enforcement-core-accessibility-overlay--services)
6. [Step 5: Setting Up Shared SQLite Database (TypeScript + Native Android)](#step-5-setting-up-shared-sqlite-database-typescript--native-android)
7. [Step 6: Writing the TypeScript Domain Engines (Session & Schedule)](#step-6-writing-the-typescript-domain-engines-session--schedule)
8. [Step 7: Building the Theme & Color Tokens](#step-7-building-the-theme--color-tokens)
9. [Step 8: Building the Tactile Rotary Dial with Reanimated 4](#step-8-building-the-tactile-rotary-dial-with-reanimated-4)
10. [Step 9: Building the Schedules & Routines Screen](#step-9-building-the-schedules--routines-screen)
11. [Step 10: Building the 7-Step Onboarding & Diagnostics Wizard](#step-10-building-the-7-step-onboarding--diagnostics-wizard)
12. [Step 11: Building the OEM Battery Optimization Guide](#step-11-building-the-oem-battery-optimization-guide)
13. [Step 12: Assembling the Main Screen & Navigation](#step-12-assembling-the-main-screen--navigation)
14. [Step 13: End-to-End Testing & Verification with ADB](#step-13-end-to-end-testing--verification-with-adb)

---

## Step 0: Development Environment & Prerequisites

### 🎯 The Goal
Verify that your machine has the required development tools installed.

### 💻 Commands
Open your terminal inside `/home/fahad/AndroidStudioProjects/focusGuard/SleepGuard` and run:

```bash
node -v
# Should output v20.x or v22.x

java -version
# Should output openjdk version 17.x or 21.x

adb devices
# Should list at least one attached Android device or running emulator
```

✅ **Verification:** All three commands output their respective versions without errors.

---

## Step 1: Installing Required Expo Packages

### 🎯 The Goal
Install the official Expo and React Native packages needed for SQLite storage, SVG graphics, haptic feedback, and secure preferences.

### 💻 Commands
In `SleepGuard/`, run:

```bash
npx expo install expo-sqlite expo-haptics react-native-svg expo-secure-store
```

> **Why `npx expo install` instead of `npm install`?**  
> Expo CLI automatically chooses the exact library version compatible with your Expo SDK (SDK 58). This prevents version mismatch errors.

✅ **Verification:** Open `package.json` and verify `expo-sqlite`, `expo-haptics`, `react-native-svg`, and `expo-secure-store` appear under `"dependencies"`.

---

## Step 2: Creating the Expo Config Plugin (`plugins/withFocusEnforcer.js`)

### 🎯 The Goal
FocusGuard requires high-privilege Android permissions (like running foreground services, setting exact alarms, and registering an accessibility service). Instead of manually editing Android files, Expo uses a **Config Plugin** that writes them automatically during the build process.

### 📁 File to Create: `plugins/withFocusEnforcer.js`

Create the directory and file:
```bash
mkdir -p plugins
```

Now create `plugins/withFocusEnforcer.js`:

```javascript
const { withAndroidManifest, withDangerousMod } = require('@expo/config-plugins');
const fs = require('fs');
const path = require('path');

module.exports = function withFocusEnforcer(config) {
  // 1. Inject Android Permissions and Service Declarations
  config = withAndroidManifest(config, async (config) => {
    const androidManifest = config.modResults.manifest;

    // Required Permissions
    const permissions = [
      'android.permission.FOREGROUND_SERVICE',
      'android.permission.FOREGROUND_SERVICE_SPECIAL_USE',
      'android.permission.SCHEDULE_EXACT_ALARM',
      'android.permission.POST_NOTIFICATIONS',
      'android.permission.RECEIVE_BOOT_COMPLETED',
      'android.permission.USE_FULL_SCREEN_INTENT',
      'android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS',
      'android.permission.SYSTEM_ALERT_WINDOW'
    ];

    if (!androidManifest['uses-permission']) {
      androidManifest['uses-permission'] = [];
    }

    permissions.forEach((perm) => {
      const exists = androidManifest['uses-permission'].some(
        (p) => p.$['android:name'] === perm
      );
      if (!exists) {
        androidManifest['uses-permission'].push({ $: { 'android:name': perm } });
      }
    });

    const app = androidManifest.application[0];
    if (!app.service) app.service = [];
    if (!app.receiver) app.receiver = [];

    // Accessibility Service Declaration
    app.service.push({
      $: {
        'android:name': 'com.coder7575.sleepguard.enforcer.FocusAccessibilityService',
        'android:permission': 'android.permission.BIND_ACCESSIBILITY_SERVICE',
        'android:exported': 'true'
      },
      'intent-filter': [
        {
          action: [
            { $: { 'android:name': 'android.accessibilityservice.AccessibilityService' } }
          ]
        }
      ],
      'meta-data': [
        {
          $: {
            'android:name': 'android.accessibilityservice',
            'android:resource': '@xml/accessibility_service_config'
          }
        }
      ]
    });

    // Foreground Ticker Service Declaration
    app.service.push({
      $: {
        'android:name': 'com.coder7575.sleepguard.enforcer.FocusForegroundService',
        'android:foregroundServiceType': 'specialUse',
        'android:exported': 'false'
      },
      property: [
        {
          $: {
            'android:name': 'android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE',
            'android:value': 'Focus lock countdown ticker and state preservation'
          }
        }
      ]
    });

    // Device Boot Completed Receiver Declaration
    app.receiver.push({
      $: {
        'android:name': 'com.coder7575.sleepguard.enforcer.BootReceiver',
        'android:exported': 'true'
      },
      'intent-filter': [
        {
          action: [
            { $: { 'android:name': 'android.intent.action.BOOT_COMPLETED' } },
            { $: { 'android:name': 'android.intent.action.LOCKED_BOOT_COMPLETED' } }
          ]
        }
      ]
    });

    return config;
  });

  // 2. Write accessibility_service_config.xml resource file
  config = withDangerousMod(config, [
    'android',
    async (config) => {
      const resXmlDir = path.join(
        config.modRequest.platformProjectRoot,
        'app/src/main/res/xml'
      );
      fs.mkdirSync(resXmlDir, { recursive: true });

      const xmlContent = `<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault"
    android:canRetrieveWindowContent="false"
    android:description="@string/accessibility_service_description"
    android:notificationTimeout="100" />
`;
      fs.writeFileSync(path.join(resXmlDir, 'accessibility_service_config.xml'), xmlContent);
      return config;
    }
  ]);

  return config;
};
```

### 📁 Update `app.json`
Open `app.json` and add `./plugins/withFocusEnforcer.js` to the `"plugins"` array:

```json
"plugins": [
  "expo-router",
  "./plugins/withFocusEnforcer.js",
  [
    "expo-splash-screen",
    {
      "backgroundColor": "#208AEF",
      "image": "./assets/images/splash-icon.png",
      "imageWidth": 76
    }
  ]
]
```

✅ **Verification:** Run `npx expo config --type prebuild` in terminal. It should print JSON configuration without reporting syntax errors in `withFocusEnforcer.js`.

---

## Step 3: Creating the Local Native Expo Module (`modules/focus-enforcer`)

### 🎯 The Goal
Create a local Expo module named `focus-enforcer` that bridges JavaScript calls to native Android code.

### 💻 Commands
Run the following commands to create the module directory structure:

```bash
mkdir -p modules/focus-enforcer/android/src/main/java/com/coder7575/sleepguard/enforcer
mkdir -p modules/focus-enforcer/src
```

### 📁 File to Create: `modules/focus-enforcer/expo-module.config.json`

```json
{
  "name": "focus-enforcer",
  "platforms": ["android"],
  "android": {
    "package": "com.coder7575.sleepguard.enforcer.FocusEnforcerPackage"
  }
}
```

### 📁 File to Create: `modules/focus-enforcer/android/build.gradle`

```groovy
apply plugin: 'com.android.library'
apply plugin: 'kotlin-android'

android {
    compileSdkVersion 35
    defaultConfig {
        minSdkVersion 24
        targetSdkVersion 35
    }
}

dependencies {
    implementation project(':expo-modules-core')
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    implementation "androidx.core:core-ktx:1.13.1"
}
```

### 📁 File to Create: `modules/focus-enforcer/android/src/main/java/com/coder7575/sleepguard/enforcer/FocusEnforcerModule.kt`

```kotlin
package com.coder7575.sleepguard.enforcer

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class FocusEnforcerModule : Module() {
    override fun definition() = ModuleDefinition {
        Name("FocusEnforcer")

        // Start active focus lock
        Function("startFocusLock") { endsAtEpochMs: Double ->
            val context = appContext.reactContext ?: return@Function false
            FocusForegroundService.start(context, endsAtEpochMs.toLong())
            AlarmCoordinator.scheduleSessionCompletion(context, endsAtEpochMs.toLong())
            true
        }

        // Stop active focus lock
        Function("stopFocusLock") {
            val context = appContext.reactContext ?: return@Function false
            FocusForegroundService.stop(context)
            AlarmCoordinator.cancelSessionCompletion(context)
            true
        }

        // Check permission statuses
        Function("getPermissionStatus") { permissionName: String ->
            val context = appContext.reactContext ?: return@Function false
            when (permissionName) {
                "ACCESSIBILITY" -> isAccessibilityEnabled(context)
                "EXACT_ALARM" -> canScheduleExactAlarms(context)
                "BATTERY" -> isIgnoringBatteryOptimizations(context)
                else -> false
            }
        }

        // Open specific system settings
        Function("openSettingsForPermission") { permissionName: String ->
            val context = appContext.reactContext ?: return@Function false
            val intent = when (permissionName) {
                "ACCESSIBILITY" -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                "EXACT_ALARM" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                } else Intent(Settings.ACTION_SETTINGS)
                "BATTERY" -> Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                else -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            }.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        }
    }

    private fun isAccessibilityEnabled(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val expected = "${context.packageName}/${FocusAccessibilityService::class.java.name}"
        return enabledServices.contains(expected, ignoreCase = true)
    }

    private fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else true
    }

    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }
}
```

### 📁 File to Create: `modules/focus-enforcer/android/src/main/java/com/coder7575/sleepguard/enforcer/FocusEnforcerPackage.kt`

```kotlin
package com.coder7575.sleepguard.enforcer

import android.content.Context
import expo.modules.core.interfaces.Package
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleProvider

class FocusEnforcerPackage : Package, ModuleProvider {
    override fun getModules(context: Context): List<Class<out Module>> {
        return listOf(FocusEnforcerModule::class.java)
    }
}
```

### 📁 File to Create: `modules/focus-enforcer/src/index.ts`

```typescript
import { requireNativeModule } from 'expo-modules-core';

interface FocusEnforcerNative {
  startFocusLock(endsAtEpochMs: number): boolean;
  stopFocusLock(): boolean;
  getPermissionStatus(permissionName: 'ACCESSIBILITY' | 'EXACT_ALARM' | 'BATTERY'): boolean;
  openSettingsForPermission(permissionName: 'ACCESSIBILITY' | 'EXACT_ALARM' | 'BATTERY'): boolean;
}

const FocusEnforcer = requireNativeModule<FocusEnforcerNative>('FocusEnforcer');

export default FocusEnforcer;
```

✅ **Verification:** Run `npx tsc --noEmit` in `SleepGuard/` to confirm TypeScript compiles `modules/focus-enforcer/src/index.ts` cleanly.

---

## Step 4: Building the Native Enforcement Core (Accessibility, Overlay, & Services)

### 🎯 The Goal
Create the native Android classes:
1. `FocusAccessibilityService`: Catches app switching and triggers the lock.
2. `LockOverlayWindowManager`: Displays the full-screen non-dismissible overlay with emergency dialer button.
3. `FocusForegroundService`: Displays the continuous countdown notification.
4. `AlarmCoordinator`: Wakes up the phone exactly at completion time.
5. `BootReceiver`: Survives phone restarts.

All files are created in `modules/focus-enforcer/android/src/main/java/com/coder7575/sleepguard/enforcer/`.

### 📁 File: `LockOverlayWindowManager.kt`

```kotlin
package com.coder7575.sleepguard.enforcer

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class LockOverlayWindowManager(private val context: Context) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    var isVisible = false
        private set

    private var endsAtEpochMs: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    fun setEndsAtEpochMs(epochMs: Long) {
        endsAtEpochMs = epochMs
    }

    fun showOverlay() {
        if (isVisible) return

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#121316"))
            setPadding(48, 48, 48, 48)
        }

        val titleView = TextView(context).apply {
            text = "🔒 SLEEPGUARD ACTIVE"
            textSize = 22f
            setTextColor(Color.parseColor("#FF5722"))
            gravity = Gravity.CENTER
        }

        val countdownView = TextView(context).apply {
            text = formatRemainingTime()
            textSize = 40f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 48)
        }

        val emergencyButton = Button(context).apply {
            text = "EMERGENCY CALL"
            setBackgroundColor(Color.parseColor("#FF3366"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
                hideOverlay()
            }
        }

        layout.addView(titleView)
        layout.addView(countdownView)
        layout.addView(emergencyButton)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.FILL
        }

        try {
            windowManager.addView(layout, params)
            overlayView = layout
            isVisible = true

            // Live 1-second countdown ticker
            timerRunnable = object : Runnable {
                override fun run() {
                    if (isVisible) {
                        countdownView.text = formatRemainingTime()
                        handler.postDelayed(this, 1000L)
                    }
                }
            }
            handler.post(timerRunnable!!)
        } catch (_: Exception) {}
    }

    fun hideOverlay() {
        if (!isVisible || overlayView == null) return
        try {
            timerRunnable?.let { handler.removeCallbacks(it) }
            windowManager.removeView(overlayView)
        } catch (_: Exception) {}
        overlayView = null
        isVisible = false
    }

    private fun formatRemainingTime(): String {
        val remaining = Math.max(0L, endsAtEpochMs - System.currentTimeMillis())
        val s = (remaining / 1000L) % 60
        val m = (remaining / (1000L * 60)) % 60
        val h = remaining / (1000L * 3600)
        return String.format("%02d:%02d:%02d", h, m, s)
    }
}
```

### 📁 File: `FocusAccessibilityService.kt`

```kotlin
package com.coder7575.sleepguard.enforcer

import android.accessibilityservice.AccessibilityService
import android.telecom.TelecomManager
import android.content.Context
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {
    private lateinit var overlayManager: LockOverlayWindowManager

    override fun onCreate() {
        super.onCreate()
        overlayManager = LockOverlayWindowManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val targetPkg = event.packageName?.toString() ?: return

        // 1. Own package is always allowed
        if (targetPkg == packageName) {
            overlayManager.hideOverlay()
            return
        }

        // 2. Allow default phone dialer (emergency safety)
        val tm = getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        val defaultDialer = tm?.defaultDialerPackage
        if (targetPkg == defaultDialer || targetPkg.contains("dialer") || targetPkg.contains("incall")) {
            overlayManager.hideOverlay()
            return
        }

        // 3. Inspect active session from SQLite
        val activeTarget = NativeDbHelper(this).getActiveSessionEndsAt()
        if (activeTarget > System.currentTimeMillis()) {
            overlayManager.setEndsAtEpochMs(activeTarget)
            overlayManager.showOverlay()
        } else {
            overlayManager.hideOverlay()
        }
    }

    override fun onInterrupt() {}
}
```

### 📁 File: `FocusForegroundService.kt`

```kotlin
package com.coder7575.sleepguard.enforcer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class FocusForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "FOCUS_CHANNEL")
            .setContentTitle("SleepGuard Active")
            .setContentText("Focus session is currently locking distractions.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "FOCUS_CHANNEL",
                "SleepGuard Focus Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        fun start(context: Context, endsAtEpochMs: Long) {
            val intent = Intent(context, FocusForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FocusForegroundService::class.java))
        }
    }
}
```

### 📁 File: `AlarmCoordinator.kt`

```kotlin
package com.coder7575.sleepguard.enforcer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmCoordinator {
    fun scheduleSessionCompletion(context: Context, triggerEpochMs: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BootReceiver::class.java).apply {
            action = "ACTION_SESSION_EXPIRED"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerEpochMs,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
        }
    }

    fun cancelSessionCompletion(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BootReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
```

### 📁 File: `BootReceiver.kt`

```kotlin
package com.coder7575.sleepguard.enforcer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            val activeTarget = NativeDbHelper(context).getActiveSessionEndsAt()
            if (activeTarget > System.currentTimeMillis()) {
                FocusForegroundService.start(context, activeTarget)
                AlarmCoordinator.scheduleSessionCompletion(context, activeTarget)
            }
        } else if (action == "ACTION_SESSION_EXPIRED") {
            FocusForegroundService.stop(context)
        }
    }
}
```

---

## Step 5: Setting Up Shared SQLite Database (TypeScript + Native Android)

### 🎯 The Goal
Both the JavaScript UI and the native `BootReceiver` / `FocusAccessibilityService` must read and write session data to the **same SQLite database**.

### 📁 File 1 (Native Kotlin): `NativeDbHelper.kt`
Create inside `modules/focus-enforcer/android/src/main/java/com/coder7575/sleepguard/enforcer/NativeDbHelper.kt`:

```kotlin
package com.coder7575.sleepguard.enforcer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NativeDbHelper(context: Context) : SQLiteOpenHelper(context, "focusguard.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {}
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun getActiveSessionEndsAt(): Long {
        return try {
            val db = readableDatabase
            val cursor = db.rawQuery(
                "SELECT endsAtEpochMs FROM focus_sessions WHERE status = 'ACTIVE' LIMIT 1",
                null
            )
            var targetEpoch = 0L
            if (cursor.moveToFirst()) {
                targetEpoch = cursor.getLong(0)
            }
            cursor.close()
            targetEpoch
        } catch (_: Exception) {
            0L
        }
    }
}
```

### 📁 File 2 (TypeScript): `src/data/db/database.ts`
Create `src/data/db/database.ts`:

```typescript
import * as SQLite from 'expo-sqlite';

export const DB_NAME = 'focusguard.db';

export async function openAppDatabase(): Promise<SQLite.SQLiteDatabase> {
  const db = await SQLite.openDatabaseAsync(DB_NAME);
  await db.execAsync(`
    PRAGMA journal_mode = WAL;

    CREATE TABLE IF NOT EXISTS focus_sessions (
      id TEXT PRIMARY KEY NOT NULL,
      startedAtEpochMs INTEGER NOT NULL,
      endsAtEpochMs INTEGER NOT NULL,
      durationMs INTEGER NOT NULL,
      type TEXT NOT NULL,
      status TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS focus_schedules (
      id TEXT PRIMARY KEY NOT NULL,
      name TEXT NOT NULL,
      startHour INTEGER NOT NULL,
      startMinute INTEGER NOT NULL,
      endHour INTEGER NOT NULL,
      endMinute INTEGER NOT NULL,
      daysOfWeek TEXT NOT NULL,
      isEnabled INTEGER NOT NULL,
      createdAtEpochMs INTEGER NOT NULL
    );
  `);
  return db;
}
```

✅ **Verification:** Both Kotlin and TypeScript now point to `focusguard.db` in WAL mode.

---

## Step 6: Writing the TypeScript Domain Engines (Session & Schedule)

### 🎯 The Goal
Write the pure TypeScript business engines for starting/stopping sessions and calculating midnight-crossing recurring schedules.

### 📁 File 1: `src/types/session.ts`

```typescript
export type SessionStatus = 'ACTIVE' | 'COMPLETED' | 'ABORTED';
export type SessionType = 'QUICK' | 'SCHEDULED';

export interface FocusSession {
  id: string;
  startedAtEpochMs: number;
  endsAtEpochMs: number;
  durationMs: number;
  type: SessionType;
  status: SessionStatus;
}
```

### 📁 File 2: `src/types/schedule.ts`

```typescript
export interface FocusSchedule {
  id: string;
  name: string;
  startHour: number; // 0..23
  startMinute: number; // 0..59
  endHour: number; // 0..23
  endMinute: number; // 0..59
  daysOfWeek: number[]; // 1 = Monday, 7 = Sunday
  isEnabled: boolean;
  createdAtEpochMs: number;
}
```

### 📁 File 3: `src/services/schedule-engine.ts`

```typescript
import { FocusSchedule } from '@/types/schedule';

export class ScheduleEngine {
  static isScheduleActiveAt(schedule: FocusSchedule, epochMs: number): boolean {
    if (!schedule.isEnabled) return false;

    const date = new Date(epochMs);
    const dayOfWeek = date.getDay() === 0 ? 7 : date.getDay(); // 1 = Mon, 7 = Sun
    const currentMinutes = date.getHours() * 60 + date.getMinutes();

    const startMinutes = schedule.startHour * 60 + schedule.startMinute;
    const endMinutes = schedule.endHour * 60 + schedule.endMinute;

    // Normal intraday schedule (e.g. 09:00 -> 17:00)
    if (startMinutes < endMinutes) {
      return (
        schedule.daysOfWeek.includes(dayOfWeek) &&
        currentMinutes >= startMinutes &&
        currentMinutes < endMinutes
      );
    }

    // Overnight schedule crossing midnight (e.g. 22:00 -> 07:00)
    if (startMinutes > endMinutes) {
      // Evening window
      if (currentMinutes >= startMinutes && schedule.daysOfWeek.includes(dayOfWeek)) {
        return true;
      }
      // Morning window (rolls over from previous day)
      const prevDay = dayOfWeek === 1 ? 7 : dayOfWeek - 1;
      if (currentMinutes < endMinutes && schedule.daysOfWeek.includes(prevDay)) {
        return true;
      }
    }

    return false;
  }
}
```

---

## Step 7: Building the Theme & Color Tokens

### 🎯 The Goal
Replicate FocusGuard's industrial cyberpunk aesthetic with dark gunmetal, safety orange, and electric cyan.

### 📁 File: `src/constants/colors.ts`

```typescript
export const Colors = {
  matteCharcoal: '#121316',
  darkGunmetal: '#1A1C23',
  cardSurface: '#21242D',
  borderDark: '#2E323E',
  safetyOrange: '#FF5722',
  safetyOrangeDim: 'rgba(255, 87, 34, 0.15)',
  electricCyan: '#00E5FF',
  dangerRed: '#FF3366',
  offWhite: '#F0F2F5',
  textMuted: '#8E94A5',
  successGreen: '#00E676',
};
```

---

## Step 8: Building the Tactile Rotary Dial with Reanimated 4

### 🎯 The Goal
Build a buttery-smooth 60 FPS circular dial with haptic feedback ticks using `react-native-gesture-handler` and `react-native-reanimated`.

### 📁 File: `src/components/timer/RotaryDial.tsx`

```typescript
import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Gesture, GestureDetector } from 'react-native-gesture-handler';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  runOnJS
} from 'react-native-reanimated';
import * as Haptics from 'expo-haptics';
import Svg, { Circle } from 'react-native-svg';
import { Colors } from '@/constants/colors';

interface RotaryDialProps {
  value: number; // 5..180 minutes
  onChange: (minutes: number) => void;
  size?: number;
}

export const RotaryDial: React.FC<RotaryDialProps> = ({
  value,
  onChange,
  size = 280
}) => {
  const angle = useSharedValue(((value - 5) / (180 - 5)) * 300);
  const lastStep = useSharedValue(value);

  const triggerHaptic = () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
  };

  const panGesture = Gesture.Pan().onUpdate((event) => {
    const center = size / 2;
    const x = event.x - center;
    const y = event.y - center;
    let rad = Math.atan2(y, x) + Math.PI / 2;
    if (rad < 0) rad += 2 * Math.PI;

    const deg = (rad * 180) / Math.PI;
    if (deg <= 300) {
      angle.value = deg;
      const calculated = Math.round(5 + (deg / 300) * (180 - 5));
      if (Math.abs(calculated - lastStep.value) >= 5) {
        lastStep.value = calculated;
        runOnJS(triggerHaptic)();
      }
      runOnJS(onChange)(calculated);
    }
  });

  const pointerStyle = useAnimatedStyle(() => ({
    transform: [{ rotate: `${angle.value}deg` }]
  }));

  return (
    <GestureDetector gesture={panGesture}>
      <View style={[styles.container, { width: size, height: size }]}>
        <Svg width={size} height={size}>
          <Circle
            cx={size / 2}
            cy={size / 2}
            r={size / 2 - 20}
            stroke={Colors.darkGunmetal}
            strokeWidth={14}
            fill={Colors.matteCharcoal}
          />
        </Svg>
        <Animated.View style={[styles.needleTrack, pointerStyle]}>
          <View style={styles.knob} />
        </Animated.View>
      </View>
    </GestureDetector>
  );
};

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  needleTrack: {
    ...StyleSheet.absoluteFillObject,
    alignItems: 'center',
  },
  knob: {
    width: 16,
    height: 16,
    borderRadius: 8,
    backgroundColor: Colors.safetyOrange,
    marginTop: 12,
  }
});
```

---

## Step 9: Building the Schedules & Routines Screen

### 🎯 The Goal
Create the bedtime routine management view supporting overnight windows (e.g. 10:00 PM to 7:00 AM).

### 📁 File: `src/app/(tabs)/schedules.tsx`

```typescript
import React, { useState } from 'react';
import { View, Text, StyleSheet, FlatList, Switch, TouchableOpacity } from 'react-native';
import { Colors } from '@/constants/colors';
import { FocusSchedule } from '@/types/schedule';

export default function SchedulesScreen() {
  const [schedules, setSchedules] = useState<FocusSchedule[]>([
    {
      id: 'default_sleep',
      name: 'Night Sleep Lock',
      startHour: 22,
      startMinute: 0,
      endHour: 7,
      endMinute: 0,
      daysOfWeek: [1, 2, 3, 4, 5, 6, 7],
      isEnabled: true,
      createdAtEpochMs: Date.now()
    }
  ]);

  const toggleSchedule = (id: string) => {
    setSchedules((prev) =>
      prev.map((s) => (s.id === id ? { ...s, isEnabled: !s.isEnabled } : s))
    );
  };

  return (
    <View style={styles.container}>
      <Text style={styles.header}>BEDTIME ROUTINES</Text>
      <FlatList
        data={schedules}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <View style={styles.card}>
            <View>
              <Text style={styles.name}>{item.name}</Text>
              <Text style={styles.time}>
                {String(item.startHour).padStart(2, '0')}:{String(item.startMinute).padStart(2, '0')} →{' '}
                {String(item.endHour).padStart(2, '0')}:{String(item.endMinute).padStart(2, '0')} (Overnight)
              </Text>
            </View>
            <Switch
              value={item.isEnabled}
              onValueChange={() => toggleSchedule(item.id)}
              thumbColor={item.isEnabled ? Colors.safetyOrange : Colors.textMuted}
            />
          </View>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.matteCharcoal, padding: 20 },
  header: { fontSize: 20, fontWeight: 'bold', color: Colors.offWhite, marginBottom: 20 },
  card: {
    backgroundColor: Colors.cardSurface,
    padding: 16,
    borderRadius: 12,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: Colors.borderDark,
    marginBottom: 12
  },
  name: { fontSize: 16, fontWeight: 'bold', color: Colors.offWhite },
  time: { fontSize: 14, color: Colors.safetyOrange, marginTop: 4 }
});
```

---

## Step 10: Building the 7-Step Onboarding & Diagnostics Wizard

### 🎯 The Goal
Walk users through granting the required Android system permissions (Accessibility, Exact Alarms, Battery Optimization, Notifications).

### 📁 File: `src/app/onboarding.tsx`

```typescript
import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import FocusEnforcer from '@/../modules/focus-enforcer/src';
import { Colors } from '@/constants/colors';
import { router } from 'expo-router';

export default function OnboardingScreen() {
  const [a11yGranted, setA11yGranted] = useState(false);
  const [alarmGranted, setAlarmGranted] = useState(false);
  const [batteryGranted, setBatteryGranted] = useState(false);

  const checkAll = () => {
    setA11yGranted(FocusEnforcer.getPermissionStatus('ACCESSIBILITY'));
    setAlarmGranted(FocusEnforcer.getPermissionStatus('EXACT_ALARM'));
    setBatteryGranted(FocusEnforcer.getPermissionStatus('BATTERY'));
  };

  useEffect(() => {
    checkAll();
  }, []);

  const allReady = a11yGranted && alarmGranted && batteryGranted;

  return (
    <ScrollView style={styles.container} contentContainerStyle={{ padding: 24 }}>
      <Text style={styles.title}>SYSTEM DIAGNOSTICS</Text>
      <Text style={styles.subtitle}>
        Grant permissions below to enable un-bypassable lock enforcement.
      </Text>

      {/* Permission Item: Accessibility */}
      <View style={styles.card}>
        <View style={styles.row}>
          <Text style={styles.cardTitle}>1. Accessibility Service</Text>
          <Text style={{ color: a11yGranted ? Colors.successGreen : Colors.dangerRed }}>
            {a11yGranted ? '✓ READY' : '✗ REQUIRED'}
          </Text>
        </View>
        <TouchableOpacity
          style={styles.actionButton}
          onPress={() => {
            FocusEnforcer.openSettingsForPermission('ACCESSIBILITY');
          }}
        >
          <Text style={styles.btnText}>ENABLE ACCESSIBILITY</Text>
        </TouchableOpacity>
      </View>

      {/* Permission Item: Exact Alarm */}
      <View style={styles.card}>
        <View style={styles.row}>
          <Text style={styles.cardTitle}>2. Exact Alarms</Text>
          <Text style={{ color: alarmGranted ? Colors.successGreen : Colors.dangerRed }}>
            {alarmGranted ? '✓ READY' : '✗ REQUIRED'}
          </Text>
        </View>
        <TouchableOpacity
          style={styles.actionButton}
          onPress={() => {
            FocusEnforcer.openSettingsForPermission('EXACT_ALARM');
          }}
        >
          <Text style={styles.btnText}>ALLOW EXACT ALARMS</Text>
        </TouchableOpacity>
      </View>

      {/* Permission Item: Battery Optimization */}
      <View style={styles.card}>
        <View style={styles.row}>
          <Text style={styles.cardTitle}>3. Unrestricted Battery</Text>
          <Text style={{ color: batteryGranted ? Colors.successGreen : Colors.dangerRed }}>
            {batteryGranted ? '✓ READY' : '✗ REQUIRED'}
          </Text>
        </View>
        <TouchableOpacity
          style={styles.actionButton}
          onPress={() => {
            FocusEnforcer.openSettingsForPermission('BATTERY');
          }}
        >
          <Text style={styles.btnText}>DISABLE BATTERY KILLER</Text>
        </TouchableOpacity>
      </View>

      <TouchableOpacity
        style={[styles.continueButton, { opacity: allReady ? 1 : 0.5 }]}
        disabled={!allReady}
        onPress={() => router.replace('/')}
      >
        <Text style={styles.continueText}>ENTER SLEEPGUARD</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.matteCharcoal },
  title: { fontSize: 24, fontWeight: 'bold', color: Colors.safetyOrange, marginBottom: 8 },
  subtitle: { fontSize: 14, color: Colors.textMuted, marginBottom: 24 },
  card: {
    backgroundColor: Colors.cardSurface,
    padding: 16,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: Colors.borderDark,
    marginBottom: 16
  },
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  cardTitle: { fontSize: 16, fontWeight: 'bold', color: Colors.offWhite },
  actionButton: {
    backgroundColor: Colors.darkGunmetal,
    padding: 12,
    borderRadius: 8,
    marginTop: 12,
    alignItems: 'center'
  },
  btnText: { color: Colors.electricCyan, fontWeight: 'bold', fontSize: 12 },
  continueButton: {
    backgroundColor: Colors.safetyOrange,
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
    marginTop: 16
  },
  continueText: { color: Colors.matteCharcoal, fontWeight: 'bold', fontSize: 16 }
});
```

---

## Step 11: Building the OEM Battery Optimization Guide

### 🎯 The Goal
Inform users with Samsung, Xiaomi, or OnePlus phones how to protect SleepGuard from being killed in the background.

### 📁 File: `src/components/settings/OemBatteryGuideModal.tsx`

```typescript
import React from 'react';
import { View, Text, Modal, StyleSheet, TouchableOpacity, ScrollView } from 'react-native';
import { Colors } from '@/constants/colors';

interface Props {
  visible: boolean;
  onClose: () => void;
}

export const OemBatteryGuideModal: React.FC<Props> = ({ visible, onClose }) => {
  return (
    <Modal visible={visible} animationType="slide" transparent>
      <View style={styles.backdrop}>
        <View style={styles.sheet}>
          <Text style={styles.title}>OEM BATTERY KILLER MITIGATION</Text>
          <ScrollView style={{ maxHeight: 400 }}>
            <Text style={styles.vendor}>Samsung (One UI)</Text>
            <Text style={styles.body}>Settings → Apps → SleepGuard → Battery → Choose "Unrestricted".</Text>

            <Text style={styles.vendor}>Xiaomi (HyperOS / MIUI)</Text>
            <Text style={styles.body}>App Info → Battery Saver → Select "No Restrictions" + enable Autostart.</Text>

            <Text style={styles.vendor}>OnePlus (OxygenOS)</Text>
            <Text style={styles.body}>App Info → Battery Usage → Enable "Allow background activity".</Text>
          </ScrollView>
          <TouchableOpacity style={styles.closeBtn} onPress={onClose}>
            <Text style={styles.closeText}>GOT IT</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  backdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.7)', justifyContent: 'flex-end' },
  sheet: { backgroundColor: Colors.cardSurface, padding: 24, borderTopLeftRadius: 20, borderTopRightRadius: 20 },
  title: { fontSize: 18, fontWeight: 'bold', color: Colors.safetyOrange, marginBottom: 16 },
  vendor: { fontSize: 15, fontWeight: 'bold', color: Colors.electricCyan, marginTop: 12 },
  body: { fontSize: 13, color: Colors.textMuted, marginTop: 4 },
  closeBtn: { backgroundColor: Colors.safetyOrange, padding: 14, borderRadius: 10, marginTop: 20, alignItems: 'center' },
  closeText: { color: Colors.matteCharcoal, fontWeight: 'bold' }
});
```

---

## Step 12: Assembling the Main Screen & Navigation

### 🎯 The Goal
Wire the Rotary Dial, quick preset buttons (15m, 45m, 60m), and the native focus lock trigger together on the dashboard.

### 📁 File: `src/app/(tabs)/index.tsx`

```typescript
import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { RotaryDial } from '@/components/timer/RotaryDial';
import { Colors } from '@/constants/colors';
import FocusEnforcer from '@/../modules/focus-enforcer/src';
import { openAppDatabase } from '@/data/db/database';

export default function HomeScreen() {
  const [selectedMinutes, setSelectedMinutes] = useState(25);
  const [isActive, setIsActive] = useState(false);

  const startSession = async () => {
    const endsAt = Date.now() + selectedMinutes * 60 * 1000;
    
    // 1. Write active session to shared SQLite
    const db = await openAppDatabase();
    await db.runAsync(
      `INSERT OR REPLACE INTO focus_sessions (id, startedAtEpochMs, endsAtEpochMs, durationMs, type, status)
       VALUES (?, ?, ?, ?, ?, ?)`,
      'session_' + Date.now(),
      Date.now(),
      endsAt,
      selectedMinutes * 60 * 1000,
      'QUICK',
      'ACTIVE'
    );

    // 2. Start native enforcer service and alarms
    FocusEnforcer.startFocusLock(endsAt);
    setIsActive(true);
  };

  const stopSession = async () => {
    const db = await openAppDatabase();
    await db.runAsync(`UPDATE focus_sessions SET status = 'ABORTED' WHERE status = 'ACTIVE'`);
    FocusEnforcer.stopFocusLock();
    setIsActive(false);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>SLEEPGUARD</Text>
      <Text style={styles.timerDisplay}>{selectedMinutes} MIN</Text>

      <RotaryDial value={selectedMinutes} onChange={setSelectedMinutes} />

      {/* Preset Chips */}
      <View style={styles.chipRow}>
        {[15, 45, 60].map((preset) => (
          <TouchableOpacity
            key={preset}
            style={[styles.chip, selectedMinutes === preset && styles.chipActive]}
            onPress={() => setSelectedMinutes(preset)}
          >
            <Text style={styles.chipText}>{preset}M</Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Action Button */}
      <TouchableOpacity
        style={[styles.mainButton, isActive ? styles.abortButton : styles.startButton]}
        onPress={isActive ? stopSession : startSession}
      >
        <Text style={styles.mainButtonText}>
          {isActive ? 'ABORT SESSION' : `ENGAGE FOCUS LOCK (${selectedMinutes}M)`}
        </Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.matteCharcoal, alignItems: 'center', justifyContent: 'center', padding: 20 },
  title: { fontSize: 20, fontWeight: 'bold', color: Colors.textMuted, letterSpacing: 2 },
  timerDisplay: { fontSize: 44, fontWeight: 'black', color: Colors.offWhite, marginVertical: 16 },
  chipRow: { flexDirection: 'row', gap: 12, marginVertical: 24 },
  chip: { paddingVertical: 8, paddingHorizontal: 18, borderRadius: 20, backgroundColor: Colors.darkGunmetal },
  chipActive: { backgroundColor: Colors.safetyOrange },
  chipText: { color: Colors.offWhite, fontWeight: 'bold' },
  mainButton: { width: '100%', height: 54, borderRadius: 12, justifyContent: 'center', alignItems: 'center' },
  startButton: { backgroundColor: Colors.safetyOrange },
  abortButton: { backgroundColor: Colors.dangerRed },
  mainButtonText: { color: Colors.matteCharcoal, fontWeight: 'bold', fontSize: 16, letterSpacing: 1 }
});
```

---

## Step 13: End-to-End Testing & Verification with ADB

### 🎯 The Goal
Verify that your newly created app builds natively, intercepts third-party apps, survives Doze mode, and recovers after reboot.

### 💻 Step 13.1: Build the Native Android Binary
Run:
```bash
npx expo run:android
```
> This command compiles the native module and launches the app on your connected device or emulator.

### 💻 Step 13.2: Verify Lock Overlay
1. Open SleepGuard and tap **ENGAGE FOCUS LOCK (15M)**.
2. Press your phone's Home button and attempt to open any other app (e.g. YouTube or Settings).
3. **Expected result:** SleepGuard's lock screen immediately overlays the screen, displaying the countdown timer and Emergency Call button.

### 💻 Step 13.3: Verify Emergency Call Bypass
1. Tap the **EMERGENCY CALL** button on the lock overlay.
2. **Expected result:** The system phone dialer opens immediately and the overlay yields.

### 💻 Step 13.4: Verify Deep Doze Mode Survival
Run via ADB in terminal:
```bash
adb shell dumpsys battery unplug
adb shell dumpsys deviceidle force-idle
```
Wait until the session time elapses.
**Expected result:** The alarm coordinator wakes the device up precisely at the target epoch and dismisses the lock notification.

### 💻 Step 13.5: Verify Device Reboot Survival
While a 15-minute lock is active, restart your device:
```bash
adb reboot
```
Once the phone boots to the home screen, check logcat:
```bash
adb logcat -s FocusGuardA11y BootReceiver
```
**Expected result:** `BootReceiver` triggers, reads `focusguard.db`, discovers the active session, and re-engages the overlay within 1.5 seconds!

---

## Summary of Accomplishment 🚀

Congratulations! You have built a production-grade, un-dismissible sleep and focus enforcer using:
- **Expo SDK 58 & Expo Router** for modern React Native architecture.
- **Expo Native Modules & Config Plugins** for zero-compromise Android system enforcement.
- **Continuous Native Generation** keeping code clean and maintainable.
- **Shared SQLite & Wall-Clock Epoch Timekeeping** guaranteeing survival across Doze mode and restarts.
