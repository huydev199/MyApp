package com.obelab.repace.model

import java.io.Serializable

data class RepaceMeasure (
    var dataType: String,
    var data: ByteArray?,
    var rawData: ByteArray?,
    var length: Byte?,
    var sequence: ArrayList<Byte>?,
    var channel1: ArrayList<Byte>?,
    var channel2: ArrayList<Byte>?,
    var channel3: ArrayList<Byte>?,
    var channel4: ArrayList<Byte>?,
    var channel5: ArrayList<Byte>?,
    var channel6: ArrayList<Byte>?,
    var rSO2: ArrayList<Byte>?,
    var accel: ArrayList<Byte>?,
    var gyro: ArrayList<Byte>?
    ): Serializable {
    companion object {
        val empty = RepaceMeasure("", null, null, null, null, null, null, null, null, null, null, null, null, null)
    }
}
