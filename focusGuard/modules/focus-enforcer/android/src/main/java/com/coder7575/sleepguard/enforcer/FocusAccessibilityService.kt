package com.coder7575.sleepguard.enforcer

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {
    private lateinit var overlayManager: LockOverlayWindowManager
    private lateinit var roleResolver: SystemRoleResolver
    private lateinit var dbHelper: NativeDbHelper

    private var lastTargetPackage: String? = null

    override fun onCreate() {
        super.onCreate()
        overlayManager = LockOverlayWindowManager(this)
        roleResolver = SystemRoleResolver(this)
        dbHelper = NativeDbHelper(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val targetPkg = event.packageName?.toString()
        if (targetPkg.isNullOrBlank()) {
            return
        }

        // Avoid re-evaluating unchanged package states when overlay is already showing
        if (targetPkg == lastTargetPackage && overlayManager.isVisible) {
            return
        }
        lastTargetPackage = targetPkg

        // Check if package is allowed (own app, dialer, sms, emergency, launcher)
        if (roleResolver.isAllowlisted(targetPkg)) {
            overlayManager.hideOverlay()
            return
        }

        // 1. Check in-memory active session state first
        var activeTarget = activeSessionEndsAt
        if (activeTarget <= System.currentTimeMillis()) {
            // 2. Fallback to persisted database state
            activeTarget = dbHelper.getActiveSessionEndsAt()
            activeSessionEndsAt = activeTarget
        }

        if (activeTarget > System.currentTimeMillis()) {
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
        overlayManager.hideOverlay()
        super.onDestroy()
    }

    companion object {
        @Volatile
        var activeSessionEndsAt: Long = 0L
    }
}
