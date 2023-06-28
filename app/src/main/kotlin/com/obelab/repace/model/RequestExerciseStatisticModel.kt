package com.obelab.repace.model

import java.io.Serializable

class RequestExerciseStatisticModel (
    val type: String,
    val activity: String,
    val date: String,
) : Serializable