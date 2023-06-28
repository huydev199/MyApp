package com.obelab.repace.model

data class UserInfoModel(
    val id: Int,
    val key: Any,
    val level: Any,
    val avatar: String,
    val badgeCnt: Any,
    val birthday: Any,
    val createdAt: String,
    val deleteAt: Any,
    val email: String,
    val gender: String,
    val height: Double,
    val weight: Double,
    val medalCnt: Any,
    val memberType: Any,
    val nickname: String,
    val os: String,
    val socialType: Any,
    val status: Any,
    val trophyCnt: Any,
    val updatedAt: String,
) {
    companion object {
        val empty = UserInfoModel(0, "", "", "", "", "", "", "", "", "", 0.0, 0.0, "", "", "", "", "", "", "", "")
    }
}