package com.obelab.repace.model

data class RequestLtTestResultModel(
    var testTypeId: String,
    var stageCnt: Int,
    var totalDuration: Int,
    var totalDistance: Double,
    var onset: Double,
    var mol: Double,
    var smo2Min: Double,
    var smo2Max: Double,
    var smo2Avg: Double,
    var heartRateMin: Int,
    var heartRateMax: Int,
    var heartRateAvg: Int,
    var speedMin: Double?,
    var speedMax: Double?,
    var speedAvg: Double?,
    var status: Int,
    var stage: ArrayList<LtTestPerformanceModel>?,
    var listSmo2: ArrayList<SmO2ChartModel>?,
    var listLactate: ArrayList<ScoreChartModel>?,
    var listLocation: MutableList<LocationModel>,
)