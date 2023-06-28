package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequestRegisterModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.CheckEmail
import com.obelab.repace.service.PostUserRegister
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel
@Inject constructor(private  val postUserRegister: PostUserRegister, private val checkEmail: CheckEmail): BaseViewModel() {

    private val _resLoginModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resLoginModel: LiveData<ResBaseModel> = _resLoginModel

    fun postUserRegister(requestRegisterModel: RequestRegisterModel) =
        postUserRegister(requestRegisterModel, viewModelScope) { it.fold(::handleFailure, ::handlePostUserRegister) }

    fun checkEmail(requestRegisterModel: RequestRegisterModel) =
        checkEmail(requestRegisterModel, viewModelScope) { it.fold(::handleFailure, ::handlePostUserRegister) }

    private fun handlePostUserRegister(resBaseModel: ResBaseModel) {
        _resLoginModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}