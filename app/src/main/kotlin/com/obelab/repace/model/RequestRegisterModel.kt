package com.obelab.repace.model

data class RequestRegisterModel(
    var email: String? = null,
    var password: String?? = null,
    var gender: String? = null,
    var height: Double? = null,
    var weight: Double? = null,
    var nickname: String? = null,
    var birthday: Any? = null,
    var avatar: String? = null,
    var medalCnt: Any? = null,
    var memberType: Any? = null,
    var os: String? = null,
    var socialType: Any? = null,
    var status: Any? = null,
    var trophyCnt: Any? = null,
    var level: Any? = null,
    var fcmToken: String? = null
)