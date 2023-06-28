package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.*
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class PostExerciseResult
@Inject constructor(private val repository: AppRepository) : UseCase<ResBaseModel, ExerciseResultModel>() {
    override suspend fun run(params: ExerciseResultModel) = repository.postExerciseResult(params)
}