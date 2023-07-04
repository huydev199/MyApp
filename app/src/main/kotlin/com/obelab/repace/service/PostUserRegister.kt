package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequestRegisterMicroModel
import com.obelab.repace.model.RequestRegisterModel
import com.obelab.repace.model.ResBaseMicroModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AuthRepository
import javax.inject.Inject

class PostUserRegister
@Inject constructor(private val authRepository: AuthRepository) : UseCase<ResBaseMicroModel, RequestRegisterMicroModel>() {
    override suspend fun run(params: RequestRegisterMicroModel) = authRepository.postUserRegister(params)
}


