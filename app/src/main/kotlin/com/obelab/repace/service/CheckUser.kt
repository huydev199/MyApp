package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequestUserUpdateModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class CheckUser
@Inject constructor(private val repository: AppRepository) : UseCase<ResBaseModel, RequestUserUpdateModel>() {
    override suspend fun run(params: RequestUserUpdateModel) = repository.checkUser(params)
}
