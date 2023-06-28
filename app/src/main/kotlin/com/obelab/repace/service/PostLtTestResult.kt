package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequestLtTestResultModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class PostLtTestResult
@Inject constructor(private val repository: AppRepository) : UseCase<ResBaseModel, RequestLtTestResultModel>() {
    override suspend fun run(params: RequestLtTestResultModel) = repository.postLtTestResult(params)
}