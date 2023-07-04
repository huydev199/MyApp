package com.obelab.repace.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequestLoginModel
import com.obelab.repace.model.ResBaseMicroModel
import com.obelab.repace.model.UserModel
import com.obelab.repace.service.GetUserList
import com.obelab.repace.service.PostUserLogin
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
@Inject constructor(private val getUserList: GetUserList , private  val postUserLogin:PostUserLogin): BaseViewModel() {
    private val _userList: MutableLiveData<List<UserModel>> = MutableLiveData()
    val userList: LiveData<List<UserModel>> = _userList

     val _resLoginModel: MutableLiveData<ResBaseMicroModel> = MutableLiveData()
//    val resLoginModel: LiveData<ResBaseModel> = _resLoginModel

//    fun loadUserList() =
//        getUserList(UseCase.None(), viewModelScope) { it.fold(::handleFailure, ::handleUserList) }

    private fun handleUserList(userList: List<UserModel>) {
        _userList.value = userList.map { UserModel(it.id, it.name) }
    }

    fun postUserLogin(requestLoginModel: RequestLoginModel) =
        postUserLogin(requestLoginModel, viewModelScope) { it.fold(::handleFailure, ::handlePostUserLogin) }

    private fun handlePostUserLogin(resBaseModel: ResBaseMicroModel) {
        Log.d("huydev","resBaseModel"+ resBaseModel)

        _resLoginModel.value = ResBaseMicroModel(resBaseModel.status, resBaseModel.message, resBaseModel.data,resBaseModel.tokenInfo)
    }
}