package com.obelab.repace.model

import java.io.Serializable

data class ExerciseStatisticSpeedModel(
    val speed: Double,
    val distance: Double,
    val time: Double,
    val smO2: Double,
    val date: String
): Serializable