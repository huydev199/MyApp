package com.obelab.repace.model

data class ResBaseModel(
    var success: Boolean?,
    var msg: String?,
    var data: Any?,
){
    companion object {
        val empty = ResBaseModel(false, "", "")
    }

    fun toResBaseModel() = ResBaseModel(success, msg, data)
}