package com.app.prayer_times.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.app.prayer_times.utils.debug.Logger

object Network {
    fun hasInternetConnection(
        activityContext: Context,
        actionNoInternet: () -> Unit = { Logger.logMsg("No Internet")}
    ): Boolean {
        val connectivityManager = activityContext.getSystemService(Context.CONNECTIVITY_SERVICE) as
                ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (!hasInternet) {
            actionNoInternet()
        }

        return hasInternet
    }
}