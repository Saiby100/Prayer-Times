package com.app.prayer_times.api

import android.content.Context

class PTApi (appContext: Context){
    private val context: Context

    private lateinit var area: String

    init {
        context = appContext
    }

    fun setArea(area: String) {
        this.area = area
    }

}