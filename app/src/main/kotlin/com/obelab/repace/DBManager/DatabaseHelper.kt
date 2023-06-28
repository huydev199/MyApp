package com.obelab.repace.DBManager

import com.obelab.repace.core.functional.Functions
import com.obelab.repace.model.ExercisePrescriptionModel

class DatabaseHelper {
    companion object {
        val instance = DatabaseManager()

        fun resetData(){
            Functions.showLog("reset data local")
            instance.deleteAllDayExercises()
            PrefManager.saveExercisePrescription(ExercisePrescriptionModel.empty)
            Functions.showLog("TABLE_EXERCISE_IN_DAY ${Functions.toJsonString(instance.getDayExercises())}")
        }
    }
}