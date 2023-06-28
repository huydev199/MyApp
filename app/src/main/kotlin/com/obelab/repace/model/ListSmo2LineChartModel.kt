package com.obelab.repace.model

data class ListSmo2LineChartModel(
    var listSmo2: ArrayList<SmO2ChartModel>?
){
    companion object {
        val empty = ListSmo2LineChartModel(null)
    }
}