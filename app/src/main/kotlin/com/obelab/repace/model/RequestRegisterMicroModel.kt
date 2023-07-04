package com.obelab.repace.model

data class RequestRegisterMicroModel(
    var email: String? = null,
    var password: String? = null,
    var confirm_password: String? = null,
    var first_name: String? = null,
    var last_name: String? = null,
)