package com.coder7575.sleepguard.enforcer

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
import androidx.core.app.NotificationCompat

class FocusForegroundService : Service() {

    private var endsAtEpochMs: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var completionRunnable: Runnable? = null

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
            scheduleCompletion()
        } else {
            val activeTarget = NativeDbHelper(this).getActiveSessionEndsAt()
            if (activeTarget > System.currentTimeMillis()) {
                endsAtEpochMs = activeTarget
                startForegroundNotification()
                scheduleCompletion()
            } else {
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterTamperObserver()
        completionRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerTamperObserver() {
        try {
            contentResolver.registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES),
                false,
                accessibilityTamperObserver
            )
        } catch (_: Exception) {}
    }

    private fun unregisterTamperObserver() {
        try {
            contentResolver.unregisterContentObserver(accessibilityTamperObserver)
        } catch (_: Exception) {}
    }

    private fun checkAccessibilityTampering() {
        if (endsAtEpochMs > System.currentTimeMillis()) {
            val enabledServices = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            val expected = "$packageName/${FocusAccessibilityService::class.java.name}"
            if (!enabledServices.contains(expected, ignoreCase = true)) {
                showTamperWarningNotification()
            }
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

        val warningNotification = NotificationCompat.Builder(this, CHANNEL_ID_TRACKER)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Focus Guard Compromised")
            .setContentText("Accessibility service was disabled! Tap to re-enable.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_TAMPER, warningNotification)
    }

    private fun startForegroundNotification() {
        val notification = buildTrackerNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID_TRACKER,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID_TRACKER, notification)
        }
    }

    private fun scheduleCompletion() {
        completionRunnable?.let { handler.removeCallbacks(it) }
        val remainingMs = endsAtEpochMs - System.currentTimeMillis()
        if (remainingMs <= 0L) {
            onSessionCompleted()
        } else {
            completionRunnable = Runnable { onSessionCompleted() }
            handler.postDelayed(completionRunnable!!, remainingMs)
        }
    }

    private fun onSessionCompleted() {
        completionRunnable?.let { handler.removeCallbacks(it) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildTrackerNotification(): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID_TRACKER)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentTitle("FocusGuard Active")
            .setContentText("Focus lock active")
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(endsAtEpochMs)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val trackerChannel = NotificationChannel(
                CHANNEL_ID_TRACKER,
                "Focus Lock Tracker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time countdown of active focus lock"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(trackerChannel)
        }
    }

    companion object {
        const val CHANNEL_ID_TRACKER = "ID_SESSION_TRACKER"
        const val NOTIFICATION_ID_TRACKER = 1001
        const val NOTIFICATION_ID_TAMPER = 1002
        const val EXTRA_ENDS_AT_EPOCH_MS = "extra_ends_at_epoch_ms"

        fun start(context: Context, endsAtEpochMs: Long) {
            val intent = Intent(context, FocusForegroundService::class.java).apply {
                putExtra(EXTRA_ENDS_AT_EPOCH_MS, endsAtEpochMs)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FocusForegroundService::class.java))
        }
    }
}
