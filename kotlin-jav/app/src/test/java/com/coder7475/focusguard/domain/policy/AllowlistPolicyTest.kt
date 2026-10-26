package com.coder7475.focusguard.domain.policy

import com.coder7475.focusguard.domain.model.AccessDecision
import com.coder7475.focusguard.platform.role.SystemRoleResolver
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AllowlistPolicyTest {

    private val systemRoleResolver: SystemRoleResolver = mockk()
    private val ownPackage = "com.coder7475.focusguard"
    private lateinit var policy: AllowlistPolicy

    @Before
    fun setUp() {
        every { systemRoleResolver.getDefaultDialerPackage() } returns "com.custom.dialer"
        every { systemRoleResolver.getDefaultSmsPackage() } returns "com.custom.sms"
        every { systemRoleResolver.getDefaultLauncherPackage() } returns "com.custom.launcher"
        policy = AllowlistPolicy(systemRoleResolver, ownPackage)
    }

    @Test
    fun `own package is always allowed`() {
        val decision = policy.evaluateAccess(ownPackage)
        assertEquals(AccessDecision.ALLOWED, decision)
    }

    @Test
    fun `default dialer resolved at runtime is allowed`() {
        val decision = policy.evaluateAccess("com.custom.dialer")
        assertEquals(AccessDecision.ALLOWED, decision)
    }

    @Test
    fun `default SMS app resolved at runtime is allowed`() {
        val decision = policy.evaluateAccess("com.custom.sms")
        assertEquals(AccessDecision.ALLOWED, decision)
    }

    @Test
    fun `default launcher resolved at runtime is allowed`() {
        val decision = policy.evaluateAccess("com.custom.launcher")
        assertEquals(AccessDecision.ALLOWED, decision)
    }

    @Test
    fun `known OEM dialer packages are allowed as fallbacks`() {
        for (dialer in SystemRoleResolver.OEM_DIALER_PACKAGES) {
            val decision = policy.evaluateAccess(dialer)
            assertEquals("Expected $dialer to be allowed", AccessDecision.ALLOWED, decision)
        }
    }

    @Test
    fun `known OEM emergency packages are allowed as fallbacks`() {
        for (emergency in SystemRoleResolver.OEM_EMERGENCY_PACKAGES) {
            val decision = policy.evaluateAccess(emergency)
            assertEquals("Expected $emergency to be allowed", AccessDecision.ALLOWED, decision)
        }
    }

    @Test
    fun `known OEM SMS packages are allowed as fallbacks`() {
        for (sms in SystemRoleResolver.OEM_SMS_PACKAGES) {
            val decision = policy.evaluateAccess(sms)
            assertEquals("Expected $sms to be allowed", AccessDecision.ALLOWED, decision)
        }
    }

    @Test
    fun `known OEM launcher packages are allowed as fallbacks`() {
        for (launcher in SystemRoleResolver.OEM_LAUNCHER_PACKAGES) {
            val decision = policy.evaluateAccess(launcher)
            assertEquals("Expected $launcher to be allowed", AccessDecision.ALLOWED, decision)
        }
    }

    @Test
    fun `null or empty package names return ALLOWED to avoid blocking system transitions`() {
        assertEquals(AccessDecision.ALLOWED, policy.evaluateAccess(null))
        assertEquals(AccessDecision.ALLOWED, policy.evaluateAccess(""))
        assertEquals(AccessDecision.ALLOWED, policy.evaluateAccess("   "))
    }

    @Test
    fun `distracting third-party apps are blocked`() {
        val blockedPackages = listOf(
            "com.instagram.android",
            "com.zhiliaoapp.musically",
            "com.facebook.katana",
            "com.twitter.android",
            "com.google.android.youtube",
            "com.netflix.mediaclient"
        )
        for (pkg in blockedPackages) {
            val decision = policy.evaluateAccess(pkg)
            assertEquals("Expected $pkg to be blocked", AccessDecision.BLOCKED, decision)
        }
    }
}
