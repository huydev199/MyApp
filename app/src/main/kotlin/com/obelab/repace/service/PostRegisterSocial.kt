package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequestSocialLoginModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AuthRepository
import javax.inject.Inject

class PostRegisterSocial
@Inject constructor(private val authRepository: AuthRepository) : UseCase<ResBaseModel, RequestSocialLoginModel>() {
    override suspend fun run(params: RequestSocialLoginModel) = authRepository.postRegisterSocial(params)
}