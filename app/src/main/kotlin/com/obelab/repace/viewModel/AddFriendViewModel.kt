package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequestAddFriendModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.AddFriendRequest

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddFriendViewModel
@Inject constructor(private val postAddFriend: AddFriendRequest) : BaseViewModel() {

    private val _resAddFriendModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val addFriendRequestModel: LiveData<ResBaseModel> = _resAddFriendModel

    fun postAddFriend(requestAddFriendModel: RequestAddFriendModel) =
        postAddFriend(requestAddFriendModel, viewModelScope) { it.fold(::handleFailure, ::handlePostAddFriend) }

    private fun handlePostAddFriend(resBaseModel: ResBaseModel) {
        _resAddFriendModel.value =
            ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }


}