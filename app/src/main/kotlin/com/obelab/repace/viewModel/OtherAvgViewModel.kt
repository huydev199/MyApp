package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequesOtherAvgModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetOtherAvg
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OtherAvgViewModel
@Inject constructor(private val getOtherAvg: GetOtherAvg): BaseViewModel() {

    private val _resOtherAvg: MutableLiveData<ResBaseModel> = MutableLiveData()
    val otherAvgModel: LiveData<ResBaseModel> = _resOtherAvg

    fun getOtherAvg(params: RequesOtherAvgModel) =
        getOtherAvg(params , viewModelScope) { it.fold(::handleFailure, ::handleOtherAvg) }

    private fun handleOtherAvg(resBaseModel: ResBaseModel) {
        _resOtherAvg.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}