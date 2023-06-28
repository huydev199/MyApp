package com.obelab.repace.model

data class ResLtTestHistoryDetailModel(
    val id: Int,
    val welcomeMemberID: Int,
    val testTypeId: String,
    val stageCnt: Int,
    val totalDuration: Int,
    val totalDistance: Int,
    val onset: Double,
    val mol: Double,
    val smo2Min: Int,
    val smo2Max: Int,
    val smo2Avg: Int,
    val heartRateMin: Int,
    val heartRateMax: Int,
    val heartRateAvg: Int,
    val speedMin: Int,
    val speedMax: Int,
    val speedAvg: Int,
    val status: Int,
    val deleteAt: Any? = null,
    val createdAt: String,
    val updatedAt: String,
    val memberID: Int,
    val listSmo2: List<ListHeartRateElement>,
    val listHeartRate: List<ListHeartRateElement>,
    val listLocation: List<ListLocation>,
    val stage: List<Stage>,
    val listLactate: List<ListLactate>
)
data class ListHeartRateElement (
    val name: String,
    val score: Int
)
data class ListLactate (
    val name: String,
    val score: Double
)


data class ListLocation (
    val latitude: Double? = null,
    val longitude: Double? = null
)
data class Stage (
    val id: Int,
    val ltTestResultID: Int,
    val speed: Double,
    val distance: Double,
    val stage: Int,
    val createdAt: String,
    val updatedAt: String
)
