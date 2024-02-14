package com.app.prayer_times.utils.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.prayer_times.utils.notifications.Notification

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //Show the notification
        val notification = Notification(context)

        var prayer = intent.getStringExtra("prayer")
        if (prayer == null) {
            prayer = "Unknown"
        }
        val timeUntil: Int = intent.getIntExtra("timeUntil", -1)

        notification.showReminderNotification(prayer, timeUntil)
    }
}