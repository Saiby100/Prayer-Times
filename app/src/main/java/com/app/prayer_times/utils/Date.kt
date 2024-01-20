package com.app.prayer_times.utils

import android.util.Log
import java.lang.NumberFormatException
import java.util.Calendar

class Date {
    val monthStrings: Array<String> = arrayOf(
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    )

    private val calendar: Calendar = Calendar.getInstance()
    var year: Int
    var month: Int
    var day: Int

    private val currentDay: Int
    private val currentMonth: Int
    private val currentYear: Int

    init {
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)

        currentYear = year
        currentMonth = month
        currentDay = day
    }

    /**
     * Adds [dayVal] to the current day.
     * Month and year will update automatically if necessary.
     */
    fun changeDay(dayVal: Int) {
        calendar.add(Calendar.DAY_OF_MONTH, dayVal)
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * Adds [monthVal] to the current month.
     * Day and Year will update automatically if necessary.
     */
    fun changeMonth(monthVal: Int) {
        calendar.add(Calendar.MONTH, monthVal)
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * Gets the month name for the current month.
     * @return the current month.
     */
    fun monthString(): String {
        return monthStrings[month - 1]
    }

    /**
     * Returns the next month starting at [startMonth].
     * @return the next month (1 - 12).
     */
    fun getNextMonth(startMonth: Int): Int {
        val nextMonth = startMonth + 1
        if (nextMonth > 12)
            return 1

        return nextMonth
    }

    /**
     * Returns the previous month starting at [startMonth].
     * @return the previous month (1 - 12).
     */
    fun getPrevMonth(startMonth: Int): Int {
        val prevMonth = startMonth - 1
        if (prevMonth < 1)
            return 12

        return prevMonth
    }

    /**
     * Gets the index of the first time in [times] that occurs after the current time.
     * @return The index of the first time following the current time.
     */
    fun timeCmp(times: List<String>): Int {
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minute: Int = calendar.get(Calendar.MINUTE)

        for (i in times.indices) {
            val timeSplit: List<String> = times[i].split(":")

            if (timeSplit.size != 2)
                return -1

            val hourInput: Int = timeSplit[0].toInt()
            val minInput: Int = timeSplit[1].toInt()

            if (hour < hourInput) {
                return i
            } else if (hour == hourInput && minute < minInput) {
                return i
            }
        }

        return -1
    }

    /**
     * Checks if the input date matches the current date.
     */
    fun isToday(): Boolean {
        return day == currentDay && month == currentMonth && year == currentYear
    }
}

fun main() {
    val date = Date()
    println("Current date: year: ${date.year}, month: ${date.month}, day: ${date.day}")
    println(date.getNextMonth(11))
    println(date.getNextMonth(12))
    println(date.getPrevMonth(3))
    println(date.getPrevMonth(1))
    println("Current date: year: ${date.year}, month: ${date.month}, day: ${date.day}")
//    date.changeDay(-1)
//
//    println(date.day)
//    println(date.month)
//    println(date.year)
}