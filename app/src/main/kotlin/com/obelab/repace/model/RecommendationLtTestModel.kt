package com.obelab.repace.model

class RecommendationLtTestModel(
    var session: Int?,
    var time: Int?,
    var speed: Double?,
    var heartRate: Int?,
    var compare: String?
) {
    constructor() : this(null, null, null, null, null)
}