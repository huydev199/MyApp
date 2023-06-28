package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class GetHeightWeight @Inject constructor(private val repository: AppRepository) : UseCase<ResBaseModel, UseCase.None>() {
    override suspend fun run(params: None) = repository.getHeightWeight()
}