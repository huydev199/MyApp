package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequesOtherInfoModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetOtherInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OtherInfoViewModel
@Inject constructor(private val getOtherInfo: GetOtherInfo): BaseViewModel() {

    private val _resOtherInfo: MutableLiveData<ResBaseModel> = MutableLiveData()
    val otherInfoModel: LiveData<ResBaseModel> = _resOtherInfo

    fun getOtherInfo(id: String) =
        getOtherInfo(id, viewModelScope) { it.fold(::handleFailure, ::handleOtherInfo) }

    private fun handleOtherInfo(resBaseModel: ResBaseModel) {
        _resOtherInfo.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}