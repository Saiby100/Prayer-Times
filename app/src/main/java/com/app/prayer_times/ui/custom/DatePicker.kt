package com.app.prayer_times.ui.custom

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.app.prayer_times.utils.debug.Logger
import java.util.Calendar

class DatePicker: DialogFragment(), DatePickerDialog.OnDateSetListener {
    interface OnDateSelectedListener {
        fun onDateSelected(year: Int, month: Int, dayOfMonth: Int)
    }
    private var listener: OnDateSelectedListener? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(requireContext(), this, year, month, day)
    }
    fun setOnDateSelectedListener(selectedListener: OnDateSelectedListener) {
        listener = selectedListener
    }
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        listener?.onDateSelected(year, month+1, dayOfMonth)
//        Logger.logDebug("Date was set to $dayOfMonth/${month + 1}/$year")
    }
}