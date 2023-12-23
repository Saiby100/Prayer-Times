package com.app.prayer_times.data

import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.time.DayOfWeek
import java.time.Month
import java.time.Year


object PTScraper {
   private const val url: String = "https://masjids.co.za/salaahtimes"
   private lateinit var timesUrl: String
   private val prayerTitles: Array<String> = arrayOf("Date", "Fajr", "Dhur", "Asr (S)", "Asr (H)", "Maghrib", "Isha")
   private var timesList: MutableList<String> = mutableListOf()
   private var areaSet: Boolean = false

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
      }

      val doc: Document = Jsoup.connect("$timesUrl/$year-$month").get()
      val table = doc.select("table.table-striped")
      val tableData = table.select("td")

      for (td in tableData) {
         timesList.add(td.text())
      }
      val timesListSize: Int = timesList.size
      //TODO: Change step size according to number of titles
      for (i in 0..<timesListSize step 8) {
         timesList.remove(tableData[i].text())
         timesList.remove(tableData[i+1].text())
         timesList.remove(tableData[i+3].text())
//         println("$i, ${i+1}, ${i+3}")
      }
      return timesList
   }

   fun getPrayerTimesDay(year: Year, month: Month, day: DayOfWeek) {

   }

}
fun main() = runBlocking {
   //For testing
   val titles: Array<String>? = PTScraper.getAreaTitles()
   PTScraper.setArea("Cape Town")

   if (titles != null) {
      for (title in titles) {
         println(title)
      }
   }
   val times = PTScraper.getPrayerTimesMonth(2023, 2)
   if (times != null) {
      times.forEachIndexed { index, time ->
         print("Time: $time ")
         if (index % 5 == 0 && index != 0) {
            println()
         }
      }
   }
}
// TODO: Get titles dynamically since only Cape Town includes Thur