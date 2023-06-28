package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequestExerciseStatisticModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class GetExercise4Weeks @Inject constructor(private val repository: AppRepository) : UseCase<ResBaseModel, RequestExerciseStatisticModel>() {
    override suspend fun run(params: RequestExerciseStatisticModel) = repository.getExercise4weeks(params.type, params.activity, params.date)
}