package com.obelab.repace.model

import java.io.Serializable

data class RepaceStatus (
    var dataType: String,
    var data: ByteArray?,
    var length: Byte?,
    var status: String?,
    var version: ArrayList<Byte>?,
) : Serializable {
    companion object {
        val empty = RepaceStatus("", null, null, null, null)
    }
}