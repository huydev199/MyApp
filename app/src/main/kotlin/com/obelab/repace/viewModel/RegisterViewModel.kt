package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequestRegisterMicroModel
import com.obelab.repace.model.RequestRegisterModel
import com.obelab.repace.model.ResBaseMicroModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.CheckEmail
import com.obelab.repace.service.PostUserRegister
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel
@Inject constructor(private  val postUserRegister: PostUserRegister, private val checkEmail: CheckEmail): BaseViewModel() {

    private val _resLoginModel: MutableLiveData<ResBaseMicroModel> = MutableLiveData()
//    val resLoginModel: LiveData<ResBaseModel> = _resLoginModel

    fun postUserRegister(requestRegisterModel: RequestRegisterMicroModel) =
        postUserRegister(requestRegisterModel, viewModelScope) { it.fold(::handleFailure, ::handlePostUserRegister) }

//    fun checkEmail(requestRegisterModel: RequestRegisterModel) =
//        checkEmail(requestRegisterModel, viewModelScope) { it.fold(::handleFailure, ::handlePostUserRegister) }

    private fun handlePostUserRegister(resBaseModel: ResBaseMicroModel) {
        _resLoginModel.value = ResBaseMicroModel(resBaseModel.status, resBaseModel.message, resBaseModel.data,resBaseModel.tokenInfo)
    }
}