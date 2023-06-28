package com.obelab.repace.model

import java.io.Serializable

data class FriendRequestModel(
    val nickname: String?,
    val avatar: String?,
    val id:Int?,
    val requested: Boolean?,
): Serializable