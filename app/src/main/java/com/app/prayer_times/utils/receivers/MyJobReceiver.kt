package com.app.prayer_times.utils.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.prayer_times.data.core.PTManager
import com.app.prayer_times.data.preferences.UserPrefs
import com.app.prayer_times.utils.schedulers.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyJobReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduler = NotificationScheduler(context)

        val ptManager = PTManager(context)
        UserPrefs(context).getString("user_area", null)?.let { ptManager.initArea(it) }
        val prayerTitles = ptManager.prayerTitles

        CoroutineScope(Dispatchers.IO).launch {
            val dayTimes = ptManager.getTodayTimesJob()
            if (dayTimes != null) {
                scheduler.scheduleAllReminders(prayerTitles, dayTimes)
            }
        }

    }
}