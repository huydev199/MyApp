package com.obelab.repace.common.adapter

import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.util.Constants
import com.obelab.repace.model.WheelPickerModel
import com.super_rabbit.wheel_picker.WheelAdapter

class WheelPickerAdapter(val pickerModel: WheelPickerModel) : WheelAdapter {
    var unitType = pickerModel.unit
    override fun getMaxIndex(): Int {
        return pickerModel.values.size - 1
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
        Functions.showLog("SHOW DATA: " + unitType)
        return if (position >= 0 && position < pickerModel.values.size) {
            when {
                unitType.isEmpty() -> {
                    pickerModel.values[position].toString()
                }
                unitType == Constants.UNIT_TYPE_HEIGHT -> {
                    Functions.getHeightUnitValue(pickerModel.values[position]).toString()
                }
                unitType == Constants.UNIT_TYPE_WEIGHT -> {
                    Functions.getWeightUnitValue(pickerModel.values[position]).toString()
                }
                else -> {
                    pickerModel.values[position].toString()
                }
            }
        } else {
            ""
        }
    }
}