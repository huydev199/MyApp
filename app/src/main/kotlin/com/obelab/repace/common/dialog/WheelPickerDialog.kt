package com.obelab.repace.common.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.obelab.repace.R
import com.obelab.repace.common.adapter.WheelPickerAdapter
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.model.WheelPickerModel
import com.super_rabbit.wheel_picker.OnValueChangeListener
import com.super_rabbit.wheel_picker.WheelPicker

class WheelPickerDialog(context: Context) : Dialog(context) {
    init {
//        dialog = Dialog(context)
        dialog = Dialog(context, R.style.FullWidthDialog)

    }

    companion object {
        var dialog: Dialog? = null
    }

    private lateinit var dialogView: View;
    var wheelPickerAdapter: WheelPickerAdapter =
        WheelPickerAdapter(WheelPickerModel("unit", mutableListOf(1.0, 2.0, 3.0, 4.0, 5.0)));
    var selectedValue = 0.0
    

    fun showPopup() {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_wheel_picker, null, false)
//        dialog?.setCancelable(true)
        dialog?.setContentView(dialogView)
//        dialog?.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
        dialog?.show()

        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<TextView>(R.id.btnOk)
        val wheelPicker = dialogView.findViewById<WheelPicker>(R.id.wheelPicker)
        val tvUnit = dialogView.findViewById<TextView>(R.id.tvUnit)

        // Left is ok
        // Right is cancel
        btnCancel.setOnClickListener {
            dialog!!.dismiss()
            onClickButtonLeft?.invoke()
        }
        btnOk.setOnClickListener {
            dialog!!.dismiss()
            onClickButtonRight?.invoke(selectedValue)
        }
        wheelPicker.setAdapter(wheelPickerAdapter)
        wheelPicker.setOnValueChangeListener(object : OnValueChangeListener {
            override fun onValueChange(picker: WheelPicker, oldVal: String, newVal: String) {
                Functions.showLog("onValueChange: $newVal")
                selectedValue = newVal.toDouble()
            }
        })




//        tvUnit.setAdapter(tvUnitAdapter)
        tvUnit.text = wheelPickerAdapter.pickerModel.unit

        if(wheelPickerAdapter.pickerModel.values.size>0){
            if(selectedValue != 0.0){
                for (i in 0 until wheelPickerAdapter.pickerModel.values.size) {
                    if(selectedValue == wheelPickerAdapter.pickerModel.values[i]){
                        wheelPicker.scrollTo(i)
                    }
                }
            } else {
                selectedValue = wheelPickerAdapter.pickerModel.values[wheelPickerAdapter.pickerModel.values.size/2]
                wheelPicker.scrollTo(wheelPickerAdapter.pickerModel.values.size/2)
            }
        }
    }

    var onClickButtonLeft: (() -> Unit)? = null
    var onClickButtonRight: ((selectValue: Double) -> Unit)? = null
}