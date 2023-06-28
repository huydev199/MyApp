package com.obelab.repace.features.ltTest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.adapter.LtTestPerformanceAdapter
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.model.*
import com.obelab.repace.viewModel.LtTestResultViewModel
import kotlinx.android.synthetic.main.activity_lt_test_treadmill_result.*
import kotlinx.android.synthetic.main.activity_lt_test_treadmill_result.tvAvgSmo2
import kotlinx.android.synthetic.main.activity_lt_test_treadmill_result.tvMaxSmo2
import kotlinx.android.synthetic.main.activity_lt_test_treadmill_result.tvMaxSpeed
import kotlinx.android.synthetic.main.activity_lt_test_treadmill_result.tvMinSmo2
import kotlinx.android.synthetic.main.activity_lt_test_treadmill_result.tvPace
import kotlinx.android.synthetic.main.activity_rx_exercise_share_result.*
import kotlinx.android.synthetic.main.header_back_share.*
import kotlinx.android.synthetic.main.header_share.imvShare
import kotlinx.android.synthetic.main.header_share.tvTitle

class LtTestTreadmillResultActivity : BaseActivity() {

    private val TAG = "LtTestTreadmillResultActivity"
    private var isShowMore = false
    private var LactateList = ArrayList<ScoreChartModel>()
    private var SmO2List = ArrayList<SmO2ChartModel>()
    private val listAnalysisData = PrefManager.getAnalysis()
    companion object {
        fun callingIntent(context: Context) =
            Intent(context, LtTestTreadmillResultActivity::class.java)
    }

    private val viewModel: LtTestResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lt_test_treadmill_result)

        with(viewModel) {
            observe(resLtTestResult, ::renderPostLtTestResult)
            failure(failure, ::handleFailure)
        }
        setUpView()
        initLineChart()
        setDataToLineChart()
    }


    private fun setUpView() {
        tvTitle.text = getText(R.string.title_LT_Test)

        if (listAnalysisData.listAnalyst?.size!! >0){
            for(i in listAnalysisData.listAnalyst!!.indices){
                if(LactateList != null){
                    LactateList.add(ScoreChartModel("",listAnalysisData.listAnalyst!![i].lactate))
                } else {
                    LactateList = arrayListOf(ScoreChartModel("", listAnalysisData.listAnalyst!![i].lactate))
                }
            }
        }

        var listData = PrefManager.getListLtTestPerformance()
        var listSmo2 = PrefManager.getListSmo2InStage().listSmo2
        var sizeListData = 0
        sizeListData = listData.listLtTestPerformance?.size!!
        var listLocation= PrefManager.getLTTestLocationList()
        //SettingDate
        val date = getCurrentDateTime()
        val dateInString = date.toString("MMM dd, yyyy")
        tvTimeResult.text = dateInString

        //SET LAST STAGE
        var lastStage = 0
        lastStage = listData.listLtTestPerformance?.get(sizeListData!! - 1)?.stage!!
        tvLastStage.text = lastStage.toString()
        listData.listLtTestPerformance?.let { settingView(it) }

        //SET MAX SPEED
        var maxSpeed = 0.0
        var minSpeed = 0.0
        var avgSpeed = 0.0
        if (listData.listLtTestPerformance != null) {
            maxSpeed = getMaxSpeed(listData.listLtTestPerformance!!)
            minSpeed = getMinSpeed(listData.listLtTestPerformance!!)
            avgSpeed = getAvgSpeed(listData.listLtTestPerformance!!)
        }
        tvMaxSpeed.text = maxSpeed.toString()

        //SET TOTAL DURATION
        tvTotalDuration.text = (sizeListData * 5).toString()

        //SET TOTAL DISTANCE
        var totalDistance = 0.0
        if (listData.listLtTestPerformance != null) {
            totalDistance = getTotalDistance(listData.listLtTestPerformance!!)
        }
        tvTotalDistance.text = String.format("%.1f", totalDistance).replace(",", ".")

        //Calculate SMO2 value
        var minSmo2 = 0.0
        var maxSmo2 = 0.0
        var avgSmo2 = 0.0
        var arrayListSmo2 = PrefManager.getAllListSMO2()
        if (arrayListSmo2 != null && arrayListSmo2.listSMO2 != null) {
            Functions.showLog("LIST" + arrayListSmo2.listSMO2!!.toString())
            if (arrayListSmo2.listSMO2 != null) {
                minSmo2 = arrayListSmo2.listSMO2!!.min()?.toDouble() ?: 0.0
                maxSmo2 = arrayListSmo2.listSMO2!!.max()?.toDouble() ?: 0.0
                avgSmo2 = arrayListSmo2.listSMO2!!.average()
                Functions.showLog(TAG,"Smo2 Min: ${arrayListSmo2.listSMO2!!.min()} || Smo2 Max: ${arrayListSmo2.listSMO2!!.max()} || Smo2 Avg: ${arrayListSmo2.listSMO2!!.average()}")
            }
            tvMinSmo2.text = minSmo2.toString()
            tvMaxSmo2.text = maxSmo2.toString()
            tvAvgSmo2.text = String.format("%.1f", avgSmo2).replace(",", ".")
        }

        //Get Analysis and calculate onset and threshold value

        var onSet = 0.0
        var threshold = 0.0
        if (listAnalysisData != null) {
            listAnalysisData.listAnalyst!!.sumByDouble { it.onset }
            var sizeData = listAnalysisData.listAnalyst!!.size
            onSet = listAnalysisData.listAnalyst!!.sumByDouble { it.onset } / sizeData
            threshold = listAnalysisData.listAnalyst!!.sumByDouble { it.threshold } / sizeData
            tvOnset.text = String.format("%.1f", onSet).replace(",", ".")
            tv4mmol.text = String.format("%.1f", threshold).replace(",", ".")
        }



        imvHelpCircle.setOnClickListener {
            if (isShowMore == false) {
                imvHelpCircle.setImageResource(R.drawable.ic_x_circle)
                isShowMore = true
                tvLactateThresholdInfoMore.visibility = View.VISIBLE
            } else {
                imvHelpCircle.setImageResource(R.drawable.ic_help_circle)
                isShowMore = false
                tvLactateThresholdInfoMore.visibility = View.GONE
            }
        }

        btnExercisePrescription.setOnClickListener {
            startActivity(ExercisePrescriptionActivity.callingIntent(this))
        }
        var pace =Functions.convertTimeSecondary(((sizeListData*5)/totalDistance).toInt())
        tvPace.text = pace

            Functions.showLog("listData11 "+listData)
        viewModel.postLtTestResult(
            RequestLtTestResultModel(
                Constants.treadmill_test,
                lastStage,
                (sizeListData * 5),
                totalDistance,
                onSet,
                threshold,
                minSmo2,
                maxSmo2,
                avgSmo2,
                0,
                0,
                0,
                minSpeed,
                maxSpeed,
                avgSpeed,
                Constants.status_lt_test_result,
                listData.listLtTestPerformance,
                listSmo2,
                LactateList,
                listLocation,
            )
        )

        imvShare.setOnClickListener {
            val intent = Intent(this, LtTestShareResultActivity::class.java)
            val data = Bundle()
            data.putString(Constants.LAST_STAGE, lastStage.toString())
            data.putString(Constants.MAX_SPEED, maxSpeed.toString())
            data.putString(
                Constants.TOTAL_DISTANCE,
                String.format("%.1f", totalDistance).replace(",", ".")
            )
            data.putString(Constants.TOTAL_DURATION, (sizeListData * 5).toString())
            data.putString(Constants.ONSET, String.format("%.1f", onSet).replace(",", "."))
            data.putString(Constants.THRESHOLD, String.format("%.1f", threshold).replace(",", "."))
            intent.putExtras(data)
            startActivity(intent)
        }
        imvBack.setOnClickListener {
            startActivity(MainActivity.callingIntent(this))
        }
    }

    fun getMaxSpeed(listData: ArrayList<LtTestPerformanceModel>): Double {
        var maxSpeed = 0.0
        if (listData.size > 0) {
            for (data in listData) {
                if (data.speed > maxSpeed) {
                    maxSpeed = data.speed
                }
            }
        }
        return maxSpeed
    }

    fun getMinSpeed(listData: ArrayList<LtTestPerformanceModel>): Double {
        var minSpeed = getMaxSpeed(listData)
        if (listData.size > 0) {
            for (data in listData) {
                if (data.speed < minSpeed) {
                    minSpeed = data.speed
                }
            }
        }
        return minSpeed
    }


    fun getAvgSpeed(listData: ArrayList<LtTestPerformanceModel>): Double {
        var avgSpeed = 0.0
        var totalSpeed = 0.0
        if (listData.size > 0) {
            for (data in listData) {
                totalSpeed += data.speed
            }
        }
        avgSpeed = totalSpeed / listData.size
        return avgSpeed
    }

    fun getTotalDistance(listData: ArrayList<LtTestPerformanceModel>): Double {
        var total = 0.0
        if (listData.size > 0) {
            for (data in listData) {
                total += data.distance
            }
        }
        return total
    }

    private fun settingView(listData: ArrayList<LtTestPerformanceModel>) {
        var layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rcvListLtTestPerformance.layoutManager = layoutManager
        var faqAdapter = LtTestPerformanceAdapter(listData)
        rcvListLtTestPerformance.adapter = faqAdapter
    }

    // ==========================================================
    //    Set up line chart
    // ==========================================================
    private fun initLineChart() {

        //    hide grid lines
        lcLtTestTreadmillResult.axisLeft.setDrawGridLines(false)
        lcLtTestTreadmillResult.setNoDataText("")

        val xAxis: XAxis = lcLtTestTreadmillResult.xAxis
        val yAxisLeft: YAxis = lcLtTestTreadmillResult.axisLeft
        val yAxisRight: YAxis = lcLtTestTreadmillResult.axisRight

        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        lcLtTestTreadmillResult.axisRight.isEnabled = true

        lcLtTestTreadmillResult.xAxis.spaceMin = 0.3f
        lcLtTestTreadmillResult.xAxis.spaceMax = 0.5f
        //remove legend
        lcLtTestTreadmillResult.legend.isEnabled = false


        // remove description label
        lcLtTestTreadmillResult.description.isEnabled = false


        // add animation
        lcLtTestTreadmillResult.animateX(1000, Easing.EaseInSine)

        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
//        xAxis.labelRotationAngle = +90f
        xAxis.textColor = resources.getColor(R.color.colorTextPrimary)
        xAxis.axisLineColor = resources.getColor(R.color.colorTextPrimary)

        yAxisLeft.textColor = resources.getColor(R.color.colorTextPrimary)
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 100f
        yAxisRight.axisMinimum = 0f
        yAxisRight.axisMaximum = 5f
        yAxisRight.setLabelCount(5)
        yAxisRight.textColor = resources.getColor(R.color.colorTextPrimary)
    }

    inner class MyAxisFormatter : IndexAxisValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            return if (index < SmO2List.size) {
                SmO2List[index].name
            } else {
                ""
            }
        }
    }

    inner class MyAxisFormatPercent : IndexAxisValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {

            val index = value.toInt()
            return index.toString() + "0%"
        }
    }

    private fun setDataToLineChart() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()
        val entriesSmO2: ArrayList<Entry> = ArrayList()
        var listSmo2 = PrefManager.getListSmo2InStage().listSmo2
        if (listSmo2 != null) {
            SmO2List = listSmo2
        }

        Functions.showLog("Lactate: " + LactateList.toString() + " | " + listAnalysisData.listAnalyst)

        //you can replace this data object with  your custom object
        for (i in LactateList.indices) {
            val score = LactateList[i]
            entries.add(Entry(i.toFloat(), score.score.toFloat()))
        }
        for (i in SmO2List.indices) {
            val score = SmO2List[i]
            entriesSmO2.add(Entry(i.toFloat(), score.score.toFloat()))
        }

        val lactateList = LineDataSet(entries, "")
        lactateList.setAxisDependency(YAxis.AxisDependency.RIGHT)
        lactateList.color = resources.getColor(R.color.colorLactate)
        lactateList.valueTextColor = resources.getColor(android.R.color.transparent)
        lactateList.setDrawCircles(false)

       //lactateList.highLightColor=resources.getColor(R.color.colorTextPrimary)
        val smO2List = LineDataSet(entriesSmO2, "")
        smO2List.color = resources.getColor(R.color.colorTextHit)
        smO2List.valueTextColor = resources.getColor(android.R.color.transparent)
        //smO2List.axisDependency = 3
        smO2List.setDrawCircles(false)

        val finaldataset = ArrayList<LineDataSet>()
        finaldataset.add(lactateList)
        finaldataset.add(smO2List)
        val data = LineData(finaldataset as List<ILineDataSet>?)
        lcLtTestTreadmillResult.data = data

        lcLtTestTreadmillResult.invalidate()
    }

    private fun renderPostLtTestResult(resBaseModel: ResBaseModel?) {
        Functions.showLog("resBaseModel: " + resBaseModel?.let { Functions.toJsonString(it) })
        hideLoading()
        if (resBaseModel?.success == true) {
            try {
                Functions.showLog("Post Lt Test Result Success")
            } catch (e: Exception) {
                Functions.showLog(getString(R.string.failure_exception))
            }
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("loginError: " + failure.toString())
        hideLoading()
        showToast(getString(R.string.failure_network_connection))
    }
}