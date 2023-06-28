package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.DeleteCancleRequestFriend

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class cancleRequestFriendViewModel
@Inject constructor(private val deleteCancleRequestFriend: DeleteCancleRequestFriend): BaseViewModel() {

    private val _deleteCancleRequestFriendModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val deleteCancleRequestFriendModel: LiveData<ResBaseModel> = _deleteCancleRequestFriendModel

    fun deleteCancleRequestFriend(deleteCancleRequestFriendModel: Int) =
        deleteCancleRequestFriend(deleteCancleRequestFriendModel, viewModelScope) { it.fold(::handleFailure, ::handleDeleteCancleRequestFriendRequest) }

    private fun handleDeleteCancleRequestFriendRequest(resBaseModel: ResBaseModel) {
        _deleteCancleRequestFriendModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}