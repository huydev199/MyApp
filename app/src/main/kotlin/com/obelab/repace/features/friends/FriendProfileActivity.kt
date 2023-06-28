package com.obelab.repace.features.friends

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.features.empty.FriendsActivity
import com.obelab.repace.model.*
import com.obelab.repace.viewModel.OtherAvgViewModel
import com.obelab.repace.viewModel.OtherInfoViewModel
import com.obelab.repace.viewModel.OtherLTTestViewModel
import com.obelab.repace.viewModel.OtherRXExerciseViewModel
import kotlinx.android.synthetic.main.activity_friend_profile.*
import kotlinx.android.synthetic.main.activity_friend_profile.btnCumulative
import kotlinx.android.synthetic.main.activity_friend_profile.btnLast4Weeks
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.civAvatar
import kotlinx.android.synthetic.main.activity_main.tvUserName
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.header_back.*
import kotlinx.android.synthetic.main.header_back.tvTitle

class FriendProfileActivity : BaseActivity() {


    private val viewModelOtherInfo: OtherInfoViewModel by viewModels()
    private val viewModelOtherLTTest: OtherLTTestViewModel by viewModels()
    private val viewModelOtherAvg: OtherAvgViewModel by viewModels()
    private val viewModelOtherRXExercise: OtherRXExerciseViewModel by viewModels()

    companion object {
        fun callingIntent(context: Context) = Intent(context, FriendsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)
        setUpView()




//        Functions.showLog("Show dataFriend111 ${id}")

        with(viewModelOtherInfo) {
            observe(otherInfoModel, ::renderOtherInfo)
            failure(failure, ::handleFailure)
        }



        with(viewModelOtherLTTest) {
            observe(otherLTTestModel, ::renderOtherLTTest)
            failure(failure, ::handleFailure)
        }


        with(viewModelOtherAvg) {
            observe(otherAvgModel, ::renderOtherAvg)
            failure(failure, ::handleFailure)
        }


        with(viewModelOtherRXExercise)
        {
            observe(otherRXExerciseModel, ::renderOtherRXExercise)
            failure(failure, ::handleFailure)
        }

    }


    private fun setUpView() {
        val nickname: String? = intent.getStringExtra("nickname")
        val id: Int? = intent.getIntExtra("id",0)
        val avatar: String? = intent.getStringExtra("avatar")
        val cumulative = false
        if(id!==null){
            Functions.showLog("Show dataFriend ${id}")
            viewModelOtherInfo.getOtherInfo(id.toString())
            viewModelOtherLTTest.getOtherLTTest(RequesOtherLTTestModel(id.toString(), cumulative))
            viewModelOtherAvg.getOtherAvg(RequesOtherAvgModel(id.toString(), cumulative))
            viewModelOtherRXExercise.getOtherRXExercise(id.toString())
        }

//        val id: String?="15"
        Functions.showLog("Show dataFriend ${nickname}")

        Functions.showLog("Show dataFriend ${avatar}")
        tvTitle.text = nickname
        imvBack.setOnClickListener {
            finish()
        }

        tvUserName.text = nickname
        Glide.with(this).load(avatar)
            .placeholder(R.drawable.ic_avatar_default)
            .error(R.drawable.ic_avatar_default)
            .into(civAvatar)


        btnLast4Weeks.setOnClickListener {
            val cumulative = false
            viewModelOtherLTTest.getOtherLTTest(RequesOtherLTTestModel(id.toString(), cumulative))
            viewModelOtherAvg.getOtherAvg(RequesOtherAvgModel(id.toString(), cumulative))
            btnLast4Weeks.setBackgroundResource(R.drawable.btn_enable)
            btnLast4Weeks.setTextColor(resources.getColor(R.color.colorTextPrimary))
            btnCumulative.setBackgroundResource(R.drawable.btn_transparent)
            btnCumulative.setTextColor(resources.getColor(R.color.colorText))
        }
        btnCumulative.setOnClickListener {
            val cumulative = true
            viewModelOtherLTTest.getOtherLTTest(RequesOtherLTTestModel(id.toString(), cumulative))
            viewModelOtherAvg.getOtherAvg(RequesOtherAvgModel(id.toString(), cumulative))
            btnLast4Weeks.setBackgroundResource(R.drawable.btn_transparent)
            btnLast4Weeks.setTextColor(resources.getColor(R.color.colorText))
            btnCumulative.setBackgroundResource(R.drawable.btn_enable)
            btnCumulative.setTextColor(resources.getColor(R.color.colorTextPrimary))
        }
    }

    private fun renderOtherRXExercise(resBaseModel: ResBaseModel?) {
        Functions.showLog("Show resrenderOtherRXExercise ${resBaseModel}")
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resOtherExerciseModel: ResOtherExerciseModel? =
                gson.fromJson(dataStr, ResOtherExerciseModel::class.java)
            if (resOtherExerciseModel!!.type == 1) {
                tvOtherExerciseType.text = getText(R.string.title_low_exercise)
                tvOtherValueCalendar.text = resOtherExerciseModel.leTime.toString()
                tvOtherFrequencyPerWeek.text = resOtherExerciseModel.leWeek.toString()
                tvOtherSpeedPrescribed.text = resOtherExerciseModel.leSpeed.toString()

            } else {
                tvOtherExerciseType.text = getText(R.string.polarized_training)
                tvOtherValueCalendar.text = resOtherExerciseModel.ptTime.toString()
                tvOtherFrequencyPerWeek.text = resOtherExerciseModel.ptWeek.toString()
                tvOtherSpeedPrescribed.text = resOtherExerciseModel.ptSpeed.toString()
            }
        }
    }

    private fun renderOtherAvg(resBaseModel: ResBaseModel?) {
        Functions.showLog("Show resBaseModelAvg ${resBaseModel}")
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resOtherAvgModel: ResOtherAvgModel? =
                gson.fromJson(dataStr, ResOtherAvgModel::class.java)
            tvHeartRateOther.text = resOtherAvgModel?.heartRateAvg.toString()
            tvSmO2Other.text = resOtherAvgModel?.smo2Avg.toString()
            tvRxExerciseOther.text = resOtherAvgModel?.rxExercise.toString()

            tvHeartRateOther.gravity = Gravity.CENTER
            tvSmO2Other.gravity = Gravity.CENTER
            tvRxExerciseOther.gravity = Gravity.CENTER
            if (resOtherAvgModel?.heartRateAvg != null && resOtherAvgModel?.heartRateAvg != 0.0) {
                tvHeartRateOther.gravity = Gravity.RIGHT
            } else {
                tvHeartRateOther.gravity = Gravity.CENTER
            }
            if (resOtherAvgModel?.smo2Avg != null && resOtherAvgModel?.smo2Avg != 0.0) {
                tvSmO2Other.gravity = Gravity.RIGHT
            } else {
                tvSmO2Other.gravity = Gravity.CENTER
            }
            if (resOtherAvgModel?.rxExercise != null && resOtherAvgModel?.rxExercise != 0.0) {
                tvRxExerciseOther.gravity = Gravity.RIGHT
            } else {
                tvRxExerciseOther.gravity = Gravity.CENTER
            }
        }
    }

    private fun renderOtherLTTest(resBaseModel: ResBaseModel?) {
        Functions.showLog("Show resBaseModel ${resBaseModel}")
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resOtherLTTestModel: ResOtherLTTestModel? =
                gson.fromJson(dataStr, ResOtherLTTestModel::class.java)

            Functions.showLog("Show dataStr ${dataStr}")
            tvValueOtherStage.text = resOtherLTTestModel?.lactateOnset.toString()
            tvValueOtherLactateOnset.text = resOtherLTTestModel?.lactateOnset.toString()
            tvValueOtherSmO2atLactate4mmol.text = resOtherLTTestModel?.smO2AtLactate4mmol.toString()
        }
    }


    private fun renderOtherInfo(resBaseModel: ResBaseModel?) {
        Functions.showLog("Show notices error: ${resBaseModel}")
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resOtherInfoModel: ResOtherInfoModel? =
                gson.fromJson(dataStr, ResOtherInfoModel::class.java)

            Functions.showLog("Show notices dataStr: ${dataStr}")
            Functions.showLog("Show resLoginModel: ${resOtherInfoModel}")
            tvEmail.text = resOtherInfoModel?.email.toString()
            tvHeight.text = "${
                Functions.getHeightUnitValue(resOtherInfoModel?.height).toString()
            } ${Functions.getHeightUnitName()}"
            tvWeight.text = "${
                Functions.getWeightUnitValue(resOtherInfoModel?.weight).toString()
            } ${Functions.getWeightUnitName()}"
            tvGender.text = resOtherInfoModel?.gender.toString()

            tvBirthdate.text = Functions.sqlDateToHumanDate(resOtherInfoModel?.birthday.toString())

        }

    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Show notices error: " + failure.toString())
        hideLoading()
    }
}