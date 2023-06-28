package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PolicyViewModel
@Inject constructor(private  val getPolicy: GetPolicy): BaseViewModel() {

    private val _resPolicyModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resPolicyModel: LiveData<ResBaseModel> = _resPolicyModel

    fun getPolicy(pathQuery: String) =
        getPolicy(pathQuery, viewModelScope) { it.fold(::handleFailure, ::handleGetPolicy) }

    private fun handleGetPolicy(resBaseModel: ResBaseModel) {
        _resPolicyModel.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}