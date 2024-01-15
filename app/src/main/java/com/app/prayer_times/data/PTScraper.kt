package com.app.prayer_times.data

import android.util.Log
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException


object PTScraper {
   private const val url: String = "https://masjids.co.za/salaahtimes"
   private lateinit var timesUrl: String

   var prayerTitles: MutableList<String> = mutableListOf()
   private var areaSet: Boolean = false

   private var mutex = 0
   private var myQueueNum = -1

   /**
    * This gets the all area titles available on website.
    * @return an array of area strings.
    */
   suspend fun getAreaTitles(): Array<String>? {
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
    * Set the timesUrl for [area].
    */
   fun setArea(area: String) {
      val areaString = area.replace("\\s".toRegex(), "").lowercase()
      timesUrl = "$url/$areaString"
      areaSet = true
   }

   /**
    * Makes a web request fetching prayer times for [year] and [month].
    * @return list of all the times.
    */
   suspend fun getPrayerTimesMonth(year: Int, month: Int): MutableList<String>? {
      if (!areaSet) {
         println("Area not set")
         return null

      }

      myQueueNum++
      waitForThread(myQueueNum)

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


      val timesList: MutableList<String> = mutableListOf()

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

      mutex++
      return timesList
   }

   /**
    * Used to synchronise access to prayerTitles.
    */
   private fun waitForThread(queueNum: Int) {
      while (mutex != queueNum) {
         run {}
      }
   }
}
fun main() = runBlocking {
//   PTScraper.setArea("Pretoria")
//
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
}