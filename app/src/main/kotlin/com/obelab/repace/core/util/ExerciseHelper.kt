package com.obelab.repace.core.util

import com.obelab.repace.DBManager.DatabaseHelper
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.model.ExercisePrescriptionModel
import com.obelab.repace.model.ExerciseSessionModel

class ExerciseHelper {
    companion object {
        fun getTodayExercise(): ExercisePrescriptionModel {
            val prescription = PrefManager.getExercisePrescription()
            val todayExercise = DatabaseHelper.instance.getTodayExercise()
            Functions.showLog("exercisePrescriptionToday -> " + prescription?.let { Functions.toJsonString(it) })
            Functions.showLog("exerciseHistoriesToday -> " + todayExercise?.let { Functions.toJsonString(it) })
            if (todayExercise?.exercise?.find { it.typeId == Constants.rx_exercise } != null) {
                prescription.isPracticedToday = true
            }
            if (prescription.type == Constants.ExerciseConstant.EXERCISE_POLARIZED_TYPE) {
                val diff: Long = Functions.getCurrentDateTime().time - Functions.sqlDateToDateTime(prescription.createdAt).timeInMillis
                val deltaDays = diff / (24 * 60 * 60 * 1000)
                Functions.showLog("getTodayExercise deltaDays -> $deltaDays")
                if (prescription.session.isNotEmpty()) {
                    val indexToday: Int = (deltaDays % prescription.session.size).toInt()
                    prescription.todaySession = prescription.session[indexToday]
                } else {
                    prescription.todaySession = ExerciseSessionModel.empty
                    prescription.todaySession.session = 1
                    prescription.todaySession.speed = prescription.ptSpeed
                    prescription.todaySession.time = prescription.ptTime
                }
            } else {
                prescription.todaySession = ExerciseSessionModel.empty
                prescription.todaySession.session = 1
                prescription.todaySession.speed = prescription.leSpeed
                prescription.todaySession.time = prescription.leTime
            }
            prescription.session = ArrayList()
            Functions.showLog("getTodayExercise -> ${Functions.toJsonString(prescription)}")
            return prescription
        }
    }
}