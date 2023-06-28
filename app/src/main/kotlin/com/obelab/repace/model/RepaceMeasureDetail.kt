package com.obelab.repace.model

import java.io.Serializable

data class RepaceMeasureDetail(
    var seqNumber: ArrayList<Byte>?,
    var ch1_780: ArrayList<Byte>?,
    var ch1_850: ArrayList<Byte>?,
    var ch1_940: ArrayList<Byte>?,
    var ch1_680: ArrayList<Byte>?,
    var ch2_780: ArrayList<Byte>?,
    var ch2_850: ArrayList<Byte>?,
    var ch2_940: ArrayList<Byte>?,
    var ch2_680: ArrayList<Byte>?,
    var ch3_780: ArrayList<Byte>?,
    var ch3_850: ArrayList<Byte>?,
    var ch3_940: ArrayList<Byte>?,
    var ch3_680: ArrayList<Byte>?,
    var ch4_780: ArrayList<Byte>?,
    var ch4_850: ArrayList<Byte>?,
    var ch4_940: ArrayList<Byte>?,
    var ch4_680: ArrayList<Byte>?,
    var ch5_780: ArrayList<Byte>?,
    var ch5_850: ArrayList<Byte>?,
    var ch5_940: ArrayList<Byte>?,
    var ch5_680: ArrayList<Byte>?,
    var ch6_780: ArrayList<Byte>?,
    var ch6_850: ArrayList<Byte>?,
    var ch6_940: ArrayList<Byte>?,
    var ch6_680: ArrayList<Byte>?,
    var rSO2: ArrayList<Byte>?,
    var ac_x: ArrayList<Byte>?,
    var ac_y: ArrayList<Byte>?,
    var ac_z: ArrayList<Byte>?,
    var gyro_x: ArrayList<Byte>?,
    var gyro_y: ArrayList<Byte>?,
    var gyro_z: ArrayList<Byte>?
): Serializable {
    companion object {
        val empty = RepaceMeasureDetail(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null, null, null, null, null, null, null, null, null)
    }
}