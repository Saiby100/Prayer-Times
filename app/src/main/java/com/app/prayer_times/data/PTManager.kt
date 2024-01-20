package com.app.prayer_times.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.runBlocking
import com.app.prayer_times.utils.Date
import kotlinx.coroutines.Job


object PTManager {
    private val date = Date()

    private var timesList: MutableList<String> = mutableListOf()
    var prayerTitles: MutableList<String> = mutableListOf()

    private var nextTimesList: MutableList<String> = mutableListOf()
    private var prevTimesList: MutableList<String> = mutableListOf()
    private var nextYear: Int = 0
    private var nextMonth: Int = 0
    private var prevYear: Int = 0
    private var prevMonth: Int = 0


    private var thisMonth: Int = date.month
    private var thisYear: Int = date.year
    private lateinit var thisArea: String

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
        return PTScraper.getAreaTitles()
    }

    /**
     * Gets the prayer times for [year] and [month].
     * Parameters [nextMonthJob] and [prevMonthJob] needed to ensure job completion
     * before assignment.
     * Parameter [context] used for storing in local storage.
     */
    suspend fun getPrayerTimesMonth(
        year: Int,
        month: Int,
        nextMonthJob: Job,
        prevMonthJob: Job,
        context: Context
    ): MutableList<String>? {
        if (thisYear == year && thisMonth == month && timesList.size != 0) {
            return timesList
        }

        var list: MutableList<String>? = mutableListOf()

        //Complete fetching adjacent months
        if (isAdjacent(month)) {
            nextMonthJob.join()
            prevMonthJob.join()
        }

        if (PTDataStore.hasLocalData(thisArea, year, month, context)) {
            //Fetch from local storage
            if (month == prevMonth) {
                nextTimesList = timesList
            } else if (month == nextMonth) {
                prevTimesList = timesList
            }

            list = PTDataStore.getPrayerTimes(thisArea, year, month, context)
            prayerTitles.clear()
            prayerTitles.addAll(PTDataStore.titles)

        } else if (year == nextYear && month == nextMonth) {
            //Use next month stored in memory
            prevTimesList = timesList
            list = nextTimesList

            nextTimesList = mutableListOf()
        } else if (year == prevYear && month == prevMonth) {
            //Use previous month stored in memory
            nextTimesList = timesList
            list = prevTimesList

            prevTimesList = mutableListOf()
        } else {
            //New request
            list = PTScraper.getPrayerTimesMonth(year, month)
            prayerTitles.clear()
            prayerTitles.addAll(PTScraper.prayerTitles)

            //Store this response if it's the current year and month
            if (thisYear == year && thisMonth == month) {
                PTDataStore.savePrayerTimes(
                    list,
                    thisArea,
                    prayerTitles,
                    thisYear,
                    thisMonth,
                    context
                )
            }
        }
        setDateVars(year, month)

        if (list != null) {
            timesList = list
        }
        return list
    }

    /**
     * Checks if [month] is the same as the previous or next month.
     * @return true if [month] is same as previous or next one, false otherwise.
     */
    private fun isAdjacent(month: Int): Boolean {
        return month == prevMonth || month == nextMonth
    }

    /**
     * Fetches and stores next month's times in local storage.
     * Intended to be called in the background and stored as next month.
     */
    suspend fun fetchNextMonth() {
        val list = PTScraper.getPrayerTimesMonth(nextYear, nextMonth)
        if (list != null) {
            nextTimesList = list
        }
    }

    /**
     * Fetches and stores previous month's times in local storage.
     * Intended to be called in the background and stored as previous month.
     */
    suspend fun fetchPrevMonth() {
        val list = PTScraper.getPrayerTimesMonth(prevYear, prevMonth)
        if (list != null) {
            prevTimesList = list
        }
    }

    /**
     * Takes the [year], [month], and [day] parameters and fetches the appropriate
     * times.
     * The [nextMonthJob] and [prevMonthJob] parameters are the threads that fetch
     * adjacent months.
     * [context] is used to store the data in local storage if necessary.
     * @return a list of times for the specified [day].
     */
    suspend fun getPrayerTimesDay(
        year: Int,
        month: Int,
        day: Int,
        nextMonthJob: Job,
        prevMonthJob: Job,
        context: Context
    ): MutableList<String>? {
        getPrayerTimesMonth(year, month, nextMonthJob, prevMonthJob, context)

        return if (timesList.size == 0) {
           null
        } else {
            val size = prayerTitles.size
            val result: MutableList<String> = mutableListOf()

            // Get all times for the day and return it
            val startIndex = (day - 1) + (size - 1) * (day - 1)
            val endIndex = startIndex + 5

            if (endIndex >= timesList.size) {
                return null
            }

            for (i in startIndex .. endIndex) {
                result.add(timesList[i])
            }
            return result
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

    fun setDateVars(year: Int, month: Int) {
        thisYear = year
        thisMonth = month

        prevMonth = date.getPrevMonth(month)
        prevYear = if (prevMonth == 12) {
            year - 1
        } else {
            year
        }

        nextMonth = date.getNextMonth(month)
        nextYear = if (nextMonth == 1) {
            year + 1
        } else {
            year
        }
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