package com.coder7475.focusguard.service.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView

open class LockOverlayWindowManager(
    private val context: Context,
    private val onEmergencyClick: () -> Unit = {}
) {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val lifecycleOwner = OverlayLifecycleOwner()
    private var overlayView: ComposeView? = null
    var isAttached: Boolean = false
        private set
    var isVisible: Boolean = false
        private set

    private var endsAtEpochMs: Long = 0L

    fun setEndsAtEpochMs(epochMs: Long) {
        endsAtEpochMs = epochMs
    }

    @Synchronized
    open fun attach() {
        if (isAttached) return

        try {
            val flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                flags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.FILL
            }

            lifecycleOwner.onCreate()

            val composeView = ComposeView(context).apply {
                lifecycleOwner.attachToDecor(this)
                setContent {
                    LockOverlayScreen(
                        endsAtEpochMsProvider = { endsAtEpochMs },
                        onEmergencyClick = onEmergencyClick
                    )
                }
                visibility = View.GONE
            }

            windowManager.addView(composeView, params)
            overlayView = composeView
            isAttached = true
            isVisible = false

            lifecycleOwner.onStart()
            lifecycleOwner.onResume()
            Log.d(TAG, "Lock overlay successfully attached to WindowManager")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach lock overlay window", e)
        }
    }

    @Synchronized
    open fun showOverlay() {
        if (!isAttached) {
            attach()
        }
        overlayView?.let { view ->
            if (view.visibility != View.VISIBLE) {
                view.visibility = View.VISIBLE
                isVisible = true
                Log.d(TAG, "Lock overlay shown")
            }
        }
    }

    @Synchronized
    open fun hideOverlay() {
        overlayView?.let { view ->
            if (view.visibility != View.GONE) {
                view.visibility = View.GONE
                isVisible = false
                Log.d(TAG, "Lock overlay hidden")
            }
        }
    }

    @Synchronized
    open fun destroy() {
        if (!isAttached) return

        try {
            lifecycleOwner.onPause()
            lifecycleOwner.onStop()
            lifecycleOwner.onDestroy()

            overlayView?.let {
                windowManager.removeView(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove lock overlay view", e)
        } finally {
            overlayView = null
            isAttached = false
            isVisible = false
            Log.d(TAG, "Lock overlay destroyed")
        }
    }

    companion object {
        private const val TAG = "LockOverlayManager"
    }
}
