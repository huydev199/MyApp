package com.obelab.repace.model

data class ExerciseStatisticModel(
    val statistic : ArrayList<ExerciseStatisticSpeedModel>,
    val average : ExerciseStatisticSpeedModel
)