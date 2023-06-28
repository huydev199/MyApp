package com.obelab.repace.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.obelab.repace.core.platform.BaseViewModel
import com.obelab.repace.model.RequestRegisterModel
import com.obelab.repace.model.RequestUserUpdateModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.service.CheckUser
import com.obelab.repace.service.PostUserRegister
import com.obelab.repace.service.PutUserUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserUpdateViewModel
@Inject constructor(
    private val putUserUpdate: PutUserUpdate,
    private val checkUser: CheckUser,
    private val postUserRegister: PostUserRegister
) : BaseViewModel() {

    private val _resUpdateUserModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    private val _resRegisUserModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    private val _resCheckMemberModel: MutableLiveData<ResBaseModel> = MutableLiveData()
    val resUpdateUserModel: LiveData<ResBaseModel> = _resUpdateUserModel
    val resCheckMemberModel: LiveData<ResBaseModel> = _resCheckMemberModel
    val resRegisUserModel: LiveData<ResBaseModel> = _resRegisUserModel

    fun putUserUpdate(requestUserUpdateModel: RequestUserUpdateModel) =
        putUserUpdate(requestUserUpdateModel, viewModelScope) {
            it.fold(
                ::handleFailure,
                ::handlePutUserUpdate
            )
        }

    fun postUserRegister(requestRegisUserModel: RequestRegisterModel) =
        postUserRegister(requestRegisUserModel, viewModelScope) {
            it.fold(
                ::handleFailure,
                ::handlePostUserRegis
            )
        }

    fun checkUser(requestUserUpdateModel: RequestUserUpdateModel) =
        checkUser(requestUserUpdateModel, viewModelScope) {
            it.fold(
                ::handleFailure,
                ::handleCheckUser
            )
        }

    private fun handlePutUserUpdate(resBaseModel: ResBaseModel) {
        _resUpdateUserModel.value =
            ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }

    private fun handleCheckUser(resBaseModel: ResBaseModel) {
        _resCheckMemberModel.value =
            ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
    fun handlePostUserRegis(resBaseModel: ResBaseModel) {
        _resRegisUserModel.value =
            ResBaseModel(resBaseModel.success, resBaseModel.msg, resBaseModel.data)
    }
}





