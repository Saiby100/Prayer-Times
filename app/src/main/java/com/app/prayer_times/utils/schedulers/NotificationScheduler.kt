package com.app.prayer_times.utils.schedulers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.app.prayer_times.data.preferences.UserPrefs
import com.app.prayer_times.utils.receivers.NotificationReceiver
import com.app.prayer_times.utils.datetime.Time
import com.app.prayer_times.utils.debug.Logger

class NotificationScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val userPrefs: UserPrefs = UserPrefs(context)

    fun scheduleReminder(timeInMillis: Long, prayer: String): Boolean {
        try {
            val remindBefore: Int = userPrefs.getInt("remind_before", 5)
            val alarmIntent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("prayer", prayer)
                putExtra("timeUntil", remindBefore)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                prayer.hashCode(),
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val time = timeInMillis - remindBefore * 60 * 1000
            if (canScheduleNotifications(alarmManager)) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)
                Logger.logMsg("Reminder set for $prayer")

                return true
            }

        } catch (e: SecurityException) {
            Logger.logErr("Failed to schedule reminder notification: $e")
        }
        return false
    }

    fun scheduleAllReminders(prayerTitles: List<String>, dayTimes: List<Time>): Boolean {
        Logger.logMsg("Schedule operation starting...")
        if (prayerTitles.size != dayTimes.size) {
            return false
        }

        try {
            val alarmPrefs: List<Boolean> = userPrefs.getBoolList(prayerTitles, false)
            for (i in prayerTitles.indices) {
                if (alarmPrefs[i]) {
                    scheduleReminder(dayTimes[i].toMillis(), prayerTitles[i])
                }
            }
        } catch (e: Exception) {
            Logger.logErr("Error in scheduleAllReminders: $e")
            return false
        }

        return true
    }

    fun cancelReminder(prayer: String) {
        try {
            val alarmIntent = Intent(context, NotificationReceiver::class.java)

            alarmManager.cancel(PendingIntent.getBroadcast(
                context,
                prayer.hashCode(),
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE
            ))
            Logger.logMsg("Reminder cancelled for $prayer")

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