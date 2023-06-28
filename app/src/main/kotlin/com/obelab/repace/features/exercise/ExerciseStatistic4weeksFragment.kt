package com.obelab.repace.features.exercise

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.facebook.FacebookSdk
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.gson.GsonBuilder
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.model.ExerciseStatisticModel
import com.obelab.repace.model.ExerciseStatisticSpeedModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.viewModel.GetExerciseResultViewModel
import kotlinx.android.synthetic.main.fragment_exercise_statistic_7days.*
import java.util.*


class ExerciseStatistic4weeksFragment : BaseFragment() {
    private val SPEED_SELECT = 1
    private val DISTANCE_SELECT = 2
    private val TIME_SELECT = 3
    private val SMO2_SELECT = 4

    private lateinit var barChart: BarChart
    private var speed = ArrayList<ExerciseStatisticSpeedModel>()

    private val viewModel: GetExerciseResultViewModel by viewModels()

    companion object {
        fun callingIntent(context: Context) = Intent(context, ExerciseStatisticDetailActivity::class.java)
    }

    override fun layoutId(): Int {
        return R.layout.fragment_exercise_statistic_7days
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        barChart = view?.findViewById(R.id.barChartMain) ?: barChartMain
        barChart.setNoDataText("")
        val mAcitive = activity as ExerciseStatisticDetailActivity
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            observe(resExerciseLast4WeeksResultModel, ::renderExercise)
            failure(failure, ::handleFailure)
        }
        viewModel.getExercise4weeks(mAcitive.query)
        setupFragment()
    }

    private fun renderExercise(resBaseModel: ResBaseModel?) {
        hideLoading()
        initBarChart()
        if (resBaseModel?.success == true) {
            val gson = GsonBuilder().create()
            Functions.showLog("Exercise String: " + resBaseModel.data?.let { Functions.toJsonString(it) })
            val exer = gson.fromJson(
                resBaseModel.data?.let { Functions.toJsonString(it) },
                ExerciseStatisticModel::class.java
            )
            Functions.showLog("dataList: " + exer)
            exer.statistic?.map {
                speed.add(it)
            }
            setDataToBarChart(SPEED_SELECT)
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Show notices error: " + failure.toString())
        hideLoading()
    }

    private fun initBarChart() {
        barChart.setNoDataText("")
//        hide grid lines
        barChart.axisLeft.setDrawGridLines(false)

        val xAxis: XAxis = barChart.xAxis
        val yAxisLeft: YAxis = barChart.axisLeft

        yAxisLeft.axisMinimum = 0f

        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.setDrawAxisLine(false)

        //remove right y-axis
        barChart.axisRight.isEnabled = false
        barChart.xAxis.spaceMin = 0.3f
        barChart.xAxis.spaceMax = 0.3f

        //remove legend
        barChart.legend.isEnabled = false


        // add description label
        barChart.description.isEnabled = false

        // add animation
        barChart.animateX(1000, Easing.EaseInSine)

        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter()
        Functions.showLog("data check ${xAxis.valueFormatter}")
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
//        xAxis.labelRotationAngle = +90f
        xAxis.textColor = resources.getColor(R.color.colorTextPrimary)
        xAxis.axisLineColor = resources.getColor(R.color.colorTextPrimary)
        yAxisLeft.textColor = resources.getColor(R.color.colorTextPrimary)

    }

    inner class MyAxisFormatter : IndexAxisValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()

            if (index < speed.size) {
                val date: Calendar = Functions.formatStringToCalendar(speed[index].date)
                val dutedate: Calendar = Functions.formatStringToCalendar(speed[index].date)
                dutedate.add(Calendar.DAY_OF_MONTH, 6)
                if (date.time.month == dutedate.time.month) {
                    return "${Functions.formatNumber(date.time.month + 1)}/${Functions.formatNumber(date.time.date)} - ${Functions.formatNumber(dutedate.time.date)}"
                } else {
                    return "${Functions.formatNumber(date.time.month + 1)}/${Functions.formatNumber(date.time.date)} - ${Functions.formatNumber(dutedate.time.month + 1)}/${Functions.formatNumber(dutedate.time.date)}"
                }
            } else {
                return ""
            }
        }
    }

    private fun setDataToBarChart(flag: Int) {

        // Check to show textview no data
        var isHasData = false
        var dataCheck = 0F

        //now draw bar chart with dynamic data
        val entries: ArrayList<BarEntry> = ArrayList()
        for (i in speed.indices) {
            val score = speed[i]
            when (flag) {
                SPEED_SELECT -> {
                    if (!isHasData) {
                        isHasData = score.speed.toFloat() != 0F
                    }
                    entries.add(BarEntry(i.toFloat(), score.speed.toFloat()))
                }
                DISTANCE_SELECT -> {
                    entries.add(BarEntry(i.toFloat(), (score.distance / 1000).toFloat()))
                }
                TIME_SELECT -> {
                    entries.add(BarEntry(i.toFloat(), score.time.toFloat()))
                }
                SMO2_SELECT -> {
                    entries.add(BarEntry(i.toFloat(), score.smO2.toFloat()))
                }
            }
            // Update the var to show textview no data
            if (!isHasData) {
                isHasData = dataCheck != 0F
            }
        }

        // Show/hide textview no data
        tvNoDataForChart.visibility = if (isHasData) View.GONE else View.VISIBLE

        val speedList = BarDataSet(entries, "")
        speedList.valueTextColor = resources.getColor(android.R.color.transparent)
        when (flag) {
            SPEED_SELECT -> speedList.color = resources.getColor(R.color.colorEdtError)
            DISTANCE_SELECT -> speedList.color = resources.getColor(R.color.colorLactate)
            TIME_SELECT -> speedList.color = resources.getColor(R.color.colorTime)
            SMO2_SELECT -> speedList.color = resources.getColor(R.color.colorSmo2)
        }

        val finaldataset = ArrayList<BarDataSet>()
        finaldataset.add(speedList)
        val data = BarData(finaldataset as List<IBarDataSet>?)
        barChart.data = data
        val dataset = BarData(speedList)
        dataset.barWidth = 0.2f // set custom bar width

        barChart.setData(dataset)
        barChart.invalidate()
    }

    private fun setupFragment() {
        btnSpeed.setOnClickListener {
            btnSpeed.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorTextPrimary))
            btnDistance.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
            btnTime.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
            btnSmo2.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
            btnSpeed.setBackgroundResource(R.drawable.btn_enable_speed)
            btnDistance.setBackgroundResource(R.drawable.btn_disable)
            btnTime.setBackgroundResource(R.drawable.btn_disable)
            btnSmo2.setBackgroundResource(R.drawable.btn_disable)
            setDataToBarChart(SPEED_SELECT)
        }
        btnDistance.setOnClickListener {
            btnSpeed.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
            btnDistance.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorTextPrimary))
            btnTime.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
            btnSmo2.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
            btnSpeed.setBackgroundResource(R.drawable.btn_disable)
            btnDistance.setBackgroundResource(R.drawable.btn_enable_distance)
            btnTime.setBackgroundResource(R.drawable.btn_disable)
            btnSmo2.setBackgroundResource(R.drawable.btn_disable)
            setDataToBarChart(DISTANCE_SELECT)
        }
        btnTime.setOnClickListener {
            btnSpeed.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
            btnDistance.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
            btnTime.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorTextPrimary))
            btnSmo2.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
            btnSpeed.setBackgroundResource(R.drawable.btn_disable)
            btnDistance.setBackgroundResource(R.drawable.btn_disable)
            btnTime.setBackgroundResource(R.drawable.btn_enable_time)
            btnSmo2.setBackgroundResource(R.drawable.btn_disable)
            setDataToBarChart(TIME_SELECT)
        }
        btnSmo2.setOnClickListener {
            btnSpeed.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
            btnDistance.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
            btnTime.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
            btnSmo2.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorTextPrimary))
            btnSpeed.setBackgroundResource(R.drawable.btn_disable)
            btnDistance.setBackgroundResource(R.drawable.btn_disable)
            btnTime.setBackgroundResource(R.drawable.btn_disable)
            btnSmo2.setBackgroundResource(R.drawable.btn_enable_smo2)
            setDataToBarChart(SMO2_SELECT)
        }
    }
}