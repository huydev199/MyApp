package com.obelab.repace.model

data class ListLtTestPerformance(
var listLtTestPerformance: ArrayList<LtTestPerformanceModel>?,
) {
    companion object {
        val empty = ListLtTestPerformance(null)
    }
}
