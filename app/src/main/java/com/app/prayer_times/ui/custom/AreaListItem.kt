package com.app.prayer_times.ui.custom

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.app.prayer_times.R

class AreaListItem(
    private val context: Context,
    areaTitle: String
) : Button(context) {
    init {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 15)
        layoutParams = params

        text = areaTitle
        textAlignment = Button.TEXT_ALIGNMENT_CENTER
        textSize = 16f
        typeface = ResourcesCompat.getFont(context, R.font.inter_medium)
        setTextColor(ContextCompat.getColor(context, R.color.white))
        setPadding(0, 13, 0, 13)
        elevation = 0f
        stateListAnimator = null
        isAllCaps = false

        background = resources.getDrawable(R.drawable.btn_ripple_highlighted)
    }
}