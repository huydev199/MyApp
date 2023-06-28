package com.obelab.repace.model

import java.io.Serializable

data class FAQModel(
    var id: Int,
    var question: String,
    var answer: String,
    var status: Int,
    var createdAt: String,
    var updatedAt: String
) : Serializable