package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetMailSupport
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MailSupportViewModel
@Inject constructor(private  val getMailSupport: GetMailSupport): BaseViewModel() {

    private val _resMailSupportModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resMailSupportModel: LiveData<ResBaseModel> = _resMailSupportModel

    fun getMailSupport() =
        getMailSupport(UseCase.None(), viewModelScope) { it.fold(::handleFailure, ::handleGetFAQs) }

    private fun handleGetFAQs(resBaseModel: ResBaseModel) {
        _resMailSupportModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}