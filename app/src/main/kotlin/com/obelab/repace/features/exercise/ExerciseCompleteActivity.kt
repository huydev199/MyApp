package com.obelab.repace.features.exercise

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.facebook.FacebookSdk
import com.google.gson.GsonBuilder
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.ExerciseHelper
import com.obelab.repace.model.*
import com.obelab.repace.viewModel.ExerciseResultViewModel
import kotlinx.android.synthetic.main.activity_exercise_complete.*
import kotlinx.android.synthetic.main.header.*
import java.util.*

class ExerciseCompleteActivity : BaseActivity() {
    private val TAG = "ExerciseCompleteActivity"
    private val viewModel: ExerciseResultViewModel by viewModels()
    private var SmO2List = ArrayList<SmO2ChartModel>()
    private var activityId = PrefManager.getActivityId()
    private var typeId = PrefManager.getTypeId()
    private var intensityId = PrefManager.getIntensityId()
    private var dataSavePrefListSmo2: ListSmo2LineChartModel = ListSmo2LineChartModel.empty
    private var freeDuration = PrefManager.getFreeDuration()
    private var resultData = ExerciseResultModel.empty
    private var minSpeed = 0.0
    private var maxSpeed = 0.0
    private var avgSpeed = 0.0

    companion object {
        fun callingIntent(context: Context) = Intent(context, ExerciseCompleteActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_complete)
        with(viewModel) {
            observe(resPostExerciseResult, ::renderPostExerciseResult)
            failure(failure, ::handleFailure)
        }
        setUpView()
    }

    private fun setUpView() {
        val dataTodayExercise = ExerciseHelper.getTodayExercise()

        tvTitle.text = getText(R.string.rx_exercise)

        //Calculate SMO2 value
        var minSmo2 = 0.0
        var maxSmo2 = 0.0
        var avgSmo2 = 0.0
        var arrayListSmo2 = PrefManager.getAllListSMO2()
        if (arrayListSmo2 != null && arrayListSmo2.listSMO2 != null) {
            Functions.showLog("LIST" + arrayListSmo2.listSMO2!!.size)
            if (arrayListSmo2.listSMO2 != null) {
                minSmo2 = arrayListSmo2.listSMO2!!.min()?.toDouble() ?: 0.0
                maxSmo2 = arrayListSmo2.listSMO2!!.max()?.toDouble() ?: 0.0
                avgSmo2 = arrayListSmo2.listSMO2!!.average()
            }

            // Calcaculate use for char. Example: List size Smo2 = 1000
            // rangeStepSmo2 = size/8 = 1000/8 = 125
            //In i = 0, j = 1..125 -> sum = array[i*j+j] = array[0*1 + 1]
            //In i = 1, j = 1..125 -> sum = array[i*j+j] = array[1*125 + 1]
            if (arrayListSmo2.listSMO2 != null) {
                var sizeListSmo2 = arrayListSmo2.listSMO2!!.size
                var rangeStepSmo2 = sizeListSmo2 / 8
                for (i in 0..7) {
                    var sum = 0.0
                    for (j in 1..rangeStepSmo2) {
                        sum += arrayListSmo2.listSMO2!![i * rangeStepSmo2 + j - 1]
                    }
                    var avgSmo2InStep = sum / rangeStepSmo2
                    Functions.showLog("avg = $avgSmo2InStep , sum = $sum, rangeStep = $rangeStepSmo2")
                    var scoreChart = SmO2ChartModel(listStageConvert[i], avgSmo2InStep)
                    if (dataSavePrefListSmo2.listSmo2 != null) {
                        scoreChart?.let { dataSavePrefListSmo2.listSmo2!!.add(it) }
                    } else {
                        dataSavePrefListSmo2.listSmo2 = scoreChart?.let { arrayListOf(it) }
                    }
                    PrefManager.saveListSmo2InStage(dataSavePrefListSmo2)
                }
                Functions.showLog("data save: " + dataSavePrefListSmo2.listSmo2!!)
                SmO2List = dataSavePrefListSmo2.listSmo2!!
            }
        }

        val totalDuration = dataTodayExercise.todaySession.time * 60
        val totalDistance = dataTodayExercise.todaySession.speed * totalDuration / 3600
        val speed = PrefManager.getListExerciseSpeed()
        if (speed != ExerciseArraySpeedModel.empty) {
            if (activityId == Constants.ex_outdoor) {
                maxSpeed = speed.exerciseArraySpeedModel?.max()!!
                minSpeed = speed.exerciseArraySpeedModel?.min()!!
                avgSpeed = speed.exerciseArraySpeedModel?.average()!!
            } else if (activityId == Constants.ex_treadmill) {
                maxSpeed = dataTodayExercise.todaySession.speed
                minSpeed = dataTodayExercise.todaySession.speed
                avgSpeed = dataTodayExercise.todaySession.speed
            }
        }

        Handler().postDelayed({
            val listLocation = PrefManager.getExerciseLocationList()
            var timeStartDb = PrefManager.getTimeStart()
            var timeEndDb = PrefManager.getTimeStart()
            val timeStart =
                "${timeStartDb.timeCalendar?.time?.hours?.let { Functions.convertStringState(it) }}:${timeStartDb.timeCalendar?.time?.minutes?.let { Functions.convertStringState(it) }}"

            timeEndDb.timeCalendar?.add(Calendar.MINUTE, dataTodayExercise.todaySession.time)
            var timeEnd = "${timeEndDb.timeCalendar?.time?.hours?.let { Functions.convertStringState(it) }}:${timeEndDb.timeCalendar?.time?.minutes?.let { Functions.convertStringState(it) }}"

            if (typeId == Constants.rx_exercise) {
                if (activityId == Constants.ex_treadmill) {
                    maxSpeed = dataTodayExercise.todaySession.speed
                    minSpeed = dataTodayExercise.todaySession.speed
                    avgSpeed = dataTodayExercise.todaySession.speed
                    resultData = ExerciseResultModel(
                        typeId, intensityId, activityId, totalDuration, totalDistance,
                        null, null, minSmo2, maxSmo2, avgSmo2, null, null, null,
                        minSpeed, maxSpeed, avgSpeed, null, dataTodayExercise.todaySession.session, null,
                        dataTodayExercise.todaySession.time, dataTodayExercise.todaySession.speed, dataTodayExercise.todaySession.heartRate,
                        timeStart, timeEnd, SmO2List, arrayListOf(), arrayListOf()
                    )
                } else {
                    val distance = String.format("%.1f", PrefManager.getTotalDistance()).toDouble()
                    val speed = PrefManager.getListExerciseSpeed()
                    maxSpeed = String.format("%.1f", speed.exerciseArraySpeedModel?.max()).toDouble()
                    minSpeed = String.format("%.1f", speed.exerciseArraySpeedModel?.min()).toDouble()
                    avgSpeed = String.format("%.1f", speed.exerciseArraySpeedModel?.average()).toDouble()
                    resultData = ExerciseResultModel(
                        typeId, intensityId, activityId, totalDuration, distance,
                        null, null, minSmo2, maxSmo2, avgSmo2, null, null, null,
                        minSpeed, maxSpeed, avgSpeed, null, dataTodayExercise.todaySession.session, null,
                        dataTodayExercise.todaySession.time, dataTodayExercise.todaySession.speed, dataTodayExercise.todaySession.heartRate,
                        timeStart, timeEnd, SmO2List, arrayListOf(), listLocation
                    )
                }
            } else {
                if (activityId == Constants.ex_treadmill) {
                    maxSpeed = dataTodayExercise.todaySession.speed
                    minSpeed = dataTodayExercise.todaySession.speed
                    avgSpeed = dataTodayExercise.todaySession.speed
                    var timeEndFreeDuration = PrefManager.getTimeStart()
                    timeEndFreeDuration.timeCalendar?.add(Calendar.SECOND, PrefManager.getFreeDuration())
                    var timeEnd = "${timeEndFreeDuration.timeCalendar?.time?.hours?.let { Functions.convertStringState(it) }}:${timeEndFreeDuration.timeCalendar?.time?.minutes?.let { Functions.convertStringState(it) }}"
                    var distance = 0.0
                    if(PrefManager.getDistance() !=  ""){
                        distance = PrefManager.getDistance().toDouble()
                    }
                    resultData = ExerciseResultModel(
                        typeId, intensityId, activityId, freeDuration, distance,
                        null, null, minSmo2, maxSmo2, avgSmo2, null, null, null,
                        minSpeed, maxSpeed, avgSpeed, null, null, null, null, null, null,
                        timeStart, timeEnd, SmO2List, arrayListOf(), arrayListOf()
                    )
                } else {
                    var distance = 0.0
                    if(PrefManager.getDistance() != null){
                        distance = String.format("%.1f", PrefManager.getTotalDistance()).toDouble()
                    }
                    val speed = PrefManager.getListExerciseSpeed()
                    var timeEndFreeDuration = PrefManager.getTimeStart()
                    timeEndFreeDuration.timeCalendar?.add(Calendar.SECOND, PrefManager.getFreeDuration())
                    var timeEnd = "${timeEndFreeDuration.timeCalendar?.time?.hours?.let { Functions.convertStringState(it) }}:${timeEndFreeDuration.timeCalendar?.time?.minutes?.let { Functions.convertStringState(it) }}"
                    maxSpeed = String.format("%.1f", speed.exerciseArraySpeedModel?.max()).toDouble()
                    minSpeed = String.format("%.1f", speed.exerciseArraySpeedModel?.min()).toDouble()
                    avgSpeed = String.format("%.1f", speed.exerciseArraySpeedModel?.average()).toDouble()
                    resultData = ExerciseResultModel(
                        typeId, intensityId, activityId, freeDuration, distance,
                        null, null, minSmo2, maxSmo2, avgSmo2, null, null, null,
                        minSpeed, maxSpeed, avgSpeed, null, null, null, null, null, null,
                        timeStart, timeEnd, SmO2List, arrayListOf(), listLocation
                    )
                }
            }
            Functions.showLog("Result Model Complete: ${Functions.toJsonString(resultData)}")
            viewModel.postExerciseResultRequest(resultData)
        }, 1000)
    }

    @SuppressLint("ResourceAsColor")
    private fun renderPostExerciseResult(resBaseModel: ResBaseModel?) {
        hideLoading()
        Functions.showLog("resBaseModel: " + resBaseModel?.let { Functions.toJsonString(it) })
        if (resBaseModel?.success == true) {
            Functions.showLog("Post Rx Exercise Result Success")
            val gson = GsonBuilder().create()
            Functions.showLog("dataStr: " + resBaseModel.data?.let { Functions.toJsonString(it) })
            val dataList = gson.fromJson(resBaseModel.data?.let { Functions.toJsonString(it) }, ResponseExerciseResultModel::class.java)
            if (dataList != null) {
                btnRxExerciseResult.setBackgroundResource(R.drawable.btn_enable)
                btnRxExerciseResult.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorTextPrimary))
                setUpGoals(dataList)
            }
            btnRxExerciseResult.setOnClickListener {
                val intent = Intent(this, RxExerciseResultActivity::class.java)
                intent.putExtra(Constants.data, resultData)
                startActivity(intent)
                finish()
            }
        } else {
            resBaseModel?.msg?.let { Functions.showLog(TAG, it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Post rx exercise result error: " + failure.toString())
        hideLoading()
    }

    private fun setUpGoals(dataList: ResponseExerciseResultModel) {
        if (dataList.badge == true) {
            imvBadge.visibility = View.VISIBLE
        } else {
            imvBadge.visibility = View.GONE
        }

        if (dataList.medal == true) {
            imvMedal.visibility = View.VISIBLE
        } else {
            imvMedal.visibility = View.GONE
        }
    }
}

