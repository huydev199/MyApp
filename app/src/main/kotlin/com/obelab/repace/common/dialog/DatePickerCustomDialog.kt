package com.obelab.repace.common.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import java.util.*

/**
 * Created by BM Elon on 25/01/2022.
 */
class DatePickerCustomDialog(
    context: Context,
    private val calendarIn: Calendar,
    private val callback: DatePickerCustomCallback
): Dialog(context) {


    init {
        dialog = Dialog(context, R.style.FullWidthDialog)
    }

    companion object {
        var dialog: Dialog? = null
    }

    private lateinit var dialogView: View;

    private var calendarOut: Calendar? = null

    fun showPopup() {
        try {

            dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_date_picker_custom, null, false)
            dialog?.setContentView(dialogView)
            dialog?.show()

            val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
            val btnOk = dialogView.findViewById<TextView>(R.id.btnOk)
            val dataPicker = dialogView.findViewById<DatePicker>(R.id.date_picker_custom)

            this.calendarOut = calendarIn

            dataPicker.init(
                calendarIn.get(Calendar.YEAR),
                calendarIn.get(Calendar.DAY_OF_MONTH),
                calendarIn.get(Calendar.MONTH)
            ) { _, year, monthOfYear, dayOfMonth ->
                calendarOut?.let {
                    it.set(Calendar.YEAR, year)
                    it.set(Calendar.MONTH, monthOfYear)
                    it.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
            }
            dataPicker.maxDate = Calendar.getInstance().timeInMillis

            btnCancel.setOnClickListener {
                dialog?.dismiss()
            }
            btnOk.setOnClickListener {
                calendarOut?.let {
                    callback.callback(it)
                    dialog?.dismiss()
                }
            }

        } catch (e: Exception) {
            Functions.showLog("Show ConfirmDialog Error -> $e")
        }
    }

//    override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
//        calendarOut?.let {
//            it.set(Calendar.YEAR, year)
//            it.set(Calendar.MONTH, monthOfYear)
//            it.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//        }
//    }


}
class DatePickerCustomCallback(private val cb: (calender: Calendar) -> Unit) {
    fun callback(calender: Calendar) = cb(calender)
}
