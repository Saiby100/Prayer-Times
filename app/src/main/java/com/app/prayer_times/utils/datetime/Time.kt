package com.app.prayer_times.utils.datetime

import java.time.LocalTime
import java.util.Calendar

class Time(private var hour: Int, private var minute: Int) {
    constructor(timeString: String) : this(
        timeString.substringBefore(":").toInt(),
        timeString.substringAfter(":").toInt()
    )

    constructor(time: LocalTime) : this(time.hour, time.minute)

    /**
     * Changes this Time instance to [hours] and [minutes] earlier.
     */
    fun setEarlier(hours: Int = 0, minutes: Int = 0) {
        hour -= hours
        minute -= minutes

        if (hour < 0) {
            hour += 24
        }

        if (minute < 0) {
            minute += 60
            setEarlier(1)
        }
    }

    /**
     * Changes this Time instance to [hours] and [minutes] later.
     */
    fun setLater(hours: Int = 0, minutes: Int = 0) {
        hour += hours
        minute += minutes

        if (hour > 23) {
            hour -= 24
        }

        if (minute > 60) {
            minute -= 60
            setLater(1)
        }
    }

    /**
     * Compares this instance of Time to the specified [time] instance.
     * @return less than, equal to, or greater than 0 if this Time occurs before, the same, or
     * after the [time] instance respectively.
     */
    fun timeCmp(time: Time): Int {
        if (time.hour == this.hour) {
            return this.minute - time.minute
        }

        if (this.hour == 0 || time.hour == 0) {
            return time.hour - this.hour
        }
        return this.hour - time.hour
    }

    /**
     * Converts this Time instance to milliseconds and returns it.
     * @return a Long that represents this Time instance in milliseconds.
     */
    fun toMillis(): Long {
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)

            if (hour == 0) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return calendar.timeInMillis
    }

    override fun toString(): String {
        var hourString: String = "$hour"
        var minString: String = "$minute"

        if (hour < 10)
            hourString = "0$hour"
        if (minute < 10)
            minString = "0$minute"

        return "$hourString:$minString"
    }
}

fun main() {
    val time1 = Time(0, 10)
    val time2 = Time(23, 10)
//    val time2 = Date().currentTime()
    println(time1.toMillis() - time2.toMillis())
}