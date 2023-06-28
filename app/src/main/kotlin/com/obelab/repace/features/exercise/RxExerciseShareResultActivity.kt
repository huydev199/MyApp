package com.obelab.repace.features.exercise

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
import com.obelab.repace.core.util.ExerciseHelper
import com.obelab.repace.model.ScoreChartModel
import com.obelab.repace.model.SmO2ChartModel
import kotlinx.android.synthetic.main.activity_rx_exercise_share_result.*
import kotlinx.android.synthetic.main.header_back_share.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class RxExerciseShareResultActivity : BaseActivity() {

    private val TAG = "RxExerciseShareResultActivity"
    private var LactateList = ArrayList<ScoreChartModel>()
    private var SmO2List = ArrayList<SmO2ChartModel>()
    private var isShowSmo2 = true
    private var isShowHeartrate = true
    private var typeId = PrefManager.getTypeId()
    private var activityId = PrefManager.getActivityId()

    companion object {
        fun callingIntent(context: Context) =
            Intent(context, RxExerciseShareResultActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rx_exercise_share_result)
        initLineChart()
        //setDataToLineChart(isShowSmo2, isShowHeartrate)
        setUpView()
    }

    private fun setUpView() {
        val typeExercise: String? = intent.getStringExtra(Constants.typeExercise)
        val dataTodayExercise = ExerciseHelper.getTodayExercise()

        tvTitle.text = getText(R.string.rx_exercise_result)
        tvSession.text = Functions.convertStringState(dataTodayExercise.todaySession.session)
        tvFreeDuration.text = convertTime(PrefManager.getFreeDuration())

        //SettingDate
        val date = getCurrentDateTime()
        val dateInString = date.toString("MMM dd, yyyy")
        var timeStart = PrefManager.getTimeStart()

        tvUserName.text = PrefManager.getProfile().nickname
        tvDateTime.text = dateInString
        tvStartTime.text =
            "${timeStart.timeCalendar?.time?.hours?.let { Functions.convertStringState(it) }}:${
                timeStart.timeCalendar?.time?.minutes?.let {
                    Functions.convertStringState(it)
                }
            }"

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
            }
            tvMinSmo2.text = minSmo2.toString()
            tvMaxSmo2.text = maxSmo2.toString()
            tvAvgSmo2.text = String.format("%.1f", avgSmo2).replace(",", ".")


            // Calcaculate use for char. Example: List size Smo2 = 1000
            // rangeStepSmo2 = size/8 = 1000/8 = 125
            //In i = 0, j = 1..125 -> sum = array[i*j+j] = array[0*1 + 1]
            //In i = 1, j = 1..125 -> sum = array[i*j+j] = array[1*125 + 1]
            if (arrayListSmo2.listSMO2 != null) {
                val dataSavePrefListSmo2 = PrefManager.getListSmo2InStage()
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
                    //PrefManager.saveListSmo2InStage(dataSavePrefListSmo2)
                }
                Functions.showLog("data save: " + dataSavePrefListSmo2.listSmo2!!)
                SmO2List = dataSavePrefListSmo2.listSmo2!!
                setDataToLineChart(true, true)
            }
        }

        // Check type id
        if (typeId == Constants.rx_exercise) {
            //Case type is RX EXERCISE
            llPrescription.visibility = View.VISIBLE
            llFreeExercise.visibility = View.GONE
            tvDuration.text = dataTodayExercise.todaySession.time.toString()
            tvSpeed.text =
                String.format("%.1f", dataTodayExercise.todaySession.speed).replace(",", ".")
            tvHeartRate.text = dataTodayExercise.todaySession.heartRate.toString()
            if (typeExercise == Constants.LOW_INTENSITY_EXERCISE) {
                tvTitleResult.text = getText(R.string.title_treadmill_running_low_intensity)
            } else {
                tvTitleResult.text = getText(R.string.title_treadmill_running_high_intensity)
            }
            var timeEnd = PrefManager.getTimeStart()
            timeEnd.timeCalendar?.add(Calendar.MINUTE, dataTodayExercise.todaySession.time)
            tvEndTime.text =
                "${timeEnd.timeCalendar?.time?.hours?.let { Functions.convertStringState(it) }}:${
                    timeEnd.timeCalendar?.time?.minutes?.let {
                        Functions.convertStringState(
                            it
                        )
                    }
                }"
        } else {
            // Case type is FREE EXERCISE
            llPrescription.visibility = View.GONE
            llFreeExercise.visibility = View.VISIBLE
            tvEndTime.text = PrefManager.getFreeDuration().toString()
            if (activityId == Constants.ex_treadmill) {
                llFreeOutdoor.visibility = View.GONE
            } else if (activityId == Constants.ex_outdoor) {
                llFreeOutdoor.visibility = View.VISIBLE
            }
            var timeEnd = PrefManager.getTimeStart()
            timeEnd.timeCalendar?.add(Calendar.SECOND, PrefManager.getFreeDuration())
            tvEndTime.text =
                "${timeEnd.timeCalendar?.time?.hours?.let { Functions.convertStringState(it) }}:${
                    timeEnd.timeCalendar?.time?.minutes?.let {
                        Functions.convertStringState(
                            it
                        )
                    }
                }"
        }

        if (activityId == Constants.ex_treadmill) {
            val distance = PrefManager.getDistance()
            if (distance != null) {
                tvDistance.text = distance
            } else {
                tvDistance.text = "-"
            }
        } else {
            val distance = PrefManager.getTotalDistance()
            val speed = PrefManager.getListExerciseSpeed()
            if (distance != null) {
                tvDistance.text = String.format("%.1f", distance).replace(",", ".")
                tvMaxSpeed.text =
                    String.format("%.1f", speed.exerciseArraySpeedModel?.max()).replace(",", ".")
                tvAvgSpeed.text = String.format("%.1f", speed.exerciseArraySpeedModel?.average())
                    .replace(",", ".")
            } else {
                tvDistance.text = "-"
            }
            var pace = convertTimeSecondary((PrefManager.getFreeDuration()/distance).toInt())
            tvPace.text = pace
        }
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
            val mBitmap = takeScreenshotOfView(linearLayout, height, stageWidth)
            if (mBitmap != null) {
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
        val uri = FileProvider.getUriForFile(
            this,
            this.getApplicationContext().getPackageName() + ".provider",
            file
        );
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
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"))
        } catch (e: ActivityNotFoundException) {
            Functions.showLog("No App Available")
        }
        finish()
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
        lcRxExerciseTreadmillResult.animateX(0, Easing.EaseInSine)

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

    private fun setDataToLineChart(isShowSmo2: Boolean, isShowHeartRate: Boolean) {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()
        val entriesSmO2: ArrayList<Entry> = ArrayList()
        Functions.showLog("typeId: $typeId")
        if(typeId == Constants.rx_exercise){
            var listSmo2 = PrefManager.getListSmo2InStage().listSmo2
            Functions.showLog("DATA: $listSmo2")
            if (isShowSmo2 == true) {
                if (listSmo2 != null) {
                    SmO2List = arrayListOf()
                    SmO2List = listSmo2
                }
            } else {
                SmO2List = arrayListOf()
            }

            if (isShowHeartRate == true) {
                if (listSmo2 != null) {
                    LactateList = arrayListOf()
                    LactateList = getLactateList()
                }
            } else {
                LactateList = arrayListOf()
            }
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
        lcRxExerciseTreadmillResult.data = data

        lcRxExerciseTreadmillResult.invalidate()
    }

    // simulate api call
    // we are initialising it directly
    private fun getLactateList(): ArrayList<ScoreChartModel> {
        LactateList.add(ScoreChartModel("A", 69.0))
        LactateList.add(ScoreChartModel("B", 50.0))
        LactateList.add(ScoreChartModel("C", 47.0))
        LactateList.add(ScoreChartModel("D", 55.0))
        LactateList.add(ScoreChartModel("E", 53.0))
        LactateList.add(ScoreChartModel("F", 70.0))
        LactateList.add(ScoreChartModel("G", 63.0))
        LactateList.add(ScoreChartModel("H", 52.0))
        return LactateList
    }

    private fun convertTime(time: Int): String {
        var hours = (time / 3600)
        var minute = ((time % 3600) / 60).toInt()
        var second = (time % 60).toInt()
        var result = "00 : 00"
        if (hours > 9) {
            if (minute > 9) {
                if (second > 9) {
                    result = "$hours : $minute : $second"
                } else {
                    result = "$hours : $minute : 0$second"
                }
            } else {
                if (second > 9) {
                    result = "$hours : 0$minute : $second"
                } else {
                    result = "$hours : 0$minute : 0$second"
                }
            }
        } else {
            if (minute > 9) {
                if (second > 9) {
                    result = "0$hours : $minute : $second"
                } else {
                    result = "0$hours : $minute : 0$second"
                }
            } else {
                if (second > 9) {
                    result = "0$hours : 0$minute : $second"
                } else {
                    result = "0$hours : 0$minute : 0$second"
                }
            }
        }

        return result
    }

    private fun convertTimeSecondary(time: Int): String {
        var minute = (time / 60)
        var second = (time % 60)
        var result = "-"
        if (minute > 9) {
            if (second > 9) {
                result = "$minute’ $second’’"
            } else {
                result = "$minute’ 0$second’’"
            }
        } else {
            if (second > 9) {
                result = "0$minute’ $second’’"
            } else {
                result = "0$minute’ 0$second’’"
            }
        }
        return result
    }
}