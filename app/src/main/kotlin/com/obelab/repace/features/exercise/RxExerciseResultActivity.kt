package com.obelab.repace.features.exercise

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.FileHelper
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.model.*
import com.obelab.repace.viewModel.ExerciseResultViewModel
import kotlinx.android.synthetic.main.activity_rx_exercise_result.*
import kotlinx.android.synthetic.main.header_back_share.*

class RxExerciseResultActivity : BaseActivity() {

    private val TAG = "RxExerciseResultActivity"
    private val viewModel: ExerciseResultViewModel by viewModels()
    private var LactateList = ArrayList<ScoreChartModel>()
    private var SmO2List = ArrayList<SmO2ChartModel>()
    private var isShowSmo2 = true
    private var isShowHeartrate = true
    private var typeId = PrefManager.getTypeId()
    private var intensityId = PrefManager.getIntensityId()
    private var activityId = PrefManager.getActivityId()
    private var dataSavePrefListSmo2: ListSmo2LineChartModel = ListSmo2LineChartModel.empty

    companion object {
        fun callingIntent(context: Context) =
            Intent(context, RxExerciseResultActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(viewModel) {
            observe(resPostExerciseResult, ::renderPostExerciseResult)
            failure(failure, ::handleFailure)
        }
        setContentView(R.layout.activity_rx_exercise_result)
        initLineChart()
        setUpView()
    }

    private fun setUpMap(data: MutableList<LocationModel>) {
        // Set up map box
        mapView?.getMapboxMap()?.loadStyleUri(Style.DARK)
        val startLocation = if (data.isNotEmpty()) data[0] else LocationModel(Constants.LAT_DEFAULT, Constants.LON_DEFAULT, 0)
        val stopLocation = if (data.isNotEmpty()) data[data.size - 1] else LocationModel(Constants.LAT_DEFAULT, Constants.LON_DEFAULT, 0)
        val startPoint = Point.fromLngLat(startLocation.longitude, startLocation.latitude)
        val stopPoint = Point.fromLngLat(stopLocation.longitude, stopLocation.latitude)
        val centerPosition = CameraOptions.Builder().zoom(15.0).center(startPoint).build()
        // set camera center position
        mapView.getMapboxMap().setCamera(centerPosition)
        // Set up point list
        val pointList = ArrayList<Point>()
        data.map {
            pointList.add(Point.fromLngLat(it.longitude, it.latitude))
        }
        val annotationApi = mapView?.annotations
        val polylineAnnotationManager = annotationApi?.createPolylineAnnotationManager()
        // Set options for the resulting line layer.
        val polylineAnnotationOptions: PolylineAnnotationOptions = PolylineAnnotationOptions()
            .withPoints(pointList)
            .withLineColor(Color.YELLOW)
            .withLineWidth(3.0)
        // Add the resulting line to the map.
        polylineAnnotationManager?.create(polylineAnnotationOptions)
        val polygon = Polygon.fromLngLats(listOf(pointList))
        // Convert to a camera options from a given geometry and padding
        val cameraPosition = mapView?.getMapboxMap()?.cameraForGeometry(polygon, EdgeInsets(-200.0, -200.0, -200.0, -200.0))
        // Set camera position
        cameraPosition?.let { mapView?.getMapboxMap()!!.setCamera(it) }
        // Add start marker and end maker
        val pointAnnotationManager = annotationApi?.createPointAnnotationManager()
        val pointStartAnnotationOptions = PointAnnotationOptions().withPoint(startPoint)
            .withIconImage(FileHelper.getResizedBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_map_start), Constants.ICON_MAP_MARKER_SIZE, Constants.ICON_MAP_MARKER_SIZE))
        val pointStopAnnotationOptions = PointAnnotationOptions().withPoint(stopPoint)
            .withIconImage(FileHelper.getResizedBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_map_stop), Constants.ICON_MAP_MARKER_SIZE, Constants.ICON_MAP_MARKER_SIZE))
        pointAnnotationManager?.create(pointStartAnnotationOptions)
        pointAnnotationManager?.create(pointStopAnnotationOptions)
    }

    private fun setUpView() {

        tvTitle.text = getText(R.string.rx_exercise_result)

        if (intent.getSerializableExtra(Constants.data) != null) {
            val data = intent.getSerializableExtra(Constants.data) as? ExerciseResultModel
            Functions.showLog("DATA CHECK: $data")
            if (data != null) {
                tvTime.text = data.createdAt?.let { Functions.dateToDayInDay(it) }
                tvStartTime.text = data.startTime
                tvEndTime.text = data.endTime
                tvHeartRate.text = data.heartRate.toString()
                dataSavePrefListSmo2.listSmo2 = data.listSmo2
                setDataToLineChart(true, true)
                data.listLocation?.let { setUpMap(it) }
                //Setup textview Title Result
                if (data.intensityId == Constants.low_intensity) {
                    if (data.activityId == Constants.ex_treadmill) {
                        tvTitleResult.text = getText(R.string.title_treadmill_running_low_intensity)
                    } else if (data.activityId == Constants.ex_outdoor) {
                        tvTitleResult.text = getText(R.string.title_outdoor_running_low_intensity)
                    }
                } else {
                    if (data.activityId == Constants.ex_treadmill) {
                        tvTitleResult.text = getText(R.string.title_treadmill_running_high_intensity)
                    } else if (data.activityId == Constants.ex_outdoor) {
                        tvTitleResult.text = getText(R.string.title_outdoor_running_high_intensity)
                    }
                }

                tvMinSmo2.text = data.smo2Min.toString()
                tvMaxSmo2.text = data.smo2Max.toString()
                tvAvgSmo2.text = String.format("%.1f", data.smo2Avg).replace(",", ".")
                Functions.showLog(TAG, "Smo2 Min: ${data.smo2Min} || Smo2 Max: ${data.smo2Max} || Smo2 Avg: ${String.format("%.1f", data.smo2Avg).replace(",", ".")}")
                tvMinHeartrate.text = if (data.heartRateMin != null) data.heartRateMin.toString() else "-"
                tvMaxHeartrate.text = if (data.heartRateMin != null) data.heartRateMax.toString() else "-"
                tvAvgHeartrate.text = if (data.heartRateMin != null) data.heartRateAvg.toString() else "-"
                // Check type id
                if (data.typeId == Constants.rx_exercise) {
                    //Case type is RX EXERCISE
                    llSession.visibility = View.VISIBLE
                    tvSession.text = if (data.sessionCnt != null) data.sessionCnt.toString() else "-"
                    llPrescription.visibility = View.VISIBLE
                    llFreeExercise.visibility = View.GONE
                    tvDuration.text = data.time.toString()
                    tvSpeed.text = data.speed.toString()
                    if (data.activityId == Constants.ex_treadmill) {
                        cvMap.visibility = View.GONE
                    } else if (data.activityId == Constants.ex_outdoor) {
                        cvMap.visibility = View.VISIBLE
                    }
                } else {
                    // Case type is FREE EXERCISE
                    llSession.visibility = View.INVISIBLE
                    llPrescription.visibility = View.GONE
                    llFreeExercise.visibility = View.VISIBLE
                    if (data.totalDistance != null) {
                        tvDistance.text = data.totalDistance.toString()
                    } else {
                        tvDistance.text = "-"
                    }
                    tvFreeDuration.text = convertTime(data.totalDuration)

                    tvMaxSpeed.text = String.format("%.1f", data.speedMax).replace(",", ".")
                    tvAvgSpeed.text = String.format("%.1f", data.speedAvg).replace(",", ".")

                    var pace = convertTimeSecondary((data.totalDuration / data.totalDistance).toInt())
                    tvPace.text = pace

                    if (data.activityId == Constants.ex_treadmill) {
                        llFreeOutdoor.visibility = View.GONE
                        cvMap.visibility = View.GONE
                    } else if (data.activityId == Constants.ex_outdoor) {
                        llFreeOutdoor.visibility = View.VISIBLE
                        cvMap.visibility = View.VISIBLE
                    }
                }
            }
        }

        btnSmo2.setOnClickListener {
            isShowSmo2 = !isShowSmo2
            showSmo2(isShowSmo2)
            setDataToLineChart(isShowSmo2, isShowHeartrate)
        }
        btnHeartrate.setOnClickListener {
            isShowHeartrate = !isShowHeartrate
            showHeartRate(isShowHeartrate)
            setDataToLineChart(isShowSmo2, isShowHeartrate)
        }

        btnOK.setOnClickListener {
            startActivity(MainActivity.callingIntent(this))
        }

        imvShare.setOnClickListener {
            startActivity(RxExerciseShareResultActivity.callingIntent(this))
        }

        imvBack.setOnClickListener {
            finish()
        }
    }

    private fun showSmo2(isShow: Boolean) {
        if (isShow == true) {
            btnSmo2.setBackgroundResource(R.drawable.btn_enable_default)
            tvBtnSmo2.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorTextPrimary
                )
            )
        } else {
            btnSmo2.setBackgroundResource(R.drawable.btn_disable)
            tvBtnSmo2.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorText
                )
            )
        }
    }

    private fun showHeartRate(isShow: Boolean) {
        if (isShow == true) {
            btnHeartrate.setBackgroundResource(R.drawable.bg_lactate)
            tvBtnHeartRate.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorTextPrimary
                )
            )
        } else {
            btnHeartrate.setBackgroundResource(R.drawable.btn_disable)
            tvBtnHeartRate.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorText
                )
            )
        }
    }

    private fun initLineChart() {

//        hide grid lines
        lcRxExerciseTreadmillResult.axisLeft.setDrawGridLines(false)
        lcRxExerciseTreadmillResult.setNoDataText("")

        val xAxis: XAxis = lcRxExerciseTreadmillResult.xAxis
        val yAxisLeft: YAxis = lcRxExerciseTreadmillResult.axisLeft
        val yAxisRight: YAxis = lcRxExerciseTreadmillResult.axisRight

        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        lcRxExerciseTreadmillResult.axisRight.isEnabled = true
        lcRxExerciseTreadmillResult.xAxis.spaceMin = 0.3f
        lcRxExerciseTreadmillResult.xAxis.spaceMax = 0.3f
        //remove legend
        lcRxExerciseTreadmillResult.legend.isEnabled = false

        // remove description label
        lcRxExerciseTreadmillResult.description.isEnabled = false

        // add animation
        lcRxExerciseTreadmillResult.animateX(1000, Easing.EaseInSine)

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
            Functions.showLog("SMO2 LIST: $SmO2List")
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

    private fun setDataToLineChart(isShowSmo2: Boolean, isShowHeartRate: Boolean) {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()
        val entriesSmO2: ArrayList<Entry> = ArrayList()
        Functions.showLog("typeId: $isShowSmo2 ${dataSavePrefListSmo2.listSmo2!!}")

        if (isShowSmo2) {
            if (dataSavePrefListSmo2 != null) {
                SmO2List = arrayListOf()
                SmO2List = dataSavePrefListSmo2.listSmo2!!
            }
        } else {
            SmO2List = arrayListOf()
        }

        SmO2List.sortBy { it.name }

        if (isShowHeartRate) {
            if (dataSavePrefListSmo2 != null) {
                LactateList = arrayListOf()
                LactateList = getLactateList()
            }
        } else {
            LactateList = arrayListOf()
        }

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
        lactateList.color = resources.getColor(R.color.colorLactate)
        lactateList.valueTextColor = resources.getColor(android.R.color.transparent)
        lactateList.setDrawCircles(false)

        val smO2List = LineDataSet(entriesSmO2, "")
        smO2List.color = resources.getColor(R.color.colorTextHit)
        smO2List.valueTextColor = resources.getColor(android.R.color.transparent)
        smO2List.setDrawCircles(false)

        val finalDataset = ArrayList<LineDataSet>()
        finalDataset.add(lactateList)
        finalDataset.add(smO2List)
        val data = LineData(finalDataset as List<ILineDataSet>?)
        lcRxExerciseTreadmillResult.data = data
        lcRxExerciseTreadmillResult.invalidate()
    }

    private fun getLactateList(): ArrayList<ScoreChartModel> {
        LactateList.add(ScoreChartModel("A", 69.0))
        return LactateList
    }

    private fun renderPostExerciseResult(resBaseModel: ResBaseModel?) {
        hideLoading()
        Functions.showLog("resBaseModel: " + resBaseModel?.let { Functions.toJsonString(it) })
        if (resBaseModel?.success == true) {
            Functions.showLog("Post Rx Exercise Result Success")
        } else {
            resBaseModel?.msg?.let { Functions.showLog(TAG, it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Post rx exercise result error: " + failure.toString())
        hideLoading()
    }

    private fun convertTime(time: Int): String {
        val hours = (time / 3600)
        val minute = ((time % 3600) / 60)
        val second = (time % 60)
        var result = "00 : 00"
        if (hours > 9) {
            result = if (minute > 9) {
                if (second > 9) {
                    "$hours : $minute : $second"
                } else {
                    "$hours : $minute : 0$second"
                }
            } else {
                if (second > 9) {
                    "$hours : 0$minute : $second"
                } else {
                    "$hours : 0$minute : 0$second"
                }
            }
        } else {
            result = if (minute > 9) {
                if (second > 9) {
                    "0$hours : $minute : $second"
                } else {
                    "0$hours : $minute : 0$second"
                }
            } else {
                if (second > 9) {
                    "0$hours : 0$minute : $second"
                } else {
                    "0$hours : 0$minute : 0$second"
                }
            }
        }

        return result
    }

    private fun convertTimeSecondary(time: Int): String {
        var minute = (time / 60)
        var second = (time % 60)
        var result = "-"
        result = if (minute > 9) {
            if (second > 9) {
                "$minute’ $second’’"
            } else {
                "$minute’ 0$second’’"
            }
        } else {
            if (second > 9) {
                "0$minute’ $second’’"
            } else {
                "0$minute’ 0$second’’"
            }
        }
        return result
    }
}