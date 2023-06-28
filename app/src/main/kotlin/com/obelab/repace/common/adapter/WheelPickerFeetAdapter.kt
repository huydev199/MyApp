package com.obelab.repace.common.adapter

import com.super_rabbit.wheel_picker.WheelAdapter

class WheelPickerFeetAdapter(val listFeetValue:  List<Int>) : WheelAdapter {

    override fun getMaxIndex(): Int {
        return listFeetValue.size - 1
    }

    override fun getMinIndex(): Int {
        return 0
    }

    override fun getPosition(vale: String): Int {
        return 0
    }

    override fun getTextWithMaximumLength(): String {
        return "Maximum for select"
    }

    override fun getValue(position: Int): String {
        return if (position >= 0 && position < listFeetValue.size) {
            listFeetValue[position].toString()
        } else {
            ""
        }
    }
}