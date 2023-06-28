package com.obelab.repace.model

import java.io.Serializable

class RepaceError(
    var dataType: String,
    var data: ByteArray,
    var lastReceive: Byte,
    var errorCode: Byte,
) : Serializable