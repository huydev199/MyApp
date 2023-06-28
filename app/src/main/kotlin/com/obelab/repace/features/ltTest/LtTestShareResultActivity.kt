package com.obelab.repace.features.ltTest

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.Display
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.FileProvider
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
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.model.ScoreChartModel
import com.obelab.repace.model.SmO2ChartModel
import kotlinx.android.synthetic.main.activity_lt_test_share_result.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class LtTestShareResultActivity : BaseActivity() {

    private var LactateList = ArrayList<ScoreChartModel>()
    private var SmO2List = ArrayList<SmO2ChartModel>()
    companion object {
        fun callingIntent(context: Context) = Intent(context, LtTestShareResultActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lt_test_share_result)
        setUpView()
        initLineChart()
        setDataToLineChart()
    }

    private fun setUpView() {
        val data = intent.extras
        if (data != null){
            val lastStage = data.getString(Constants.LAST_STAGE)
            val maxSpeed = data.getString(Constants.MAX_SPEED)
            val totalDuration = data.getString(Constants.TOTAL_DURATION)
            val totalDistance = data.getString(Constants.TOTAL_DISTANCE)
            val onset = data.getString(Constants.ONSET)
            val threshold = data.getString(Constants.THRESHOLD)

            tvLastStage.text = lastStage
            tvMaxSpeed.text = maxSpeed
            tvTotalDuration.text = totalDuration
            tvTotalDistance.text = totalDistance
            tvOnset.text = onset
            tv4mmol.text = threshold
        }

        //SettingDate
        val date = getCurrentDateTime()
        val dateInString = date.toString("MMM dd, yyyy")
        tvTimeResult.text = dateInString

        //Setting profile
        tvUserName.text = PrefManager.getProfile().nickname

        // this is the card view whose screenshot
        // we will take in this article
        // get the view using fin view bt id
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        val display: Display = windowManager.defaultDisplay
        val stageWidth: Int = display.getWidth()
        var height = 0
        Handler().postDelayed({
            if (linearLayout != null) {
                height = linearLayout.measuredHeight
            }
        }, 50)
        Handler().postDelayed({
            val mBitmap = takeScreenshotOfView(linearLayout,height,stageWidth)
            if(mBitmap != null){
                saveMediaToStorage(mBitmap)
            }
        }, 100)
    }

    fun takeScreenshotOfView(view: View, height: Int, width: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap
    }

    //this method saves the image to gallery
    private fun saveMediaToStorage(bitmap: Bitmap) {
        //Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            this.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    shareImage(imageUri)
                }
                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }

            }
        } else {
            //These for devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            shareImage(image)
            fos = FileOutputStream(image)
        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    //Android < Q
    private fun shareImage(file: File) {
        //Android 10
        val uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
        //val uri = Uri.fromFile(file);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"))
        } catch (e: ActivityNotFoundException) {
            Functions.showLog("No App Available")
        }
        finish()
    }

    //Android >= Q
    private fun shareImage(uri: Uri) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "LT TEST RESULT")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"))
        } catch (e: ActivityNotFoundException) {
            Functions.showLog("No App Available")
        }
        finish()
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
        lcLtTestTreadmillResult.xAxis.spaceMax = 0.3f
        //remove legend
        lcLtTestTreadmillResult.legend.isEnabled = false

        // remove description label
        lcLtTestTreadmillResult.description.isEnabled = false

        // add animation
        lcLtTestTreadmillResult.animateX(0, Easing.EaseInSine)

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

        //LactateList = getLactateList()
        //you can replace this data object with  your custom object
        for (i in LactateList.indices) {
            val score = LactateList[i]
            //entries.add(Entry(i.toFloat(), scogetAxisLabelre.score.toFloat()))
        }
        for (i in SmO2List.indices) {
            val score = SmO2List[i]
            entriesSmO2.add(Entry(i.toFloat(), score.score.toFloat()))
        }

        val lactateList = LineDataSet(entries, "")
        lactateList.color = resources.getColor(R.color.colorLactate)
        lactateList.valueTextColor = resources.getColor(android.R.color.transparent)
        lactateList.setDrawCircles(false)

//        lactateList.highLightColor=resources.getColor(R.color.colorTextPrimary)
        val smO2List = LineDataSet(entriesSmO2, "")
        smO2List.color = resources.getColor(R.color.colorTextHit)
        smO2List.valueTextColor = resources.getColor(android.R.color.transparent)
        smO2List.setDrawCircles(false)

        val finaldataset = ArrayList<LineDataSet>()
        finaldataset.add(lactateList)
        finaldataset.add(smO2List)
        val data = LineData(finaldataset as List<ILineDataSet>?)
//        val data = LineData(lineDataSet)
        lcLtTestTreadmillResult.data = data

        lcLtTestTreadmillResult.invalidate()
    }
}