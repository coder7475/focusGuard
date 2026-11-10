package com.coder7575.sleepguard.enforcer

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {
    private lateinit var overlayManager: LockOverlayWindowManager
    private lateinit var roleResolver: SystemRoleResolver
    private lateinit var dbHelper: NativeDbHelper

    private var lastTargetPackage: String? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        overlayManager = LockOverlayWindowManager(this)
        roleResolver = SystemRoleResolver(this)
        dbHelper = NativeDbHelper(this)

        // Initial check on service start
        val dbTarget = dbHelper.getActiveSessionEndsAt()
        if (dbTarget > System.currentTimeMillis()) {
            activeSessionEndsAt = dbTarget
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val targetPkg = event.packageName?.toString()
        if (targetPkg.isNullOrBlank()) {
            return
        }

        // 1. Prevent self-dismissal when overlay window itself fires window events
        if (targetPkg == packageName) {
            val className = event.className?.toString()
            val isMainActivity = className != null && (
                className == "$packageName.MainActivity" ||
                className.endsWith(".MainActivity")
            )
            if (!isMainActivity) {
                // Event originates from our overlay layout/window, NOT MainActivity. Drop to prevent loop.
                return
            }
        }

        // Avoid re-evaluating unchanged package states when overlay is already showing AND session is active
        val now = System.currentTimeMillis()
        if (targetPkg == lastTargetPackage && overlayManager.isVisible && activeSessionEndsAt > now) {
            return
        }
        lastTargetPackage = targetPkg

        // 2. Ignore transient system surfaces (e.g. status bar, notification shade, volume dialogs)
        if (roleResolver.isSystemUi(targetPkg)) {
            return
        }

        // Check if package is allowed (own app MainActivity, dialer, sms, emergency, launcher)
        if (roleResolver.isAllowlisted(targetPkg)) {
            overlayManager.hideOverlay()
            return
        }

        // 1. Check in-memory active session state first
        var activeTarget = activeSessionEndsAt
        if (activeTarget <= now) {
            // 2. Fallback to persisted database state throttled by cooldown to prevent disk churn during normal use
            if (now - lastDbCheckEpochMs > DB_RECHECK_COOLDOWN_MS) {
                lastDbCheckEpochMs = now
                val dbTarget = dbHelper.getActiveSessionEndsAt()
                if (dbTarget > now) {
                    activeTarget = dbTarget
                    activeSessionEndsAt = dbTarget
                }
            }
        }

        if (activeTarget > now) {
            overlayManager.setEndsAtEpochMs(activeTarget)
            overlayManager.showOverlay()
        } else {
            overlayManager.hideOverlay()
        }
    }

    override fun onInterrupt() {
        overlayManager.hideOverlay()
    }

    override fun onDestroy() {
        if (instance == this) {
            instance = null
        }
        overlayManager.hideOverlay()
        super.onDestroy()
    }

    companion object {
        @Volatile
        var activeSessionEndsAt: Long = 0L

        @Volatile
        var lastDbCheckEpochMs: Long = 0L

        private const val DB_RECHECK_COOLDOWN_MS = 30_000L

        @Volatile
        var instance: FocusAccessibilityService? = null

        fun clearActiveSession() {
            activeSessionEndsAt = 0L
            lastDbCheckEpochMs = 0L
            instance?.let { service ->
                Handler(Looper.getMainLooper()).post {
                    service.overlayManager.hideOverlay()
                    service.lastTargetPackage = null
                }
            }
        }
    }
}
