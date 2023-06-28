package com.obelab.repace.features.empty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.gson.Gson
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.model.*
import com.obelab.repace.viewModel.StatisticByViewModel
import kotlinx.android.synthetic.main.activity_statistic_by.*
import kotlinx.android.synthetic.main.header_back.*


class StatisticByActivity : BaseActivity() {
    private lateinit var id_smo2_heartrate: LineChart
    private lateinit var id_distance_speed: CombinedChart
    val viewStatisticByModel: StatisticByViewModel by viewModels()
    private var chartStatisticList = ArrayList<StatisticChartModel>()

    companion object {
        fun callingIntent(context: Context) = Intent(context, FriendsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic_by)

        settingView()
//        initLineChart()
        Functions.showLog("data intent444 ${intent.getStringExtra("data")} ")
        id_smo2_heartrate = findViewById(R.id.id_smo2_heartrate)
        initLineChart()

        id_distance_speed = findViewById(R.id.id_distance_speed)
        initCombinedChart()
    }

    private fun settingView() {
        val data = intent.getSerializableExtra("data") as? ExerciseStatisticSpeedModel
        val type = intent.getIntExtra(
            "type",
            0
        )
        val typeExercise = intent.getStringExtra("typeExercise")
        val acitivity = intent.getStringExtra("acitivity")

        Functions.showLog("data intent ${intent.getStringExtra("data")}  ${type} ")
        if (type == 1) {
            tvTitle.text = getText(R.string.title_statistics_by_day)
        } else if (type == 2) {
            tvTitle.text = getText(R.string.title_statistics_by_week)
        } else if (type == 3) {
            tvTitle.text = getText(R.string.title_statistics_by_month)
        }

        imvBack.setOnClickListener {
            finish()
        }

        Functions.showLog("data?.date.toString()", data?.date.toString())


        if (data != null) {
            val dateTime = Functions.dateInDay(data?.date.toString())
            Functions.showLog("dateTime date.toString() ${data?.date.toString()}")
            Functions.showLog("dateTime ${dateTime}")
            with(viewStatisticByModel) {
                observe(resStatisticByModel, ::renderStatistic)
                failure(failure, ::handleFailure)
            }
            viewStatisticByModel.getStatisticBy(RequestStatisticByModel(type, dateTime, typeExercise.toString(), acitivity.toString()))
        }

    }

    private fun renderStatistic(resBaseModel: ResBaseModel?) {
        Functions.showLog("resBaseModel: " + resBaseModel)
        Functions.showLog("resBaseModel1111: " + resBaseModel?.let { Functions.toJsonString(it) })
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel?.data?.let { Functions.toJsonString(it) }
            val resStatisticModel: ResStatisticModel? =
                gson.fromJson(dataStr, ResStatisticModel::class.java)

//        val Statistic = resStatisticModel?.average?.let { Functions.toJsonString(it) }
//        resStatisticModel?.let { setDataToLineChart(it) }
            val dataList = resStatisticModel?.statistic
            dataList?.map {
                chartStatisticList.add(it)
//                smO2List.add(it.result)
            }
            setDataToLineChart()
            setDataToCombinedChart()
            Functions.showLog("resStatisticModel ${resStatisticModel?.average?.smO2Min}")
            Functions.showLog("resStatisticModel statistic ${resStatisticModel?.statistic}")
            Functions.showLog("resStatisticModel ${resStatisticModel?.start}")
//            Functions.showLog("resStatisticModel ${Functions.formatStringToCalendar(resStatisticModel?.start.toString())}")

            val type = intent.getIntExtra(
                "type",
                0
            )
            if (type == 1) {
                tvTime.text = Functions.dateToDayInDay(resStatisticModel?.start.toString())
//                    Functions.(resStatisticModel?.start.toString()).toString()
            } else if (type == 2) {
                tvTime.text = Functions.dateToDayInWeek(resStatisticModel?.start.toString(), resStatisticModel?.end.toString())
            } else if (type == 3) {
                tvTime.text = Functions.dateToDayInWeek(resStatisticModel?.start.toString(), resStatisticModel?.end.toString())
//                    Functions.dateToDayInWeek(resStatisticModel?.start.toString(), resStatisticModel?.end.toString())
            }


//        Functions.showLog("Statistic ${Statistic}")
            id_min_smo2.text = resStatisticModel?.average?.smO2Min.toString()
            id_max_smo2.text = resStatisticModel?.average?.smO2Max.toString()
            id_avg_smo2.text = resStatisticModel?.average?.smO2Avg.toString()

            id_min_heartrate.text = resStatisticModel?.average?.heartRateMin.toString()
            id_max_heartrate.text = resStatisticModel?.average?.heartRateMax.toString()
            id_avg_heartrate.text = resStatisticModel?.average?.heartRateAvg.toString()

            id_duration.text = resStatisticModel?.average?.duration.toString()
            id_distance.text = resStatisticModel?.average?.distance.toString()
            id_max_speed.text = resStatisticModel?.average?.speedMax.toString()
            id_avg_speed.text = resStatisticModel?.average?.speedAvg.toString()
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Show notices error: " + failure.toString())
        hideLoading()
    }

    //set up combine Chart
    private fun initCombinedChart() {
        id_distance_speed.setNoDataText("")
        val xAxis: XAxis = id_distance_speed.xAxis
        val yAxisLeft: YAxis = id_distance_speed.axisLeft
        val yAxisRight: YAxis = id_distance_speed.axisRight

        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        id_distance_speed.axisRight.isEnabled = true
        id_distance_speed.xAxis.spaceMin = 0.3f
        id_distance_speed.xAxis.spaceMax = 0.3f
        //remove legend
        id_distance_speed.legend.isEnabled = false


        // remove description label
        id_distance_speed.description.isEnabled = false


        // add animation
        id_distance_speed.animateX(1000, Easing.EaseInSine)

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
        yAxisRight.axisMinimum = 100f
        yAxisRight.axisMaximum = 200f
        yAxisRight.textColor = resources.getColor(R.color.colorTextPrimary)
    }

    // ==========================================================
    //    Set up line chart
    // ==========================================================
    private fun initLineChart() {

        //    hide grid lines
//        id_smo2_heartrate.axisLeft.setDrawGridLines(false)
        id_smo2_heartrate.setNoDataText("")

        val xAxis: XAxis = id_smo2_heartrate.xAxis
        val yAxisLeft: YAxis = id_smo2_heartrate.axisLeft
        val yAxisRight: YAxis = id_smo2_heartrate.axisRight

        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        id_smo2_heartrate.axisRight.isEnabled = true
        id_smo2_heartrate.xAxis.spaceMin = 0.3f
        id_smo2_heartrate.xAxis.spaceMax = 0.3f
        //remove legend
        id_smo2_heartrate.legend.isEnabled = false


        // remove description label
        id_smo2_heartrate.description.isEnabled = false


        // add animation
        id_smo2_heartrate.animateX(1000, Easing.EaseInSine)

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
        yAxisRight.axisMinimum = 100f
        yAxisRight.axisMaximum = 200f
        yAxisRight.textColor = resources.getColor(R.color.colorTextPrimary)
    }

    inner class MyAxisFormatter : IndexAxisValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            val type = intent.getIntExtra(
                "type",
                0
            )
            if (index < chartStatisticList.size) {
//                SmO2List[index].name
                var text = ""
                if (type == 1) {
                    if (index == 0) {
                        text = "1st"
                    } else if (index == 1) {
                        text = "2nd"
                    } else if (index == 2) {
                        text = "3rd"
                    } else {
                        text = "${index + 1}th"
                    }
                } else if (type == 2) {
                    text = Functions.simpleDatetoMD(chartStatisticList[index].date)
                } else if (type == 3) {
                    text = Functions.simpleDateToWeek(chartStatisticList[index].date, chartStatisticList[index].dateEnd)
                }
                return text

            } else {
                return ""
            }
        }
    }

    private fun setDataToCombinedChart() {
        val data = CombinedData()
        data.setData(generateLineData());
        data.setData(generateBarData());

        id_distance_speed.setData(data);
        id_distance_speed.invalidate();
    }

    private fun generateLineData(): LineData? {
        val lineData = LineData()
        val minSpeed: ArrayList<Entry> = ArrayList()
        val maxSpeed: ArrayList<Entry> = ArrayList()
        val avgSpeed: ArrayList<Entry> = ArrayList()
        for (i in chartStatisticList.indices) {
            val score = chartStatisticList[i]
            minSpeed.add(Entry(i.toFloat(), score.speedMin.toFloat()))
            maxSpeed.add(Entry(i.toFloat(), score.speedMax.toFloat()))
            avgSpeed.add(Entry(i.toFloat(), score.speedAvg.toFloat()))
        }
        val setMinSpeed = LineDataSet(minSpeed, "")
        val setMaxSpeed = LineDataSet(maxSpeed, "")
        val setAvgSpeed = LineDataSet(avgSpeed, "")

//        val minSmo2List = LineDataSet(minSmo2, "")
        setMinSpeed.color = resources.getColor(R.color.minHR)
        setMinSpeed.valueTextColor = resources.getColor(android.R.color.transparent)
        setMinSpeed.setCircleColor(android.R.color.transparent)


        setMaxSpeed.color = resources.getColor(R.color.maxHR)
        setMaxSpeed.valueTextColor = resources.getColor(android.R.color.transparent)
        setMaxSpeed.setCircleColor(android.R.color.transparent)

        setAvgSpeed.color = resources.getColor(R.color.colorEdtError)
        setAvgSpeed.valueTextColor = resources.getColor(android.R.color.transparent)
        setAvgSpeed.setCircleColor(android.R.color.transparent)

//        setMinSpeed.color = R.color.minHR
//        setMinSpeed.lineWidth = 2.5f
//        setMinSpeed.setCircleColor( R.color.minHR)
//        setMinSpeed.circleRadius = 5f
//        setMinSpeed.fillColor =  R.color.minHR
        setMinSpeed.mode = LineDataSet.Mode.CUBIC_BEZIER
        setMinSpeed.setDrawValues(true)
//        set.valueTextSize = 10f
//        set.valueTextColor = R.color.maxHR
        setMinSpeed.axisDependency = YAxis.AxisDependency.LEFT
        setMaxSpeed.axisDependency = YAxis.AxisDependency.LEFT
        setAvgSpeed.axisDependency = YAxis.AxisDependency.LEFT
        lineData.addDataSet(setMinSpeed)
        lineData.addDataSet(setMaxSpeed)
        lineData.addDataSet(setAvgSpeed)

        return lineData
    }

    private fun generateBarData(): BarData? {
//        val entries1: ArrayList<BarEntry> = ArrayList()
//        val entries2: ArrayList<BarEntry> = ArrayList()
//        for (index in 0 until count) {
//            entries1.add(BarEntry(0, getRandom(25, 25)))
//
//            // stacked
//            entries2.add(BarEntry(0, floatArrayOf(getRandom(13, 12), getRandom(13, 12))))
//        }
        val distance: ArrayList<BarEntry> = ArrayList()
        for (i in chartStatisticList.indices) {
            Functions.showLog("Combined 555 ${chartStatisticList}")
            val score = chartStatisticList[i]
            distance.add(BarEntry(i.toFloat(), score.distance.toFloat()))
        }

        val setDistance = BarDataSet(distance, "")
        setDistance.color = resources.getColor(R.color.colorLactate)
//        set1.valueTextColor = R.color.colorLactate
//        set1.valueTextSize = 10f
        setDistance.axisDependency = YAxis.AxisDependency.RIGHT
//        val set2 = BarDataSet(entries2, "")
//        set2.stackLabels = arrayOf("Stack 1", "Stack 2")
//        set2.setColors(R.color.minHR, R.color.minSmo2)
//        set2.valueTextColor = R.color.minHR
//        set2.valueTextSize = 10f
//        set2.axisDependency = YAxis.AxisDependency.LEFT
        val groupSpace = 0.06f
        val barSpace = 0.02f // x2 dataset
        val barWidth = 0.45f // x2 dataset
        // (0.45 + 0.02) * 2 + 0.06 = 1.00 -> interval per "group"
//        val barData = BarData(set1, set2)
        val barData = BarData(setDistance)
        barData.barWidth = barWidth

        // make this BarData object grouped
//        barData.groupBars(0f, groupSpace, barSpace) // start at x = 0
        return barData
    }

    private fun setDataToLineChart() {
//now draw bar chart with dynamic data
        val minSmo2: ArrayList<Entry> = ArrayList()
        val maxSmo2: ArrayList<Entry> = ArrayList()
        val avgSmo2: ArrayList<Entry> = ArrayList()

        val minHR: ArrayList<Entry> = ArrayList()
        val maxHR: ArrayList<Entry> = ArrayList()
        val avgHR: ArrayList<Entry> = ArrayList()
        //you can replace this data object with  your custom object
        for (i in chartStatisticList.indices) {
            Functions.showLog("resStatisticModel555 ${chartStatisticList}")
            val score = chartStatisticList[i]
            Functions.showLog("resStatisticModel score ${score}")

            minSmo2.add(Entry(i.toFloat(), score.smo2Min.toFloat()))
            maxSmo2.add(Entry(i.toFloat(), score.smO2Max.toFloat()))
            avgSmo2.add(Entry(i.toFloat(), score.smO2Avg.toFloat()))

            minHR.add(Entry(i.toFloat(), score.heartRateMin.toFloat()))
            maxHR.add(Entry(i.toFloat(), score.heartRateMax.toFloat()))
            avgHR.add(Entry(i.toFloat(), score.heartRateAvg.toFloat()))
//            val score = chartStatisticList[i]
//            entries.add(Entry(i.toFloat(), score.lactateOnset.toFloat()))
        }
        val minSmo2List = LineDataSet(minSmo2, "")
        minSmo2List.color = resources.getColor(R.color.minSmo2)
        minSmo2List.valueTextColor = resources.getColor(android.R.color.transparent)
        minSmo2List.setCircleColor(android.R.color.transparent)

        val maxSmo2List = LineDataSet(maxSmo2, "")
        maxSmo2List.color = resources.getColor(R.color.maxSmo2)
        maxSmo2List.valueTextColor = resources.getColor(android.R.color.transparent)
        maxSmo2List.setCircleColor(android.R.color.transparent)

        val avgSmo2List = LineDataSet(avgSmo2, "")
        avgSmo2List.color = resources.getColor(R.color.colorSmo2)
        avgSmo2List.valueTextColor = resources.getColor(android.R.color.transparent)
        avgSmo2List.setCircleColor(android.R.color.transparent)

        val minHRList = LineDataSet(minHR, "")
        minHRList.color = resources.getColor(R.color.minHR)
        minHRList.valueTextColor = resources.getColor(android.R.color.transparent)
        minHRList.setCircleColor(android.R.color.transparent)

        val maxHRList = LineDataSet(maxHR, "")
        maxHRList.color = resources.getColor(R.color.maxHR)
        maxHRList.valueTextColor = resources.getColor(android.R.color.transparent)
        maxHRList.setCircleColor(android.R.color.transparent)

        val avgHRList = LineDataSet(avgHR, "")
        avgHRList.color = resources.getColor(R.color.colorEdtError)
        avgHRList.valueTextColor = resources.getColor(android.R.color.transparent)
        avgHRList.setCircleColor(android.R.color.transparent)

        val finaldataset = ArrayList<LineDataSet>()
        finaldataset.add(minSmo2List)
        finaldataset.add(maxSmo2List)
        finaldataset.add(avgSmo2List)

        finaldataset.add(minHRList)
        finaldataset.add(maxHRList)
        finaldataset.add(avgHRList)
        val data = LineData(finaldataset as List<ILineDataSet>?)
//        val data = LineData(lineDataSet)
        id_smo2_heartrate.data = data

        id_smo2_heartrate.invalidate()
        Functions.showLog("resStatisticModel5556 ${chartStatisticList} ")
    }
}