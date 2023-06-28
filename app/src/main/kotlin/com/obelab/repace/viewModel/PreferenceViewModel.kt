package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.ResMemberSettingModel
import com.obelab.repace.service.GetMemberSetting
import com.obelab.repace.service.PutUpdateMemberSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PreferenceViewModel
@Inject constructor(private val getMemberSetting: GetMemberSetting, private val putUpdateMemberSetting: PutUpdateMemberSetting,): BaseViewModel() {

    private val _resMemberSetting: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resMemberSetting: LiveData<ResBaseModel> = _resMemberSetting

    private val _resUpdateMemberSetting: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resUpdateMemberSetting: LiveData<ResBaseModel> = _resUpdateMemberSetting

    fun getMemberSetting() =
        getMemberSetting(UseCase.None(), viewModelScope) { it.fold(::handleFailure, ::handleGetMemberSetting) }

    private fun handleGetMemberSetting(resBaseModel: ResBaseModel) {
        _resMemberSetting.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }

    fun putUpdateMemberSetting(body: ResMemberSettingModel) =
        putUpdateMemberSetting(body, viewModelScope) { it.fold(::handleFailure, ::handleUpdateMemberSetting) }

    private fun handleUpdateMemberSetting(resBaseModel: ResBaseModel) {
        _resUpdateMemberSetting.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}