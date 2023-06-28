package com.obelab.repace.model

data class ExerciseSessionModel (
    var session: Int,
    var time: Int,
    var speed: Double,
    var heartRate: Int
){
    companion object {
        var empty = ExerciseSessionModel(0, 0, 0.0, 0)
    }
}