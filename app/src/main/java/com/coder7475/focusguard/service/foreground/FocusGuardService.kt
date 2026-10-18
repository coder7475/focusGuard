package com.coder7475.focusguard.service.foreground

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.coder7475.focusguard.MainActivity
import com.coder7475.focusguard.R
import com.coder7475.focusguard.data.local.FocusGuardDatabase
import com.coder7475.focusguard.data.repository.RoomSessionRepository
import com.coder7475.focusguard.domain.engine.SessionEngine
import com.coder7475.focusguard.platform.permission.PermissionChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max

class FocusGuardService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: Job? = null
    private var endsAtEpochMs: Long = 0L

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val permissionChecker: PermissionChecker by lazy {
        PermissionChecker(this)
    }

    private val accessibilityTamperObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            checkAccessibilityTampering()
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        registerTamperObserver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val targetEpoch = intent?.getLongExtra(EXTRA_ENDS_AT_EPOCH_MS, 0L) ?: 0L
        if (targetEpoch > 0L) {
            endsAtEpochMs = targetEpoch
            startForegroundNotification()
            startTicker()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterTamperObserver()
        tickerJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerTamperObserver() {
        try {
            contentResolver.registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES),
                false,
                accessibilityTamperObserver
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register accessibility tamper observer", e)
        }
    }

    private fun unregisterTamperObserver() {
        try {
            contentResolver.unregisterContentObserver(accessibilityTamperObserver)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister accessibility tamper observer", e)
        }
    }

    private fun checkAccessibilityTampering() {
        if (endsAtEpochMs > System.currentTimeMillis() && !permissionChecker.isAccessibilityServiceEnabled()) {
            Log.w(TAG, "Accessibility service disabled during active session! Showing warning.")
            showTamperWarningNotification()
        }
    }

    private fun showTamperWarningNotification() {
        val settingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            103,
            settingsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val warningNotification = NotificationCompat.Builder(this, CHANNEL_ID_TAMPER)
            .setContentTitle("⚠️ Focus Lock Alert: Accessibility Disabled!")
            .setContentText("Your focus lock was interrupted. Tap here to re-enable Accessibility Service.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_TAMPER, warningNotification)
    }

    private fun startForegroundNotification() {
        val notification = buildOngoingNotification(calculateRemainingText())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            startForeground(NOTIFICATION_ID_TRACKER, notification, fgsType)
        } else {
            startForeground(NOTIFICATION_ID_TRACKER, notification)
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                val remainingMs = max(0L, endsAtEpochMs - System.currentTimeMillis())
                if (remainingMs <= 0L) {
                    try {
                        val db = FocusGuardDatabase.getInstance(applicationContext)
                        val sessionRepo = RoomSessionRepository(db.sessionDao())
                        val sessionEngine = SessionEngine(sessionRepo)
                        sessionEngine.completeActiveSession()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to complete active session on ticker expiry", e)
                    }

                    showCompletionNotification(applicationContext)
                    stopSelf()
                    break
                }
                val formattedTime = formatTime(remainingMs)
                notificationManager.notify(NOTIFICATION_ID_TRACKER, buildOngoingNotification(formattedTime))
                delay(1000L)
            }
        }
    }

    private fun calculateRemainingText(): String {
        val remainingMs = max(0L, endsAtEpochMs - System.currentTimeMillis())
        return formatTime(remainingMs)
    }

    private fun formatTime(remainingMs: Long): String {
        val seconds = remainingMs / 1000L
        val h = seconds / 3600L
        val m = (seconds % 3600L) / 60L
        val s = seconds % 60L
        return if (h > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
        } else {
            String.format(Locale.US, "%02d:%02d", m, s)
        }
    }

    private fun buildOngoingNotification(timeText: String): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID_TRACKER)
            .setContentTitle("Focus Session Active")
            .setContentText("Time remaining: $timeText")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val trackerChannel = NotificationChannel(
                CHANNEL_ID_TRACKER,
                "Focus Session Tracker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live remaining time countdown during focus sessions"
                setShowBadge(false)
            }

            val completionChannel = NotificationChannel(
                CHANNEL_ID_COMPLETION,
                "Session Completed",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a focus session concludes"
                enableVibration(true)
            }

            val tamperChannel = NotificationChannel(
                CHANNEL_ID_TAMPER,
                "Tamper Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority warnings when focus security permissions are disabled"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(trackerChannel)
            notificationManager.createNotificationChannel(completionChannel)
            notificationManager.createNotificationChannel(tamperChannel)
        }
    }

    companion object {
        private const val TAG = "FocusGuardService"
        const val CHANNEL_ID_TRACKER = "ID_SESSION_TRACKER"
        const val CHANNEL_ID_COMPLETION = "ID_SESSION_COMPLETION"
        const val CHANNEL_ID_TAMPER = "ID_TAMPER_ALERT"
        const val NOTIFICATION_ID_TRACKER = 101
        const val NOTIFICATION_ID_COMPLETION = 102
        const val NOTIFICATION_ID_TAMPER = 103
        const val EXTRA_ENDS_AT_EPOCH_MS = "extra_ends_at_epoch_ms"

        fun start(context: Context, endsAtEpochMs: Long) {
            val intent = Intent(context, FocusGuardService::class.java).apply {
                putExtra(EXTRA_ENDS_AT_EPOCH_MS, endsAtEpochMs)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FocusGuardService::class.java))
        }

        fun showCompletionNotification(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val contentIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_COMPLETION)
                .setContentTitle("Focus Session Complete!")
                .setContentText("Congratulations! You kept your focus discipline.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .build()

            notificationManager.notify(NOTIFICATION_ID_COMPLETION, notification)
        }
    }
}
