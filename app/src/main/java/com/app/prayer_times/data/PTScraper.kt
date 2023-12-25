package com.app.prayer_times.data

import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException


object PTScraper {
   private const val url: String = "https://masjids.co.za/salaahtimes"
   private lateinit var timesUrl: String

   var prayerTitles: MutableList<String> = mutableListOf()
   private var timesList: MutableList<String> = mutableListOf()
   private var areaSet: Boolean = false

   private var thisMonth: Int = 0
   private var thisYear: Int = 0

   /**
    * This gets the area titles.
    */
   fun getAreaTitles(): Array<String>? {
      val titles = mutableListOf<String>()

      try {
         val doc: Document = Jsoup.connect(url).get()
         val container = doc.select(".col-lg-8")
         val areaTags = container.select("h5")

         for (area in areaTags) {
            titles.add(area.text())
         }
      } catch (e: IOException) {
         return null
      }

      return titles.toTypedArray()
   }

   /**
    * Set the timesUrl for the given area
    */
   fun setArea(area: String) {
      val areaString = area.replace("\\s".toRegex(), "").lowercase()
      timesUrl = "$url/$areaString"
      areaSet = true
   }

   fun getPrayerTimesMonth(year: Int, month: Int): MutableList<String>? {
      if (!areaSet) {
         println("Area not set")
         return null

      } else if (thisYear == year && thisMonth == month && timesList.size != 0) {
         return timesList
      }

      thisYear = year
      thisMonth = month
      timesList.clear()
      prayerTitles.clear()

      val doc: Document = Jsoup.connect("$timesUrl/$year-$month").get()
      val table = doc.select("table.table-striped")
      val tableData = table.select("td")

      // Initialize the titles
      val thead = table.select("thead")
      if (thead != null) {
         val tHeaders = thead[0].select("th")
         for (header in tHeaders) {
            prayerTitles.add(header.text())
         }
         // Remove first 2 titles (date and day)
         prayerTitles.removeAt(0)
         prayerTitles.removeAt(0)
      }

      // Populate the times list
      for (td in tableData) {
         timesList.add(td.text())
      }

      // Remove date and day from times data
      val timesListSize: Int = timesList.size
      for (i in 0..<timesListSize step prayerTitles.size + 2) {
         timesList.remove(tableData[i].text())
         timesList.remove(tableData[i+1].text())
      }
      return timesList
   }

   fun getPrayerTimesDay(year: Int, month: Int, day: Int): MutableList<String>? {
      val result: MutableList<String> = mutableListOf()
      if (thisYear != year || thisMonth != month || timesList.size == 0) {
         getPrayerTimesMonth(year, month)
      }

      val size = prayerTitles.size

      return if (timesList.size == 0) {
         null
      } else {
         // Get all times for the day and return it
         val startIndex = (day - 1) + (size - 1) * (day - 1)
         val endIndex = startIndex + 5

         if (startIndex >= timesList.size) {
            println("Invalid day")
            return null
         }

         var j: Int = 0
         for (i in startIndex .. endIndex) {
            result.add("${prayerTitles[j]}: ${timesList[i]}")
            j++
         }
         return result
      }
   }

}
fun main() = runBlocking {
   PTScraper.setArea("Pretoria")
   val times = PTScraper.getPrayerTimesDay(2023, 12, 25)
   if (times != null) {
      for (time in times) {
         println(time)
      }
   }

   //For testing
//   val titles: Array<String>? = PTScraper.getAreaTitles()
//   PTScraper.setArea("Cape Town")
//
//   if (titles != null) {
//      for (title in titles) {
//         println(title)
//      }
//   }
//   val times = PTScraper.getPrayerTimesMonth(2023, 12)
//   var temp = 0
//   if (times != null) {
//      times.forEachIndexed { index, time ->
//         if (index % 6 == 0 && index != 0) {
//            println()
//            temp = index
//         }
//         print("${PTScraper.prayerTitles[index-temp]}: $time ")
//      }
//   }
//
//   val dayTimes = PTScraper.getPrayerTimesDay(2022, 12, 7)
//   if (dayTimes != null) {
//      dayTimes.forEach { time ->
//         println(time)
//      }
//   }

}