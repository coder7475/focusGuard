package com.coder7475.focusguard.service.accessibility

import android.view.accessibility.AccessibilityEvent
import com.coder7475.focusguard.MainActivity
import com.coder7475.focusguard.domain.model.AccessDecision
import com.coder7475.focusguard.domain.model.FocusSession
import com.coder7475.focusguard.domain.model.SessionStatus
import com.coder7475.focusguard.domain.model.SessionType
import com.coder7475.focusguard.domain.policy.AllowlistPolicy
import com.coder7475.focusguard.service.overlay.LockOverlayWindowManager
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class FocusGuardAccessibilityServiceTest {

    private lateinit var service: FocusGuardAccessibilityService
    private val overlayManager: LockOverlayWindowManager = mockk(relaxed = true)
    private val allowlistPolicy: AllowlistPolicy = mockk(relaxed = true)

    private val activeSession = FocusSession(
        id = "session-1",
        type = SessionType.QUICK,
        status = SessionStatus.ACTIVE,
        startTimeEpochMs = System.currentTimeMillis() - 1000L,
        endsAtEpochMs = System.currentTimeMillis() + 60_000L,
        durationMs = 60_000L
    )

    @Before
    fun setUp() {
        service = Robolectric.buildService(FocusGuardAccessibilityService::class.java).get()
        service.overlayManager = overlayManager
        service.allowlistPolicy = allowlistPolicy
        service.currentActiveSession = activeSession
    }

    @Test
    fun `ignore TYPE_WINDOWS_CHANGED events to prevent oscillation feedback loop`() {
        val event = mockk<AccessibilityEvent>(relaxed = true)
        every { event.eventType } returns AccessibilityEvent.TYPE_WINDOWS_CHANGED
        every { event.packageName } returns null

        service.onAccessibilityEvent(event)

        verify(exactly = 0) { allowlistPolicy.evaluateAccess(any()) }
        verify(exactly = 0) { overlayManager.showOverlay() }
        verify(exactly = 0) { overlayManager.hideOverlay() }
    }

    @Test
    fun `ignore null or blank package names on TYPE_WINDOW_STATE_CHANGED`() {
        val eventNull = mockk<AccessibilityEvent>(relaxed = true)
        every { eventNull.eventType } returns AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        every { eventNull.packageName } returns null

        service.onAccessibilityEvent(eventNull)

        val eventBlank = mockk<AccessibilityEvent>(relaxed = true)
        every { eventBlank.eventType } returns AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        every { eventBlank.packageName } returns "   "

        service.onAccessibilityEvent(eventBlank)

        verify(exactly = 0) { allowlistPolicy.evaluateAccess(any()) }
        verify(exactly = 0) { overlayManager.showOverlay() }
        verify(exactly = 0) { overlayManager.hideOverlay() }
    }

    @Test
    fun `ignore own package events from overlay compose view and do not dismiss overlay`() {
        val event = mockk<AccessibilityEvent>(relaxed = true)
        every { event.eventType } returns AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        every { event.packageName } returns service.packageName
        every { event.className } returns "androidx.compose.ui.platform.ComposeView"

        service.onAccessibilityEvent(event)

        verify(exactly = 0) { allowlistPolicy.evaluateAccess(any()) }
        verify(exactly = 0) { overlayManager.hideOverlay() }
    }

    @Test
    fun `permit own package MainActivity and dismiss overlay`() {
        every { allowlistPolicy.evaluateAccess(service.packageName) } returns AccessDecision.ALLOWED

        val event = mockk<AccessibilityEvent>(relaxed = true)
        every { event.eventType } returns AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        every { event.packageName } returns service.packageName
        every { event.className } returns MainActivity::class.java.name

        service.onAccessibilityEvent(event)

        verify { overlayManager.hideOverlay() }
    }

    @Test
    fun `show overlay when non-allowlisted package is opened during active session`() {
        every { allowlistPolicy.evaluateAccess("com.google.android.youtube") } returns AccessDecision.BLOCKED

        val event = mockk<AccessibilityEvent>(relaxed = true)
        every { event.eventType } returns AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        every { event.packageName } returns "com.google.android.youtube"
        every { event.className } returns "com.google.android.youtube.HomeActivity"

        service.onAccessibilityEvent(event)

        verify { overlayManager.showOverlay() }
    }

    @Test
    fun `debounces identical package transitions when overlay is already visible`() {
        every { allowlistPolicy.evaluateAccess("com.google.android.youtube") } returns AccessDecision.BLOCKED
        every { overlayManager.isVisible } returns false

        val event = mockk<AccessibilityEvent>(relaxed = true)
        every { event.eventType } returns AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        every { event.packageName } returns "com.google.android.youtube"
        every { event.className } returns "com.google.android.youtube.HomeActivity"

        service.onAccessibilityEvent(event)
        verify(exactly = 1) { overlayManager.showOverlay() }

        // Second identical event while overlay is visible
        every { overlayManager.isVisible } returns true
        clearMocks(overlayManager, answers = false)

        service.onAccessibilityEvent(event)
        verify(exactly = 0) { overlayManager.showOverlay() }
        verify(exactly = 0) { overlayManager.hideOverlay() }
    }
}
