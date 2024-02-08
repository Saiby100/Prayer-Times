package com.app.prayer_times.utils.notifications

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.app.prayer_times.R
import com.app.prayer_times.utils.permissions.Permission

class Notification (private val context: Context) {
    private data class Channel (
        val id: String,
        val name: String,
        val description: String,
        val importance: Int
    )
    private val notificationManager: NotificationManager

    init {
        //Set up and create notification channels
        val channels: Array<Channel> = arrayOf(
            Channel(
                "REMINDER",
                "Prayer Reminder",
                "Notification for approaching prayers",
                NotificationManager.IMPORTANCE_DEFAULT
            ),
        )

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        for (channel in channels) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    channel.id,
                    channel.name,
                    channel.importance
                ).apply { description = channel.description }
            )
        }
    }

    fun showReminderNotification(title: String, text: String) {
        val hasNotificationPermission: Boolean = Permission
            .getNotificationPermission(context as Activity)
        val reminderId = 0
        val builder = NotificationCompat.Builder(context, "REMINDER")
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (hasNotificationPermission) {
            notificationManager.notify(reminderId, builder.build())
        }
    }
}