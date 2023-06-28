package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetAllFriendsList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FriendsListViewModel
@Inject constructor(private val getAllFriendsList: GetAllFriendsList) : BaseViewModel(){

    private val _resFriendsListViewModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resFriendsListViewModel: LiveData<ResBaseModel> = _resFriendsListViewModel

    fun getAllFriendsList() = getAllFriendsList(UseCase.None(), viewModelScope) {
        it.fold(::handleFailure, ::handleGetAllFriendsList)}

    private fun handleGetAllFriendsList(resBaseModel: ResBaseModel) {
        _resFriendsListViewModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}