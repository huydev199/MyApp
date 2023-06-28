package com.obelab.repace.model

import com.obelab.repace.core.functional.Functions

data class DayExerciseModel(
    var id: String?,
    var date: Int?,
    var month: Int?,
    var year: Int?,
    var exercise: MutableList<ExerciseResultModel>
) {
    companion object {
        var empty = DayExerciseModel(Functions.getTodayId(), Functions.getCurrentDateTime().day,Functions.getCurrentDateTime().month, Functions.getCurrentDateTime().year, ArrayList())
    }
}