package com.obelab.repace.model

data class ResMemberSettingModel(
    var memberId: Int?,
    var pushNotice: Int?,
    var mailNotice: Int?,
    var guide: Int?,
    var language: String?,
    var unit: String?,
    var profileDisclosure: String?,
) {
    constructor() : this( null, null, null,null,null,null,null)
}