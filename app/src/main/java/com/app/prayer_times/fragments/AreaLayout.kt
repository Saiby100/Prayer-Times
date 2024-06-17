package com.app.prayer_times.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.add
import androidx.lifecycle.lifecycleScope
import com.app.prayer_times.R
import com.app.prayer_times.data.core.PTManager
import com.app.prayer_times.data.preferences.UserPrefs
import com.app.prayer_times.ui.custom.AreaListItem
import com.app.prayer_times.utils.datetime.Date
import com.app.prayer_times.utils.debug.Logger
import com.app.prayer_times.utils.network.Network
import kotlinx.coroutines.launch

class AreaLayout : Fragment(R.layout.select_area_layout) {

    private lateinit var ptManager: PTManager
    private lateinit var userPrefs: UserPrefs
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPrefs = UserPrefs(requireContext())

        initAreaList()
    }

    private fun initAreaList() {
        val areaList = requireView().findViewById<LinearLayout>(R.id.areaList)
        ptManager = PTManager(requireContext(), Date())
        Logger.logDebug("Initialised pt manager")

        if (!Network.hasInternetConnection(requireContext())) {
            Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
        lifecycleScope.launch {
            try {
                val areaStrings: Array<String>? = ptManager.getAreaTitles()
                Logger.logDebug("Fetched area titles $areaStrings")
                if (areaStrings != null) {
                    var button: Button
                    for (area in areaStrings) {
                        button = AreaListItem(requireContext(), area)
                        button.setOnClickListener {
                            Toast.makeText(requireContext(), "$area was selected", Toast.LENGTH_SHORT).show()
                            handleAreaSelected(area)
                        }
                        areaList.addView(button)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to fetch areas", Toast.LENGTH_SHORT).show()
                Log.e("ERROR", "Failed to fetch areas: $e")
            }
        }

    }

    private fun handleAreaSelected(areaString: String) {
        userPrefs.setString("user_area", areaString)
        //Change Fragment
        parentFragmentManager.commit {
            setReorderingAllowed(true)
            add<TimesLayout>(R.id.fragment_container)
        }
    }
}