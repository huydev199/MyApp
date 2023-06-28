package com.obelab.repace.common.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.obelab.repace.R
import com.obelab.repace.common.adapter.WheelPickerFeetAdapter
import com.obelab.repace.common.adapter.WheelPickerInchAdapter
import com.obelab.repace.core.functional.Functions
import com.super_rabbit.wheel_picker.OnValueChangeListener
import com.super_rabbit.wheel_picker.WheelPicker

class WheelPickerImperialDialog(context: Context) : Dialog(context) {
    init {
//        dialog = Dialog(context)
        dialog = Dialog(context, R.style.FullWidthDialog)

    }

    companion object {
        var dialog: Dialog? = null
    }

    private lateinit var dialogView: View;

    var wheelPickerFeetAdapter: WheelPickerFeetAdapter =
        WheelPickerFeetAdapter(mutableListOf(1, 2, 3, 4, 5));

    var wheelPickerInchAdapter: WheelPickerInchAdapter =
        WheelPickerInchAdapter(mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));

    var selectedValueFeet = 0
    var selectedValueInch = -1
    var selectValue = 0.0

    fun showPopup() {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_wheel_picker_weight_imperial, null, false)
//        dialog?.setCancelable(true)
        dialog?.setContentView(dialogView)
//        dialog?.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )

        dialog?.show()

        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<TextView>(R.id.btnOk)
        val wheelPickerFeet = dialogView.findViewById<WheelPicker>(R.id.wheelPickerFeet)
        val wheelPickerInch = dialogView.findViewById<WheelPicker>(R.id.wheelPickerInch)

        // Left is ok
        // Right is cancel
        btnCancel.setOnClickListener {
            dialog!!.dismiss()
            onClickButtonLeft?.invoke()
        }

        btnOk.setOnClickListener {
            dialog!!.dismiss()
            if (selectValue != null) {
                onClickButtonRight?.invoke(selectValue!!)
            }
        }

        wheelPickerFeet.setAdapter(wheelPickerFeetAdapter)
        wheelPickerFeet.setOnValueChangeListener(object : OnValueChangeListener {
            override fun onValueChange(picker: WheelPicker, oldVal: String, newVal: String) {
                Functions.showLog("onValueChange: $newVal")
                selectedValueFeet = newVal.toInt()
                selectValue = Functions.convertFeetToCm(selectedValueFeet)!! + Functions.convertInchToCm(selectedValueInch)!!
            }
        })


        if (wheelPickerFeetAdapter.listFeetValue.size > 0) {
            if (selectedValueFeet != 0) {
                for (i in 0 until wheelPickerFeetAdapter.listFeetValue.size) {
                    if (selectedValueFeet == wheelPickerFeetAdapter.listFeetValue[i]) {
                        wheelPickerFeet.scrollTo(i)
                    }
                }
            } else {
                selectedValueFeet = wheelPickerFeetAdapter.listFeetValue[wheelPickerFeetAdapter.listFeetValue.size / 2]
                wheelPickerFeet.scrollTo(wheelPickerFeetAdapter.listFeetValue.size / 2)
            }
        }

        wheelPickerInch.setAdapter(wheelPickerInchAdapter)
        wheelPickerInch.setOnValueChangeListener(object : OnValueChangeListener {
            override fun onValueChange(picker: WheelPicker, oldVal: String, newVal: String) {
                Functions.showLog("onValueChange: $newVal")
                selectedValueInch = newVal.toInt()
                selectValue = Functions.convertFeetToCm(selectedValueFeet)!! + Functions.convertInchToCm(selectedValueInch)!!
            }
        })


        if (wheelPickerInchAdapter.listFeetValue.size > 0) {
            if (selectedValueInch != -1) {
                for (i in 0 until wheelPickerInchAdapter.listFeetValue.size) {
                    if (selectedValueInch == wheelPickerInchAdapter.listFeetValue[i]) {
                        wheelPickerInch.scrollTo(i)
                    }
                }
            } else {
                selectedValueInch = wheelPickerInchAdapter.listFeetValue[wheelPickerInchAdapter.listFeetValue.size / 2]
                wheelPickerInch.scrollTo(wheelPickerInchAdapter.listFeetValue.size / 2)
            }
        }
    }

    var onClickButtonLeft: (() -> Unit)? = null
    var onClickButtonRight: ((selectValue: Double) -> Unit)? = null
}