package com.coder7575.sleepguard.enforcer

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class LockOverlayWindowManager(private val context: Context) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    var isVisible = false
        private set

    private var endsAtEpochMs: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    fun setEndsAtEpochMs(epochMs: Long) {
        endsAtEpochMs = epochMs
    }

    fun showOverlay() {
        if (isVisible) return

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#121316"))
            setPadding(48, 48, 48, 48)
        }

        val titleView = TextView(context).apply {
            text = "🔒 SLEEPGUARD ACTIVE"
            textSize = 22f
            setTextColor(Color.parseColor("#FF5722"))
            gravity = Gravity.CENTER
        }

        val countdownView = TextView(context).apply {
            text = formatRemainingTime()
            textSize = 40f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 48)
        }

        val emergencyButton = Button(context).apply {
            text = "EMERGENCY CALL"
            setBackgroundColor(Color.parseColor("#FF3366"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
                hideOverlay()
            }
        }

        layout.addView(titleView)
        layout.addView(countdownView)
        layout.addView(emergencyButton)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.FILL
        }

        try {
            windowManager.addView(layout, params)
            overlayView = layout
            isVisible = true

            // Live 1-second countdown ticker
            timerRunnable = object : Runnable {
                override fun run() {
                    if (isVisible) {
                        countdownView.text = formatRemainingTime()
                        handler.postDelayed(this, 1000L)
                    }
                }
            }
            handler.post(timerRunnable!!)
        } catch (_: Exception) {}
    }

    fun hideOverlay() {
        if (!isVisible || overlayView == null) return
        try {
            timerRunnable?.let { handler.removeCallbacks(it) }
            windowManager.removeView(overlayView)
        } catch (_: Exception) {}
        overlayView = null
        isVisible = false
    }

    private fun formatRemainingTime(): String {
        val remaining = Math.max(0L, endsAtEpochMs - System.currentTimeMillis())
        val s = (remaining / 1000L) % 60
        val m = (remaining / (1000L * 60)) % 60
        val h = remaining / (1000L * 3600)
        return String.format("%02d:%02d:%02d", h, m, s)
    }
}
