package com.hostelhub.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.hostelhub.app.MainActivity
import com.hostelhub.app.R
import com.hostelhub.app.data.local.AppSettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostelNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSettingsManager: AppSettingsManager
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val notifIdCounter = AtomicInteger(1001)

    companion object {
        const val CHANNEL_ANNOUNCEMENTS = "channel_hostel_announcements"
        const val CHANNEL_FEE_REMINDERS = "channel_fee_reminders"
        const val CHANNEL_MESS_MENU = "channel_mess_menu"
        const val CHANNEL_EMERGENCY = "channel_emergency_alerts"
    }

    init {
        createNotificationChannels()
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val announcementsChannel = NotificationChannel(
                CHANNEL_ANNOUNCEMENTS,
                "Hostel Announcements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notices, administrative updates, and hostel circulars"
                enableLights(true)
                enableVibration(true)
            }

            val feeChannel = NotificationChannel(
                CHANNEL_FEE_REMINDERS,
                "Fee Payment Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Upcoming invoices, fee due alerts, and payment receipts"
            }

            val messChannel = NotificationChannel(
                CHANNEL_MESS_MENU,
                "Weekly Mess Menu Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Weekly hostel meal schedule and daily special menu updates"
            }

            val emergencyChannel = NotificationChannel(
                CHANNEL_EMERGENCY,
                "Emergency & Safety Broadcasts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent safety warnings, security hazards, and campus emergencies"
                enableLights(true)
                enableVibration(true)
            }

            systemNotificationManager.createNotificationChannels(
                listOf(announcementsChannel, feeChannel, messChannel, emergencyChannel)
            )
        }
    }

    fun areNotificationsEnabledOnDevice(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }

    private fun hasPostNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun getLaunchPendingIntent(destination: String? = null): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            destination?.let { putExtra("navigate_to", it) }
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    /**
     * Dispatches a hostel announcement notification.
     * Respects user's Push Notifications preference.
     */
    fun showAnnouncement(title: String, message: String, announcementId: String = ""): Boolean {
        if (!appSettingsManager.pushNotifications.value) return false
        if (!areNotificationsEnabledOnDevice() || !hasPostNotificationPermission()) return false

        val notifId = notifIdCounter.getAndIncrement()
        val builder = NotificationCompat.Builder(context, CHANNEL_ANNOUNCEMENTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getLaunchPendingIntent("announcements"))
            .setAutoCancel(true)

        try {
            notificationManager.notify(notifId, builder.build())
            return true
        } catch (e: SecurityException) {
            return false
        }
    }

    /**
     * Dispatches a real fee reminder notification.
     * Respects both Push Notifications and Fee Payment Reminders preferences.
     */
    fun showFeeReminder(feeTitle: String, amountDue: Double, dueDate: String = "", feeId: String = ""): Boolean {
        if (!appSettingsManager.pushNotifications.value || !appSettingsManager.feeReminders.value) return false
        if (!areNotificationsEnabledOnDevice() || !hasPostNotificationPermission()) return false

        val notifId = notifIdCounter.getAndIncrement()
        val body = if (dueDate.isNotBlank()) {
            "Payment of ₹${amountDue.toInt()} for '$feeTitle' is due on $dueDate. Tap to pay now."
        } else {
            "Pending fee of ₹${amountDue.toInt()} for '$feeTitle'. Please settle at your earliest."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_FEE_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Fee Payment Reminder")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getLaunchPendingIntent("fee_payment"))
            .setAutoCancel(true)

        try {
            notificationManager.notify(notifId, builder.build())
            return true
        } catch (e: SecurityException) {
            return false
        }
    }

    /**
     * Dispatches a weekly mess menu alert.
     * Respects both Push Notifications and Weekly Mess Menu Alerts preferences.
     */
    fun showMessMenuAlert(menuTitle: String, details: String = ""): Boolean {
        if (!appSettingsManager.pushNotifications.value || !appSettingsManager.menuUpdates.value) return false
        if (!areNotificationsEnabledOnDevice() || !hasPostNotificationPermission()) return false

        val notifId = notifIdCounter.getAndIncrement()
        val body = if (details.isNotBlank()) details else "New meal schedule published: $menuTitle"

        val builder = NotificationCompat.Builder(context, CHANNEL_MESS_MENU)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Fresh Mess Menu Published")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getLaunchPendingIntent("mess_menu"))
            .setAutoCancel(true)

        try {
            notificationManager.notify(notifId, builder.build())
            return true
        } catch (e: SecurityException) {
            return false
        }
    }

    /**
     * Dispatches a high-priority safety or emergency alert.
     * Emergency alerts are campus safety critical broadcasts.
     */
    fun showEmergencyAlert(title: String, message: String, incidentId: String = ""): Boolean {
        if (!areNotificationsEnabledOnDevice() || !hasPostNotificationPermission()) return false

        val notifId = notifIdCounter.getAndIncrement()
        val formattedTitle = if (!title.startsWith("🚨")) "🚨 URGENT: $title" else title

        val builder = NotificationCompat.Builder(context, CHANNEL_EMERGENCY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(formattedTitle)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(getLaunchPendingIntent("emergency"))
            .setAutoCancel(true)

        try {
            notificationManager.notify(notifId, builder.build())
            return true
        } catch (e: SecurityException) {
            return false
        }
    }
}
