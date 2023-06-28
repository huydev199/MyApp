package com.obelab.repace.model

import java.io.Serializable

data class RepaceGain (
    var dataType: String,
    var data: ByteArray?,
    var length: Byte?,
    var result: String?,
) : Serializable {
    companion object {
        val empty = RepaceGain("", byteArrayOf(), 0, "")
    }
}