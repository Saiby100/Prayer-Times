package com.app.prayer_times.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.prayer_times.R
import com.app.prayer_times.data.core.PTManager
import com.app.prayer_times.ui.custom.AreaListItem
import com.app.prayer_times.utils.datetime.Date
import com.app.prayer_times.utils.network.Network
import kotlinx.coroutines.launch

class AreaLayout : Fragment(R.layout.select_area_layout) {

    private lateinit var ptManager: PTManager
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAreaList()
    }

    private fun initAreaList() {
        val areaList = requireView().findViewById<LinearLayout>(R.id.areaList)
        ptManager = PTManager(requireContext(), Date())

        if (!Network.hasInternetConnection(requireContext())) {
            Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
        lifecycleScope.launch {
            try {
                val areaStrings: Array<String>? = ptManager.getAreaTitles()
                if (areaStrings != null) {
                    var button: Button
                    for (area in areaStrings) {
                        button = AreaListItem(requireContext(), area)
                        button.setOnClickListener {
                            Toast.makeText(requireContext(), "$area was selected", Toast.LENGTH_SHORT).show()
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
}