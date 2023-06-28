package com.obelab.repace.model

import java.io.Serializable

data class FriendFacebookModel(
    val nickname: String,
    val avatar: String,
    val id:Int,
    val status:String,
): Serializable