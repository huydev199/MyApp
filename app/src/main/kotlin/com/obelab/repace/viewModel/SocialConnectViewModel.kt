package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequestSocialConnectModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.AcceptFriendRequest
import com.obelab.repace.service.SocialConnectRequest

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SocialConnectViewModel
@Inject constructor(private val socialConnectRequest: SocialConnectRequest): BaseViewModel() {

    private val _socialConnectRequestModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val socialConnectRequestModel: LiveData<ResBaseModel> = _socialConnectRequestModel

    fun socialConnectRequest(param: RequestSocialConnectModel) =
        socialConnectRequest(param, viewModelScope) { it.fold(::handleFailure, ::handlesocialConnectRequest) }

    private fun handlesocialConnectRequest(resBaseModel: ResBaseModel) {
        _socialConnectRequestModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}