package com.obelab.repace.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.obelab.repace.DBManager.DatabaseHelper
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.exception.Failure.NetworkConnection
import com.obelab.repace.core.exception.Failure.ServerError
import com.obelab.repace.core.functional.Either
import com.obelab.repace.core.functional.Either.Left
import com.obelab.repace.core.functional.Either.Right
import com.obelab.repace.core.platform.NetworkHandler
import com.obelab.repace.model.*
import com.obelab.repace.service.ApiService
import retrofit2.Call
import javax.inject.Inject

interface AuthRepository {
    fun postUserLogin(requestBody: RequestLoginModel): Either<Failure, ResBaseModel>
    fun postUserRegister(requestBody: RequestRegisterModel): Either<Failure, ResBaseModel>
    fun postSignInSocial(requestBody: RequestSocialLoginModel): Either<Failure, ResBaseModel>
    fun postRegisterSocial(requestBody: RequestSocialLoginModel): Either<Failure, ResBaseModel>
    fun putForgetPassword(requestBody: RequestForgetPasswordModel): Either<Failure, ResBaseModel>
    fun getUserList(): Either<Failure, List<UserModel>>


    class Network
    @Inject constructor(
        private val networkHandler: NetworkHandler,
        private val service: ApiService
    ) : AuthRepository {

        private fun <T, R> request(
            call: Call<T>,
            transform: (T) -> R,
            default: T
        ): Either<Failure, R> {
            return try {
                val response = call.execute()
                when (response.isSuccessful) {
                    true -> Right(transform((response.body() ?: default)))
                    false -> {
                        val gson = Gson()
                        val type = object : TypeToken<ResBaseModel>() {}.type
                        val errorResponse: ResBaseModel? = gson.fromJson(response.errorBody()!!.charStream(), type)
                        return Right(transform(((errorResponse ?: default) as T)))
                    }
                }
            } catch (exception: Throwable) {
                Left(ServerError)
            }
        }

        override fun postUserLogin(requestBody: RequestLoginModel): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.postUserLogin(requestBody),
                    {
                        if (it.success == true) {
                            DatabaseHelper.resetData()
                        }
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun postUserRegister(requestBody: RequestRegisterModel): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.postUserRegister(requestBody),
                    {
                        if (it.success == true) {
                            DatabaseHelper.resetData()
                        }
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun postSignInSocial(requestBody: RequestSocialLoginModel): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.postSignInSocial(requestBody),
                    {
                        if (it.success == true) {
                            DatabaseHelper.resetData()
                        }
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun postRegisterSocial(requestBody: RequestSocialLoginModel): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.postRegisterSocial(requestBody),
                    {
                        if (it.success == true) {
                            DatabaseHelper.resetData()
                        }
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun putForgetPassword(requestBody: RequestForgetPasswordModel): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.putForgetPassword(requestBody),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }


        override fun getUserList(): Either<Failure, List<UserModel>> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getUserList(),
                    { it.map { userEntity -> userEntity.toToUser() } },
                    emptyList()
                )
                false -> Left(NetworkConnection)
            }
        }
    }
}
