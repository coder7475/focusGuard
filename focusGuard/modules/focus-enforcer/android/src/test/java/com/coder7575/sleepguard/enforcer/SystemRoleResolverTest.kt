package com.coder7575.sleepguard.enforcer

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemRoleResolverTest {

    @Test
    fun testMiuiHomeIsLauncherAndNotDialer() {
        // Verification for Audit Finding 2.3:
        // com.miui.home was misclassified as a dialer. It must be an OEM launcher, not dialer.
        assertTrue(
            "com.miui.home must be listed in OEM_LAUNCHER_PACKAGES",
            SystemRoleResolver.OEM_LAUNCHER_PACKAGES.contains("com.miui.home")
        )
        assertFalse(
            "com.miui.home must NOT be listed in OEM_DIALER_PACKAGES",
            SystemRoleResolver.OEM_DIALER_PACKAGES.contains("com.miui.home")
        )
    }

    @Test
    fun testKnownDialerPackagesIntegrity() {
        assertTrue(SystemRoleResolver.OEM_DIALER_PACKAGES.contains("com.google.android.dialer"))
        assertTrue(SystemRoleResolver.OEM_DIALER_PACKAGES.contains("com.samsung.android.dialer"))
        assertTrue(SystemRoleResolver.OEM_DIALER_PACKAGES.contains("com.oneplus.dialer"))
    }

    @Test
    fun testEmergencyPackagesIntegrity() {
        assertTrue(SystemRoleResolver.OEM_EMERGENCY_PACKAGES.contains("com.android.phone.EmergencyDialer"))
        assertTrue(SystemRoleResolver.OEM_EMERGENCY_PACKAGES.contains("com.android.phone"))
    }
}
