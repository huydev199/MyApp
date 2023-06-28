package com.obelab.repace.model

data class ImperialModel(
    var feet: Int,
    var inch: Int
){
    companion object {
        var empty = ImperialModel(0, 0)
    }
}