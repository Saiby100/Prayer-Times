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
import com.app.prayer_times.data.PTScraper
import com.app.prayer_times.ui.theme.PrayerTimesTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val areaStrings: Array<String>? = withContext(Dispatchers.IO) { PTScraper.getAreaTitles() }
                if (areaStrings != null) {
                    for (area in areaStrings) {
                        linearLayout.addView(createListItem(area, false))
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Failed to fetch areas: $e", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun createListItem(text: String, highlighted: Boolean): Button {
        val button = Button(this)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 5)

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
            //TODO: Change to highlighted drawable
            button.background = resources.getDrawable(R.drawable.list_item_background)
        }

        return button
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