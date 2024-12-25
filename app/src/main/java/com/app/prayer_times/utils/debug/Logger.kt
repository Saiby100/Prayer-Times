package com.app.prayer_times.utils.debug

import android.util.Log

object Logger {
    val tags = arrayOf("MESSAGE", "DEBUG", "ERROR")

    fun logMsg(text: String) {
        Log.d(tags[0], text)
    }

    fun logDebug(text: String) {
        Log.d(tags[1], text)
    }

    fun logErr(text: String) {
        Log.e(tags[2], text)
    }
}