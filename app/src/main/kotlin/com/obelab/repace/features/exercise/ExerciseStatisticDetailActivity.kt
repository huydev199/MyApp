package com.obelab.repace.features.exercise

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.gson.GsonBuilder
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.model.ExerciseStatisticModel
import com.obelab.repace.model.ExerciseStatisticSpeedModel
import com.obelab.repace.model.RequestExerciseStatisticModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.viewModel.GetExerciseResultViewModel
import kotlinx.android.synthetic.main.activity_exercise_statistic_detail.*
import kotlinx.android.synthetic.main.header_back.*
import java.time.format.DateTimeFormatter
import java.util.*


class ExerciseStatisticDetailActivity : BaseActivity() {
    private val DAYS = 1
    private val WEEKS = 2
    private val YEARS = 3
    var currentType = 1
    val currentDate = Calendar.getInstance()
    var preDate = Calendar.getInstance()
    private var avg = ExerciseStatisticSpeedModel(0.0, 0.0, 0.0, 0.0, "0000-01-01")
    var statistic = ArrayList<ExerciseStatisticSpeedModel>()
    private val viewModel: GetExerciseResultViewModel by viewModels()
    var mType = ""
    var mAcitivity = ""
    var query = RequestExerciseStatisticModel("", "", "")

    companion object {
        fun callingIntent(context: Context) = Intent(context, ExerciseStatisticDetailActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_statistic_detail)
        preDate.time = currentDate.time
        preDate.add(Calendar.DAY_OF_MONTH, -6)
        Functions.showLog("DATEEE 2: " + Functions.formatCalendarToString(preDate))
        Functions.showLog("DATEEE: " + Functions.formatCalendarToString(currentDate))
        updateTextTime(preDate, currentDate)
        mType = intent.getStringExtra(Constants.typeId).toString()
        mAcitivity = intent.getStringExtra(Constants.activityId).toString()
        query = RequestExerciseStatisticModel(mType, mAcitivity, Functions.formatCalendarToString(currentDate))
        Functions.showLog("Pass intent: ${intent.getStringExtra(Constants.typeId).toString()}, ${intent.getStringExtra(Constants.activityId)}")
        setUpView()
    }

    fun getAverageAPI(type: Int) {
        query = RequestExerciseStatisticModel(mType, mAcitivity, Functions.formatCalendarToString(currentDate))
        if (type == DAYS) {
            with(viewModel) {
                observe(resExerciseLast7DaysResultModel, ::renderAverage)
                failure(failure, ::handleFailure)
            }
            viewModel.getExercise7days(query)
        } else if (type == WEEKS) {
            with(viewModel) {
                observe(resExerciseLast4WeeksResultModel, ::renderAverage)
                failure(failure, ::handleFailure)
            }
            viewModel.getExercise4weeks(query)
        } else {
            with(viewModel) {
                observe(resExerciseLastYearResultModel, ::renderAverage)
                failure(failure, ::handleFailure)
            }
            viewModel.getExercise1year(query)
        }
    }

    private fun renderAverage(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = GsonBuilder().create()
            Functions.showLog("Exercise String detail : " + resBaseModel.data?.let { Functions.toJsonString(it) })
            val exer = gson.fromJson(
                resBaseModel.data?.let { Functions.toJsonString(it) },
                ExerciseStatisticModel::class.java
            )
            avg = exer.average
            statistic = exer.statistic
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.flList, ExerciseList7daysFragment())
            ft.commit()
            changeAverageValue()
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    fun changeAverageValue() {
        tv_speedvalue.setText(String.format("%.0f", avg.speed))
        tv_distance_value.setText(String.format("%.0f", avg.distance / 1000))
        tv_time_value.setText(Functions.formatSecondToHumanTime(avg.time))
        tv_smo2_value.setText(String.format("%.0f", avg.smO2))
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Get exercise fail: " + failure.toString())
        hideLoading()
    }

    private fun setUpView() {
        btn_7day.setOnClickListener {
            currentType = DAYS
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.flChart, ExerciseStatistic7daysFragment())
            ft.replace(R.id.flList, ExerciseList7daysFragment())
            ft.commit()
            btn_7day.setTextColor(getApplication().getResources().getColor(R.color.colorTextPrimary))
            btn_7day.setBackgroundResource(R.drawable.btn_enable)
            btn_4week.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorText))
            btn_4week.setBackgroundResource(R.drawable.btn_disable)
            btn_1year.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorText))
            btn_1year.setBackgroundResource(R.drawable.btn_disable)
            preDate.time = currentDate.time
            preDate.add(Calendar.DAY_OF_MONTH, -6)
            updateTextTime(preDate, currentDate)
            getAverageAPI(DAYS)
        }
        btn_4week.setOnClickListener {
            currentType = WEEKS
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.flChart, ExerciseStatistic4weeksFragment())
            ft.commit()
            btn_7day.setTextColor(getApplication().getResources().getColor(R.color.colorText))
            btn_7day.setBackgroundResource(R.drawable.btn_disable)
            btn_4week.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorTextPrimary))
            btn_4week.setBackgroundResource(R.drawable.btn_enable)
            btn_1year.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorText))
            btn_1year.setBackgroundResource(R.drawable.btn_disable)
            preDate.time = currentDate.time
            preDate.add(Calendar.DAY_OF_MONTH, -27)
            updateTextTime(preDate, currentDate)
            getAverageAPI(WEEKS)
        }
        btn_1year.setOnClickListener {
            currentType = YEARS
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.flChart, ExerciseStatistic1yearFragment())
            ft.commit()
            btn_7day.setTextColor(getApplication().getResources().getColor(R.color.colorText))
            btn_7day.setBackgroundResource(R.drawable.btn_disable)
            btn_4week.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorText))
            btn_4week.setBackgroundResource(R.drawable.btn_disable)
            btn_1year.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorTextPrimary))
            btn_1year.setBackgroundResource(R.drawable.btn_enable)
            preDate.time = currentDate.time
            preDate.add(Calendar.DAY_OF_MONTH, -365)
            updateTextTime(preDate, currentDate)
            getAverageAPI(YEARS)
        }
        mType = intent.getStringExtra(Constants.typeId).toString()
        mAcitivity = intent.getStringExtra(Constants.activityId).toString()
        if (mType == Constants.rx_exercise)
            tvType.setText(R.string.rx_exercise)
        else
            tvType.setText(R.string.free_exercise)
        if (mAcitivity == Constants.ex_treadmill)
            tvExercise.setText(R.string.treadmill_running)
        else if (mAcitivity == Constants.ex_outdoor)
            tvExercise.setText(R.string.outdoor_running)
        else
            tvExercise.setText(R.string.profile_disclosure_all)
        tvTitle.text = getText(R.string.title_exercise_statistic)
        imvBack.setOnClickListener {
            finish()
        }
        getAverageAPI(DAYS)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.flChart, ExerciseStatistic7daysFragment())
        ft.replace(R.id.flList, ExerciseStatistic7daysFragment())
        ft.commit()

        //Prev
        imvPrev.setOnClickListener {
            clickChangeTime(false)
        }

        //Next
        imvNext.setOnClickListener {
            clickChangeTime(true)
        }
    }

    fun updateTextTime(preDate: Calendar, currentDate: Calendar) {
        if (preDate.time.year != currentDate.time.year) {
            tvTime.text = "${Functions.getMonthNameFromCalendar(preDate)} ${Functions.formatNumber(preDate.time.date)}, ${Functions.getYearFromCalendar(preDate)} ~ ${Functions.getMonthNameFromCalendar(currentDate)} ${Functions.formatNumber(currentDate.time.date)}, ${Functions.getYearFromCalendar(currentDate)}"
        } else {
            tvTime.text = "${Functions.getMonthNameFromCalendar(preDate)} ${Functions.formatNumber(preDate.time.date)} ~ ${Functions.getMonthNameFromCalendar(preDate)} ${Functions.formatNumber(currentDate.time.date)}, ${Functions.getYearFromCalendar(currentDate)}"
        }
    }

    fun clickChangeTime(isNext: Boolean) {
        if (isNext == false && currentType == DAYS) {
            currentDate.add(Calendar.DAY_OF_MONTH, -7)
            preDate.time = currentDate.time
            preDate.add(Calendar.DAY_OF_MONTH, -6)
        } else if( isNext == true && currentType == DAYS) {
            currentDate.add(Calendar.DAY_OF_MONTH, +7)
            preDate.time = currentDate.time
            preDate.add(Calendar.DAY_OF_MONTH, -6)
        } else if(isNext == false && currentType == WEEKS ){
            currentDate.add(Calendar.DAY_OF_MONTH, -28)
            preDate.time = currentDate.time
            preDate.add(Calendar.DAY_OF_MONTH, -27)
        } else if(isNext == true && currentType == WEEKS ){
            currentDate.add(Calendar.DAY_OF_MONTH, +28)
            preDate.time = currentDate.time
            preDate.add(Calendar.DAY_OF_MONTH, -27)
        } else if(isNext == false && currentType == YEARS){
            currentDate.add(Calendar.DAY_OF_MONTH, -365)
            preDate.time = currentDate.time
            preDate.add(Calendar.DAY_OF_MONTH, -364)
        } else {
            currentDate.add(Calendar.DAY_OF_MONTH, +365)
            preDate.time = currentDate.time
            preDate.add(Calendar.DAY_OF_MONTH, -364)
        }

        updateTextTime(preDate, currentDate)
        query = RequestExerciseStatisticModel(mType, mAcitivity, Functions.formatCalendarToString(currentDate))
        if (currentType == DAYS){
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.flChart, ExerciseStatistic7daysFragment())
            ft.commit()
        }
        else if(currentType == WEEKS){
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.flChart, ExerciseStatistic4weeksFragment())
            ft.commit()
        }else{
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.flChart, ExerciseStatistic1yearFragment())
            ft.commit()
        }
        getAverageAPI(currentType)
    }
}