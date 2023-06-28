package com.obelab.repace.model

import java.io.Serializable

data class ScoreChartModel(
    val name:String,
    val score: Double,
) : Serializable