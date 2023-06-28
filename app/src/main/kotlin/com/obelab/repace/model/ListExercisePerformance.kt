package com.obelab.repace.model

data class ListExercisePerformance(
var listExercisePerformance: ArrayList<LtTestPerformanceModel>?,
) {
    companion object {
        val empty = ListExercisePerformance(null)
    }
}
