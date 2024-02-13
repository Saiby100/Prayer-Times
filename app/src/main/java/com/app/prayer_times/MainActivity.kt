package com.app.prayer_times

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.app.prayer_times.data.core.PTManager
import kotlinx.coroutines.launch
import com.app.prayer_times.utils.datetime.Date
import com.app.prayer_times.utils.datetime.Time
import com.app.prayer_times.utils.debug.Logger
import com.app.prayer_times.utils.notifications.Notification
import com.app.prayer_times.utils.notifications.NotificationScheduler
import com.app.prayer_times.utils.permissions.Permission

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val date = Date()

    private val ptManager: PTManager = PTManager(date)

    private lateinit var notification: Notification
    private lateinit var scheduler: NotificationScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notification = Notification(this)
        scheduler = NotificationScheduler(this)

        sharedPreferences = getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val area: String? = getSetting("user_area")

        if (area == null) {
            initAreaLayout()
        } else {
            ptManager.initArea(area)
            initDayLayout(area)
        }
    }

    private fun isNewUser(): Boolean {
        val isNew = getSetting("new_user")
        if (isNew == null) {
            saveSetting("new_user", "false")
        }

        return isNew == null
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
        saveSetting("user_area", areaString)
        initDayLayout(areaString)

        if (isNewUser()) {
            Permission.getNotificationPermission(this@MainActivity)
        }
    }

    private fun initDayLayout(areaString: String) {
        setContentView(R.layout.day_layout)
        val cityTitle: TextView = findViewById(R.id.cityTitle)
        cityTitle.text = areaString

        //Bind actions to next/prev buttons
        val nextDayBtn: ImageButton = findViewById(R.id.nextDayBtn)
        val prevDayBtn: ImageButton = findViewById(R.id.prevDayBtn)
        val todayBtn = findViewById<Button>(R.id.todayBtn)

        hasInternetConnection("Check internet connection")

        nextDayBtn.setOnClickListener { showPrayerTimes(1) }
        prevDayBtn.setOnClickListener { showPrayerTimes(-1) }
        todayBtn.setOnClickListener {
            showToast("Today Button Pressed")
        }

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
                val dayTimes: MutableList<String>? = if (from < 0) {
                    ptManager.getPrevDayTimes(this@MainActivity)
                } else if (from > 0) {
                    ptManager.getNextDayTimes(this@MainActivity)
                } else {
                    ptManager.getTodayTimes(this@MainActivity)
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
    private fun addDayTimes(dayTimes: MutableList<String>) {
        val layout = findViewById<LinearLayout>(R.id.timesLayout)
        val dateTitle = findViewById<TextView>(R.id.dateTitle)

        val dateString = "${date.day}/${date.month}/${date.year}"
        dateTitle.text = dateString
        layout.removeAllViews()

        val targetIndex: Int =  if (date.isToday()) {
            date.timeCmp(dayTimes)
        } else {
            -1
        }

        for (i in 0..< dayTimes.size) {
            val prayerTitle = ptManager.prayerTitles[i]
            val btn = createButtonItem(
                "${prayerTitle}: ${dayTimes[i]}",
                targetIndex == i
            )
            btn.setOnClickListener {
                if (Permission.getNotificationPermission(this@MainActivity)) {
                    val currentTime = date.currentTime()

                    val remindBefore: Int = 5 //TODO: Fetch from preferences
                    val time = Time(dayTimes[i])
                    time.setEarlier(minutes = remindBefore) //5 minutes before

                    if (currentTime.timeCmp(time) < 0) {
                        val reminderScheduled = scheduler.toggleReminder(
                            time.toMillis(),
                            prayerTitle,
                            remindBefore
                        )
                        if (reminderScheduled)
                            showToast("Reminder set successfully")
                        else
                            showToast("Failed to schedule reminder")
                        //TODO: update notification indicator here
                    }
                }
            }

            layout.addView(btn)
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

    private fun saveSetting(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun getSetting(key: String): String? {
        return sharedPreferences.getString(key, null)
    }
}