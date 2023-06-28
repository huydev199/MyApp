package com.obelab.repace.model

import java.io.Serializable

class RequestSocialConnectModel(
    val socialId: String,
    val socialToken: String,
) : Serializable