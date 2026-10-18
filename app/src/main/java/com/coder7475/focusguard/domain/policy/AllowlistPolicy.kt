package com.coder7475.focusguard.domain.policy

import com.coder7475.focusguard.domain.model.AccessDecision
import com.coder7475.focusguard.platform.role.SystemRoleResolver

class AllowlistPolicy(
    private val systemRoleResolver: SystemRoleResolver,
    private val ownPackageName: String = "com.coder7475.focusguard"
) {

    fun evaluateAccess(packageName: String?): AccessDecision {
        if (packageName.isNullOrBlank()) {
            return AccessDecision.ALLOWED
        }

        // 1. Own package is always allowed
        if (packageName == ownPackageName) {
            return AccessDecision.ALLOWED
        }

        // 2. Resolved default dialer
        val defaultDialer = systemRoleResolver.getDefaultDialerPackage()
        if (!defaultDialer.isNullOrBlank() && packageName == defaultDialer) {
            return AccessDecision.ALLOWED
        }

        // 3. Resolved default SMS
        val defaultSms = systemRoleResolver.getDefaultSmsPackage()
        if (!defaultSms.isNullOrBlank() && packageName == defaultSms) {
            return AccessDecision.ALLOWED
        }

        // 4. Fallback OEM dialers
        if (SystemRoleResolver.OEM_DIALER_PACKAGES.contains(packageName)) {
            return AccessDecision.ALLOWED
        }

        // 5. Fallback OEM emergency dialers & phone services
        if (SystemRoleResolver.OEM_EMERGENCY_PACKAGES.contains(packageName)) {
            return AccessDecision.ALLOWED
        }

        // 6. Fallback OEM SMS apps
        if (SystemRoleResolver.OEM_SMS_PACKAGES.contains(packageName)) {
            return AccessDecision.ALLOWED
        }

        // 7. Resolved default home launcher
        val defaultLauncher = systemRoleResolver.getDefaultLauncherPackage()
        if (!defaultLauncher.isNullOrBlank() && packageName == defaultLauncher) {
            return AccessDecision.ALLOWED
        }

        // 8. Fallback OEM launchers
        if (SystemRoleResolver.OEM_LAUNCHER_PACKAGES.contains(packageName)) {
            return AccessDecision.ALLOWED
        }

        return AccessDecision.BLOCKED
    }
}
