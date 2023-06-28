package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequestRegisterModel
import com.obelab.repace.model.RequestUserUpdateModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class CheckEmail
@Inject constructor(private val repository: AppRepository) : UseCase<ResBaseModel, RequestRegisterModel>() {
    override suspend fun run(params: RequestRegisterModel) = repository.checkEmail(params)
}
