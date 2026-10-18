package com.coder7475.focusguard.service.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.coder7475.focusguard.ui.theme.FocusGuardTheme

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

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.FILL
        }
    }

    @Synchronized
    open fun attach() {
        if (isAttached && overlayView != null) return

        try {
            lifecycleOwner.onCreate()

            val composeView = ComposeView(context).apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner)
                )
                lifecycleOwner.attachToDecor(this)
                setContent {
                    FocusGuardTheme {
                        LockOverlayScreen(
                            endsAtEpochMsProvider = { endsAtEpochMs },
                            onEmergencyClick = onEmergencyClick
                        )
                    }
                }
            }

            overlayView = composeView
            isAttached = true
            lifecycleOwner.onStart()
            lifecycleOwner.onResume()
            Log.d(TAG, "Lock overlay view created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize lock overlay view", e)
        }
    }

    @Synchronized
    open fun showOverlay() {
        if (!isAttached || overlayView == null) {
            attach()
        }

        val view = overlayView ?: return
        if (!isVisible) {
            try {
                windowManager.addView(view, createLayoutParams())
                isVisible = true
                Log.d(TAG, "Lock overlay added to WindowManager (VISIBLE)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add overlay view to WindowManager", e)
            }
        }
    }

    @Synchronized
    open fun hideOverlay() {
        val view = overlayView ?: return
        if (isVisible) {
            try {
                windowManager.removeView(view)
                isVisible = false
                Log.d(TAG, "Lock overlay removed from WindowManager (HIDDEN)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove overlay view from WindowManager", e)
            }
        }
    }

    @Synchronized
    open fun destroy() {
        if (isVisible && overlayView != null) {
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove lock overlay view during destroy", e)
            }
        }

        try {
            lifecycleOwner.onPause()
            lifecycleOwner.onStop()
            lifecycleOwner.onDestroy()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clean up lifecycle owner", e)
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
