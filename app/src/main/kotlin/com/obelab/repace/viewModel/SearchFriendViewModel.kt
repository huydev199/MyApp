package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel

import com.obelab.repace.service.SearchFriend

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchFriendViewModel
@Inject constructor(private val searchFriend: SearchFriend): BaseViewModel() {

    private val _searchFriendModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val searchFriendModel: LiveData<ResBaseModel> = _searchFriendModel

    fun searchFriend(searchFriendModel: SearchFriend.Params) =
        searchFriend(searchFriendModel, viewModelScope) { it.fold(::handleFailure, ::handleSearchFriendRequest)  }

    private fun handleSearchFriendRequest(resBaseModel: ResBaseModel) {
        _searchFriendModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}