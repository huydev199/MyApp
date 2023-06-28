package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.AcceptFriendRequest

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AcceptFriendViewModel
@Inject constructor(private  val acceptFriendRequest: AcceptFriendRequest): BaseViewModel() {

    private val _acceptFriendRequestModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val acceptFriendRequestModel: LiveData<ResBaseModel> = _acceptFriendRequestModel

    fun acceptFriendRequest(acceptFriendRequestModel: Int) =
        acceptFriendRequest(acceptFriendRequestModel, viewModelScope) { it.fold(::handleFailure, ::handleAcceptFriendRequest) }

    private fun handleAcceptFriendRequest(resBaseModel: ResBaseModel) {
        _acceptFriendRequestModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}