package com.obelab.repace.model

import java.io.Serializable

data class FriendAddModel(
    val nickname: String,
    val avatar: String,
    val id:Int,
    val requested: Boolean,
    val status:String,
): Serializable