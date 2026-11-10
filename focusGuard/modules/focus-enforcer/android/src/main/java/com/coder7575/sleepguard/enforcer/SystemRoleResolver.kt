package com.coder7575.sleepguard.enforcer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import android.telecom.TelecomManager

open class SystemRoleResolver(private val context: Context) {

    private val telecomManager: TelecomManager? by lazy {
        context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
    }

    open fun getDefaultDialerPackage(): String? {
        return try {
            telecomManager?.defaultDialerPackage
        } catch (_: Exception) {
            null
        }
    }

    open fun getDefaultSmsPackage(): String? {
        return try {
            Telephony.Sms.getDefaultSmsPackage(context)
        } catch (_: Exception) {
            null
        }
    }

    open fun getDefaultLauncherPackage(): String? {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = context.packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            val pkg = resolveInfo?.activityInfo?.packageName
            if (pkg == "android") null else pkg
        } catch (_: Exception) {
            null
        }
    }

    fun isAllowlisted(pkg: String): Boolean {
        // Own package
        if (pkg == context.packageName) return true

        // Default dynamic roles
        val defaultDialer = getDefaultDialerPackage()
        if (defaultDialer != null && pkg == defaultDialer) return true

        val defaultSms = getDefaultSmsPackage()
        if (defaultSms != null && pkg == defaultSms) return true

        val defaultLauncher = getDefaultLauncherPackage()
        if (defaultLauncher != null && pkg == defaultLauncher) return true

        // Known OEM Dialers / In-Call UI
        if (OEM_DIALER_PACKAGES.contains(pkg) || pkg.contains("dialer") || pkg.contains("incall")) {
            return true
        }

        // Emergency packages
        if (OEM_EMERGENCY_PACKAGES.contains(pkg)) {
            return true
        }

        return false
    }

    companion object {
        val OEM_DIALER_PACKAGES = setOf(
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.android.server.telecom",
            "com.android.incallui",
            "com.google.android.incallui",
            "com.oneplus.dialer"
        )

        val OEM_EMERGENCY_PACKAGES = setOf(
            "com.android.phone.EmergencyDialer",
            "com.android.phone",
            "com.sec.android.app.easylauncher",
            "com.samsung.android.incallui"
        )

        val OEM_SMS_PACKAGES = setOf(
            "com.google.android.apps.messaging",
            "com.android.mms",
            "com.samsung.android.messaging"
        )

        val OEM_LAUNCHER_PACKAGES = setOf(
            "com.google.android.apps.nexuslauncher",
            "com.sec.android.app.launcher",
            "com.miui.home",
            "com.huawei.android.launcher",
            "com.oppo.launcher",
            "com.bbk.launcher2"
        )
    }
}
