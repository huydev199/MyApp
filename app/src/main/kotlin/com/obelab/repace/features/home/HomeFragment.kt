package com.obelab.repace.features.home

import android.os.Bundle
import android.os.Handler
import android.view.Display
import android.view.Gravity
import android.view.View
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.core.util.ExerciseHelper
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.model.*
import com.obelab.repace.viewModel.GetExerciseResultViewModel
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : BaseFragment() {
    override fun layoutId(): Int {
        return R.layout.fragment_home
    }

    private val TAG = "HomeFragment"
    private lateinit var exerciseAvgResult: ExerciseHomeResultModel
    private lateinit var exerciseLast4WeeksResult: ExerciseHomeResultModel
    private val viewModel: GetExerciseResultViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            observe(resExerciseAvgResultModel, ::renderExerciseAvgResult)
            observe(resExerciseLast4WeeksResultModel, ::renderExerciseLast4WeeksResult)
            //observe(resRecommendedExerciseModel, ::renderRecommendedExerciseModel)
            observe(resLtTestResultModel, ::renderLtTestResult)
            observe(resLastPrescriptionModel, ::renderLastPrescription)
            failure(failure, ::handleFailure)
        }
        viewModel.getExerciseAvgResult()
        viewModel.getExerciseLast4WeeksResult()
        viewModel.getLtTestResult(false)
        viewModel.getLastPrescription()
        val display: Display = requireActivity().windowManager.defaultDisplay
        val stageWidth: Int = display.getWidth()

        Handler().postDelayed({
            setUpView()
        }, 100)
        ctl_heartrate.layoutParams.width = (stageWidth * 0.26).toInt()
        ctl_heartrate.layoutParams.height = (stageWidth * 0.26).toInt()

        ctl_rx_exercise.layoutParams.width = (stageWidth * 0.26).toInt()
        ctl_rx_exercise.layoutParams.height = (stageWidth * 0.26).toInt()

        ctl_smo2.layoutParams.width = (stageWidth * 0.32).toInt()
        ctl_smo2.layoutParams.height = (stageWidth * 0.32).toInt()
//        divideSpeed.visibility=View.GONE
        //       tvSpeedPrescribed.visibility=View.GONE
        ExerciseHelper.getTodayExercise()
    }

    private fun setUpView() {

        btnLast4Weeks.setOnClickListener {
            viewModel.getLtTestResult(false)
//            divideSpeed.visibility=View.GONE
//            tvSpeedPrescribed.visibility=View.GONE
            btnLast4Weeks.setBackgroundResource(R.drawable.btn_enable)
            btnLast4Weeks.setTextColor(resources.getColor(R.color.colorTextPrimary))
            btnCumulative.setBackgroundResource(R.drawable.btn_transparent)
            btnCumulative.setTextColor(resources.getColor(R.color.colorText))
            if (exerciseLast4WeeksResult != null) {
                if (exerciseLast4WeeksResult.heartRateAvg != null && exerciseLast4WeeksResult.heartRateAvg != 0) {
                    tvHeartRate.text = exerciseLast4WeeksResult.heartRateAvg.toString()
                    tvHeartRate.gravity = Gravity.RIGHT
                } else {
                    tvHeartRate.gravity = Gravity.CENTER
                }
                if (exerciseLast4WeeksResult.smo2Avg != null && exerciseLast4WeeksResult.smo2Avg != 0) {
                    tvSmO2.text = exerciseLast4WeeksResult.smo2Avg.toString()
                    tvSmO2.gravity = Gravity.RIGHT
                } else {
                    tvSmO2.gravity = Gravity.CENTER
                }
                if (exerciseLast4WeeksResult.rxExercise != null && exerciseLast4WeeksResult.rxExercise != 0) {
                    tvRxExercise.text = exerciseLast4WeeksResult.rxExercise.toString()
                    tvRxExercise.gravity = Gravity.RIGHT
                } else {
                    tvRxExercise.gravity = Gravity.CENTER
                }
            }
        }

        btnCumulative.setOnClickListener {
            viewModel.getLtTestResult(true)
//            divideSpeed.visibility=View.VISIBLE
            tvSpeedPrescribed.visibility = View.VISIBLE
            btnCumulative.setBackgroundResource(R.drawable.btn_enable)
            btnCumulative.setTextColor(resources.getColor(R.color.colorTextPrimary))
            btnLast4Weeks.setBackgroundResource(R.drawable.btn_transparent)
            btnLast4Weeks.setTextColor(resources.getColor(R.color.colorText))
            if (exerciseAvgResult != null) {
                if (exerciseAvgResult.heartRateAvg != null && exerciseAvgResult.heartRateAvg != 0) {
                    tvHeartRate.text = exerciseAvgResult.heartRateAvg.toString()
                    tvHeartRate.gravity = Gravity.RIGHT
                } else {
                    tvHeartRate.gravity = Gravity.CENTER
                    tvHeartRate.text = "-"
                }
                if (exerciseAvgResult.smo2Avg != null && exerciseAvgResult.smo2Avg != 0) {
                    tvSmO2.text = exerciseAvgResult.smo2Avg.toString()
                    tvSmO2.gravity = Gravity.RIGHT
                } else {
                    tvSmO2.gravity = Gravity.CENTER
                    tvSmO2.text = "-"
                }
                if (exerciseAvgResult.rxExercise != null && exerciseAvgResult.rxExercise != 0) {
                    tvRxExercise.text = exerciseAvgResult.rxExercise.toString()
                    tvRxExercise.gravity = Gravity.RIGHT
                } else {
                    tvRxExercise.gravity = Gravity.CENTER
                    tvRxExercise.text = "-"
                }
            }
        }

        btnGoToLtTest.setOnClickListener {
            val mainActivity = activity as MainActivity
            mainActivity.goToLtTestFragment()
        }
    }

    private fun renderExerciseAvgResult(resBaseModel: ResBaseModel?) {
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resExerciseResult: ExerciseHomeResultModel? =
                gson.fromJson(dataStr, ExerciseHomeResultModel::class.java)
            if (resExerciseResult != null) {
                exerciseAvgResult = resExerciseResult
            }
            Functions.showLog("resExerciseResult: " + resExerciseResult?.let {
                Functions.toJsonString(
                    it
                )
            })
        } else {
            resBaseModel?.msg?.let { Functions.showLog(TAG, it) }
        }
    }

    private fun renderExerciseLast4WeeksResult(resBaseModel: ResBaseModel?) {
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resExerciseResult: ExerciseHomeResultModel? =
                gson.fromJson(dataStr, ExerciseHomeResultModel::class.java)
            if (resExerciseResult != null) {
                exerciseLast4WeeksResult = resExerciseResult
                if (resExerciseResult.heartRateAvg != null && resExerciseResult.heartRateAvg != 0) {
                    tvHeartRate.text = resExerciseResult.heartRateAvg.toString()
                    tvHeartRate.gravity = Gravity.RIGHT
                } else {
                    tvHeartRate.gravity = Gravity.CENTER
                    tvHeartRate.text = "-"
                }
                if (resExerciseResult.smo2Avg != null && resExerciseResult.smo2Avg != 0) {
                    tvSmO2.text = resExerciseResult.smo2Avg.toString()
                    tvSmO2.gravity = Gravity.RIGHT
                } else {
                    tvSmO2.gravity = Gravity.CENTER
                    tvSmO2.text = "-"
                }
                if (resExerciseResult.rxExercise != null && resExerciseResult.rxExercise != 0) {
                    tvRxExercise.text = resExerciseResult.rxExercise.toString()
                    tvRxExercise.gravity = Gravity.RIGHT
                } else {
                    tvRxExercise.gravity = Gravity.CENTER
                    tvRxExercise.text = "-"
                }
            }
        } else {
            resBaseModel?.msg?.let { Functions.showLog(TAG, it) }
        }
    }

    private fun renderRecommendedExerciseModel(resBaseModel: ResBaseModel?) {
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resRecommendedExercise: RecommendedExerciseModel? =
                gson.fromJson(dataStr, RecommendedExerciseModel::class.java)
            if (resRecommendedExercise != null) {
                ctlRXExercise.visibility = View.VISIBLE
                tvRxExerciseNoData.visibility = View.GONE
                setUpRxExerciseView(resRecommendedExercise)
            } else {
                ctlRXExercise.visibility = View.GONE
                tvRxExerciseNoData.visibility = View.VISIBLE
            }
        } else {
            resBaseModel?.msg?.let { Functions.showLog(TAG, it) }
        }
    }

    private fun renderLtTestResult(resBaseModel: ResBaseModel?) {
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resLtTestResult: LtTestResultModel? =
                gson.fromJson(dataStr, LtTestResultModel::class.java)
            if(resLtTestResult != null){
                if (resLtTestResult.totalStages != 0) {
                    ctlLTTestResult.visibility = View.VISIBLE
                    lnNoDataAvailable.visibility = View.GONE
                    setUpLtTestResultView(resLtTestResult)
                } else {
                    ctlLTTestResult.visibility = View.GONE
                    lnNoDataAvailable.visibility = View.VISIBLE
                }
            }
        } else {
            resBaseModel?.msg?.let { Functions.showLog(TAG, it) }
        }
    }

    private fun renderLastPrescription(resBaseModel: ResBaseModel?) {
        Functions.showLog("getLastPrescription -> ${resBaseModel?.data}")
        hideLoading()
        setUpPrescriptionData()
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Get Exercise Result Error: " + failure.toString())
        hideLoading()
        setUpPrescriptionData()
    }

    fun setUpPrescriptionData(){
        if(PrefManager.getExercisePrescription().type == 0){
            ctlRXExercise.visibility = View.GONE
            tvRxExerciseNoData.visibility = View.VISIBLE
        } else {
            setUpLastPrescriptionView(PrefManager.getExercisePrescription())
        }
    }

    private fun setUpRxExerciseView(data: RecommendedExerciseModel) {
//        tvExerciseType.text = data.exerciseType
//        tvFrequencyPerWeek.text = data.frequencyPerWeek.toString()
//        tvValueCalendar.text = data.week.toString()
//        tvSpeedBetweenWorkouts.text = data.speedBetweenWorkouts.toString()
//        tvSpeedPrescribed.text = data.speedPrescribed.toString()
    }

    private fun setUpLtTestResultView(data: LtTestResultModel) {
        tvValueStage.text = data.totalStages.toString()
        tvValueLactateOnset.text = data.lactateOnset.toString()
        tvValueSmO2atLactate4mmol.text = data.smO2AtLactate4mmol.toString()
    }

    private fun setUpLastPrescriptionView(data: ExercisePrescriptionModel) {
        if (data.type == 1) {
            tvExerciseType.text = getText(R.string.title_low_exercise)
            tvValueCalendar.text = data.leWeek.toString()
            tvFrequencyPerWeek.text = data.leTime.toString()
            tvSpeedPrescribed.text = data.leSpeed.toString()
        } else {
            tvExerciseType.text = getText(R.string.polarized_training)
            tvValueCalendar.text = data.ptWeek.toString()
            tvFrequencyPerWeek.text = data.ptTime.toString()
            tvSpeedPrescribed.text = data.ptSpeed.toString()
        }
    }
}
