package com.app.prayer_times

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.app.prayer_times.data.PTManager
import com.app.prayer_times.data.PTScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.app.prayer_times.utils.Date

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val date = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val area: String? = getSetting("user_area")

        if (area == null) {
            setContentView(R.layout.select_area_layout)
            initAreaLayout()
        } else {
            setContentView(R.layout.day_layout)
            PTManager.initArea(area)
            initDayLayout(area)
        }
    }

    private fun saveSetting(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun getSetting(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    private fun initAreaLayout() {
        val linearLayout = findViewById<LinearLayout>(R.id.areaLayout)
        lifecycleScope.launch {
            try {
                val areaStrings: Array<String>? = withContext(Dispatchers.IO) { PTManager.getAreaTitles() }
                if (areaStrings != null) {
                    var button: Button
                    for (area in areaStrings) {
                        button = createButtonItem(area, false)
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

    private fun createButtonItem(text: String, highlighted: Boolean): Button {
        val button = Button(this)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 10)

        button.layoutParams = layoutParams
        button.text = text
        button.textAlignment = Button.TEXT_ALIGNMENT_CENTER
        button.textSize = 16f
        button.typeface = ResourcesCompat.getFont(this, R.font.inter_medium)
        button.setTextColor(ContextCompat.getColor(this, R.color.text_color_light))
        button.setPadding(0, 13, 0, 13)
        button.elevation = 0f
        button.stateListAnimator = null
        button.isAllCaps = false

        if (!highlighted) {
            button.background = resources.getDrawable(R.drawable.list_item_background)
        } else {
            button.background = resources.getDrawable(R.drawable.list_item_highlighted_bg)
        }

        return button
    }

    private fun handleAreaSelected(areaString: String) {
        PTManager.initArea(areaString)
        saveSetting("user_area", areaString)
        initDayLayout(areaString)
    }

    private fun initDayLayout(areaString: String) {
        setContentView(R.layout.day_layout)
        val cityTitle: TextView = findViewById(R.id.cityTitle)
        cityTitle.text = areaString

        //Bind actions to next/prev buttons
        val nextDayBtn: Button = findViewById(R.id.nextDayBtn)
        val prevDayBtn: Button = findViewById(R.id.prevDayBtn)

        nextDayBtn.setOnClickListener { nextDay() }
        prevDayBtn.setOnClickListener { prevDay() }

        initTimesLayout()
    }

    private fun initMonthLayout() {
        //TODO: Implement
        setContentView(R.layout.month_layout)
    }

    private fun nextDay() {
        date.changeDay(1)
        initTimesLayout()
    }

    private fun prevDay() {
        date.changeDay(-1)
        initTimesLayout()
    }

    private fun initTimesLayout() {
        lifecycleScope.launch {
            try {
                val dayTimes: MutableList<String>? = withContext(Dispatchers.IO) {
                    PTManager.getPrayerTimesDay(date.year, date.month, date.day, this@MainActivity)
                }
                if (dayTimes != null) {
                    addDayTimes(dayTimes)
                } else {
                    showToast("No prayer times found")
                }
            } catch (e: Exception) {
                showToast("Failed to get prayer times")
                Log.e("ERROR", "Failed to fetch prayer times: $e")
            }
        }
    }

    private fun addDayTimes(dayTimes: MutableList<String>) {
        val layout = findViewById<LinearLayout>(R.id.timesLayout)
        val dateTitle = findViewById<TextView>(R.id.dateTitle)

        dateTitle.text = "${date.day}/${date.month}/${date.year}"
        layout.removeAllViews()

        //TODO: Highlight button with nearest time
        for (time in dayTimes) {
            layout.addView(createButtonItem(time, false))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT)
            .show()
    }
}