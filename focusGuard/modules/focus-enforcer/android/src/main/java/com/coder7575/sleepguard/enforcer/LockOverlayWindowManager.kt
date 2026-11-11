package com.coder7575.sleepguard.enforcer

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
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

    private fun dp(value: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            context.resources.displayMetrics
        ).toInt()
    }

    fun setEndsAtEpochMs(epochMs: Long) {
        endsAtEpochMs = epochMs
    }

    @Synchronized
    fun showOverlay() {
        if (isVisible || overlayView != null) return

        val remainingNow = endsAtEpochMs - System.currentTimeMillis()
        if (remainingNow <= 0L) {
            hideOverlay()
            return
        }

        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#121316"))
            val pad = dp(24f)
            setPadding(pad, pad, pad, pad)
        }

        val cardDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(20f).toFloat()
            setColor(Color.parseColor("#1C1E24"))
            setStroke(dp(1f), Color.parseColor("#2A2E39"))
        }

        val cardLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = cardDrawable
            val padCardH = dp(24f)
            val padCardV = dp(32f)
            setPadding(padCardH, padCardV, padCardH, padCardV)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val iconView = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_lock_idle_lock)
            setColorFilter(Color.parseColor("#FF5722"))
            val iconSize = dp(44f)
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
                bottomMargin = dp(16f)
            }
        }

        val titleView = TextView(context).apply {
            text = "FOCUSGUARD LOCKED"
            textSize = 18f
            setTextColor(Color.parseColor("#FF5722"))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val subtitleView = TextView(context).apply {
            text = "Discipline mode is active"
            textSize = 13f
            setTextColor(Color.parseColor("#8E95A2"))
            gravity = Gravity.CENTER
            setPadding(0, dp(4f), 0, dp(16f))
        }

        val countdownView = TextView(context).apply {
            text = formatRemainingTime()
            textSize = 38f
            setTextColor(Color.parseColor("#E6F4FE"))
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(24f))
        }

        val buttonDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(14f).toFloat()
            setColor(Color.parseColor("#FF3366"))
        }

        val emergencyButton = Button(context).apply {
            text = "EMERGENCY CALL"
            textSize = 14f
            background = buttonDrawable
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            val btnH = dp(48f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                btnH
            )
            setOnClickListener {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
                hideOverlay()
            }
        }

        cardLayout.addView(iconView)
        cardLayout.addView(titleView)
        cardLayout.addView(subtitleView)
        cardLayout.addView(countdownView)

        // Bag Slot Packed App Launcher
        val currentBagPkg = bagAppPackage
        val currentBagLabel = bagAppName
        if (!currentBagPkg.isNullOrBlank() && !currentBagLabel.isNullOrBlank()) {
            val bagButtonDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(14f).toFloat()
                setColor(Color.parseColor("#00E5FF"))
            }

            val bagButton = Button(context).apply {
                text = "OPEN ${currentBagLabel.uppercase()}"
                textSize = 14f
                background = bagButtonDrawable
                setTextColor(Color.parseColor("#121316"))
                typeface = Typeface.DEFAULT_BOLD
                val btnH = dp(48f)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    btnH
                ).apply {
                    bottomMargin = dp(12f)
                }
                setOnClickListener {
                    try {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage(currentBagPkg)?.apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        if (launchIntent != null) {
                            context.startActivity(launchIntent)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to launch bag app", e)
                    }
                    hideOverlay()
                }
            }
            cardLayout.addView(bagButton)
        }

        cardLayout.addView(emergencyButton)
        rootLayout.addView(cardLayout)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.FILL
        }

        try {
            windowManager.addView(rootLayout, params)
            overlayView = rootLayout
            isVisible = true
            Log.d(TAG, "Lock overlay successfully displayed")

            // Live 1-second countdown ticker with auto-dismiss on expiration
            timerRunnable = object : Runnable {
                override fun run() {
                    if (isVisible) {
                        val remaining = endsAtEpochMs - System.currentTimeMillis()
                        if (remaining <= 0L) {
                            hideOverlay()
                            return
                        }
                        countdownView.text = formatRemainingTime()
                        handler.postDelayed(this, 1000L)
                    }
                }
            }
            handler.post(timerRunnable!!)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to display lock overlay view", e)
            overlayView = null
            isVisible = false
        }
    }

    @Synchronized
    fun hideOverlay() {
        if (!isVisible && overlayView == null) return
        try {
            timerRunnable?.let { handler.removeCallbacks(it) }
            overlayView?.let { windowManager.removeView(it) }
            Log.d(TAG, "Lock overlay hidden")
        } catch (e: Exception) {
            Log.e(TAG, "Error while removing lock overlay view", e)
        } finally {
            overlayView = null
            isVisible = false
        }
    }

    private fun formatRemainingTime(): String {
        val remaining = Math.max(0L, endsAtEpochMs - System.currentTimeMillis())
        val s = (remaining / 1000L) % 60
        val m = (remaining / (1000L * 60)) % 60
        val h = remaining / (1000L * 3600)
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    companion object {
        private const val TAG = "LockOverlayManager"

        @Volatile
        var bagAppPackage: String? = null

        @Volatile
        var bagAppName: String? = null
    }
}
