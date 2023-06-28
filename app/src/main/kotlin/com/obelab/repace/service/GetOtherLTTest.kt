package com.obelab.repace.service

import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.functional.Either
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequesOtherLTTestModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class GetOtherLTTest
@Inject constructor(private val repository: AppRepository):
    UseCase<ResBaseModel, RequesOtherLTTestModel>(){
    override suspend fun run(params:RequesOtherLTTestModel): Either<Failure, ResBaseModel> = repository.getOtherLTTest(params.id,params.cumulative)
}
