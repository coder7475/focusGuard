package com.coder7575.sleepguard.enforcer

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {
    private lateinit var overlayManager: LockOverlayWindowManager
    private lateinit var roleResolver: SystemRoleResolver
    private lateinit var dbHelper: NativeDbHelper

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

        val targetPkg = event.packageName?.toString() ?: return

        // Check if package is allowed (own app, dialer, sms, emergency, launcher)
        if (roleResolver.isAllowlisted(targetPkg)) {
            overlayManager.hideOverlay()
            return
        }

        // Query active session from SQLite
        val activeTarget = dbHelper.getActiveSessionEndsAt()
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
}
