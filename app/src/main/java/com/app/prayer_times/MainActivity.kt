package com.app.prayer_times

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.app.prayer_times.data.PTScraper
import com.app.prayer_times.ui.theme.PrayerTimesTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    val calendar = Calendar.getInstance()
    val year: Int = calendar.get(Calendar.YEAR)
    val month: Int = calendar.get(Calendar.MONTH) + 1
    val day: Int = calendar.get(Calendar.DAY_OF_MONTH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val area: String? = getSetting("user_area")

        if (area == null) {
            setContentView(R.layout.select_area_layout)
            initAreaLayout()
        } else {
            setContentView(R.layout.day_layout)
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
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val areaStrings: Array<String>? = withContext(Dispatchers.IO) { PTScraper.getAreaTitles() }
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
                Toast.makeText(this@MainActivity, "Failed to fetch areas: $e", Toast.LENGTH_SHORT)
                    .show()
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
        //TODO: Save area to memory
        PTScraper.setArea(areaString)
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

        //TODO: Highlight nearest time
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val dayTimes: MutableList<String>? = withContext(Dispatchers.IO) {
                    PTScraper.getPrayerTimesDay(year, month, day)
                }
                if (dayTimes != null) {
                    addDayTimes(dayTimes)
                } else {
                    Toast.makeText(this@MainActivity, "Failed to get prayer times", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Failed to get prayer times: $e", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun initMonthLayout() {
        //TODO: Implement
        setContentView(R.layout.month_layout)
    }

    private fun nextDay() {
        //TODO: Implement
        Toast.makeText(this@MainActivity, "TODO: Go to next day", Toast.LENGTH_SHORT)
            .show()
    }

    private fun prevDay() {
        //TODO: Implement
        Toast.makeText(this@MainActivity, "TODO: Go to previous day", Toast.LENGTH_SHORT)
            .show()
    }

    private fun addDayTimes(dayTimes: MutableList<String>) {
        val layout = findViewById<LinearLayout>(R.id.timesLayout)
        val dateTitle = findViewById<TextView>(R.id.dateTitle)

        dateTitle.text = "$day/$month/$year"
        layout.removeAllViews()

        //TODO: Highlight button with nearest time
        for (time in dayTimes) {
            layout.addView(createButtonItem(time, false))
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PrayerTimesTheme {
        Greeting("Android")
    }
}