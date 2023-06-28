package com.obelab.repace.model

import java.io.Serializable

data class LTTestHistoryOnsetModel(
    val smO2Avg: Double,
    val distance: Double,
    val duration: Double,
    val speedMax: Double,
    val speedAvg: Double,
    val lactateOnset: Double,
    val stage: Double,
    val date: String

): Serializable