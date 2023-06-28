package com.obelab.repace.features.ltTest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.gson.GsonBuilder
import com.obelab.repace.R
import com.obelab.repace.common.adapter.LtTestHistoryAdapter
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.model.*
import com.obelab.repace.viewModel.LtTestHistoryViewModel
import kotlinx.android.synthetic.main.header_back.*

class LtTestHistoryActivity : BaseActivity() {


    private lateinit var lineChart: LineChart
    private var lactateList = ArrayList<ResAllLtTestHistoryModel>()
    private var smO2List = ArrayList<ResAllLtTestHistoryModel>()
    private var mListHistory =ArrayList<ResAllLtTestHistoryModel>()

    private  val viewModel: LtTestHistoryViewModel by viewModels()

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    companion object {
        fun callingIntent(context: Context) = Intent(context, LtTestHistoryActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lt_test_history)
        setUpView()


        lineChart = findViewById(R.id.lineChart)
        initLineChart()


////get api
        with(viewModel){
            observe(resLtTestHistoryModel, ::renderAllHistory)
            failure(failure, ::handleFailure)
        }
        viewModel.getAllLtTestHistory()

    }




    private fun renderAllHistory(resBaseModel: ResBaseModel?) {
        hideLoading()
        Functions.showLog("resBaseModel: "+ resBaseModel?.let { Functions.toJsonString(it) })
        if (resBaseModel?.success == true) {
            val gson = GsonBuilder().create()
            Functions.showLog("dataStr: "+ resBaseModel.data?.let { Functions.toJsonString(it) })
            val dataList = gson.fromJson(resBaseModel.data?.let { Functions.toJsonString(it) },Array<ResAllLtTestHistoryModel>::class.java).toList()
            dataList.map {
                lactateList.add(it)
                smO2List.add(it)
            }


            settingView(dataList)
            setDataToLineChart()
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }
    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Show notices error: " + failure.toString())
        hideLoading()
    }

    private fun setUpView() {
//        val type:String = intent.getStringExtra("type")
        tvTitle.text = getText(R.string.title_lt_test_history)
        imvBack.setOnClickListener {
            finish()
        }
    }




    private fun initLineChart() {

//        hide grid lines
        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.setNoDataText("")
        val xAxis: XAxis = lineChart.xAxis
        val yAxisLeft: YAxis = lineChart.axisLeft
        val yAxisRight:YAxis= lineChart.axisRight
        yAxisLeft.axisMinimum = 0f
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        lineChart.axisRight.isEnabled = true
        lineChart.xAxis.spaceMin=0.3f
        lineChart.xAxis.spaceMax=0.3f

        //remove legend
        lineChart.legend.isEnabled = false


       // remove description label
        lineChart.description.isEnabled = false


       // add animation
        lineChart.animateX(1000, Easing.EaseInSine)

        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f

//        xAxis.labelRotationAngle = +90f
        xAxis.textColor=resources.getColor(R.color.colorTextPrimary)
        xAxis.axisLineColor=resources.getColor(R.color.colorTextPrimary)
        yAxisLeft.textColor=resources.getColor(R.color.colorTextPrimary)
        yAxisRight.textColor=resources.getColor(R.color.colorTextPrimary)
        yAxisRight.valueFormatter= MyAxisFormatPercent()
    }
    inner class MyAxisFormatter : IndexAxisValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            return if (index < lactateList.size) {
//                val dateFormatMoth=Functions.formatDateToMoth(lactateList[index].createdAt)
//                Functions.showLog("Show noti: " + dateFormatMoth)
                Functions.formatDateToMoth( lactateList[index].date.toString())

            } else {
                ""
            }
        }
    }
    inner class MyAxisFormatPercent :IndexAxisValueFormatter(){
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {

            val index = value.toInt()
            return index.toString()+"0%"


        }
    }

    private fun setDataToLineChart() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()
        val entriesSmO2: ArrayList<Entry> = ArrayList()
//        lactateList = getLactateList()

//        SmO2List = getSmO2List()
        //you can replace this data object with  your custom object
        for (i in lactateList.indices) {
            val score = lactateList[i]
            entries.add(Entry(i.toFloat(), score.lactateOnset.toFloat()))
        }
        for (i in smO2List.indices) {
            val score = smO2List[i]
//            entriesSmO2.add(Entry(i.toFloat(), score.smo2Avg.toFloat()))
            entriesSmO2.add(Entry(i.toFloat(), score.smO2Avg.toFloat()/10))
        }
        if(entriesSmO2.size==0){
            entriesSmO2.add((Entry(0f,0f)))
        }
//        if(entries.size==0){
//            entries.add((Entry(0f,0f)))
//        }

        val lactateList = LineDataSet(entries, "")
        lactateList.color=resources.getColor(R.color.colorLactate)
        lactateList.valueTextColor=resources.getColor(android.R.color.transparent)
        lactateList.setCircleColor(android.R.color.transparent)
//
        lactateList.enableDashedLine(30F, 15F, 0F)

//        lactateList.highLightColor=resources.getColor(R.color.colorTextPrimary)
        val smO2List = LineDataSet(entriesSmO2, "")
        smO2List.setAxisDependency(YAxis.AxisDependency.RIGHT)
        smO2List.color=resources.getColor(R.color.colorTextHit)
        smO2List.valueTextColor=resources.getColor(android.R.color.transparent)
        smO2List.setCircleColor(android.R.color.transparent)

        smO2List.enableDashedLine(30f,15f,0f)

    val  finaldataset=ArrayList<LineDataSet>()
        finaldataset.add(lactateList)
        finaldataset.add(smO2List)
        val data = LineData(finaldataset as List<ILineDataSet>?)
//        val data = LineData(lineDataSet)
        lineChart.data = data

        lineChart.invalidate()
    }

    private fun settingView(listHistory: List<ResAllLtTestHistoryModel>) {
        for(item in listHistory){
            mListHistory.add(item)
        }

        mRecyclerView = findViewById(R.id.rcvListHistory)
        layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mRecyclerView.layoutManager = layoutManager
        var ltTestHistoryAdapter = LtTestHistoryAdapter(mListHistory)
        mRecyclerView.adapter = ltTestHistoryAdapter

//        mRecyclerView.setNestedScrollingEnabled(false)
        ltTestHistoryAdapter.onClickMoreLtTestDetail={
//            Functions.showLog("it" +it)


            val intent = Intent(this, LtTestHistoryDetailActivity::class.java)
            Functions.showLog("chuyen Lt Test history "+it.id)
            intent.putExtra("id", it.id)
//            intent.putExtra("id", it.id)
//            intent.putExtra("avatar", it.avatar)
            startActivity(intent)
        }
    }



}