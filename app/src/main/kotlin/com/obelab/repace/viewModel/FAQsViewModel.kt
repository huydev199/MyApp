package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetFAQs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FAQsViewModel
@Inject constructor(private  val getFAQs: GetFAQs): BaseViewModel() {

    private val _resFAQModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resFAQModel: LiveData<ResBaseModel> = _resFAQModel

    fun getFAQs() =
        getFAQs(UseCase.None(), viewModelScope) { it.fold(::handleFailure, ::handleGetFAQs) }

    private fun handleGetFAQs(resBaseModel: ResBaseModel) {
        _resFAQModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}