package com.obelab.repace.service

import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.functional.Either
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequesOtherInfoModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class GetOtherInfo
@Inject constructor(private val repository: AppRepository):
    UseCase<ResBaseModel, String>(){
    override suspend fun run(id: String): Either<Failure, ResBaseModel> = repository.getOtherInfo(id)
}
