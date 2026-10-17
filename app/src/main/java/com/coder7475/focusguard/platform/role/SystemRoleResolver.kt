package com.coder7475.focusguard.platform.role

import android.content.Context
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

    companion object {
        val OEM_DIALER_PACKAGES = setOf(
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.android.server.telecom",
            "com.android.incallui",
            "com.google.android.incallui",
            "com.oneplus.dialer",
            "com.miui.home" // Some OEMs route via launcher/dialer package
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
    }
}
