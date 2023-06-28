package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class GetFAQs
@Inject constructor(private val faqsRepository: AppRepository) : UseCase<ResBaseModel, UseCase.None>() {
    override suspend fun run(params: None) = faqsRepository.getFAQs()
}