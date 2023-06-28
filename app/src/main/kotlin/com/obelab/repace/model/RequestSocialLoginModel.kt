package com.obelab.repace.model

import java.io.Serializable

class RequestSocialLoginModel(
    val id: String,
    val social_type: String,
    val email: String?,
    val os: String,
    val token: String,
    val fcmToken: String
) : Serializable