package com.app.prayer_times.utils.schedulers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.app.prayer_times.utils.datetime.Time
import com.app.prayer_times.utils.debug.Logger
import com.app.prayer_times.utils.receivers.MyJobReceiver

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
                Time(0, 0).toMillis(), //Midnight
                AlarmManager.INTERVAL_DAY,
                alarmIntent
            )
            return true

        } catch (e: SecurityException) {
            Logger.logErr("Failed to schedule reminder notification: $e")
        }
        return false
    }
}