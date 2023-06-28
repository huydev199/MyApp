package com.obelab.repace.service

import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.functional.Either
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequesOtherInfoModel
import com.obelab.repace.model.RequestExerciseStatisticModel
import com.obelab.repace.model.RequestStatisticByModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class GetStatisticBy
@Inject constructor(private val repository: AppRepository) :
    UseCase<ResBaseModel, RequestStatisticByModel>() {
    override suspend fun run(params: RequestStatisticByModel) = repository.getStatisticBy(params.type, params.date, params.typeexercise,params.activity)

}
