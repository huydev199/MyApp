package com.obelab.repace.model

data class RecommendedExerciseModel(
    val exerciseType: String,
    val frequencyPerWeek: Int,
    val week: Int,
    val speedBetweenWorkouts: Int,
    val speedPrescribed: Double
)