package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class GetLtTestResult
@Inject constructor(private val ltTestResult: AppRepository) : UseCase<ResBaseModel, Boolean>() {
    override suspend fun run(params: Boolean) = ltTestResult.getLtTestResult(params)
}