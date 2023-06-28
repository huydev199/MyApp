package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.ResMemberSettingModel
import com.obelab.repace.model.ResMemberTokenModel
import com.obelab.repace.service.GetMemberSetting
import com.obelab.repace.service.PutUpdateMemberSetting
import com.obelab.repace.service.PutUpdateMemberToken
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FcmViewModel
@Inject constructor( private val putUpdateMemberToken: PutUpdateMemberToken): BaseViewModel() {

    private val _resMemberToken: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resMemberToken: LiveData<ResBaseModel> = _resMemberToken

    fun putUpdateMemberToken(body: ResMemberTokenModel) =
        putUpdateMemberToken(body, viewModelScope) { it.fold(::handleFailure, ::handleUpdateMemberSetting) }

    private fun handleUpdateMemberSetting(resBaseModel: ResBaseModel) {
        _resMemberToken.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}