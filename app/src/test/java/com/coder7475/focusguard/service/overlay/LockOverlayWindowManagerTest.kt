package com.coder7475.focusguard.service.overlay

import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class LockOverlayWindowManagerTest {

    private lateinit var baseContext: Context
    private val mockWindowManager: WindowManager = mockk(relaxed = true)
    private lateinit var overlayWindowManager: LockOverlayWindowManager

    @Before
    fun setUp() {
        baseContext = ApplicationProvider.getApplicationContext()
        val wrappedContext = object : ContextWrapper(baseContext) {
            override fun getSystemService(name: String): Any {
                if (name == Context.WINDOW_SERVICE) {
                    return mockWindowManager
                }
                return super.getSystemService(name)
            }
        }
        overlayWindowManager = LockOverlayWindowManager(wrappedContext)
    }

    @Test
    fun `attach sets modal flags without FLAG_NOT_TOUCH_MODAL`() {
        val paramsSlot = slot<WindowManager.LayoutParams>()
        every { mockWindowManager.addView(any(), capture(paramsSlot)) } returns Unit

        overlayWindowManager.attach()

        assertTrue(overlayWindowManager.isAttached)
        assertFalse(overlayWindowManager.isVisible)

        val capturedParams = paramsSlot.captured
        val flags = capturedParams.flags

        val hasNotTouchModal = (flags and WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL) != 0
        assertFalse("FLAG_NOT_TOUCH_MODAL must not be set to ensure modal barrier", hasNotTouchModal)

        assertTrue(
            "FLAG_LAYOUT_IN_SCREEN should be present",
            (flags and WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN) != 0
        )
        assertTrue(
            "FLAG_LAYOUT_NO_LIMITS should be present",
            (flags and WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) != 0
        )
        @Suppress("DEPRECATION")
        assertTrue(
            "FLAG_SHOW_WHEN_LOCKED should be present",
            (flags and WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED) != 0
        )
        assertTrue(
            "FLAG_KEEP_SCREEN_ON should be present",
            (flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
        )
        assertEquals(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, capturedParams.type)
    }

    @Test
    fun `showOverlay attaches if needed and updates visibility`() {
        overlayWindowManager.attach()
        overlayWindowManager.showOverlay()
        assertTrue(overlayWindowManager.isVisible)

        overlayWindowManager.hideOverlay()
        assertFalse(overlayWindowManager.isVisible)
    }

    @Test
    fun `destroy cleans up views and marks unattached`() {
        overlayWindowManager.attach()
        overlayWindowManager.destroy()
        assertFalse(overlayWindowManager.isAttached)
        assertFalse(overlayWindowManager.isVisible)
        verify { mockWindowManager.removeView(any()) }
    }
}
