package com.app.prayer_times.data

import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlinx.coroutines.runBlocking

object PTDataStore {
    val titles: MutableList<String> = mutableListOf()
    /**
     * This saves prayer times to local storage.
     */
    fun savePrayerTimes(
        timesList: MutableList<String>?,
        area: String,
        titles: MutableList<String>?,
        year: Int,
        month: Int
    ) {
        try {
            val filename: String = "times_${month}${year}_${area}.csv"
            val file: File = File(filename)
            if (!file.exists()) {
                file.createNewFile()
            }

            val fileWriter = FileWriter(filename, false)
            val csvWriter = CSVWriter(fileWriter)

            csvWriter.writeNext(titles?.toTypedArray())
            csvWriter.writeNext(timesList?.toTypedArray())

            csvWriter.close()
            fileWriter.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * This gets prayer times from local storage.
     */
    fun getPrayerTimes(area: String, year: Int, month: Int): MutableList<String>? {
        val timesList: MutableList<String> = mutableListOf()
        var isFirstArrayRead: Boolean = false
        try {
            val filename: String = "times_${month}${year}_${area}.csv"
            if (!File(filename).exists()) {
                return null
            }
            val fileReader = FileReader(filename)
            val csvReader = CSVReader(fileReader)

            var array: Array<String>?

            while (csvReader.readNext().also {array = it} != null) {
                array?.let {
                    if (!isFirstArrayRead) {
                        titles.addAll(it)
                        isFirstArrayRead = true
                    } else {
                        timesList.addAll(it)
                    }
                }
            }

            csvReader.close()
            fileReader.close()

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return timesList
    }

    fun hasLocalData(area: String, year: Int, month: Int): Boolean {
        val filename: String = "times_${month}${year}_${area}.csv"
        val file: File = File(filename)

        return file.exists()
    }
}

fun main() = runBlocking {
//    val area: String = "Cape Town"
//    PTScraper.setArea(area)
//
//    val times = PTDataStore.getPrayerTimes(area, 2023, 12)
//    if (times != null) {
//        var temp = 0
////        PTDataStore.savePrayerTimes(times, area, PTScraper.prayerTitles, 2023, 12)
//
//        times.forEachIndexed { index, time ->
//            if (index % 6 == 0 && index != 0) {
//                println()
//                temp = index
//            }
//            print("${PTDataStore.titles[index-temp]}: $time ")
//        }
//    }
}