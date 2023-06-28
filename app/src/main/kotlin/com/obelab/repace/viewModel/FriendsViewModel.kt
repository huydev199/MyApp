package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetAllFriends
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel
@Inject constructor(private val getAllFriendsRequest: GetAllFriends) : BaseViewModel(){

    private val _resFriendsRequestViewModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resFriendsRequestViewModel: LiveData<ResBaseModel> = _resFriendsRequestViewModel

    fun getAllFriendsRequest() = getAllFriendsRequest(UseCase.None(), viewModelScope) {
        it.fold(::handleFailure, ::handleGetAllFriends) }

    private fun handleGetAllFriends(resBaseModel: ResBaseModel) {
        _resFriendsRequestViewModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}