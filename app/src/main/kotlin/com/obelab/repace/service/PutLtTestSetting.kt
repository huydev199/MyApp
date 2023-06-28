package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.LtTestSettingModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class PutLtTestSetting
@Inject constructor(private val repository: AppRepository) : UseCase<ResBaseModel, LtTestSettingModel>() {
    override suspend fun run(params: LtTestSettingModel) = repository.putLtTestSetting(params)
}