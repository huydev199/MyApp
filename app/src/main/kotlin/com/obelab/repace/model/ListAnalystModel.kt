package com.obelab.repace.model

import com.obelab.library.repace.data.LTAnalysis

data class ListAnalysisModel (
    var listAnalyst : ArrayList<LTAnalysis>?,
) {
    companion object {
        val empty = ListAnalysisModel(null)
    }
}