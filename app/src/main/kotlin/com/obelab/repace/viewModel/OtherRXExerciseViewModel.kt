package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetOtherRxExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OtherRXExerciseViewModel
@Inject constructor(private val getOtherRXExercise: GetOtherRxExercise): BaseViewModel() {

    private val _resOtherRXExercise: MutableLiveData<ResBaseModel> = MutableLiveData()
    val otherRXExerciseModel: LiveData<ResBaseModel> = _resOtherRXExercise

    fun getOtherRXExercise(id: String) =
        getOtherRXExercise(id, viewModelScope) { it.fold(::handleFailure, ::handleOtherRXExercise) }

    private fun handleOtherRXExercise(resBaseModel: ResBaseModel) {
        _resOtherRXExercise.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}