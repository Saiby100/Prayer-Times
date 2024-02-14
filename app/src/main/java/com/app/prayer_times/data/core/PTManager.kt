package com.app.prayer_times.data.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.runBlocking
import com.app.prayer_times.utils.datetime.Date
import com.app.prayer_times.utils.datetime.Time
import com.app.prayer_times.utils.debug.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PTManager (private val context: Context, startDate: Date = Date()) {
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

    suspend fun getTodayTimesJob(): MutableList<Time>? {
        return withContext(Dispatchers.IO) {
            timesList = getMonthTimes(date.month, date.year)!!
            getDayTimes()
        }
    }

    suspend fun getTodayTimes(): MutableList<Time>? {
        return withContext(Dispatchers.IO) {
            Logger.logMsg(date.toString())
            Logger.logMsg("Fetching current times")
            timesList = getMonthTimes(date.month, date.year)!!
            fetchNextMonthTimes()
            fetchPrevMonthTimes()
            getDayTimes()
        }
    }

    suspend fun getNextDayTimes(): MutableList<Time>? {
        return withContext(Dispatchers.IO) {
            date.changeDay(1)
            if (date.day == 1) {
                if (!hasInternetConnection()) {
                    date.changeDay(-1)
                } else {
                    Logger.logMsg("Moved into next month ${date.month}")
                    nextMonthJob.join()
                    prevMonthJob.cancel()

                    //make prev month current month
                    prevTimesList = timesList
                    //make current month next month
                    timesList = nextTimesList
                    fetchNextMonthTimes(3)
                }
            }
            getDayTimes()
        }
    }

    suspend fun getPrevDayTimes(): MutableList<Time>? {
        return withContext(Dispatchers.IO) {
            val oldDay = date.day
            date.changeDay(-1)
            if (date.day > oldDay) {
                if (!hasInternetConnection()) {
                    date.changeDay(1)
                } else {
                    Logger.logMsg("Moved into previous month ${date.month}")
                    prevMonthJob.join()
                    nextMonthJob.cancel()

                    //make next month current month
                    nextTimesList = timesList
                    //make current month previous month
                    timesList = prevTimesList
                    fetchPrevMonthTimes(3)
                }
            }

            getDayTimes()
        }
    }

    private suspend fun getMonthTimes(month: Int, year: Int): MutableList<String>? {
        val list: MutableList<String>?

        if (hasLocalData(year, month)) {
            Logger.logMsg("Local data found")
            list = PTDataStore.getPrayerTimes(thisArea, year, month, context)

            if (prayerTitles.size == 0) {
                prayerTitles.addAll(PTDataStore.prayerTitles)
            }
            return list
        }

        waitForInternet()
        Logger.logMsg("Scraping new data")
        list = PTScraper.getPrayerTimesMonth(year, month)

        if (prayerTitles.size == 0) {
            prayerTitles.addAll(PTScraper.prayerTitles)
        }

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
        return list
    }

    private fun getDayTimes(): MutableList<Time>? {
        if (timesList.size == 0) {
            Logger.logMsg("Times list is empty")
            return null
        }

        val size = prayerTitles.size
        val result: MutableList<Time> = mutableListOf()

        // Get all times for the day and return it
        val startIndex = (date.day - 1) + (size - 1) * (date.day - 1)
        val endIndex = startIndex + 5

        if (endIndex >= timesList.size) {
            return null
        }

        for (i in startIndex .. endIndex) {
            result.add(Time(timesList[i]))
        }
        return result
    }

    private suspend fun fetchNextMonthTimes(delaySeconds: Long = 0) {
        nextMonthJob = CoroutineScope(Dispatchers.IO).launch {
            delay(delaySeconds*1000)
            val month = date.getNextMonth(date.month)
            val year = if (month == 1) {
                date.year + 1
            } else {
                date.year
            }
            nextTimesList = getMonthTimes(month, year)!!
            Logger.logMsg("Fetching next month ($month) times completed")
        }
    }

    private suspend fun fetchPrevMonthTimes(delaySeconds: Long = 0) {
        prevMonthJob = CoroutineScope(Dispatchers.IO).launch {
            delay(delaySeconds*1000)
            val month = date.getPrevMonth(date.month)
            val year = if (month == 12) {
                date.year - 1
            } else {
                date.year
            }
            prevTimesList = getMonthTimes(month, year)!!
            Logger.logMsg("Fetching previous month ($month) times completed")
        }
    }

    private suspend fun waitForInternet(): Unit = suspendCoroutine { continuation ->
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

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as
                ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    /**
     * Checks if their are prayer times stored locally for the specified [year]
     * and [month]. The [context] is needed to check local storage.
     * @return true if data exists in local storage, false otherwise.
     */
    private fun hasLocalData(year: Int, month: Int): Boolean {
        return PTDataStore.hasLocalData(thisArea, year, month, context)
    }
}

fun main() = runBlocking {
}