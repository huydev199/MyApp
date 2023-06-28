package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetAllLtTestHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LtTestHistoryViewModel
@Inject constructor(private val getAllLtTestHistory: GetAllLtTestHistory) : BaseViewModel(){

    private val _resTestHistoryModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resLtTestHistoryModel: LiveData<ResBaseModel> = _resTestHistoryModel

    fun getAllLtTestHistory() = getAllLtTestHistory(UseCase.None(), viewModelScope) {
        it.fold(::handleFailure, ::handleGetAllLtTestHistory) }

    private fun handleGetAllLtTestHistory(resBaseModel: ResBaseModel) {
        _resTestHistoryModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}