package com.app.prayer_times.ui.custom

import android.content.Context
import android.view.Gravity
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.app.prayer_times.R

class PrayerCard(
    private val context: Context,
    prayerTitle: String,
    prayerTime: String,
    isHighlighted: Boolean,
    notificationIsActive: Boolean
    ): LinearLayout(context) {

    private val title: String
    private val time: String
    private var highlighted: Boolean
    private var notificationActive: Boolean
    lateinit var notificationButton: ImageButton

    init {
        title = prayerTitle
        time = prayerTime
        highlighted = isHighlighted
        notificationActive = notificationIsActive

        orientation = HORIZONTAL
        setPadding(50, 20, 30, 20)
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 15)
        }
        gravity = Gravity.CENTER_VERTICAL
        if (highlighted) {
            background = context.getDrawable(R.drawable.btn_ripple_highlighted)
        }

        addViews()
    }

    private fun addViews() {
        val prayerTitleView = createTextView(title).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        val rightContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
        }

        val prayerTimeView = createTextView(time).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        notificationButton = ImageButton(context).apply {
            layoutParams = LayoutParams(100, 100)
                .apply { setMargins(40, 0, 0, 0) }
            val icon =  if (notificationActive)
                R.drawable.notification_active
            else
                R.drawable.notification_inactive
            setImageResource(icon)
            setBackgroundResource(R.drawable.btn_ripple)
            setPadding(5, 5, 5, 5)
        }

        rightContainer.addView(prayerTimeView)
        rightContainer.addView(notificationButton)

        addView(prayerTitleView)
        addView(rightContainer)
    }

    fun setNotificationIcon(active: Boolean) {
        val icon = if (active)
            R.drawable.notification_active
        else
            R.drawable.notification_inactive
        notificationButton.setImageResource(icon)
    }

    private fun createTextView(textString: String): TextView {
        val textView = TextView(context).apply {
            text = textString
            textSize = 18f
            typeface = ResourcesCompat.getFont(context, R.font.inter_medium)
            setTextColor(ContextCompat.getColor(context, R.color.white))
        }

        return textView
    }
}