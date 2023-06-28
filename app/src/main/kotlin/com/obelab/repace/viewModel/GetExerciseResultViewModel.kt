package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequestExerciseStatisticModel
import com.obelab.repace.model.ExercisePrescriptionModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GetExerciseResultViewModel
@Inject constructor(private val getExerciseAvgResult: GetExerciseAvgResult, private val getExerciseLast4WeeksResult: GetExerciseLast4WeeksResult, private val getRecommendedExercise: GetRecommendedExercise, private val getLtTestResult: GetLtTestResult, private  val getExercise7days: GetExercise7Days,private val getExercise4weeks : GetExercise4Weeks, private val getExercise1year: GetExercise1Year, private val getLastPrescription: GetLastPrescription,
                    private val postExercisePrescription: PostExercisePrescription) : BaseViewModel() {

    private val _resExerciseAvgResultModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    private val _resExerciseLast4WeeksResultModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    private val _resRecommendedExerciseModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    private val _resLtTestResultModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    private val _resExerciseLast7DaysResultModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    private val _resExerciseLastYearResultModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    private val _resLastPrescriptionModel: MutableLiveData<ResBaseModel> = MutableLiveData()

    val resExerciseAvgResultModel: LiveData<ResBaseModel> = _resExerciseAvgResultModel
    val resExerciseLast4WeeksResultModel: LiveData<ResBaseModel> = _resExerciseLast4WeeksResultModel
    val resRecommendedExerciseModel: LiveData<ResBaseModel> = _resRecommendedExerciseModel
    val resLtTestResultModel: LiveData<ResBaseModel> = _resLtTestResultModel
    val resExerciseLast7DaysResultModel: LiveData<ResBaseModel> = _resExerciseLast7DaysResultModel
    val resExerciseLastYearResultModel: LiveData<ResBaseModel> = _resExerciseLastYearResultModel
    val resLastPrescriptionModel: LiveData<ResBaseModel> = _resLastPrescriptionModel

    fun getExerciseAvgResult() = getExerciseAvgResult(UseCase.None(), viewModelScope) { it.fold(::handleFailure, ::handleGetExerciseAvgResult) }

    fun getExerciseLast4WeeksResult() = getExerciseLast4WeeksResult(UseCase.None(), viewModelScope) { it.fold(::handleFailure, ::handleGetExerciseLast4WeeksResult) }

    fun getRecommendedExercise() = getRecommendedExercise(UseCase.None(), viewModelScope) { it.fold(::handleFailure, ::handleGetRecommendedExercise) }

    fun getLtTestResult(cumulative: Boolean) =
        getLtTestResult(cumulative, viewModelScope) { it.fold(::handleFailure, ::handleGetLtTestResult) }

    fun getExercise7days(query: RequestExerciseStatisticModel) =
        getExercise7days(query, viewModelScope) {it.fold(::handleFailure, ::handleGetExercise7days) }

    fun getExercise4weeks(query: RequestExerciseStatisticModel) =
        getExercise4weeks(query, viewModelScope) {it.fold(::handleFailure, ::handleGetExercise4weeks) }

    fun getExercise1year(query: RequestExerciseStatisticModel) =
        getExercise1year(query, viewModelScope) {it.fold(::handleFailure, ::handleGetExercise1year)}

    fun getLastPrescription() = getLastPrescription(UseCase.None(), viewModelScope) {it.fold(::handleFailure, ::handleGetLastPrescriptionModel)}

    fun postExercisePrescription(params: ExercisePrescriptionModel) = postExercisePrescription(params, viewModelScope) {}

    private fun handleGetExerciseAvgResult(resBaseModel: ResBaseModel) {
        _resExerciseAvgResultModel.value =
            ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }

    private fun handleGetExerciseLast4WeeksResult(resBaseModel: ResBaseModel) {
        _resExerciseLast4WeeksResultModel.value =
            ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }

    private fun handleGetRecommendedExercise(resBaseModel: ResBaseModel) {
        _resRecommendedExerciseModel.value =
            ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }

    private fun handleGetLtTestResult(resBaseModel: ResBaseModel) {
        _resLtTestResultModel.value =
            ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }

    private fun handleGetExercise7days(resBaseModel: ResBaseModel) {
        _resExerciseLast7DaysResultModel.value =
            ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }

    private fun handleGetExercise4weeks(resBaseModel: ResBaseModel) {
        _resExerciseLast4WeeksResultModel.value = ResBaseModel(resBaseModel.success,resBaseModel.msg,resBaseModel.data)
    }

    private fun handleGetExercise1year(resBaseModel: ResBaseModel){
        _resExerciseLastYearResultModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }

    private fun handleGetLastPrescriptionModel(resBaseModel: ResBaseModel){
        _resLastPrescriptionModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}