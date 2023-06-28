package com.obelab.repace.model

import java.io.Serializable

data class FriendListModel(
    val nickname: String,
    val avatar: String,
    val id:Int,
): Serializable