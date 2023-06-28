package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequestForgetPasswordModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.PutForgetPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ForgetPasswordViewModel
@Inject constructor(private  val putForgetPassword: PutForgetPassword ): BaseViewModel() {

    private val _resForgetPasswordModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resForgetPasswordModel: LiveData<ResBaseModel> = _resForgetPasswordModel

    fun putForgetPassword(requestForgetPasswordModel: RequestForgetPasswordModel) =
        putForgetPassword(requestForgetPasswordModel, viewModelScope) { it.fold(::handleFailure, ::handlePutForgetPassword) }

    private fun handlePutForgetPassword(resBaseModel: ResBaseModel) {
        _resForgetPasswordModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}