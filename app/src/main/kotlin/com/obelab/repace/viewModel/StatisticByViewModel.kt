package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequestStatisticByModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetStatisticBy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticByViewModel
@Inject constructor(private val getStatisticBy: GetStatisticBy): BaseViewModel() {

    private val _resStatisticByModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resStatisticByModel: LiveData<ResBaseModel> = _resStatisticByModel

    fun getStatisticBy(params: RequestStatisticByModel) =
        getStatisticBy(params, viewModelScope) { it.fold(::handleFailure, ::handleStatisticBy) }

    private fun handleStatisticBy(resBaseModel: ResBaseModel) {
        _resStatisticByModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}