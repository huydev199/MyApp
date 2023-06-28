package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequestCalendarModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class GetCalendarExercise @Inject constructor(private val repository: AppRepository) :
    UseCase<ResBaseModel, RequestCalendarModel>() {
    override suspend fun run(params: RequestCalendarModel)= repository.getCalendarExercise(params.month, params.year)
}