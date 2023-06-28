package com.obelab.repace.model

import java.io.Serializable

class RequestStatisticByModel (
    val type: Int,
    val date: String,
    val typeexercise:String,
    val activity:String,
) : Serializable