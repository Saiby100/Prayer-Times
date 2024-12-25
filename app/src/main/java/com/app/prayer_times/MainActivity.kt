package com.app.prayer_times

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.app.prayer_times.data.core.PTManager
import com.app.prayer_times.data.preferences.UserPrefs
import com.app.prayer_times.ui.custom.PrayerCard
import com.app.prayer_times.ui.datepicker.DatePicker
import kotlinx.coroutines.launch
import com.app.prayer_times.utils.datetime.Date
import com.app.prayer_times.utils.datetime.Time
import com.app.prayer_times.utils.notifications.Notification
import com.app.prayer_times.utils.permissions.Permission
import com.app.prayer_times.utils.schedulers.MyJobScheduler
import com.app.prayer_times.utils.schedulers.NotificationScheduler
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    private val date = Date()

    private lateinit var ptManager: PTManager
    private lateinit var notification: Notification
    private lateinit var scheduler: NotificationScheduler
    private lateinit var userPrefs: UserPrefs
    private lateinit var jobScheduler: MyJobScheduler

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set action bar
        val actionBarColor = ContextCompat.getColor(this, R.color.dark_blue_secondary)
        actionBar?.setBackgroundDrawable(ColorDrawable(actionBarColor))

        ptManager = PTManager(this, date)
        notification = Notification(this)
        scheduler = NotificationScheduler(this)
        userPrefs = UserPrefs(this)
        jobScheduler = MyJobScheduler(this)

        val area: String? = userPrefs.getString("user_area", null)

        if (area == null) {
            initAreaLayout()
        } else {
            ptManager.initArea(area)
            initDayLayout(area)
        }

        actionBar?.apply { title = area }
        findViewById<ImageButton>(R.id.datePicker).setOnClickListener {
            val datePicker = DatePicker()
            datePicker.show(supportFragmentManager, "datePicker")
        }
    }

    private fun initAreaLayout() {
        setContentView(R.layout.select_area_layout)

        val linearLayout = findViewById<LinearLayout>(R.id.areaLayout)

        if (!hasInternetConnection("Unable to connect to internet")) {
            //TODO: Implement retry button
            return
        }

        lifecycleScope.launch {
            try {
                val areaStrings: Array<String>? = ptManager.getAreaTitles()
                if (areaStrings != null) {
                    var button: Button
                    for (area in areaStrings) {
                        button = createButtonItem(area, true)
                        button.setOnClickListener {
                            handleAreaSelected(area)
                        }
                        linearLayout.addView(button)
                    }
                }
            } catch (e: Exception) {
                showToast("Failed to fetch areas")
                Log.e("ERROR", "Failed to fetch areas: $e")
            }
        }
    }
}
