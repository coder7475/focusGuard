package com.coder7475.focusguard.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.coder7475.focusguard.MainActivity
import com.coder7475.focusguard.data.local.FocusGuardDatabase
import com.coder7475.focusguard.data.repository.RoomSessionRepository
import com.coder7475.focusguard.domain.model.AccessDecision
import com.coder7475.focusguard.domain.model.FocusSession
import com.coder7475.focusguard.domain.model.SessionStatus
import com.coder7475.focusguard.domain.policy.AllowlistPolicy
import com.coder7475.focusguard.domain.repository.SessionRepository
import com.coder7475.focusguard.platform.role.SystemRoleResolver
import com.coder7475.focusguard.service.overlay.LockOverlayWindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FocusGuardAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var systemRoleResolver: SystemRoleResolver

    lateinit var allowlistPolicy: AllowlistPolicy
        internal set

    lateinit var overlayManager: LockOverlayWindowManager
        internal set

    private lateinit var sessionRepository: SessionRepository
    var currentActiveSession: FocusSession? = null
        internal set

    private var lastTargetPackage: String? = null
    var onPackageInspected: ((packageName: String?, decision: AccessDecision) -> Unit)? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

        systemRoleResolver = SystemRoleResolver(this)
        allowlistPolicy = AllowlistPolicy(systemRoleResolver, packageName)

        overlayManager = LockOverlayWindowManager(this) {
            launchEmergencyDialer()
        }

        val db = FocusGuardDatabase.getInstance(this)
        sessionRepository = RoomSessionRepository(db.sessionDao())

        serviceScope.launch {
            sessionRepository.observeActiveSession().collectLatest { session ->
                currentActiveSession = session
                if (session != null && session.status == SessionStatus.ACTIVE) {
                    overlayManager.setEndsAtEpochMs(session.endsAtEpochMs)
                } else {
                    overlayManager.hideOverlay()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        overlayManager.destroy()
        if (instance == this) {
            instance = null
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // 1. Only process explicit window state transitions; ignore window hierarchy jitter
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val targetPackage = event.packageName?.toString()
        val className = event.className?.toString()

        // 2. Ignore events with null or blank package names to prevent false dismissals
        if (targetPackage.isNullOrBlank()) {
            return
        }

        // 3. Prevent self-dismissal when overlay window itself fires window events
        if (targetPackage == packageName) {
            val isMainActivity = className == MainActivity::class.java.name
            if (!isMainActivity) {
                // Event comes from our overlay ComposeView / window, NOT MainActivity
                return
            }
        }

        // 4. Avoid re-evaluating unchanged package states
        if (targetPackage == lastTargetPackage && overlayManager.isVisible) {
            return
        }
        lastTargetPackage = targetPackage

        val decision = allowlistPolicy.evaluateAccess(targetPackage)
        Log.d(TAG, "Window transition: package=$targetPackage, class=$className, decision=$decision")
        onPackageInspected?.invoke(targetPackage, decision)

        val session = currentActiveSession
        val isSessionActive = session != null &&
            session.status == SessionStatus.ACTIVE &&
            System.currentTimeMillis() < session.endsAtEpochMs

        if (isSessionActive) {
            if (decision == AccessDecision.BLOCKED) {
                overlayManager.showOverlay()
            } else {
                overlayManager.hideOverlay()
            }
        } else {
            overlayManager.hideOverlay()
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "FocusGuardAccessibilityService interrupted")
    }

    private fun launchEmergencyDialer() {
        try {
            val emergencyIntent = Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(emergencyIntent)
            overlayManager.hideOverlay()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch emergency dialer", e)
        }
    }

    companion object {
        private const val TAG = "FocusGuardA11y"
        var instance: FocusGuardAccessibilityService? = null
            internal set
    }
}
