package com.app.prayer_times.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.runBlocking
import com.app.prayer_times.utils.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

object PTManager {
    private val date = Date()

    private var timesList: MutableList<String> = mutableListOf()
    var prayerTitles: MutableList<String> = mutableListOf()

    private var nextTimesList: MutableList<String> = mutableListOf()
    private var prevTimesList: MutableList<String> = mutableListOf()

    private lateinit var thisArea: String

    private lateinit var nextMonthJob: Job
    private lateinit var prevMonthJob: Job

    /**
     * Initializes area for PTScraper.
     */
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
                logMsg("Moved into next month")
                nextMonthJob.join()
                prevMonthJob.cancel()

                //make prev month current month
                prevTimesList = timesList
                //make current month next month
                timesList = nextTimesList
                //Thread to fetch next month
                fetchNextMonthTimes(context)
            }

            getDayTimes()
        }
    }

    suspend fun getPrevDayTimes(context: Context): MutableList<String>? {
        return withContext(Dispatchers.IO) {
            val oldDay = date.day
            date.changeDay(-1)
            if (date.day > oldDay) {
                logMsg("Moved into previous month")
                prevMonthJob.join()
                nextMonthJob.cancel()

                //make next month current month
                nextTimesList = timesList
                //make current month previous month
                timesList = prevTimesList
                //Thread to fetch previous month
                fetchPrevMonthTimes(context)
            }

            getDayTimes()
        }
    }

    suspend fun getMonthTimes(context: Context, month: Int, year: Int): MutableList<String>? {
        val list: MutableList<String>?

        if (hasLocalData(year, month, context)) {
            list = PTDataStore.getPrayerTimes(thisArea, year, month, context)

            if (prayerTitles.size == 0) {
                prayerTitles.addAll(PTDataStore.titles)
            }
            return list
        }

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

    private suspend fun fetchNextMonthTimes(context: Context) {
        nextMonthJob = CoroutineScope(Dispatchers.IO).launch {
            val month = date.getNextMonth(date.month)
            val year = if (month == 1) {
                date.year + 1
            } else {
                date.year
            }
            nextTimesList = getMonthTimes(context, month, year)!!
            logMsg("Fetching next month times completed")
        }
    }

    private suspend fun fetchPrevMonthTimes(context: Context) {
        prevMonthJob = CoroutineScope(Dispatchers.IO).launch {
            val month = date.getPrevMonth(date.month)
            val year = if (month == 12) {
                date.year - 1
            } else {
                date.year
            }

            prevTimesList = getMonthTimes(context, month, year)!!
            logMsg("Fetching previous month times completed")
        }
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
    val area: String = "Pretoria"
    PTManager.initArea(area)

    //GET MONTH
//    val times = PTManager.getPrayerTimesMonth(2023, 12)
//    if (times != null) {
//        var temp = 0
//        times.forEachIndexed { index, time ->
//            if (index % 6 == 0 && index != 0) {
//                println()
//                temp = index
//            }
//            print("${PTManager.prayerTitles[index-temp]}: $time ")
//        }
//    }

    //GET DAY
//    val times = PTManager.getPrayerTimesDay(2023, 12, 28)
//    val times2 = PTManager.getPrayerTimesDay(2023, 12, 29)
//    println()
//    if (times != null && times2 != null) {
//        for (time in times) {
//            println(time)
//        }
//
//        println()
//        for (time in times2) {
//            println(time)
//        }
//    }
}