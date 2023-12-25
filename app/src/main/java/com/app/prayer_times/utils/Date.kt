package com.app.prayer_times.utils

import java.util.Calendar

class Date {
    val monthStrings: Array<String> = arrayOf(
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    )

    val calendar: Calendar
    var year: Int
    var month: Int
    var day: Int

    init {
        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    fun changeDay(dayVal: Int) {
        calendar.add(Calendar.DAY_OF_MONTH, dayVal)
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    fun changeMonth(monthVal: Int) {
        calendar.add(Calendar.MONTH, monthVal)
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    fun changeYear(yearVal: Int) {
        calendar.add(Calendar.YEAR, yearVal)
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    fun monthString(): String {
        return monthStrings[month - 1]
    }

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
    date.changeDay(-1)

    println(date.day)
    println(date.month)
    println(date.year)
}