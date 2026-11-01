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
