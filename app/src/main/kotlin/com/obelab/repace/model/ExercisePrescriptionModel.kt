package com.obelab.repace.model

import com.obelab.repace.core.functional.Functions

data class ExercisePrescriptionModel(
    var type: Int,
    var leSpeed: Double,
    var leWeek: Int,
    var leTime: Int,
    var leMin: Int,
    var ptSpeed: Double,
    var ptWeek: Int,
    var ptTime: Int,
    var ptMin: Int,
    var ptIndex: Double,
    var session: List<ExerciseSessionModel>,
    var todaySession: ExerciseSessionModel,
    var createdAt: String,
    var isPracticedToday: Boolean
){
    companion object {
        var empty = ExercisePrescriptionModel(0, 0.0, 0, 0, 0, 0.0, 0, 0, 0, 0.0, ArrayList(), ExerciseSessionModel.empty, Functions.getCurrentSqlDate(), false)
    }
}