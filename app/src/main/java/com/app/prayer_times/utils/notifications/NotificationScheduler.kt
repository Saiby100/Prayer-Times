package com.app.prayer_times.utils.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.app.prayer_times.utils.alarms.AlarmReceiver
import com.app.prayer_times.utils.debug.Logger

object NotificationScheduler {
    fun scheduleReminder(context: Context, timeInMillis: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            }
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent)

        } catch (e: SecurityException) {
            Logger.logErr("Failed to schedule reminder notification: $e")
        }
    }
}