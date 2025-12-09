package com.attendifyplus.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import timber.log.Timber

/**
 * Helper class to manage notifications for the app.
 * Handles channel creation and notification dispatch.
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_SYNC = "sync_channel"
        const val CHANNEL_ID_EVENTS = "events_channel"
        
        const val NOTIFICATION_ID_SYNC = 1001
        const val NOTIFICATION_ID_EVENT = 2001
    }

    private val notificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel for Sync Status
            val syncChannel = NotificationChannel(
                CHANNEL_ID_SYNC,
                "Data Sync",
                NotificationManager.IMPORTANCE_LOW // Low importance for background sync status
            ).apply {
                description = "Notifications about data synchronization status"
            }

            // Channel for School Events
            val eventChannel = NotificationChannel(
                CHANNEL_ID_EVENTS,
                "School Events",
                NotificationManager.IMPORTANCE_DEFAULT // Default for reminders
            ).apply {
                description = "Reminders for upcoming school events"
            }

            notificationManager.createNotificationChannels(listOf(syncChannel, eventChannel))
            Timber.d("Notification channels created")
        }
    }

    /**
     * Shows a notification for sync status
     */
    fun showSyncNotification(title: String, message: String, isError: Boolean = false) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_SYNC)
            .setSmallIcon(android.R.drawable.ic_popup_sync) // Use a standard icon or app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)

        if (isError) {
             builder.setSmallIcon(android.R.drawable.stat_notify_error)
        }

        notificationManager.notify(NOTIFICATION_ID_SYNC, builder.build())
    }

    /**
     * Shows a notification for a school event
     */
    fun showEventNotification(title: String, description: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_EVENTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Ideally, we would add a PendingIntent here to open the app to the relevant screen
        // val intent = Intent(context, MainActivity::class.java)
        // val pendingIntent = PendingIntent.getActivity(...)
        // builder.setContentIntent(pendingIntent)

        // Using a unique ID based on time to allow multiple event notifications
        val uniqueId = (System.currentTimeMillis() % 10000).toInt() + NOTIFICATION_ID_EVENT
        notificationManager.notify(uniqueId, builder.build())
    }
}
