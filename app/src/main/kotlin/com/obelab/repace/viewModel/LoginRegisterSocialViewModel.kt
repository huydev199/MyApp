package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequestSocialLoginModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.PostRegisterSocial
import com.obelab.repace.service.PostSignInSocial
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginRegisterSocialViewModel
@Inject constructor(private  val postSignInSocial: PostSignInSocial, private val postRegisterSocial: PostRegisterSocial): BaseViewModel() {

    private val _resSignInSocialModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resSignInSocialModel: LiveData<ResBaseModel> = _resSignInSocialModel

    private val _resRegisterSocialModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resRegisterSocialModel: LiveData<ResBaseModel> = _resRegisterSocialModel

    fun postSignInSocial(requestSocialLoginModel: RequestSocialLoginModel) =
        postSignInSocial(requestSocialLoginModel, viewModelScope) { it.fold(::handleFailure, ::handlePostSignInSocial) }

    fun postRegisterSocial(requestSocialLoginModel: RequestSocialLoginModel) =
        postRegisterSocial(requestSocialLoginModel, viewModelScope) { it.fold(::handleFailure, ::handlePostRegisterSocial) }

    private fun handlePostSignInSocial(resBaseModel: ResBaseModel) {
        _resSignInSocialModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }

    private fun handlePostRegisterSocial(resBaseModel: ResBaseModel) {
        _resRegisterSocialModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}