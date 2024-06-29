package com.app.prayer_times.fragments

import androidx.fragment.app.Fragment
import com.app.prayer_times.R
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.app.prayer_times.data.core.PTManager
import com.app.prayer_times.data.preferences.UserPrefs
import com.app.prayer_times.ui.custom.DatePicker
import com.app.prayer_times.ui.custom.PrayerCard
import com.app.prayer_times.utils.datetime.Date
import com.app.prayer_times.utils.datetime.Time
import com.app.prayer_times.utils.debug.Logger
import com.app.prayer_times.utils.network.Network
import com.app.prayer_times.utils.permissions.Permission
import com.app.prayer_times.utils.schedulers.MyJobScheduler
import com.app.prayer_times.utils.schedulers.NotificationScheduler
import kotlinx.coroutines.launch

class TimesLayout : Fragment(R.layout.times_layout) {
    private val date = Date()

    private lateinit var ptManager: PTManager
    private lateinit var userPrefs: UserPrefs
    private lateinit var dayTimes: MutableList<Time>
    private lateinit var scheduler: NotificationScheduler
    private lateinit var jobScheduler: MyJobScheduler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ptManager = PTManager(requireContext(), date)
        userPrefs = UserPrefs(requireContext())

        val selectedArea = userPrefs.getString("user_area", "Cape Town") //TODO: use location
        ptManager.initArea(selectedArea!!)
        initLayout()
    }

    private fun initLayout() {
        setLayoutButtons()
        fetchNewTimes(0)
    }

    private fun setLayoutButtons() {
        val todayButton: Button = requireView().findViewById(R.id.todayBtn)
        todayButton.text = "${date.day}"
        todayButton.setOnClickListener {
            if (!date.isToday()) {
                date.reset()
                ptManager.setDate(date)
                fetchNewTimes(0)
            }
        }
        requireView().findViewById<ImageButton>(R.id.datePicker).setOnClickListener {
            val datePicker = DatePicker()
            datePicker.show(parentFragmentManager, "datePicker")
        }

        requireView().findViewById<ImageButton>(R.id.nextDayBtn).setOnClickListener {
            fetchNewTimes(1)
        }
        requireView().findViewById<ImageButton>(R.id.prevDayBtn).setOnClickListener {
            fetchNewTimes(-1)
        }
    }

    /**
     * Fetches the times for the day relative to the current day (date.day), and
     * calls showNewTimes to update the ui.
     * Uses [from] to determine if it should get the previous, current, or
     * next day's times. (-1, 0, 1 respectively)
     * @param [from] indicates which day to call.
     */
    private fun fetchNewTimes(from: Int) {
        val prevDayBtn = requireView().findViewById<ImageButton>(R.id.prevDayBtn)
        val nextDayBtn = requireView().findViewById<ImageButton>(R.id.nextDayBtn)

        prevDayBtn.isEnabled = false
        nextDayBtn.isEnabled = false
        if (!Network.hasInternetConnection(requireContext()) {
                prevDayBtn.isEnabled = true
                nextDayBtn.isEnabled = true
                Toast.makeText(requireActivity(), "Check Internet connection and try again", Toast.LENGTH_SHORT).show()
            }) {
            return
        }

        lifecycleScope.launch {
            try {
                dayTimes = when {
                    from < 0 -> ptManager.getPrevDayTimes()
                    from > 0 -> ptManager.getNextDayTimes()
                    else -> ptManager.getTodayTimes()
                } ?: mutableListOf()

                if (dayTimes.isNotEmpty()) {
                    showNewTimes()
                } else {
                    Toast.makeText(requireActivity(), "No prayer times found", Toast.LENGTH_SHORT).show()
                }

                prevDayBtn.isEnabled = true
                nextDayBtn.isEnabled = true
            } catch (e: Exception) {
                Toast.makeText(requireActivity(), "Failed to get prayer times", Toast.LENGTH_SHORT).show()
                Logger.logErr("Failed to fetch prayer times: $e")
            }
        }
    }

    /**
     * Updates the timesList to the data in dayTimes.
     * Sets the alarm preferences.
     */
    private fun showNewTimes() {
        val layout = requireView().findViewById<LinearLayout>(R.id.timesList)
        val dateTitle = requireView().findViewById<TextView>(R.id.dateTitle)

        val dayText: TextView = requireView().findViewById(R.id.dayTitle)

        val dateString = "${date.day} ${date.monthString()} ${date.year}"
        dateTitle.text = dateString
        dayText.text = date.dayString()

        layout.removeAllViews()

        val targetIndex: Int =  if (date.isToday()) {
            date.timeCmp(dayTimes)
        } else {
            -1
        }
        val alarmPrefs: List<Boolean> = userPrefs.getBoolList(ptManager.prayerTitles, false)

        for (i in 0..< dayTimes.size) {
            val prayerTitle = ptManager.prayerTitles[i]
            val card = PrayerCard(
                requireContext(),
                prayerTitle,
                dayTimes[i].toString(),
                targetIndex == i,
                alarmPrefs[i]
            )

            card.notificationButton.setOnClickListener {
                if (Permission.getNotificationPermission(requireActivity())) {
                    if (!userPrefs.getBool(prayerTitle, false)) {
                        userPrefs.setBool(prayerTitle, true)
                        card.setNotificationIcon(true)

                        if (date.currentTime().timeCmp(dayTimes[i]) < 0 && date.isToday())
                            scheduler.scheduleReminder(dayTimes[i].toMillis(), prayerTitle)

                        if (!userPrefs.getBool("alarms_enabled", false)) {
                            jobScheduler.scheduleJob()
                            userPrefs.setBool("alarms_enabled", true)
                        }

                    } else {
                        userPrefs.setBool(prayerTitle, false)
                        card.setNotificationIcon(false)
                        scheduler.cancelReminder(prayerTitle)
                    }
                }
            }
            layout.addView(card)
        }
    }
}