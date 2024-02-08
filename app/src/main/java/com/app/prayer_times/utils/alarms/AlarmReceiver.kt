package com.app.prayer_times.utils.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.prayer_times.utils.notifications.Notification

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notification: Notification = Notification(context)

        notification.showReminderNotification("", "")
    }
}