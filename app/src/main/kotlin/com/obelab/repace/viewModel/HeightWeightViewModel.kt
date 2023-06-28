package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetHeightWeight
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HeightWeightViewModel
@Inject constructor(private  val getHeightWeight: GetHeightWeight): BaseViewModel() {

    private val _resHeightWeightModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resHeightWeightModel: LiveData<ResBaseModel> = _resHeightWeightModel

    fun getHeightWeight() =
        getHeightWeight(UseCase.None(), viewModelScope) { it.fold(::handleFailure, ::handleGetHeightWeight) }

    private fun handleGetHeightWeight(resBaseModel: ResBaseModel) {
        _resHeightWeightModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}