package com.obelab.repace.service

import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.functional.Either
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.core.util.Constants
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class GetAllFriends
@Inject constructor( private  val repository: AppRepository):UseCase<ResBaseModel,UseCase.None>(){
    override  suspend fun run(params: None): Either<Failure, ResBaseModel> = repository.getAllFriends()
}
