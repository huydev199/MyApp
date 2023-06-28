package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequesOtherLTTestModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.GetOtherLTTest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OtherLTTestViewModel
@Inject constructor(private val getOtherLTTest: GetOtherLTTest): BaseViewModel() {

    private val _resOtherLTTest: MutableLiveData<ResBaseModel> = MutableLiveData()
    val otherLTTestModel: LiveData<ResBaseModel> = _resOtherLTTest

    fun getOtherLTTest(params:RequesOtherLTTestModel) =
        getOtherLTTest(params , viewModelScope) { it.fold(::handleFailure, ::handleOtherLTTest) }

    private fun handleOtherLTTest(resBaseModel: ResBaseModel) {
        _resOtherLTTest.value = ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}