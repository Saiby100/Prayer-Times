package com.app.prayer_times.utils.permissions

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.app.prayer_times.utils.debug.Logger

object Permission {
    fun getNotificationPermission(activity: Activity) : Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission(android.Manifest.permission.POST_NOTIFICATIONS, activity)) {
                ActivityCompat.requestPermissions(activity,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1)
                return false
            }
        }
        return true
    }

    private fun hasPermission(permission: String, activity: Activity): Boolean {
        return ActivityCompat.checkSelfPermission(activity, permission) ==
                PackageManager.PERMISSION_GRANTED
    }
}