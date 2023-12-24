package com.app.prayer_times

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat
import com.app.prayer_times.data.PTScraper
import com.app.prayer_times.ui.theme.PrayerTimesTheme

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val area = getSetting("user_area")

        if (area == null) {
            setContentView(R.layout.select_area_layout)
            initAreaLayout()
        } else {
            setContentView(R.layout.select_area_layout)
            initAreaLayout()
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
//        val areaStrings: Array<String>? = PTScraper.getAreaTitles()
        val areaStrings: Array<String>? = arrayOf("Cape Town", "Town 2", "Town 3", "Town 4", "Town 5", "Town 6", "Town 7")

        if (areaStrings != null) {
            for (area in areaStrings) {
                linearLayout.addView(createListItem(area, false))
            }
        }
    }

    private fun createListItem(text: String, highlighted: Boolean): TextView {
        val textView = TextView(this)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 5)

        textView.layoutParams = layoutParams
        textView.text = text
        textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        textView.textSize = 16f
        textView.typeface = ResourcesCompat.getFont(this, R.font.inter_medium)
        textView.setPadding(0, 15, 0, 15)
        if (!highlighted) {
            textView.background = resources.getDrawable(R.drawable.list_item_background)
        } else {
            //TODO: Change to highlighted drawable
            textView.background = resources.getDrawable(R.drawable.list_item_background)
        }

        return textView
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