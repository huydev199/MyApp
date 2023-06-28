package com.obelab.repace.model

import java.io.Serializable

data class RepaceBattery (
    var dataType: String,
    var data: ByteArray?,
    var length: Byte?,
    var status: String?,
    var rawData: ArrayList<Byte>?,
    var level: Byte?
) : Serializable {
    companion object {
        val empty = RepaceBattery("", null, null, null, null, null)
    }
}