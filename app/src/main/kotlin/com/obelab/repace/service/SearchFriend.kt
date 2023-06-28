package com.obelab.repace.service

import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.functional.Either
import com.obelab.repace.core.interactor.UseCase
import com.obelab.repace.model.RequestUserUpdateModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.DeleteFriendRequestModel

import com.obelab.repace.repository.AppRepository
import javax.inject.Inject

class SearchFriend
@Inject constructor(private val repository: AppRepository) : UseCase<ResBaseModel, SearchFriend.Params>() {
    override suspend fun run(params: Params) = repository.searchFriend(params.nickName,0,50)

    data class Params(val nickName: String, val page : Int =0, val limit: Int=50)
}

