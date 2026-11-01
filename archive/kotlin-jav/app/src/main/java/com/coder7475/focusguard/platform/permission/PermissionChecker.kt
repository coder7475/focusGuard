package com.coder7475.focusguard.platform.permission

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

enum class PermissionTier(
    val title: String,
    val description: String
) {
    CORE_ENFORCERS(
        "Core Enforcers",
        "Mandatory services required for foreground detection, exact alarms, and active notification countdowns."
    ),
    BACKGROUND_DURABILITY(
        "Background Durability",
        "Durability configurations to prevent battery management termination and survive reboots."
    )
}

enum class FocusPermission(
    val title: String,
    val description: String,
    val isMandatory: Boolean,
    val tier: PermissionTier
) {
    ACCESSIBILITY(
        "Accessibility Service",
        "Required to detect foreground window transitions and enforce focus locks over blocked apps without inspecting view content.",
        true,
        PermissionTier.CORE_ENFORCERS
    ),
    NOTIFICATIONS(
        "Notifications",
        "Displays real-time session countdown and high-priority completion alerts.",
        true,
        PermissionTier.CORE_ENFORCERS
    ),
    EXACT_ALARM(
        "Exact Alarms",
        "Ensures your focus sessions and scheduled locks conclude precisely on time, even during deep Doze sleep.",
        true,
        PermissionTier.CORE_ENFORCERS
    ),
    BATTERY_OPTIMIZATION(
        "Unrestricted Battery Usage",
        "Prevents aggressive background process killers from destroying your active session timer.",
        true,
        PermissionTier.BACKGROUND_DURABILITY
    ),
    BOOT_COMPLETED(
        "Run After Reboot",
        "Guarantees that active focus sessions and alarms survive unexpected device reboots.",
        true,
        PermissionTier.BACKGROUND_DURABILITY
    ),
    USAGE_STATS(
        "Usage Access",
        "Provides supplemental application package verification for maximum lock reliability.",
        false,
        PermissionTier.BACKGROUND_DURABILITY
    ),
    FULL_SCREEN_INTENT(
        "Full-Screen Alerts",
        "Allows high-priority completion screens and emergency tamper alarms.",
        false,
        PermissionTier.BACKGROUND_DURABILITY
    )
}

open class PermissionChecker(private val context: Context) {

    open fun isGranted(permission: FocusPermission): Boolean {
        return when (permission) {
            FocusPermission.ACCESSIBILITY -> isAccessibilityServiceEnabled()
            FocusPermission.EXACT_ALARM -> canScheduleExactAlarms()
            FocusPermission.NOTIFICATIONS -> hasNotificationPermission()
            FocusPermission.BATTERY_OPTIMIZATION -> isIgnoringBatteryOptimizations()
            FocusPermission.USAGE_STATS -> hasUsageStatsPermission()
            FocusPermission.BOOT_COMPLETED -> hasBootPermission()
            FocusPermission.FULL_SCREEN_INTENT -> canUseFullScreenIntent()
        }
    }

    open fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val expectedComponent = "${context.packageName}/com.coder7475.focusguard.service.accessibility.FocusGuardAccessibilityService"
            val shortExpectedComponent = "${context.packageName}/.service.accessibility.FocusGuardAccessibilityService"

            enabledServices.split(':').any {
                it.equals(expectedComponent, ignoreCase = true) ||
                    it.equals(shortExpectedComponent, ignoreCase = true)
            }
        } catch (_: Exception) {
            false
        }
    }

    open fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    open fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    open fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    open fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    open fun hasBootPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
        ) == PackageManager.PERMISSION_GRANTED
    }

    open fun canUseFullScreenIntent(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.canUseFullScreenIntent() ?: true
        } else {
            true
        }
    }

    open fun areAllMandatoryGranted(): Boolean {
        return FocusPermission.entries
            .filter { it.isMandatory }
            .all { isGranted(it) }
    }

    open fun getNextUngrantedMandatoryPermission(): FocusPermission? {
        val priorityOrder = listOf(
            FocusPermission.ACCESSIBILITY,
            FocusPermission.NOTIFICATIONS,
            FocusPermission.EXACT_ALARM,
            FocusPermission.BATTERY_OPTIMIZATION,
            FocusPermission.BOOT_COMPLETED
        )
        return priorityOrder.firstOrNull { !isGranted(it) }
    }

    @SuppressLint("BatteryLife")
    fun createIntentForPermission(permission: FocusPermission): Intent {
        return when (permission) {
            FocusPermission.ACCESSIBILITY -> {
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            }
            FocusPermission.EXACT_ALARM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                } else {
                    Intent(Settings.ACTION_SETTINGS)
                }
            }
            FocusPermission.NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                }
            }
            FocusPermission.BATTERY_OPTIMIZATION -> {
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = "package:${context.packageName}".toUri()
                }
            }
            FocusPermission.USAGE_STATS -> {
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            }
            FocusPermission.BOOT_COMPLETED -> {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                }
            }
            FocusPermission.FULL_SCREEN_INTENT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                }
            }
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
