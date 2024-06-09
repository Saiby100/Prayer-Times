package com.app.prayer_times

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.app.prayer_times.data.core.PTManager
import com.app.prayer_times.data.preferences.UserPrefs
import com.app.prayer_times.fragments.AreaLayout
import com.app.prayer_times.ui.custom.DatePicker
import com.app.prayer_times.ui.custom.PrayerCard
import kotlinx.coroutines.launch
import com.app.prayer_times.utils.datetime.Date
import com.app.prayer_times.utils.datetime.Time
import com.app.prayer_times.utils.notifications.Notification
import com.app.prayer_times.utils.permissions.Permission
import com.app.prayer_times.utils.schedulers.MyJobScheduler
import com.app.prayer_times.utils.schedulers.NotificationScheduler

class MainActivity : FragmentActivity() {

    private val date = Date()

    private lateinit var ptManager: PTManager
    private lateinit var notification: Notification
    private lateinit var scheduler: NotificationScheduler
    private lateinit var userPrefs: UserPrefs
    private lateinit var jobScheduler: MyJobScheduler

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

        setContentView(R.layout.fragment_container)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<AreaLayout>(R.id.fragment_container)
            }
        }

//        if (area == null) {
//            initAreaLayout()
//        } else {
//            ptManager.initArea(area)
//            initDayLayout(area)
//        }
//        actionBar?.apply { title = area }
//        findViewById<ImageButton>(R.id.datePicker).setOnClickListener {
//            val datePicker = DatePicker()
//            datePicker.show(supportFragmentManager, "datePicker")
//        }
    }

    private fun initAreaLayout() {
        setContentView(R.layout.select_area_layout)

        val linearLayout = findViewById<LinearLayout>(R.id.areaList)

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

    private fun createButtonItem(text: String, highlighted: Boolean = false): Button {
        val button = Button(this)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 15)

        button.layoutParams = layoutParams
        button.text = text
        button.textAlignment = Button.TEXT_ALIGNMENT_CENTER
        button.textSize = 16f
        button.typeface = ResourcesCompat.getFont(this, R.font.inter_medium)
        button.setTextColor(ContextCompat.getColor(this, R.color.white))
        button.setPadding(0, 13, 0, 13)
        button.elevation = 0f
        button.stateListAnimator = null
        button.isAllCaps = false

        if (highlighted) {
            button.background = resources.getDrawable(R.drawable.btn_ripple_highlighted)
        } else {
            button.background = resources.getDrawable(R.drawable.btn_ripple)
        }
        return button
    }

    private fun handleAreaSelected(areaString: String) {
        ptManager.initArea(areaString)
        userPrefs.setString("user_area", areaString)
        initDayLayout(areaString)
    }

    private fun initDayLayout(areaString: String) {
        setContentView(R.layout.day_layout)

        val todayBtn: Button = findViewById(R.id.todayBtn)
        todayBtn.text = "${date.day}"
        todayBtn.setOnClickListener {
            if (!date.isToday()) {
                date.reset()
                ptManager.setDate(date)
                showPrayerTimes(0)
            }
        }
        //Bind actions to next/prev buttons
        val nextDayBtn: ImageButton = findViewById(R.id.nextDayBtn)
        val prevDayBtn: ImageButton = findViewById(R.id.prevDayBtn)

        hasInternetConnection("Check internet connection")

        nextDayBtn.setOnClickListener { showPrayerTimes(1) }
        prevDayBtn.setOnClickListener { showPrayerTimes(-1) }

        showPrayerTimes(0)
    }

    /**
     * Displays the day's prayer times to the user.
     * Uses [from] to determine if it should get the previous, current, or
     * next day's times.
     * @param [from] indicates where the call is coming from (-1, 0, 1).
     */
    private fun showPrayerTimes(from: Int) {
        val prevDayBtn = findViewById<ImageButton>(R.id.prevDayBtn)
        val nextDayBtn = findViewById<ImageButton>(R.id.nextDayBtn)

        prevDayBtn.isEnabled = false
        nextDayBtn.isEnabled = false

        lifecycleScope.launch {
            try {
                val dayTimes: MutableList<Time>? = if (from < 0) {
                    ptManager.getPrevDayTimes()
                } else if (from > 0) {
                    ptManager.getNextDayTimes()
                } else {
                    ptManager.getTodayTimes()
                }

                if (dayTimes != null) {
                    addDayTimes(dayTimes)
                } else {
                    showToast("No prayer times found")
                }

                prevDayBtn.isEnabled = true
                nextDayBtn.isEnabled = true
            } catch (e: Exception) {
                showToast("Failed to get prayer times")
                Log.e("ERROR", "Failed to fetch prayer times: $e")
            }
        }
    }

    /**
     * Adds the list of day times [dayTimes] to the layout to display to the user.
     */
    private fun addDayTimes(dayTimes: MutableList<Time>) {
        val layout = findViewById<LinearLayout>(R.id.timesLayout)
        val dateTitle = findViewById<TextView>(R.id.dateTitle)

        val dayText: TextView = findViewById(R.id.dayTitle)

        val dateString = "${date.day} ${date.monthString()} ${date.year}"
        dateTitle.text = dateString

        dayText.text = date.dayString()

        layout.removeAllViews()

        val targetIndex: Int =  if (date.isToday()) {
            date.timeCmp(dayTimes)
        } else {
            -1
        }
        val alarmPrefs: List<Boolean> = userPrefs.getBoolList(ptManager.prayerTitles, false)

        for (i in 0..< dayTimes.size) {
            val prayerTitle = ptManager.prayerTitles[i]
            val card = PrayerCard(
                this,
                prayerTitle,
                dayTimes[i].toString(),
                targetIndex == i,
                alarmPrefs[i]
            )

            card.notificationButton.setOnClickListener {
                if (Permission.getNotificationPermission(this@MainActivity)) {
                    if (!userPrefs.getBool(prayerTitle, false)) {
                        userPrefs.setBool(prayerTitle, true)
                        card.setNotificationIcon(true)

                        if (date.currentTime().timeCmp(dayTimes[i]) < 0 && date.isToday())
                            scheduler.scheduleReminder(dayTimes[i].toMillis(), prayerTitle)

                        if (!userPrefs.getBool("alarms_enabled", false)) {
                            jobScheduler.scheduleJob()
                            userPrefs.setBool("alarms_enabled", true)
                        }

                    } else {
                        userPrefs.setBool(prayerTitle, false)
                        card.setNotificationIcon(false)
                        scheduler.cancelReminder(prayerTitle)
                    }
                }
            }
            layout.addView(card)
        }
    }

    /**
     * Shows the [message] as a toast to the user.
     */
    private fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * Check if user has internet connection.
     * Displays a toast to the user with [message] if internet connection is not found.
     */
    private fun hasInternetConnection(message: String? = null): Boolean {
        val context = this@MainActivity
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as
                ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (!hasInternet && message != null) {
            showToast(message)
        }

        return hasInternet
    }
}