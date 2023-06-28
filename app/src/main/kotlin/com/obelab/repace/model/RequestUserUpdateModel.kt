package com.obelab.repace.model

data class RequestUserUpdateModel(
    var email: String?,
    var password: String?,
    var gender: String?,
    var height: Double?,
    var weight: Double?,
    var nickname: String?,
    var birthday: Any?,
    var avatar: String?,
    var medalCnt: Any?,
    var memberType: Any?,
    var os: String?,
    var socialType: Any?,
    var status: Any?,
    var trophyCnt: Any?,
    var level: Any?,
) {
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
}