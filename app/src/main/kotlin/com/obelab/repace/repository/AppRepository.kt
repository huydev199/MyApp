package com.obelab.repace.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.obelab.repace.DBManager.DatabaseHelper
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.exception.Failure.NetworkConnection
import com.obelab.repace.core.exception.Failure.ServerError
import com.obelab.repace.core.functional.Either
import com.obelab.repace.core.functional.Either.Left
import com.obelab.repace.core.functional.Either.Right
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.NetworkHandler
import com.obelab.repace.model.*
import com.obelab.repace.service.ApiService
import retrofit2.Call
import javax.inject.Inject

interface AppRepository {
    fun getPolicy(pathQuery: String): Either<Failure, ResBaseModel>
    fun getHeightWeight(): Either<Failure, ResBaseModel>
    fun getDistance(): Either<Failure, ResBaseModel>
    fun getGoals(): Either<Failure, ResBaseModel>
    fun getProfile(): Either<Failure, ResBaseModel>
    fun putUserUpdate(requestBody: RequestUserUpdateModel): Either<Failure, ResBaseModel>
    fun checkUser(requestBody: RequestUserUpdateModel): Either<Failure, ResBaseModel>
    fun checkEmail(requestBody: RequestRegisterModel): Either<Failure, ResBaseModel>
    fun getMemberSetting(): Either<Failure, ResBaseModel>
    fun putUpdateMemberSetting(body: ResMemberSettingModel): Either<Failure, ResBaseModel>
    fun putUpdateMemberToken(body: ResMemberTokenModel): Either<Failure, ResBaseModel>
    fun getAllNotices(page: Int, limit: Int): Either<Failure, ResBaseModel>
    fun getExerciseAvgResult(cumulative: Boolean): Either<Failure, ResBaseModel>
    fun getRecommendedExercise(): Either<Failure, ResBaseModel>
    fun getFAQs(): Either<Failure, ResBaseModel>
    fun getMailSupport(): Either<Failure, ResBaseModel>
    fun getLtTestSetting(): Either<Failure, ResBaseModel>
    fun putLtTestSetting(body: LtTestSettingModel): Either<Failure, ResBaseModel>
    fun getLtTestHistory(): Either<Failure, ResBaseModel>
    fun getLtTestResult(cumulative: Boolean): Either<Failure, ResBaseModel>
    fun getCalendarExercise(month: Int, year: Int): Either<Failure, ResBaseModel>
    fun getExercise7days(
        type: String,
        activity: String,
        date: String
    ): Either<Failure, ResBaseModel>

    fun getExercise4weeks(
        type: String,
        activity: String,
        date: String
    ): Either<Failure, ResBaseModel>

    fun getExercise1year(
        type: String,
        activity: String,
        date: String
    ): Either<Failure, ResBaseModel>

    fun getLastPrescription(): Either<Failure, ResBaseModel>
    fun getAllFriends(): Either<Failure, ResBaseModel>
    fun getAllFriendsList(): Either<Failure, ResBaseModel>
    fun deleteFriendRequest(id: Int): Either<Failure, ResBaseModel>
    fun acceptFriendRequest(id: Int): Either<Failure, ResBaseModel>
    fun deleteUnfriend(id: Int): Either<Failure, ResBaseModel>
    fun searchFriend(nickname: String, page: Int, limit: Int): Either<Failure, ResBaseModel>
    fun addFriendRequest(params: RequestAddFriendModel): Either<Failure, ResBaseModel>

    fun postLtTestResult(requestBody: RequestLtTestResultModel): Either<Failure, ResBaseModel>
    fun deleteCancleRequestFriend(id: Int): Either<Failure, ResBaseModel>
    fun getOtherInfo(id: String): Either<Failure, ResBaseModel>
    fun getOtherLTTest(id: String, cumulative: Boolean): Either<Failure, ResBaseModel>
    fun getOtherAvg(id: String, cumulative: Boolean): Either<Failure, ResBaseModel>
    fun postExercisePrescription(requestBody: ExercisePrescriptionModel): Either<Failure, ResBaseModel>
    fun postExerciseResult(requestBody: ExerciseResultModel): Either<Failure, ResBaseModel>
    fun getOtherRxExercise(id: String): Either<Failure, ResBaseModel>
    fun getStatisticBy(
        type: Int,
        date: String,
        typeexercise: String,
        activity: String
    ): Either<Failure, ResBaseModel>

    fun socialConnectRequest(param: RequestSocialConnectModel): Either<Failure, ResBaseModel>

    fun getLtTestHistoryDetail(id: Int): Either<Failure, ResBaseModel>


    class Network
    @Inject constructor(
        private val networkHandler: NetworkHandler,
        private val service: ApiService
    ) : AppRepository {

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
                        val errorResponse: ResBaseModel? =
                            gson.fromJson(response.errorBody()!!.charStream(), type)
                        return Right(transform(((errorResponse ?: default) as T)))
                    }
                }
            } catch (exception: Throwable) {
                Left(ServerError)
            }
        }

        override fun getPolicy(path: String): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getPolicy(path),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getAllNotices(page: Int, limit: Int): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getAllNotices(page, limit),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getExerciseAvgResult(cumulative: Boolean): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getExerciseAvgResult(cumulative),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getRecommendedExercise(): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getRecommendedExercise(),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getLtTestHistory(): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getLtTestHistory(),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getLtTestResult(cumulative: Boolean): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getLtTestResult(cumulative),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getCalendarExercise(month: Int, year: Int): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getCalendarExercise(month, year),
                    {
                        if (it.success == true) {
                            it.data?.let { it1 -> syncCalendarExercise(it1, month, year) }
                        }
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        private fun syncCalendarExercise(exerciseData: Any, month: Int, year: Int) {
            try {
                val exerciseHistoriesServer = Gson().fromJson(
                    Functions.toJsonString(exerciseData),
                    Array<DayExerciseModel>::class.java
                ).toList()
                val exerciseHistoriesDb =
                    DatabaseHelper.instance.getDayExercisesByMonthYear(month, year)
                exerciseHistoriesDb.map { exerciseDb ->
                    val exerciseDbDiff =
                        exerciseHistoriesServer.find { exerciseDb.id == it.id && exerciseDb.exercise.size > it.exercise.size }
                    if (exerciseDbDiff != null) {
                        postExerciseResult(exerciseDb.exercise[0])
                    }
                }
                exerciseHistoriesServer.map { exerciseServer ->
                    var exerciseDB = exerciseHistoriesDb.find { exerciseServer.id == it.id }
                    if (exerciseDB != null && exerciseServer.exercise.size > exerciseDB.exercise.size) {
                        exerciseDB.exercise = exerciseServer.exercise
                        DatabaseHelper.instance.updateTodayExercise(exerciseServer)
                    } else {
                        DatabaseHelper.instance.addDayExercise(exerciseServer)
                    }
                }
            } catch (e: Exception) {
            }
        }

        override fun getHeightWeight(): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getHeightWeight(),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getDistance(): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getDistance(),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getGoals(): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getGoals(),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getProfile(): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getProfile(),
                    {
                        if (it.success == true) {
                            it.data?.let { it1 -> PrefManager.saveProfile(it1) }
                        }
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getMailSupport(): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getMailSupport(),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getLtTestSetting(): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getLtTestSetting(),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun putLtTestSetting(body: LtTestSettingModel): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.putLtTestSetting(body),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }


        override fun getFAQs(): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getListFAQ(),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun putUserUpdate(requestBody: RequestUserUpdateModel): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.putUserUpdate(requestBody),
                    {
                        if (it.success == true) {
                            it.data?.let { it1 -> PrefManager.saveProfile(it1) }
                        }
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun checkUser(requestBody: RequestUserUpdateModel): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.checkUser(requestBody),
                    {
                        if (it.success == true) {
                            it.data?.let { it1 -> PrefManager.saveProfile(it1) }
                        }
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun checkEmail(requestBody: RequestRegisterModel): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.checkEmail(requestBody),
                    {
                        if (it.success == true) {
                            it.data?.let { it1 -> PrefManager.saveProfile(it1) }
                        }
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        private fun updateLocalUnit(res: ResBaseModel) {
            try {
                if (res.success == true) {
                    val gson = Gson()
                    val dataStr = res.data?.let { Functions.toJsonString(it) }
                    val memberSetting = gson.fromJson(dataStr, ResMemberSettingModel::class.java)
                    memberSetting.unit?.let { it -> PrefManager.saveUnit(it) }
                    memberSetting.language?.let { it -> PrefManager.saveLanguage(it) }
                }
            } catch (e: Exception) {
                Functions.showLog("getMemberSetting: $e")
            }
        }

        override fun getMemberSetting(): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getMemberSetting(),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun putUpdateMemberSetting(body: ResMemberSettingModel): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.putUpdateMemberSetting(body),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun putUpdateMemberToken(body: ResMemberTokenModel): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.putUpdateMemberToken(body),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getExercise7days(
            type: String,
            activity: String,
            date: String
        ): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getExercise7days(type, activity, date),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getExercise4weeks(
            type: String,
            activity: String,
            date: String
        ): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getExercise4weeks(type, activity, date),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getExercise1year(
            type: String,
            activity: String,
            date: String
        ): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getExercise1year(type, activity, date),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getLastPrescription(): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getLastPrescription(),
                    {
                        if (it.success == true) {
                            it.data?.let { it1 -> syncLastPrescription(it1) }
                        }
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        private fun syncLastPrescription(prescriptionData: Any) {
            try {
                val lastPrescription: ExercisePrescriptionModel = Gson().fromJson(
                    Functions.toJsonString(prescriptionData),
                    ExercisePrescriptionModel::class.java
                )
                if (lastPrescription.type != 0) {
                    // Check local or server is newer
                    if (PrefManager.getExercisePrescription().type == 0 || Functions.sqlDateToDateTime(
                            lastPrescription.createdAt
                        )
                            .after(Functions.sqlDateToDateTime(PrefManager.getExercisePrescription().createdAt))
                    ) {
                        PrefManager.saveExercisePrescription(lastPrescription)
                    } else if (PrefManager.getExercisePrescription().type != 0 && Functions.sqlDateToDateTime(
                            lastPrescription.createdAt
                        )
                            .before(Functions.sqlDateToDateTime(PrefManager.getExercisePrescription().createdAt))
                    ) {
                        postExercisePrescription(PrefManager.getExercisePrescription())
                    }
                }
            } catch (e: Exception) {
            }
        }

        override fun getAllFriends(): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getAllFriends(),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getAllFriendsList(): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getAllFriendsList(),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun deleteFriendRequest(id: Int): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.deleteFriendRequest(id),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun acceptFriendRequest(id: Int): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.acceptFriendRequest(id),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun deleteUnfriend(id: Int): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.deleteUnFriend(id),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun searchFriend(nickname: String,page: Int,limit: Int): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.searchFriend(nickname,page,limit),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun postLtTestResult(requestBody: RequestLtTestResultModel): Either<Failure, ResBaseModel> {
            Functions.showLog("requestBody 66"+ requestBody)
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.postLtTestResult(requestBody),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun deleteCancleRequestFriend(id: Int): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.deleteCancleRequestFriend(id),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getOtherInfo(id: String): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getOtherInfo(id),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getOtherLTTest(
            id: String,
            cumulative: Boolean
        ): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getOtherLTTest(id, cumulative),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getOtherAvg(id: String, cumulative: Boolean): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getOtherAvg(id, cumulative),
                    {
                        updateLocalUnit(it)
                        it.toResBaseModel()
                    },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun addFriendRequest(params: RequestAddFriendModel): Either<Failure, ResBaseModel> {
            Functions.showLog("paramsApp ${params}")
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.postAddFriend(params),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun postExercisePrescription(requestBody: ExercisePrescriptionModel): Either<Failure, ResBaseModel> {
            Functions.showLog(
                "postExercisePrescriptionParams: " + Functions.toJsonString(
                    requestBody
                )
            )
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.postExercisePrescription(requestBody),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun postExerciseResult(requestBody: ExerciseResultModel): Either<Failure, ResBaseModel> {
            Functions.showLog(
                "postExercisePrescriptionParams: " + Functions.toJsonString(
                    requestBody
                )
            )
            addExerciseResultToDB(requestBody)
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.postExerciseResult(requestBody),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        private fun addExerciseResultToDB(exercise: ExerciseResultModel) {
            var todayExercise = DatabaseHelper.instance.getTodayExercise()
            if (todayExercise == null) {
                todayExercise = DayExerciseModel.empty
            }
            todayExercise.exercise.add(exercise)
            DatabaseHelper.instance.updateTodayExercise(todayExercise)
        }

        override fun getOtherRxExercise(id: String): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getOtherRxExercise(id),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getStatisticBy(
            type: Int,
            date: String,
            typeexercise: String,
            activity: String
        ): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getStatisticBy(type, date, typeexercise, activity),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun socialConnectRequest(param: RequestSocialConnectModel): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.putSocialConnect(param),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

        override fun getLtTestHistoryDetail(id: Int): Either<Failure, ResBaseModel> {
            return when (networkHandler.isNetworkAvailable()) {
                true -> request(
                    service.getLtTestHistoryDetail(id),
                    { it.toResBaseModel() },
                    ResBaseModel.empty
                )
                false -> Left(NetworkConnection)
            }
        }

    }
}
