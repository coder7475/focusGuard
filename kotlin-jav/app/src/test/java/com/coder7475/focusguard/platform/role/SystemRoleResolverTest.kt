package com.coder7475.focusguard.platform.role

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.telecom.TelecomManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class SystemRoleResolverTest {

    private val context: Context = mockk()
    private val packageManager: PackageManager = mockk()
    private val telecomManager: TelecomManager = mockk()
    private lateinit var resolver: SystemRoleResolver

    @Before
    fun setUp() {
        every { context.getSystemService(Context.TELECOM_SERVICE) } returns telecomManager
        every { context.packageManager } returns packageManager
        resolver = SystemRoleResolver(context)
    }

    @Test
    fun `getDefaultDialerPackage returns system dialer`() {
        every { telecomManager.defaultDialerPackage } returns "com.google.android.dialer"
        assertEquals("com.google.android.dialer", resolver.getDefaultDialerPackage())
    }

    @Test
    fun `getDefaultLauncherPackage returns resolved home launcher`() {
        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "com.google.android.apps.nexuslauncher"
            }
        }
        every {
            packageManager.resolveActivity(any(), any<Int>())
        } returns resolveInfo

        assertEquals("com.google.android.apps.nexuslauncher", resolver.getDefaultLauncherPackage())
    }

    @Test
    fun `getDefaultLauncherPackage returns null when android resolver activity is returned`() {
        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "android"
            }
        }
        every {
            packageManager.resolveActivity(any(), any<Int>())
        } returns resolveInfo

        assertNull(resolver.getDefaultLauncherPackage())
    }
}
