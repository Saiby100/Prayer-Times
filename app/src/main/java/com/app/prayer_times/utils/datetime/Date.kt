package com.app.prayer_times.utils.datetime

import com.app.prayer_times.utils.debug.Logger
import java.time.LocalTime
import java.util.Calendar

class Date {
    val monthStrings: Array<String> = arrayOf(
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    )
    val dayStrings: Array<String> = arrayOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    private val calendar: Calendar = Calendar.getInstance()
    var year: Int
    var month: Int
    var day: Int
    var weekDay: Int

    private val currentDay: Int
    private val currentMonth: Int
    private val currentYear: Int

    init {
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
        weekDay = calendar.get(Calendar.DAY_OF_WEEK)

        currentYear = year
        currentMonth = month
        currentDay = day
    }

    fun reset() {
        calendar.set(currentYear, currentMonth - 1, currentDay)

        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
        weekDay = calendar.get(Calendar.DAY_OF_WEEK)
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
        weekDay = calendar.get(Calendar.DAY_OF_WEEK)
    }

    /**
     * Gets the month name for the current month.
     * @return the current month.
     */
    fun monthString(): String {
        return monthStrings[month - 1]
    }

    fun dayString(): String {
        return dayStrings[weekDay - 1]
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
    fun timeCmp(times: List<Time>): Int {
        val currentTime = currentTime()

        for (i in times.indices) {
            if (times[i].timeCmp(currentTime) >= 0) {
                return i
            }
        }

        return -1
    }

    fun currentTime(): Time {
        return Time(LocalTime.now())
    }

    /**
     * Checks if the input date matches the current date.
     */
    fun isToday(): Boolean {
        return day == currentDay && month == currentMonth && year == currentYear
    }

    override fun toString(): String {
        return "$day ${monthString()} $year"
    }
}

fun main() {

    val hour = Calendar.HOUR_OF_DAY
    val minute = Calendar.MINUTE

    println("$hour:$minute")

    val date = Date()
    val time = Time("${hour}:${minute}")

    println(time.toMillis())
}