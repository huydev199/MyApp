package com.obelab.repace.model

import java.io.Serializable

data class NoticeModel(
    var id: Int,
    var title: String,
    var content: String,
    var htmlContent: String,
    var updatedAt: String,
    var createdAt: String,
    var announcementId: String,
    var typeId: String,
    var status: Int,
) : Serializable