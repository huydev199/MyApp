package com.obelab.repace.service

import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.functional.Either
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequestUserUpdateModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.DeleteFriendRequestModel
import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class DeleteCancleRequestFriend
@Inject constructor(private val repository: AppRepository) : UseCase<ResBaseModel, Int>() {
    override suspend fun run(id: Int) = repository.deleteCancleRequestFriend(id)


}

