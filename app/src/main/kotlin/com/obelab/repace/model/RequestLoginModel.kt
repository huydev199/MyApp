package com.obelab.repace.model

data class RequestLoginModel(
    var email: String,
    var password: String,
    var fcmToken : String
)
