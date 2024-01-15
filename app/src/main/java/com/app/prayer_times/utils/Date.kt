package com.app.prayer_times.utils

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

    init {
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
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
     * Sets the current year to [yearInput] and month to [monthInput].
     */
    fun setDate(yearInput: Int, monthInput: Int) {
        calendar.set(Calendar.YEAR, yearInput)
        calendar.set(Calendar.MONTH, monthInput-1)

        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * Returns the next month starting at [startMonth].
     * @return the next month (1 - 12).
     */
    fun getNextMonth(startMonth: Int): Int {
        calendar.set(Calendar.MONTH, startMonth - 1)
        calendar.add(Calendar.MONTH, 1)

        val result = calendar.get(Calendar.MONTH) + 1

        calendar.set(Calendar.MONTH, month - 1)

        return result
    }

    /**
     * Returns the previous month starting at [startMonth].
     * @return the previous month (1 - 12).
     */
    fun getPrevMonth(startMonth: Int): Int {
        calendar.set(Calendar.MONTH, startMonth - 1)
        calendar.add(Calendar.MONTH, -1)

        val result = calendar.get(Calendar.MONTH) + 1

        calendar.set(Calendar.MONTH, month - 1)

        return result
    }

    /**
     * Returns the current date of this Date instance.
     * [length] specifies the format of the date.
     * @return the date in 'LONG' or 'SHORT' format.
     */
    fun fullDate(length: String): String {
        if (length == "LONG") {
            return "$day ${monthString()} $year"
        } else {
            return "$$day ${monthString().substring(0, 3)} $year"
        }
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