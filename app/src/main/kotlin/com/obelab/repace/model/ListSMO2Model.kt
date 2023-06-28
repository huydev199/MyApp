package com.obelab.repace.model

data class ListSMO2Model(
    var listSMO2: ArrayList<Double>?,
) {
    companion object {
        val empty = ListSMO2Model(null)
    }
}
