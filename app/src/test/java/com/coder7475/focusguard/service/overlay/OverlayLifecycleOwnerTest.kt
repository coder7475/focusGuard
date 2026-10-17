package com.coder7475.focusguard.service.overlay

import android.os.Looper
import android.view.View
import androidx.lifecycle.Lifecycle
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OverlayLifecycleOwnerTest {

    @Before
    fun setUp() {
        mockkStatic(Looper::class)
        val mockLooper = mockk<Looper>(relaxed = true)
        every { mockLooper.thread } returns Thread.currentThread()
        every { Looper.getMainLooper() } returns mockLooper
    }

    @After
    fun tearDown() {
        unmockkStatic(Looper::class)
    }

    @Test
    fun `lifecycleOwner transitions through expected states`() {
        val owner = OverlayLifecycleOwner()
        assertEquals(Lifecycle.State.INITIALIZED, owner.lifecycle.currentState)

        owner.onCreate()
        assertEquals(Lifecycle.State.CREATED, owner.lifecycle.currentState)

        owner.onStart()
        assertEquals(Lifecycle.State.STARTED, owner.lifecycle.currentState)

        owner.onResume()
        assertEquals(Lifecycle.State.RESUMED, owner.lifecycle.currentState)

        owner.onPause()
        assertEquals(Lifecycle.State.STARTED, owner.lifecycle.currentState)

        owner.onStop()
        assertEquals(Lifecycle.State.CREATED, owner.lifecycle.currentState)

        owner.onDestroy()
        assertEquals(Lifecycle.State.DESTROYED, owner.lifecycle.currentState)
    }

    @Test
    fun `savedStateRegistry is initialized and active`() {
        val owner = OverlayLifecycleOwner()
        owner.onCreate()
        assertNotNull(owner.savedStateRegistry)
        assertTrue(owner.savedStateRegistry.isRestored)
    }

    @Test
    fun `attachToDecor sets tags on view`() {
        val mockView = mockk<View>(relaxed = true)
        val owner = OverlayLifecycleOwner()
        owner.onCreate()

        owner.attachToDecor(mockView)

        verify(atLeast = 3) { mockView.setTag(any(), any()) }
    }
}
