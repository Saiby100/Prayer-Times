package com.app.prayer_times.data.preferences

import android.content.Context
import android.content.SharedPreferences

class UserPrefs(context: Context) {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    private val editor = sharedPrefs.edit()

    fun getString(key: String, default: String?): String? {
        return sharedPrefs.getString(key, default)
    }

    fun getBool(key: String, default: Boolean): Boolean {
        return sharedPrefs.getBoolean(key, default)
    }

    fun getInt(key: String, default: Int): Int {
        return sharedPrefs.getInt(key, default)
    }

    fun setString(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    fun setBool(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun setInt(key: String, value: Int) {
        editor.putInt(key, value)
        editor.apply()
    }

    fun getBoolList(keys: List<String>, default: Boolean): MutableList<Boolean> {
        val list: MutableList<Boolean> = mutableListOf()

        for (key in keys) {
            list.add(sharedPrefs.getBoolean(key, default))
        }
        return list
    }
}