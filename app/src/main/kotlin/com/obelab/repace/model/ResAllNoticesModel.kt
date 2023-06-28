package com.obelab.repace.model

data class ResAllNoticesModel (
    var totalItems: Int,
    var data: ArrayList<NoticeModel>,
    var totalPages: Int,
    var currentPage: Int
)