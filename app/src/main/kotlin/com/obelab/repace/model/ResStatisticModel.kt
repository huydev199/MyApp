package com.obelab.repace.model

data class ResStatisticModel(
    var statistic: ArrayList<StatisticChartModel>,
    var average: StatisticAverageModel,
    var start:String,
    var end:String,
)
