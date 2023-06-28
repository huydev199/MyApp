package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject constructor(private  val getProfile:GetProfile): BaseViewModel() {

    private val _resProfileModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resProfileModel: LiveData<ResBaseModel> = _resProfileModel

    fun getProfile() =
        getProfile(UseCase.None(), viewModelScope) { it.fold(::handleFailure, ::handlePostUserLogin) }

    private fun handlePostUserLogin(resBaseModel: ResBaseModel) {
        _resProfileModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}