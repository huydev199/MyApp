package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetGoals
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(private  val getGoals: GetGoals): BaseViewModel() {

    private val _resGoal: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resGoal: LiveData<ResBaseModel> = _resGoal

    fun getGoals() =
        getGoals(UseCase.None(), viewModelScope) { it.fold(::handleFailure, ::handleGoal) }

    private fun handleGoal(resBaseModel: ResBaseModel) {
        _resGoal.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}