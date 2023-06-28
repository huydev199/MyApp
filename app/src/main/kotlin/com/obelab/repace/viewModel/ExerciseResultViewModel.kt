package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ExerciseResultModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.PostExerciseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExerciseResultViewModel
@Inject constructor(private val postExerciseResult: PostExerciseResult ): BaseViewModel() {

    private val _resPostExerciseResult: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resPostExerciseResult: LiveData<ResBaseModel> = _resPostExerciseResult

    fun postExerciseResultRequest(params: ExerciseResultModel) =
        postExerciseResult(params, viewModelScope) { it.fold(::handleFailure, ::handlePostExerciseResultRequest) }

    private fun handlePostExerciseResultRequest(resBaseModel: ResBaseModel) {
        _resPostExerciseResult.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}