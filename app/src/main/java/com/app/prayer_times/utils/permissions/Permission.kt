package com.app.prayer_times.utils.permissions

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object Permission {

    fun getPermission(permission: String, activity: Activity): Boolean {
        val hasPermission: Boolean = hasPermission(permission, activity)
        if (hasPermission) {
            return true
        }

        when (permission) {
            android.Manifest.permission.POST_NOTIFICATIONS ->
                getNotificationPermission(activity)
        }

        return false
    }

    private fun getNotificationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(activity,
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            1)
    }

    private fun hasPermission(permission: String, activity: Activity): Boolean {
        return ActivityCompat.checkSelfPermission(activity, permission) ==
                PackageManager.PERMISSION_GRANTED
    }
}