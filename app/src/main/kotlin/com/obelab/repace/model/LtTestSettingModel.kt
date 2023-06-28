package com.obelab.repace.model

import com.obelab.repace.core.util.Constants

data class LtTestSettingModel(
    var id: Int?,
    var testType: String?,
    var memberId: Int?,
    var userType: String?,
    var time: Double?,
    var distance: Int?,
    var number: Int?
) {
    constructor() : this(
        null, null, null, null, null, null, null
    )
}