package com.obelab.repace.model

import java.io.Serializable

data class StatisticAverageModel(
    var smO2Min: Int,
    var smO2Max: Int,
    var smO2Avg: Int,
    var heartRateMin: Int,
    var heartRateMax: Int,
    var heartRateAvg: Int,
    var distance: Int,
    var duration: Int,
    var speedMax: Int,
    var speedAvg: Int,
) : Serializable