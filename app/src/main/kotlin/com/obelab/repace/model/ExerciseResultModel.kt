package com.obelab.repace.model

import java.io.Serializable

data class ExerciseResultModel (
    val typeId: String,
    val intensityId: String,
    val activityId: String,
    val totalDuration: Int,
    val totalDistance: Double,
    val onset: Int?,
    val mol: Int?,
    val smo2Min: Double,
    val smo2Max: Double,
    val smo2Avg: Double,
    val heartRateMin: Double?,
    val heartRateMax: Double?,
    val heartRateAvg: Double?,
    val speedMin: Double?,
    val speedMax: Double?,
    val speedAvg: Double?,
    val status: Int?,
    val sessionCnt: Int?,
    val createdAt: String?,
    val time: Int?,
    val speed: Double?,
    val heartRate: Int?,
    val startTime: String?,
    val endTime: String?,
    val listSmo2: ArrayList<SmO2ChartModel>?,
    val listHeartRate: ArrayList<ScoreChartModel>?,
    val listLocation: MutableList<LocationModel>?
): Serializable {
    companion object {
        var empty = ExerciseResultModel("", "", "", 0, 0.0,0,0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0,0, "",0,0.0,0, null,null,null, null, null)
    }
}