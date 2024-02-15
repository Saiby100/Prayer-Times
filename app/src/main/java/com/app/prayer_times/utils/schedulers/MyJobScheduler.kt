package com.app.prayer_times.utils.schedulers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.app.prayer_times.utils.datetime.Time
import com.app.prayer_times.utils.debug.Logger
import com.app.prayer_times.utils.receivers.MyJobReceiver
import com.app.prayer_times.utils.receivers.NotificationReceiver

class MyJobScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleJob(): Boolean {
        try {
            val alarmIntent = Intent(context, MyJobReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
            alarmManager.setInexactRepeating(
                AlarmManager.RTC,
                Time(0, 10).toMillis(),
                AlarmManager.INTERVAL_DAY,
                alarmIntent
            )
            return true

        } catch (e: SecurityException) {
            Logger.logErr("Failed to schedule reminder notification: $e")
        }
        return false
    }

    fun cancelJob() {
        try {
            val alarmIntent = Intent(context, NotificationReceiver::class.java)

            alarmManager.cancel(PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE
            ))
            Logger.logDebug("Scheduling job cancel successful")
        } catch (e: Exception) {
            Logger.logErr("Failed to cancel schedule operation: $e")
        }
    }
}