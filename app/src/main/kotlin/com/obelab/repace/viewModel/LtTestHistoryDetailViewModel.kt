package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetAllLtTestHistory
import com.obelab.repace.service.GetLtTestHistoryDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LtTestHistoryDetailViewModel
@Inject constructor(private val getLtTestHistoryDetail: GetLtTestHistoryDetail) : BaseViewModel() {

    private val _resLtTestHistoryDetailModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resLtTestHistoryDetailModel: LiveData<ResBaseModel> = _resLtTestHistoryDetailModel


    fun getLtTestHistoryDetail(id: Int?) = id?.let {
        getLtTestHistoryDetail(it, viewModelScope) {
            it.fold(::handleFailure, ::handleGetAllLtTestHistoryDetail)
        }
    }


    private fun handleGetAllLtTestHistoryDetail(resBaseModel: ResBaseModel) {
        _resLtTestHistoryDetailModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}