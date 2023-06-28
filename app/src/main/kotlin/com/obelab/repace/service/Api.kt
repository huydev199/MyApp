package com.obelab.repace.service

import com.obelab.repace.model.*
import retrofit2.Call
import retrofit2.http.*

internal interface Api {

    @POST("public/user/login")
    fun postUserLogin(@Body requestBody: RequestLoginModel): Call<ResBaseModel>

    @POST("public/user/register")
    fun postUserRegister(@Body requestBody: RequestRegisterModel): Call<ResBaseModel>

    @POST("public/user/check-mail")
    fun checkEmail(@Body requestBody: RequestRegisterModel): Call<ResBaseModel>


    @POST("public/user/login-social")
    fun postSignInSocial(@Body requestBody: RequestSocialLoginModel): Call<ResBaseModel>

    @POST("public/user/register-social")
    fun postRegisterSocial(@Body requestBody: RequestSocialLoginModel): Call<ResBaseModel>

    @POST("private/admin/lt-test/upload-result")
    fun postLtTestResult(@Body requestBody: RequestLtTestResultModel): Call<ResBaseModel>

    @PUT("private/user-profile/update")
    fun putUserUpdate(@Body requestBody: RequestUserUpdateModel): Call<ResBaseModel>

    @POST("public/admin/member/check")
    fun checkUser(@Body requestBody: RequestUserUpdateModel): Call<ResBaseModel>

    @PUT("public/user/forget-password")
    fun putForgetPassword(@Body requestBody: RequestForgetPasswordModel): Call<ResBaseModel>

    @GET("https://jsonplaceholder.typicode.com/users")
    fun getUserList(): Call<List<UserModel>>

    @GET("private/user-profile")
    fun getProfile(): Call<ResBaseModel>

    @GET("public/commoncode/get-by-code/{path}")
    fun getPolicy(@Path("path") path: String): Call<ResBaseModel>

    @GET("public/commoncode/get-list-height")
    fun getHeightWeight(): Call<ResBaseModel>

    @GET("private/user/commoncode/get-distance")
    fun getDistance(): Call<ResBaseModel>

    @GET("private/user/achievement/get-all")
    fun getGoals(): Call<ResBaseModel>

    @GET("private/member-setting")
    fun getMemberSetting(): Call<ResBaseModel>

    @PUT("private/member-setting/update")
    fun putUpdateMemberSetting(@Body requestBody: ResMemberSettingModel): Call<ResBaseModel>

    @POST("private/member-token/post")
    fun putUpdateMemberToken(@Body requestBody: ResMemberTokenModel): Call<ResBaseModel>

    @GET("private/admin/notice/get-all")
    fun getAllNotices(@Query("page") page: Int, @Query("limit") limit: Int): Call<ResBaseModel>

    @GET("private/faq/get-list")
    fun getListFAQ(): Call<ResBaseModel>

    @GET("public/commoncode/get-by-code/mail_support")
    fun getMailSupport(): Call<ResBaseModel>

    @GET("private/user/lt-test/protocol")
    fun getLtTestSetting(): Call<ResBaseModel>

    @PUT("private/user/lt-test/protocol/update")
    fun putLtTestSetting(@Body requestBody: LtTestSettingModel): Call<ResBaseModel>

    @GET("private/user/lt-test/get-per-month")
    fun getLtTestHistory(): Call<ResBaseModel>

    @GET("private/exercise-result/get-avg")
    fun getExerciseAvgResult(@Query("cumulative") cumulative: Boolean): Call<ResBaseModel>

    @GET("private/admin/exercise-result/recommended-exercise")
    fun getRecommendedExercise(): Call<ResBaseModel>

    @GET("private/admin/lt-test/get-by-user")
    fun getLtTestResult(@Query("cumulative") cumulative: Boolean): Call<ResBaseModel>

    @GET("private/user/exercise-result/by-month")
    fun getCalendarExercise(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Call<ResBaseModel>

    @GET("private/user/exercise-result/7-day")
    fun getExercise7days(
        @Query("type") type: String,
        @Query("activity") activity: String,
        @Query("date") date: String
    ): Call<ResBaseModel>

    @GET("private/user/exercise-result/4-week")
    fun getExercise4weeks(
        @Query("type") type: String,
        @Query("activity") activity: String,
        @Query("date") date: String
    ): Call<ResBaseModel>

    @GET("private/user/exercise-result/1-year")
    fun getExercise1year(
        @Query("type") type: String,
        @Query("activity") activity: String,
        @Query("date") date: String
    ): Call<ResBaseModel>

    @GET("private/user/exercise-prescription/me")
    fun getLastPrescription(): Call<ResBaseModel>

    @GET("private/user/friend-request/all-invite")
    fun getAllFriends(): Call<ResBaseModel>

    @GET("private/user/friend/all")
    fun getAllFriendsList(): Call<ResBaseModel>

    @DELETE("private/user/friend-request/reject/{id}")
    fun deleteFriendRequest(@Path("id") id: Int): Call<ResBaseModel>

    @PUT("private/user/friend-request/accept/{id}")
    fun acceptFriendRequest(@Path("id") id: Int): Call<ResBaseModel>

    @DELETE("private/user/friend/unfriend/{id}")
    fun deleteUnFriend(@Path("id") id: Int): Call<ResBaseModel>

    @GET("private/user/friend/get-other?")
    fun searchFriend(
        @Query("nickname") nickname: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): Call<ResBaseModel>

    @POST("private/user/friend-request/create")
    fun postAddFriend(@Body requestBody: RequestAddFriendModel): Call<ResBaseModel>

    @DELETE("private/user/friend-request/cancel/{id}")
    fun deleteCancleRequestFriend(@Path("id") id: Int): Call<ResBaseModel>

    @GET("private/user/friend/get-other-info/{id}")
    fun getOtherInfo(@Path("id") id: String): Call<ResBaseModel>

    @GET("private/admin/lt-test/get-by-member/{id}")
    fun getOtherLTTest(
        @Path("id") id: String,
        @Query("cumulative") cumulative: Boolean
    ): Call<ResBaseModel>

    @GET("private/exercise-result/get-other-avg/{id}")
    fun getOtherAvg(
        @Path("id") id: String,
        @Query("cumulative") cumulative: Boolean
    ): Call<ResBaseModel>

    @POST("private/user/exercise-prescription/low-intensity")
    fun postExercisePrescription(@Body requestBody: ExercisePrescriptionModel): Call<ResBaseModel>

    @POST("private/exercise-result/create")
    fun postExerciseResult(@Body requestBody: ExerciseResultModel): Call<ResBaseModel>

    @GET("private/user/exercise-prescription/other/{id}")
    fun getOtherRxExercise(@Path("id") id: String): Call<ResBaseModel>

    @GET("private/user/exercise-statistic/detail")
    fun getStatisticBy(
        @Query("type") type: Int,
        @Query("date") date: String,
        @Query("typeexercise") typeexercise: String,
        @Query("activity") activity: String
    ): Call<ResBaseModel>

    @PUT("private/user-connect/social-connect")
    fun putSocialConnect(@Body param: RequestSocialConnectModel): Call<ResBaseModel>

    @GET("private/admin/lt-test/get-by-id/{id}")
    fun getLtTestHistoryDetail(@Path("id") id: Int): Call<ResBaseModel>
}