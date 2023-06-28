package com.obelab.repace.service

import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.functional.Either
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.util.Constants
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class GetAllNotices
@Inject constructor(private val repository: AppRepository) : UseCase<ResBaseModel, Int>() {
    override suspend fun run(page: Int) = repository.getAllNotices(page, Constants.LIMIT_NOTICES)
}