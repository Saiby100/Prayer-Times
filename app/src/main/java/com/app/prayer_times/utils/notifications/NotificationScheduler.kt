package com.app.prayer_times.utils.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.app.prayer_times.utils.alarms.AlarmReceiver
import com.app.prayer_times.utils.debug.Logger

class NotificationScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val alarmScheduled = mutableMapOf<Int, Boolean>()

    fun toggleReminder(timeInMillis: Long, prayer: String, timeUntil: Int): Boolean {
        try {
            if (alarmScheduled[prayer.hashCode()] == true) {
                cancelReminder(prayer)
                alarmScheduled[prayer.hashCode()] = false
            } else {
                return scheduleReminder(timeInMillis, prayer, timeUntil)
                alarmScheduled[prayer.hashCode()] = true
            }
        } catch (e: Exception) {
            return false
        }

        return true
    }
    fun scheduleReminder(timeInMillis: Long, prayer: String, timeUntil: Int): Boolean {
        try {
            if (alarmScheduled[prayer.hashCode()] == true) {
                return true
            }
            val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("prayer", prayer)
                putExtra("timeUntil", timeUntil)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                prayer.hashCode(),
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            if (canScheduleNotifications(alarmManager)) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                return true
            }

        } catch (e: SecurityException) {
            Logger.logErr("Failed to schedule reminder notification: $e")
        }
        return false
    }

    private fun cancelReminder(prayer: String) {
        try {
            val alarmIntent = Intent(context, AlarmReceiver::class.java)

            alarmManager.cancel(PendingIntent.getBroadcast(
                context,
                prayer.hashCode(),
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE
            ))
        } catch (e: Exception) {
            Logger.logErr("Failed to cancel reminder notification: $e")
        }

    }

    private fun canScheduleNotifications(alarmManager: AlarmManager): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms()
        }
        return true
    }
}