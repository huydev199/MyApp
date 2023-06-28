package com.obelab.repace.model

import java.io.Serializable

data class LTTestHistorySmoModel(
    var id: Int,
    var createdAt: String,
    var smo2Avg:Int,
): Serializable