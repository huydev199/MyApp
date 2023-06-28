package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel

import com.obelab.repace.service.DeleteFriendRequest

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RejectFriendViewModel
@Inject constructor(private val deleteFriendRequest: DeleteFriendRequest): BaseViewModel() {

    private val _deleteFriendRequestModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val deleteFriendRequestModel: LiveData<ResBaseModel> = _deleteFriendRequestModel

    fun deleteFriendRequest(deleteFriendRequestModel: Int) =
        deleteFriendRequest(deleteFriendRequestModel, viewModelScope) { it.fold(::handleFailure, ::handleDeleteFriendRequest) }

    private fun handleDeleteFriendRequest(resBaseModel: ResBaseModel) {
        _deleteFriendRequestModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}