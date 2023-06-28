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
import com.google.gson.GsonBuilder
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
import com.obelab.repace.common.adapter.LtTestHistoryDetailAdapter
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.FileHelper
import com.obelab.repace.model.*
import com.obelab.repace.viewModel.LtTestHistoryDetailViewModel
import kotlinx.android.synthetic.main.activity_lt_test_history_detail.*
import kotlinx.android.synthetic.main.activity_lt_test_history_detail.btnExercisePrescription
import kotlinx.android.synthetic.main.activity_lt_test_history_detail.rcvListLtTestPerformance
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.*
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.id_4mmol
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.id_avg_smo2
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.id_max_smo2
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.id_max_speed
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.id_min_smo2
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.id_onset
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.id_time_result
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.id_total_distance
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.id_total_duration
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.id_total_stage
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.imvHelpCircle
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.lcLtTestOutdoorResult
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.mapView
import kotlinx.android.synthetic.main.activity_lt_test_outdoor_result.tvLactateThresholdInfoMore

import kotlinx.android.synthetic.main.header_back_share.*
import kotlinx.android.synthetic.main.header_share.imvShare
import kotlinx.android.synthetic.main.header_share.tvTitle

class LtTestHistoryDetailActivity : BaseActivity() {
    private val TAG = "LtTestOutdoorResultActivity"
    private var isShowMore = false
    private var allDataList = ArrayList<ListHeartRateElement>()
    lateinit var ltTestHistoryDetail: ResLtTestHistoryDetailModel

    private val viewModelLtTestHistoryDetail: LtTestHistoryDetailViewModel by viewModels()

    companion object {
        fun callingIntent(context: Context) =
            Intent(context, LtTestHistoryDetailActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        Functions.showLog("id LtTestHistory "+id)
//view model lt test history
        with(viewModelLtTestHistoryDetail) {
            observe(resLtTestHistoryDetailModel, ::renderLtTestHistoryResult)
            failure(failure, ::handleFailure)
        }



        setContentView(R.layout.activity_lt_test_history_detail)
        setUpView()
        initLineChart()
    }

    private fun renderLtTestHistoryResult(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = GsonBuilder().create()
            Functions.showLog("dataStr: " + resBaseModel.data?.let { Functions.toJsonString(it) })
            val dataList = gson.fromJson(resBaseModel.data?.let { Functions.toJsonString(it) }, ResLtTestHistoryDetailModel::class.java)
            if (dataList != null) {
                ltTestHistoryDetail = dataList

                id_time_result.text = Functions.formatDateToYear(dataList?.createdAt)

                id_total_stage.text = dataList?.stageCnt.toString()
                id_max_speed.text = dataList?.speedMax.toString()
                id_total_duration.text = dataList?.totalDuration.toString()
                id_total_distance.text = dataList?.totalDistance.toString()
                id_onset.text =  Functions.getDouble1Decimal(dataList?.onset).toString()
                id_4mmol.text = Functions.getDouble1Decimal(dataList?.mol).toString()
                id_min_smo2.text = dataList?.smo2Min.toString()
                id_max_smo2.text = dataList?.smo2Max.toString()
                id_avg_smo2.text = dataList?.smo2Avg.toString()

                id_minHeartrate.text = dataList?.heartRateMin.toString()
                id_maxheartrate.text = dataList?.heartRateMax.toString()
                id_avgheartrate.text = dataList?.heartRateAvg.toString()
//                id_lactate_4mmol.text= dataList.testType.code
                if (dataList?.totalDuration > 0 && dataList?.totalDistance > 0) {
                    id_pace.text = Functions.convertTimeSecondary(((dataList?.totalDuration) / (dataList?.totalDistance)))
                }
//                id_heartrate_at_the_first_onset.text= dataList

                val xAxis: XAxis = lcLtTestOutdoorResult.xAxis
                xAxis.valueFormatter = MyAxisFormatter()

                dataList.stage?.let { settingView(it) }
                setDataToLineChart(dataList)
                renderShare(dataList)
                Functions.showLog("dataList.testTypeID " + dataList?.testTypeId)
                if (dataList?.testTypeId == "outdoor_test") {
                    mapView.visibility = View.VISIBLE
                    Functions.showLog("dataList.location " + dataList?.listLocation)
                    if (dataList?.listLocation.size > 0) {
                        setUpMap()
                    }
//
                } else {
                    mapView.visibility = View.GONE
                }
            }
//            id_total_stage.text=resBaseModel.data?.stageCnt
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun renderShare(dataList: ResLtTestHistoryDetailModel) {


        imvShare.setOnClickListener {
            val intent = Intent(this, LtTestHistoryDetailShareActivity::class.java)
            val data = Bundle()
            data.putString(Constants.LAST_STAGE, dataList?.stageCnt.toString())
            data.putString(Constants.MAX_SPEED, dataList?.speedMax.toString())
            data.putString(Constants.TOTAL_DISTANCE, dataList?.totalDistance.toString())
            data.putString(Constants.TOTAL_DURATION, (dataList?.stageCnt * 5).toString())
            data.putString(Constants.ONSET, dataList?.onset.toString())
            data.putString(Constants.THRESHOLD, dataList?.mol.toString())
            data.putString(Constants.CREATE_AT, dataList?.createdAt)
            data.putString(Constants.LISTSMO2, Functions.toJsonString(dataList?.listSmo2))
            data.putString(Constants.LISTHEART_RATE, Functions.toJsonString(dataList?.listHeartRate))
            intent.putExtras(data)
            startActivity(intent)
        }
        btnExercisePrescription.setOnClickListener {
            startActivity(ExercisePrescriptionActivity.callingIntent(this))
            finish()
        }
    }

    private fun setUpMap() {

        Functions.showLog("ltTestHistoryDetail " + ltTestHistoryDetail?.listLocation)
        // Set up map box
        mapView?.getMapboxMap()?.loadStyleUri(Style.DARK)
        val startLocation = ltTestHistoryDetail?.listLocation[0]
        val stopLocation = ltTestHistoryDetail?.listLocation[ltTestHistoryDetail?.listLocation.size - 1]
        val startPoint = startLocation?.longitude?.let { startLocation?.latitude?.let { it1 -> Point.fromLngLat(it, it1) } }
        val stopPoint = stopLocation?.longitude?.let { stopLocation?.latitude?.let { it1 -> Point.fromLngLat(it, it1) } }

        val centerPosition = CameraOptions.Builder().zoom(15.0).center(startPoint).build()
        // set camera center position
        mapView.getMapboxMap().setCamera(centerPosition)
        // Set up point list
        val pointList = ArrayList<Point>()
        ltTestHistoryDetail?.listLocation.map {
            it?.longitude?.let { it1 -> it?.latitude?.let { it2 -> Point.fromLngLat(it1, it2) } }?.let { it2 -> pointList.add(it2) }
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
        val pointStartAnnotationOptions = startPoint?.let {
            PointAnnotationOptions().withPoint(it)
                .withIconImage(FileHelper.getResizedBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_map_start), Constants.ICON_MAP_MARKER_SIZE, Constants.ICON_MAP_MARKER_SIZE))
        }
        val pointStopAnnotationOptions = stopPoint?.let {
            PointAnnotationOptions().withPoint(it)
                .withIconImage(FileHelper.getResizedBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_map_stop), Constants.ICON_MAP_MARKER_SIZE, Constants.ICON_MAP_MARKER_SIZE))
        }
        if (pointStartAnnotationOptions != null) {
            pointAnnotationManager?.create(pointStartAnnotationOptions)
        }
        if (pointStopAnnotationOptions != null) {
            pointAnnotationManager?.create(pointStopAnnotationOptions)
        }
    }

    private fun setUpView() {
        // get data ltTestHistory
        var id: Int? = intent.getIntExtra("id", 0)
        showLoading()
        viewModelLtTestHistoryDetail.getLtTestHistoryDetail(id)
        Functions.showLog("id LtTestHistory " + id)
        // Set text
        tvTitle.text = getText(R.string.title_LT_Test)

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
            finish()
        }
    }

    private fun settingView(listData: List<Stage>) {
        var layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rcvListLtTestPerformance.layoutManager = layoutManager
        var faqAdapter = LtTestHistoryDetailAdapter(listData)
        rcvListLtTestPerformance.adapter = faqAdapter
    }

    private fun initLineChart() {
        lcLtTestOutdoorResult.axisLeft.setDrawGridLines(false)
        lcLtTestOutdoorResult.setNoDataText("")

        val xAxis: XAxis = lcLtTestOutdoorResult.xAxis
        val yAxisLeft: YAxis = lcLtTestOutdoorResult.axisLeft
        val yAxisRight: YAxis = lcLtTestOutdoorResult.axisRight
//        val yAxisRight: YAxis = mChart.getAxisLeft ();
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
//        xAxis.valueFormatter = MyAxisFormatter()

        yAxisRight.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
//        yAxisRight.position=
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.textColor = resources.getColor(R.color.colorTextPrimary)
        xAxis.axisLineColor = resources.getColor(R.color.colorTextPrimary)
        yAxisLeft.textColor = resources.getColor(R.color.colorTextPrimary)
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 100f
        yAxisRight.axisMinimum = 1f
        yAxisRight.axisMaximum = 10f
        yAxisRight.textColor = resources.getColor(R.color.colorTextPrimary)
    }

    inner class MyAxisFormatter : IndexAxisValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            return if (index < allDataList.size) {
                allDataList[index].name
            } else {
                ""
            }
        }
    }

    private fun setDataToLineChart(dataList: ResLtTestHistoryDetailModel) {
        val entries: ArrayList<Entry> = ArrayList()
        val entriesSmO2: ArrayList<Entry> = ArrayList()
        var listSmo2 = dataList?.listSmo2
//        var listHeartRate = dataList?.listHeartRate
        var listLactate = dataList?.listLactate



        if (listSmo2 != null) {
            allDataList = listSmo2 as ArrayList<ListHeartRateElement>
        }
        //you can replace this data object with  your custom object
        for (i in listLactate.indices) {
            val score = listLactate[i]
            entries.add(Entry(i.toFloat(), score?.score.toFloat()))
        }
        for (i in listSmo2.indices) {
            val score = listSmo2[i]
            entriesSmO2.add(Entry(i.toFloat(), score?.score.toFloat()))
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


    private fun handleFailure(failure: Failure?) {
        Functions.showLog("loginError: " + failure.toString())
        hideLoading()
        showToast(getString(R.string.failure_network_connection))
    }
}