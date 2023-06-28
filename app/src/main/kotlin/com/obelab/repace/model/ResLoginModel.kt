package com.obelab.repace.model

data class ResLoginModel(
    var token: String?,
    var user: UserInfoModel?,
    var isRegister: Boolean
)
