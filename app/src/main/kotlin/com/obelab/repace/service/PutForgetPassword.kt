package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequestForgetPasswordModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AuthRepository
import javax.inject.Inject

class PutForgetPassword
@Inject constructor(private val authRepository: AuthRepository) : UseCase<ResBaseModel, RequestForgetPasswordModel>() {
    override suspend fun run(params: RequestForgetPasswordModel) = authRepository.putForgetPassword(params)
}