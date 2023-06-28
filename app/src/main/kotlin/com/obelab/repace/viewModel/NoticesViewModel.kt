package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetAllNotices
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoticesViewModel
@Inject constructor(private val getAllNotices: GetAllNotices) : BaseViewModel() {

    private val _resNoticesModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resNoticesModel: LiveData<ResBaseModel> = _resNoticesModel

    fun getAllNotices(page: Int) =
        getAllNotices(page, viewModelScope) { it.fold(::handleFailure, ::handleGetAllNotices) }

    private fun handleGetAllNotices(resBaseModel: ResBaseModel) {
        _resNoticesModel.value =
            ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}