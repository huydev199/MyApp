package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.LtTestSettingModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetLtTestSetting
import com.obelab.repace.service.PutLtTestSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LtTestSettingViewModel
@Inject constructor(private val getLtTestSetting: GetLtTestSetting, private val putLtTestSetting: PutLtTestSetting): BaseViewModel() {

    private val _resLtTestSetting: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resLtTestSetting: LiveData<ResBaseModel> = _resLtTestSetting

    fun getLtTestSetting() =
        getLtTestSetting(UseCase.None(), viewModelScope) { it.fold(::handleFailure, ::handleGetLtTestSetting) }

    private fun handleGetLtTestSetting(resBaseModel: ResBaseModel) {
        _resLtTestSetting.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }

    private val _resPutLtTestSetting: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resPutLtTestSetting: LiveData<ResBaseModel> = _resPutLtTestSetting

    fun putLtTestSetting(body: LtTestSettingModel) =
        putLtTestSetting(body, viewModelScope) { it.fold(::handleFailure, ::handlePutLtTestSetting) }

    private fun handlePutLtTestSetting(resBaseModel: ResBaseModel) {
        _resPutLtTestSetting.value =
            ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}