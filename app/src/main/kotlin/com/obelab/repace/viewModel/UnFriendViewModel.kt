package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel

import com.obelab.repace.service.DeleteUnfriend

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UnFriendViewModel
@Inject constructor(private val deleteUnfriend: DeleteUnfriend): BaseViewModel() {

    private val _deleteUnfriendModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val deleteUnfriendModel: LiveData<ResBaseModel> = _deleteUnfriendModel

    fun deleteUnfriend(deleteUnfriendModel: Int) =
        deleteUnfriend(deleteUnfriendModel, viewModelScope) { it.fold(::handleFailure, ::handleDeleteUnfriendRequest) }

    private fun handleDeleteUnfriendRequest(resBaseModel: ResBaseModel) {
        _deleteUnfriendModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}