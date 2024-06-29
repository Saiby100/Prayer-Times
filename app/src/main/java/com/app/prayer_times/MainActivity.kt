package com.app.prayer_times

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.app.prayer_times.fragments.AreaLayout

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set action bar
        val actionBarColor = ContextCompat.getColor(this, R.color.dark_blue_secondary)
        actionBar?.setBackgroundDrawable(ColorDrawable(actionBarColor))

        setContentView(R.layout.fragment_container)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<AreaLayout>(R.id.fragment_container)
            }
        }
    }
}