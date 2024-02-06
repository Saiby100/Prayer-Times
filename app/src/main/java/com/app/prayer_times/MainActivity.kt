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
import com.app.prayer_times.data.PTManager
import kotlinx.coroutines.launch
import com.app.prayer_times.utils.Date

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val date = Date()

    private var ignoreAsrType: String? = null
    private val ptManager: PTManager = PTManager(date)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val area: String? = getSetting("user_area")
        ignoreAsrType = getSetting("asr_type")

        if (ignoreAsrType == null) {
            ignoreAsrType = "Asr(H)"
            saveSetting("asr_type", ignoreAsrType!!)
        }

        if (area == null) {
            initAreaLayout()
        } else {
            ptManager.initArea(area)
            initDayLayout(area)
        }
    }


    private fun initAreaLayout(): Boolean {
        setContentView(R.layout.select_area_layout)

        val linearLayout = findViewById<LinearLayout>(R.id.areaLayout)

        if (!hasInternetConnection("Unable to connect to internet")) {
            //TODO: Implement retry button
            return false
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
        return true
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
    }

    private fun initDayLayout(areaString: String) {
        setContentView(R.layout.day_layout)
        val cityTitle: TextView = findViewById(R.id.cityTitle)
        cityTitle.text = areaString

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

            layout.addView(createButtonItem(
                "${prayerTitle}: ${dayTimes[i]}",
                targetIndex == i
            ))
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

    private fun logMsg(message: String) {
        Log.d("debugging", message)
    }
}