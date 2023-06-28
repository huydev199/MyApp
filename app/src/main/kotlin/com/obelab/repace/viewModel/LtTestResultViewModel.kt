package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequestLtTestResultModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.PostLtTestResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LtTestResultViewModel
@Inject constructor(private val postLtTestResult: PostLtTestResult): BaseViewModel() {

    private val _resLtTestResult: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resLtTestResult: LiveData<ResBaseModel> = _resLtTestResult

    fun postLtTestResult(requestLtTestResultModel: RequestLtTestResultModel) =
        postLtTestResult(requestLtTestResultModel, viewModelScope) { it.fold(::handleFailure, ::handlePostResultLtTest) }

    private fun handlePostResultLtTest(resBaseModel: ResBaseModel) {
        _resLtTestResult.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}