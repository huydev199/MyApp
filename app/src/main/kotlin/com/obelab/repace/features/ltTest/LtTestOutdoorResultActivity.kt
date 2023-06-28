package com.obelab.repace.features.ltTest

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
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
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.min
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.adapter.LtTestPerformanceAdapter
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.FileHelper
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.model.*
import com.obelab.repace.viewModel.LtTestResultViewModel
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.*
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.btnExercisePrescription
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.imvHelpCircle
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.rcvListLtTestPerformance
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.tvLactateThresholdInfoMore
import kotlinx.android.synthetic.main.activity_lt_test_treadmill_result.*
import kotlinx.android.synthetic.main.header_back_share.*
import kotlinx.android.synthetic.main.header_share.imvShare
import kotlinx.android.synthetic.main.header_share.tvTitle

class LtTestOutdoorResultActivity : BaseActivity() {
    private val TAG = "LtTestOutdoorResultActivity"
    private var isShowMore = false
    private var LactateList = ArrayList<ScoreChartModel>()
    private var smO2List = ArrayList<SmO2ChartModel>()
    private val viewModel: LtTestResultViewModel by viewModels()
    private val listAnalysisData = PrefManager.getAnalysis()

    companion object {
        fun callingIntent(context: Context) =
            Intent(context, LtTestOutdoorResultActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lt_test_outdoor_result)
        with(viewModel) {
            observe(resLtTestResult, ::renderPostLtTestResult)
            failure(failure, ::handleFailure)
        }
        setUpView()
        initLineChart()
        setDataToLineChart()
        Functions.showLog("LTTestLocationList -> ${Functions.toJsonString(PrefManager.getLTTestLocationList())}")
        setUpMap()
    }

    private fun setUpMap(){
        // Set up map box
        mapView?.getMapboxMap()?.loadStyleUri(Style.DARK)
        val startLocation = if(PrefManager.getLTTestLocationList().isNotEmpty()) PrefManager.getLTTestLocationList()[0] else LocationModel(Constants.LAT_DEFAULT, Constants.LON_DEFAULT, 0)
        val stopLocation = if(PrefManager.getLTTestLocationList().isNotEmpty()) PrefManager.getLTTestLocationList()[PrefManager.getLTTestLocationList().size-1] else LocationModel(Constants.LAT_DEFAULT, Constants.LON_DEFAULT, 0)
        val startPoint = Point.fromLngLat(startLocation.longitude, startLocation.latitude)
        val stopPoint = Point.fromLngLat(stopLocation.longitude, stopLocation.latitude)
        val centerPosition = CameraOptions.Builder().zoom(15.0).center(startPoint).build()
        // set camera center position
        mapView.getMapboxMap().setCamera(centerPosition)
        // Set up point list
        val pointList = ArrayList<Point>()
        PrefManager.getLTTestLocationList().map {
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
        // Set text
        tvTitle.text = getText(R.string.title_LT_Test)
        id_total_distance.text = if(PrefManager?.getTotalDistance() == -1.0f ) "0.0" else    String.format("%.1f", PrefManager?.getTotalDistance()).replace(",", ".")

        if (listAnalysisData.listAnalyst?.size!! >0){
            for(i in listAnalysisData.listAnalyst!!.indices){
                if(LactateList != null){
                    LactateList.add(ScoreChartModel("",listAnalysisData.listAnalyst!![i].lactate))
                } else {
                    LactateList = arrayListOf(ScoreChartModel("", listAnalysisData.listAnalyst!![i].lactate))
                }
            }
        }

        id_total_duration.text = (PrefManager?.getStage() * 5).toString()
        id_total_stage.text = PrefManager.getStage().toString()

        // SettingDate
        val date = getCurrentDateTime()
        val dateInString = date.toString("MMM dd, yyyy")
        id_time_result.text = dateInString
        var listData = PrefManager.getListLtTestPerformance()
        listData.listLtTestPerformance?.let { settingView(it) }

        Functions.showLog("listData "+listData)
        var listSmo2 = PrefManager.getListSmo2InStage().listSmo2
        var stage=listData
       var listLocation= PrefManager.getLTTestLocationList()
        //SET MAX SPEED
        var maxSpeed: Double? = null
        var minSpeed: Double? = null
        var avgSpeed: Double? = null
        if (listData.listLtTestPerformance != null) {
            maxSpeed = getMaxSpeed(listData.listLtTestPerformance!!)
            minSpeed = getMinSpeed(listData.listLtTestPerformance!!)
            avgSpeed = getAvgSpeed(listData.listLtTestPerformance!!)
        }
        id_max_speed.text = String.format("%.1f", maxSpeed).replace(",", ".")

        //Setting SMO2 value
        var minSmo2 = 0.0
        var maxSmo2 = 0.0
        var avgSmo2 = 0.0
        var arrayListSmo2 = PrefManager.getAllListSMO2()
        if (arrayListSmo2?.listSMO2 != null) {
            if (arrayListSmo2.listSMO2 != null) {
                minSmo2 = arrayListSmo2.listSMO2!!.min()?.toDouble() ?: 0.0
                maxSmo2 = arrayListSmo2.listSMO2!!.max()?.toDouble() ?: 0.0
                avgSmo2 = arrayListSmo2.listSMO2!!.average()
                Functions.showLog(TAG,"Smo2 Min: ${arrayListSmo2.listSMO2!!.min()} || Smo2 Max: ${arrayListSmo2.listSMO2!!.max()} || Smo2 Avg: ${arrayListSmo2.listSMO2!!.average()}")
            }
            id_min_smo2.text = minSmo2.toString()
            id_max_smo2.text = maxSmo2.toString()
            id_avg_smo2.text = String.format("%.1f", avgSmo2).replace(",", ".")
        }

        //Get Analysis and calculate onset and threshold value
        val listAnalysisData = PrefManager.getAnalysis()
        var onSet = 0.0
        var threshold = 0.0
        if (listAnalysisData?.listAnalyst?.isNotEmpty() == true) {
            listAnalysisData.listAnalyst!!.sumByDouble { it.onset }
            var sizeData = listAnalysisData.listAnalyst!!.size
            onSet = listAnalysisData.listAnalyst!!.sumByDouble { it.onset } / sizeData
            threshold = listAnalysisData.listAnalyst!!.sumByDouble { it.threshold } / sizeData
            id_onset.text = String.format("%.1f", onSet).replace(",", ".")
            id_4mmol.text = String.format("%.1f", threshold).replace(",", ".")
        }

        // Set speaker
        imvHelpCircle.setOnClickListener {
            if (!isShowMore) {
                imvHelpCircle.setImageResource(R.drawable.ic_x_circle)
                isShowMore = true
                tvLactateThresholdInfoMore.visibility = View.VISIBLE
            } else {
                imvHelpCircle.setImageResource(R.drawable.ic_help_circle)
                isShowMore = false
                tvLactateThresholdInfoMore.visibility = View.GONE
            }
        }
        imvBack.setOnClickListener {
            startActivity(MainActivity.callingIntent(this))
        }

        btnExercisePrescription.setOnClickListener {
            startActivity(ExercisePrescriptionActivity.callingIntent(this))
        }
        if(PrefManager.getStage()>0&&PrefManager?.getTotalDistance()>0) {
            var pace = Functions.convertTimeSecondary(((PrefManager.getStage() * 5) / PrefManager?.getTotalDistance()).toInt())
            tvPace_outdoor.text = pace
            Functions.showLog("PrefManager?.getTotalDistance() " + (PrefManager.getStage() * 5) + PrefManager?.getTotalDistance())
        }
        Functions.showLog("listLocation "+listLocation)
        viewModel.postLtTestResult(
            RequestLtTestResultModel(
                Constants.outdoor_test,
                PrefManager.getStage(),
                (PrefManager.getStage()*5),
                PrefManager?.getTotalDistance().toDouble(),
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
            data.putString(Constants.LAST_STAGE, PrefManager.getStage().toString())
            data.putString(Constants.MAX_SPEED, maxSpeed.toString())
            data.putString(Constants.TOTAL_DISTANCE, String.format("%.1f", PrefManager?.getTotalDistance()).replace(",", "."))
            data.putString(Constants.TOTAL_DURATION, (PrefManager.getStage() * 5).toString())
            data.putString(Constants.ONSET,String.format("%.1f", onSet).replace(",", "."))
            data.putString(Constants.THRESHOLD,String.format("%.1f", threshold).replace(",", "."))
            intent.putExtras(data)
            startActivity(intent)
        }
    }

    private fun getMaxSpeed(listData: ArrayList<LtTestPerformanceModel>): Double {
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

    private fun getMinSpeed(listData: ArrayList<LtTestPerformanceModel>): Double {
        var minSpeed = 9999999.0
        if (listData.size > 0) {
            for (data in listData) {
                if (data.speed < minSpeed) {
                    minSpeed = data.speed
                }
            }
        }
        return minSpeed
    }

    private fun getAvgSpeed(listData: ArrayList<LtTestPerformanceModel>): Double {
        var avgSpeed = 0.0
        var sum = 0.0
        if (listData.size > 0) {
            for (data in listData) {
                sum += data.speed
            }
        }
        avgSpeed = sum/listData.size
        return avgSpeed
    }

    private fun settingView(listData: ArrayList<LtTestPerformanceModel>) {
        var layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rcvListLtTestPerformance.layoutManager = layoutManager
        var faqAdapter = LtTestPerformanceAdapter(listData)
        rcvListLtTestPerformance.adapter = faqAdapter
    }

    private fun initLineChart() {
        lcLtTestOutdoorResult.axisLeft.setDrawGridLines(false)
        lcLtTestOutdoorResult.setNoDataText("")

        val xAxis: XAxis = lcLtTestOutdoorResult.xAxis
        val yAxisLeft: YAxis = lcLtTestOutdoorResult.axisLeft
        val yAxisRight: YAxis = lcLtTestOutdoorResult.axisRight
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        //remove right y-axis
        lcLtTestOutdoorResult.axisRight.isEnabled = true
        lcLtTestOutdoorResult.xAxis.spaceMin = 0.3f
        lcLtTestOutdoorResult.xAxis.spaceMax = 0.3f
        //remove legend
        lcLtTestOutdoorResult.legend.isEnabled = false
        // remove description label
        lcLtTestOutdoorResult.description.isEnabled = false
        // add animation
        lcLtTestOutdoorResult.animateX(1000, Easing.EaseInSine)
        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
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
            return if (index < smO2List.size) {
                smO2List[index].name
            } else {
                ""
            }
        }
    }

    private fun setDataToLineChart() {
        val entries: ArrayList<Entry> = ArrayList()
        val entriesSmO2: ArrayList<Entry> = ArrayList()
        var listSmo2 = PrefManager.getListSmo2InStage().listSmo2
        if (listSmo2 != null) {
            for (data in listSmo2){
                Functions.showLog("DATA TEST: ${data.name}  ${data.score}")
            }
        }
        if (listSmo2 != null) {
            smO2List = listSmo2
        }
        //you can replace this data object with  your custom object
        for (i in LactateList.indices) {
            val score = LactateList[i]
            entries.add(Entry(i.toFloat(), score.score.toFloat()))
        }
        for (i in smO2List.indices) {
            val score = smO2List[i]
            entriesSmO2.add(Entry(i.toFloat(), score.score.toFloat()))
        }
        val lactateList = LineDataSet(entries, "")
        lactateList.setAxisDependency(YAxis.AxisDependency.RIGHT)
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
        lcLtTestOutdoorResult.data = data
        lcLtTestOutdoorResult.invalidate()
    }

    private fun renderPostLtTestResult(resBaseModel: ResBaseModel?) {
        Functions.showLog("postLtTestResultRes: " + resBaseModel?.let { Functions.toJsonString(it) })
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