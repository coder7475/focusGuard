package com.coder7475.focusguard.platform.permission

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class PermissionCheckerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    private class FakePermissionChecker(
        context: Context,
        private val grantedMap: MutableMap<FocusPermission, Boolean> = mutableMapOf()
    ) : PermissionChecker(context) {

        fun setGranted(permission: FocusPermission, granted: Boolean) {
            grantedMap[permission] = granted
        }

        override fun isGranted(permission: FocusPermission): Boolean {
            return grantedMap[permission] ?: false
        }
    }

    @Test
    fun permissionTierAssignmentsAreCorrect() {
        assertEquals(PermissionTier.CORE_ENFORCERS, FocusPermission.ACCESSIBILITY.tier)
        assertEquals(PermissionTier.CORE_ENFORCERS, FocusPermission.NOTIFICATIONS.tier)
        assertEquals(PermissionTier.CORE_ENFORCERS, FocusPermission.EXACT_ALARM.tier)

        assertEquals(PermissionTier.BACKGROUND_DURABILITY, FocusPermission.BATTERY_OPTIMIZATION.tier)
        assertEquals(PermissionTier.BACKGROUND_DURABILITY, FocusPermission.BOOT_COMPLETED.tier)
        assertEquals(PermissionTier.BACKGROUND_DURABILITY, FocusPermission.USAGE_STATS.tier)
        assertEquals(PermissionTier.BACKGROUND_DURABILITY, FocusPermission.FULL_SCREEN_INTENT.tier)
    }

    @Test
    fun sequentialResolutionSelectsHighestPriorityUngrantedPermission() {
        val fakeChecker = FakePermissionChecker(context)

        // Initial state: nothing granted -> Accessibility is first priority
        assertEquals(FocusPermission.ACCESSIBILITY, fakeChecker.getNextUngrantedMandatoryPermission())

        // Grant Accessibility -> Notifications is next
        fakeChecker.setGranted(FocusPermission.ACCESSIBILITY, true)
        assertEquals(FocusPermission.NOTIFICATIONS, fakeChecker.getNextUngrantedMandatoryPermission())

        // Grant Notifications -> Exact Alarms is next
        fakeChecker.setGranted(FocusPermission.NOTIFICATIONS, true)
        assertEquals(FocusPermission.EXACT_ALARM, fakeChecker.getNextUngrantedMandatoryPermission())

        // Grant Exact Alarm -> Battery Optimization is next
        fakeChecker.setGranted(FocusPermission.EXACT_ALARM, true)
        assertEquals(FocusPermission.BATTERY_OPTIMIZATION, fakeChecker.getNextUngrantedMandatoryPermission())

        // Grant Battery Optimization -> Boot Completed is next
        fakeChecker.setGranted(FocusPermission.BATTERY_OPTIMIZATION, true)
        assertEquals(FocusPermission.BOOT_COMPLETED, fakeChecker.getNextUngrantedMandatoryPermission())

        // Grant Boot Completed -> All mandatory granted, next is null
        fakeChecker.setGranted(FocusPermission.BOOT_COMPLETED, true)
        assertNull(fakeChecker.getNextUngrantedMandatoryPermission())
        assertTrue(fakeChecker.areAllMandatoryGranted())
    }

    @Test
    fun areAllMandatoryGrantedReturnsFalseWhenAnyMandatoryIsMissing() {
        val fakeChecker = FakePermissionChecker(context)
        fakeChecker.setGranted(FocusPermission.ACCESSIBILITY, true)
        fakeChecker.setGranted(FocusPermission.NOTIFICATIONS, true)
        fakeChecker.setGranted(FocusPermission.EXACT_ALARM, true)
        fakeChecker.setGranted(FocusPermission.BATTERY_OPTIMIZATION, true)
        fakeChecker.setGranted(FocusPermission.BOOT_COMPLETED, false)

        assertFalse(fakeChecker.areAllMandatoryGranted())
        assertEquals(FocusPermission.BOOT_COMPLETED, fakeChecker.getNextUngrantedMandatoryPermission())
    }
}
