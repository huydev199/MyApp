package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ExercisePrescriptionModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.PostExercisePrescription
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExercisePrescriptionDetailViewModel
@Inject constructor(private val postExercisePrescription: PostExercisePrescription ): BaseViewModel() {

    private val _resPostExercisePrescription: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resPostExercisePrescription: LiveData<ResBaseModel> = _resPostExercisePrescription

    fun postExercisePrescriptionRequest(params: ExercisePrescriptionModel) =
        postExercisePrescription(params, viewModelScope) { it.fold(::handleFailure, ::handlePostExercisePrescriptionRequest) }

    private fun handlePostExercisePrescriptionRequest(resBaseModel: ResBaseModel) {
        _resPostExercisePrescription.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}