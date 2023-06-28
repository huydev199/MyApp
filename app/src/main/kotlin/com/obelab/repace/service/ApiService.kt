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

import com.obelab.repace.model.*
import retrofit2.Call
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiService
@Inject constructor(retrofit: Retrofit) : Api {
    private val api by lazy { retrofit.create(Api::class.java) }

    override fun postUserLogin(requestBody: RequestLoginModel) = api.postUserLogin(requestBody)

    override fun postUserRegister(requestBody: RequestRegisterModel) =
        api.postUserRegister(requestBody)

    override fun checkEmail(requestBody: RequestRegisterModel) = api.checkEmail(requestBody)

    override fun postSignInSocial(requestBody: RequestSocialLoginModel) =
        api.postSignInSocial(requestBody)

    override fun postRegisterSocial(requestBody: RequestSocialLoginModel) =
        api.postRegisterSocial(requestBody)

    override fun postLtTestResult(requestBody: RequestLtTestResultModel) =
        api.postLtTestResult(requestBody)

    override fun putForgetPassword(requestBody: RequestForgetPasswordModel) =
        api.putForgetPassword(requestBody)

    override fun putUserUpdate(requestBody: RequestUserUpdateModel) = api.putUserUpdate(requestBody)

    override fun checkUser(requestBody: RequestUserUpdateModel) = api.checkUser(requestBody)

    override fun getPolicy(path: String) = api.getPolicy(path)

    override fun getAllNotices(page: Int, limit: Int) = api.getAllNotices(page, limit)

    override fun getUserList() = api.getUserList()

    override fun getProfile() = api.getProfile()

    override fun getHeightWeight() = api.getHeightWeight()

    override fun getDistance() = api.getDistance()

    override fun getGoals() = api.getGoals()

    override fun getMemberSetting() = api.getMemberSetting()

    override fun getListFAQ() = api.getListFAQ()

    override fun getMailSupport() = api.getMailSupport()

    override fun getAllFriends() = api.getAllFriends()

    override fun getAllFriendsList() = api.getAllFriendsList()

    override fun deleteFriendRequest(id: Int) = api.deleteFriendRequest(id)

    override fun acceptFriendRequest(id: Int) = api.acceptFriendRequest(id)

    override fun deleteUnFriend(id: Int) = api.deleteUnFriend(id)

    override fun searchFriend(nickname: String, page: Int,limit: Int) = api.searchFriend(nickname,page,limit)

    override fun postAddFriend(requestBody: RequestAddFriendModel) = api.postAddFriend(requestBody)

    override fun deleteCancleRequestFriend(id: Int) = api.deleteCancleRequestFriend(id)

    override fun getOtherInfo(id: String) = api.getOtherInfo(id)

    override fun getOtherLTTest(id: String, cumulative: Boolean) =api.getOtherLTTest(id, cumulative)

    override fun getOtherAvg(id: String, cumulative: Boolean) =api.getOtherAvg(id,cumulative)

    override fun getLtTestSetting() = api.getLtTestSetting()

    override fun getLtTestHistory() = api.getLtTestHistory()

    override fun getExerciseAvgResult(cumulative: Boolean) = api.getExerciseAvgResult(cumulative)

    override fun getExercise7days(type : String, activity: String, date: String) = api.getExercise7days(type, activity, date)

    override fun getExercise4weeks(type : String, activity: String, date: String) = api.getExercise4weeks(type, activity, date)

    override fun getExercise1year(type : String, activity: String, date: String) = api.getExercise1year(type, activity, date)

    override fun getLastPrescription() = api.getLastPrescription()

    override fun getRecommendedExercise() = api.getRecommendedExercise()

    override fun getLtTestResult(cumulative: Boolean) = api.getLtTestResult(cumulative)

    override fun getCalendarExercise(month: Int, year: Int) = api.getCalendarExercise(month, year)

    override fun putLtTestSetting(requestBody: LtTestSettingModel) =
        api.putLtTestSetting(requestBody)

    override fun putUpdateMemberSetting(requestBody: ResMemberSettingModel) =
        api.putUpdateMemberSetting(requestBody)

    override fun putUpdateMemberToken(requestBody: ResMemberTokenModel) = api.putUpdateMemberToken(requestBody)

    override fun postExercisePrescription(requestBody: ExercisePrescriptionModel) = api.postExercisePrescription(requestBody)

    override fun postExerciseResult(requestBody: ExerciseResultModel) = api.postExerciseResult(requestBody)

    override fun getOtherRxExercise(id: String) = api.getOtherRxExercise(id)

    override fun getStatisticBy(type: Int, date: String, typeexercise:String,activity:String)= api.getStatisticBy(type,date,typeexercise,activity)

    override fun putSocialConnect(param:RequestSocialConnectModel) = api.putSocialConnect(param)

    override fun getLtTestHistoryDetail(id: Int) = api.getLtTestHistoryDetail(id)

}
