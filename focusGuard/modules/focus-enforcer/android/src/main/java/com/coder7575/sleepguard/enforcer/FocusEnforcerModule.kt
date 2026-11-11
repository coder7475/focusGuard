package com.coder7575.sleepguard.enforcer

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class FocusEnforcerModule : Module() {
    override fun definition() = ModuleDefinition {
        Name("FocusEnforcer")

        // Start active focus lock
        Function("startFocusLock") { endsAtEpochMs: Double ->
            val context = appContext.reactContext ?: return@Function false
            val epoch = endsAtEpochMs.toLong()
            FocusAccessibilityService.activeSessionEndsAt = epoch
            FocusForegroundService.start(context, epoch)
            AlarmCoordinator.scheduleSessionCompletion(context, epoch)
            true
        }

        // Stop active focus lock
        Function("stopFocusLock") {
            val context = appContext.reactContext ?: return@Function false
            FocusAccessibilityService.clearActiveSession()
            FocusForegroundService.stop(context)
            AlarmCoordinator.cancelSessionCompletion(context)
            ScheduleCoordinator.evaluateAndScheduleNext(context)
            true
        }

        // Reschedule recurring background routines
        Function("rescheduleRoutines") {
            val context = appContext.reactContext ?: return@Function false
            ScheduleCoordinator.evaluateAndScheduleNext(context)
            true
        }

        // Check permission statuses
        Function("getPermissionStatus") { permissionName: String ->
            val context = appContext.reactContext ?: return@Function false
            when (permissionName) {
                "ACCESSIBILITY" -> isAccessibilityEnabled(context)
                "EXACT_ALARM" -> canScheduleExactAlarms(context)
                "BATTERY" -> isIgnoringBatteryOptimizations(context)
                "OVERLAY" -> Settings.canDrawOverlays(context)
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
                "OVERLAY" -> Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                "APP_DETAILS" -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
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

        // Open emergency phone dialer
        Function("openEmergencyDialer") {
            val context = appContext.reactContext ?: return@Function false
            val intent = Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        }

        // Query installed launchable applications
        Function("getInstalledApps") {
            val context = appContext.reactContext ?: return@Function emptyList<Map<String, String>>()
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            resolveInfos.mapNotNull { ri ->
                val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
                if (pkg == context.packageName) return@mapNotNull null
                val label = ri.loadLabel(pm)?.toString() ?: pkg
                mapOf("packageName" to pkg, "label" to label)
            }.distinctBy { it["packageName"] }.sortedBy { it["label"]?.lowercase() }
        }

        // Set Bag Slot allowed app
        Function("setBagApp") { packageName: String, label: String ->
            val context = appContext.reactContext ?: return@Function false
            val prefs = context.getSharedPreferences(PREFS_BAG, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_BAG_PKG, packageName)
                .putString(KEY_BAG_LABEL, label)
                .apply()

            FocusAccessibilityService.bagAppPackage = packageName
            FocusAccessibilityService.bagAppName = label
            LockOverlayWindowManager.bagAppPackage = packageName
            LockOverlayWindowManager.bagAppName = label
            true
        }

        // Get Bag Slot allowed app
        Function("getBagApp") {
            val context = appContext.reactContext ?: return@Function null
            val prefs = context.getSharedPreferences(PREFS_BAG, Context.MODE_PRIVATE)
            val pkg = prefs.getString(KEY_BAG_PKG, null)
            val label = prefs.getString(KEY_BAG_LABEL, null)
            if (!pkg.isNullOrBlank()) {
                mapOf("packageName" to pkg, "label" to (label ?: pkg))
            } else {
                null
            }
        }

        // Clear Bag Slot
        Function("clearBagApp") {
            val context = appContext.reactContext ?: return@Function false
            val prefs = context.getSharedPreferences(PREFS_BAG, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            FocusAccessibilityService.bagAppPackage = null
            FocusAccessibilityService.bagAppName = null
            LockOverlayWindowManager.bagAppPackage = null
            LockOverlayWindowManager.bagAppName = null
            true
        }

        // Open application by package name
        Function("openApp") { packageName: String ->
            val context = appContext.reactContext ?: return@Function false
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
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

    companion object {
        const val PREFS_BAG = "focusguard_bag"
        const val KEY_BAG_PKG = "bag_package"
        const val KEY_BAG_LABEL = "bag_label"
    }
}
