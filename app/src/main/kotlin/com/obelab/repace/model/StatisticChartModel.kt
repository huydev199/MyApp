package com.obelab.repace.model

import java.io.Serializable

data class StatisticChartModel(
    var speedMin: Int,
    var speedMax: Int,
    var speedAvg: Int,
    var distance: Int,


    var smo2Min: Int,
    var smO2Max: Int,
    var smO2Avg: Int,
    var heartRateMin: Int,
    var heartRateMax: Int,
    var heartRateAvg: Int,
    var date:String,
    var dateEnd:String,
) : Serializable