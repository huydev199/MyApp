package com.obelab.repace.model

import java.io.Serializable

data class LocationModel(
    val latitude:Double,
    val longitude: Double,
    val time: Long,
): Serializable