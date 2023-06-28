package com.obelab.repace.model

import com.obelab.repace.core.functional.Functions

data class ExerciseArraySpeedModel(
//    var exerciseArraySpeedModel: ArrayList<ExerciseSpeedModel>?,
    var exerciseArraySpeedModel: ArrayList<Double>?,

){
    companion object {
        var empty = ExerciseArraySpeedModel(null)
    }
}