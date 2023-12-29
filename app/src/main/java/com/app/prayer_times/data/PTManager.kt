package com.app.prayer_times.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.runBlocking
import java.util.Calendar

object PTManager {
    private var timesList: MutableList<String> = mutableListOf()
    private var prayerTitles: MutableList<String> = mutableListOf()

    private var calendar = Calendar.getInstance()
    private var thisMonth: Int = calendar.get(Calendar.MONTH) + 1
    private var thisYear: Int = calendar.get(Calendar.YEAR)
    private lateinit var thisArea: String

    fun initArea(area: String) {
        thisArea = area
        PTScraper.setArea(area)
//        Log.d("MESSAGE", "PTScraper initialization successful")
    }

    suspend fun getAreaTitles(): Array<String>? {
        return PTScraper.getAreaTitles()
    }

    suspend fun getPrayerTimesMonth(
        year: Int,
        month: Int,
        context: Context
    ): MutableList<String>? {
        if (thisYear == year && thisMonth == month && timesList.size != 0) {
//            Log.d("MESSAGE", "Times list already initialized so just sending it back")
            return timesList
        }

        timesList.clear()

        val list: MutableList<String>?

        if (PTDataStore.hasLocalData(thisArea, year, month, context)) {
//            Log.d("MESSAGE", "Local data found")
//            Log.d("MESSAGE", "reading from local storage")
            list = PTDataStore.getPrayerTimes(thisArea, year, month, context)
//            Log.d("MESSAGE", "Setting prayer titles")
            prayerTitles = PTDataStore.titles
        } else {
//            Log.d("MESSAGE", "No local data found")
//            Log.d("MESSAGE", "Making web request")
            list = PTScraper.getPrayerTimesMonth(year, month)
//            Log.d("MESSAGE", "Setting prayer titles")
            prayerTitles = PTScraper.prayerTitles

            //Store this response if it's the current year and month
            if (thisYear == year && thisMonth == month) {
//                Log.d("MESSAGE", "Is current year and month. Saving web request data")
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
        thisYear = year
        thisMonth = month

        if (list != null) {
            timesList = list
        }
        return list
    }

    suspend fun getPrayerTimesDay(
        year: Int,
        month: Int,
        day: Int,
        context: Context
    ): MutableList<String>? {
        getPrayerTimesMonth(year, month, context)

        return if (timesList.size == 0) {
           null
        } else {
            val size = prayerTitles.size
            val result: MutableList<String> = mutableListOf()
            // Get all times for the day and return it
            val startIndex = (day - 1) + (size - 1) * (day - 1)
            val endIndex = startIndex + 5

            if (startIndex >= timesList.size) {
                return null
            }

            for ((j, i) in (startIndex .. endIndex).withIndex()) {
                result.add("${prayerTitles[j]}: ${timesList[i]}")
            }
            return result
        }
    }

    fun hasLocalData(year: Int, month: Int, context: Context): Boolean {
        return PTDataStore.hasLocalData(thisArea, year, month, context)
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