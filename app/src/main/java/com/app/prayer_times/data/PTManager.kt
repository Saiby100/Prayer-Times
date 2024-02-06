package com.app.prayer_times.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.runBlocking
import com.app.prayer_times.utils.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PTManager (startDate: Date = Date()) {
    private lateinit var thisArea: String
    private val date: Date

    var prayerTitles: MutableList<String> = mutableListOf()

    private var prevTimesList: MutableList<String> = mutableListOf()
    private var timesList: MutableList<String> = mutableListOf()
    private var nextTimesList: MutableList<String> = mutableListOf()

    private lateinit var nextMonthJob: Job
    private lateinit var prevMonthJob: Job

    init {
        date = startDate
    }

    fun initArea(area: String) {
        thisArea = area
        PTScraper.setArea(area)
    }

    /**
     * Gets all area titles available.
     * @return array of area strings.
     */
    suspend fun getAreaTitles(): Array<String>? {
        return withContext(Dispatchers.IO) { PTScraper.getAreaTitles() }
    }

    suspend fun getTodayTimes(context: Context): MutableList<String>? {
        return withContext(Dispatchers.IO) {
            logMsg(date.toString())
            logMsg("Fetching current times")
            timesList = getMonthTimes(context, date.month, date.year)!!
            fetchNextMonthTimes(context)
            fetchPrevMonthTimes(context)
            getDayTimes()
        }
    }

    suspend fun getNextDayTimes(context: Context): MutableList<String>? {
        return withContext(Dispatchers.IO) {
            date.changeDay(1)
            if (date.day == 1) {
                if (!hasInternetConnection(context)) {
                    date.changeDay(-1)
                } else {
                    logMsg("Moved into next month ${date.month}")
                    nextMonthJob.join()
                    prevMonthJob.cancel()

                    //make prev month current month
                    prevTimesList = timesList
                    //make current month next month
                    timesList = nextTimesList
                    fetchNextMonthTimes(context, 3)
                }
            }
            getDayTimes()
        }
    }

    suspend fun getPrevDayTimes(context: Context): MutableList<String>? {
        return withContext(Dispatchers.IO) {
            val oldDay = date.day
            date.changeDay(-1)
            if (date.day > oldDay) {
                if (!hasInternetConnection(context)) {
                    date.changeDay(1)
                } else {
                    logMsg("Moved into previous month ${date.month}")
                    prevMonthJob.join()
                    nextMonthJob.cancel()

                    //make next month current month
                    nextTimesList = timesList
                    //make current month previous month
                    timesList = prevTimesList
                    fetchPrevMonthTimes(context, 3)
                }
            }

            getDayTimes()
        }
    }

    suspend fun getMonthTimes(context: Context, month: Int, year: Int): MutableList<String>? {
        val list: MutableList<String>?

        if (hasLocalData(year, month, context)) {
            logMsg("Local data found")
            list = PTDataStore.getPrayerTimes(thisArea, year, month, context)

            if (prayerTitles.size == 0) {
                prayerTitles.addAll(PTDataStore.prayerTitles)
            }
            return list
        }

        waitForInternet(context)
        logMsg("Scraping new data")
        list = PTScraper.getPrayerTimesMonth(year, month)
        if (date.month == month && date.year == year && list != null) {
            PTDataStore.savePrayerTimes(
                list,
                thisArea,
                prayerTitles,
                year,
                month,
                context
            )
        }

        if (prayerTitles.size == 0) {
            prayerTitles.addAll(PTScraper.prayerTitles)
        }

        return list
    }

    private fun getDayTimes(): MutableList<String>? {
        if (timesList.size == 0) {
            logMsg("Times list is empty")
            return null
        }

        val size = prayerTitles.size
        val result: MutableList<String> = mutableListOf()

        // Get all times for the day and return it
        val startIndex = (date.day - 1) + (size - 1) * (date.day - 1)
        val endIndex = startIndex + 5

        if (endIndex >= timesList.size) {
            return null
        }

        for (i in startIndex .. endIndex) {
            result.add(timesList[i])
        }
        return result
    }

    private suspend fun fetchNextMonthTimes(context: Context, delaySeconds: Long = 0) {
        nextMonthJob = CoroutineScope(Dispatchers.IO).launch {
            delay(delaySeconds*1000)
            val month = date.getNextMonth(date.month)
            val year = if (month == 1) {
                date.year + 1
            } else {
                date.year
            }
            nextTimesList = getMonthTimes(context, month, year)!!
            logMsg("Fetching next month ($month) times completed")
        }
    }

    private suspend fun fetchPrevMonthTimes(context: Context, delaySeconds: Long = 0) {
        prevMonthJob = CoroutineScope(Dispatchers.IO).launch {
            delay(delaySeconds*1000)
            val month = date.getPrevMonth(date.month)
            val year = if (month == 12) {
                date.year - 1
            } else {
                date.year
            }
            prevTimesList = getMonthTimes(context, month, year)!!
            logMsg("Fetching previous month ($month) times completed")
        }
    }

    suspend fun waitForInternet(context: Context): Unit = suspendCoroutine { continuation ->
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as
                ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                continuation.resume(Unit) //continue coroutine
                //unregister callback when connection is established
                connectivityManager.unregisterNetworkCallback(this)
            }
        }

        //Register callback
        val networkRequest = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun hasInternetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as
                ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        return hasInternet
    }

    /**
     * Checks if their are prayer times stored locally for the specified [year]
     * and [month]. The [context] is needed to check local storage.
     * @return true if data exists in local storage, false otherwise.
     */
    fun hasLocalData(year: Int, month: Int, context: Context): Boolean {
        return PTDataStore.hasLocalData(thisArea, year, month, context)
    }

    private fun logMsg(message: String) {
        Log.d("debugging", message)
    }
}

fun main() = runBlocking {
}