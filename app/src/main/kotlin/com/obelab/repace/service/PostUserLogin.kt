/**
 * Copyright (C) 2020 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.obelab.repace.service

import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequestLoginModel
import com.obelab.repace.model.ResBaseMicroModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.ResLoginModel
import com.obelab.repace.repository.AuthRepository
import javax.inject.Inject

class PostUserLogin
@Inject constructor(private val authRepository: AuthRepository) : UseCase<ResBaseMicroModel, RequestLoginModel>() {
    override suspend fun run(params: RequestLoginModel) = authRepository.postUserLogin(params)
}