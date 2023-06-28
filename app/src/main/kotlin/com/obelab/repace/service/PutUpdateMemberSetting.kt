package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.ResMemberSettingModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class PutUpdateMemberSetting
@Inject constructor(private val repository: AppRepository) : UseCase<ResBaseModel, ResMemberSettingModel>() {
    override suspend fun run(params: ResMemberSettingModel) = repository.putUpdateMemberSetting(params)
}