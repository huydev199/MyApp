package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetDistance
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DistanceViewModel
@Inject constructor(private val getDistance: GetDistance) : BaseViewModel() {

    private val _resDistance: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resDistance: LiveData<ResBaseModel> = _resDistance

    fun getDistance() =
        getDistance(UseCase.None(), viewModelScope) { it.fold(::handleFailure, ::handleDistance) }

    private fun handleDistance(resBaseModel: ResBaseModel) {
        Functions.showLog("show resBaseModel Modle ${resBaseModel}")
        _resDistance.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}