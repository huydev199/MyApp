package com.obelab.repace.service

import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.functional.Either
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import com.obelab.repace.repository.AuthRepository
import javax.inject.Inject

class GetPolicy
@Inject constructor(private val authRepository: AppRepository) : UseCase<ResBaseModel, String>() {
    override suspend fun run(path: String) = authRepository.getPolicy(path)
}