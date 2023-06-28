package com.obelab.repace.features.ltTest

import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.view.View
import com.obelab.library.repace.Externer
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.dialog.ConfirmDialog
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.DateTimeUtil.convertLongtoMinuteSecondString
import com.obelab.repace.core.util.DateTimeUtil.convertLongtoSecondValue
import com.obelab.repace.model.ListSMO2Model
import com.obelab.repace.model.LtTestPerformanceModel
import com.obelab.repace.model.RepaceMeasure
import com.obelab.repace.model.SmO2ChartModel
import com.obelab.repace.service.ble.BleService
import kotlinx.android.synthetic.main.activity_lt_test_start_treadmill.*
import kotlinx.android.synthetic.main.header_back_bell.*

class LtTestStartTreadmillActivity : BaseActivity() {
    private val TAG = "LtTestStartTreadmillActivity"
    private var timeRun: Int = 1
    private var isFinishCountdown = true
    private var isSaveBaseLine = false
    private var rawData: ByteArray? = null
    private var measureData: RepaceMeasure = RepaceMeasure.empty
    private var protocol = PrefManager.getProtocol()
    private var defaultSpeed =
        String.format("%.1f", PrefManager.getSpeed().toDouble()).replace(",", ".").toDouble()
    private var arrayListSmo2 = PrefManager.getListSMO2()
    private var listSmo2InStage = ListSMO2Model.empty
    private var stage = PrefManager.getStage()
    private var lactate = PrefManager.getLactate()
    private var timeLeft = Constants.Countdown.DURATION_START
    var timer: CountDownTimer? = null

    companion object {
        fun callingIntent(context: Context) =
            Intent(context, LtTestStartTreadmillActivity::class.java)
    }

    private lateinit var mService: BleService
    private var mBound: Boolean = false

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as BleService.LocalBinder
            mService = binder.getService()
            mBound = true
            mService.sendStopMeasureCmd()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lt_test_start_treadmill)
        setUpView()
        tvTimeRemaining.text = convertLongtoMinuteSecondString(timeLeft)

        Handler().postDelayed({
            try {
                startTimer()
                circularProgressBar.apply {
                    progressMax = Constants.Countdown.DURATION_START.toFloat()
                }
            } catch (e: Exception){

            }
        }, 1000)

    }

    private fun setUpView() {
        imvBack.visibility = View.INVISIBLE
        imvBack.setOnClickListener {
            finish()
        }

        if (stage == 1) {
            try {
                //Base line
                var baseLineSMO2 =
                    arrayListSmo2.listSMO2!!.sum().toDouble() / arrayListSmo2.listSMO2!!.size
                PrefManager.saveBaseLineSMO2(baseLineSMO2.toFloat())
            } catch (e: Exception) {
                Functions.showLog("getBaseLineSMO2 -> $e")
            }
        }

        //Reset list SMO2
        PrefManager.saveListSMO2(ListSMO2Model.empty)

        tvTitle.text = getText(R.string.lt_test)

        tvSpeed.text = String.format("%.1f", defaultSpeed).replace(",", ".")

        tvStage.text = Functions.convertStringState(stage)

        imvNarration.setOnClickListener {
            if (PrefManager.getIsSpeakerTurnOn()) {
                PrefManager.saveIsSpeakerTurnOn(false)
                imvNarration.setImageResource(R.drawable.ic_bell_mute)
            } else {
                PrefManager.saveIsSpeakerTurnOn(true)
                imvNarration.setImageResource(R.drawable.ic_bell)
            }
        }


        imvBtnStop.setOnClickListener {
            //isRun = false
            var confirmDialog = ConfirmDialog(this)
            confirmDialog.isShowTitle = false
            confirmDialog.content = getString(R.string.content_dialog_lt_test_stop)
            confirmDialog.textButtonRight = getString(R.string.btn_cancel)
            confirmDialog.textButtonLeft = getString(R.string.btn_ok)
            confirmDialog.onClickButtonLeft = {
                val intent = Intent(this@LtTestStartTreadmillActivity, BleService::class.java)
                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
                finish()
            }
            confirmDialog.onClickButtonRight = {
            }
            confirmDialog.showPopup()
            confirmDialog.setCanceledOnTouchOutside(false)
        }

        //Save performance each stage
        var dataLtTestPerformance = PrefManager.getListLtTestPerformance()
        var data = LtTestPerformanceModel(stage, defaultSpeed, defaultSpeed * 5 / 60)
        if (dataLtTestPerformance.listLtTestPerformance != null) {
            dataLtTestPerformance.listLtTestPerformance!!.add(data)
        } else {
            dataLtTestPerformance.listLtTestPerformance = arrayListOf(data)
        }
        PrefManager.saveListLtTestPerformance(dataLtTestPerformance)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiverActivity, makeGattUpdateIntentFilter())
        checkSpeaker()
     //   startTimer()

    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mGattUpdateReceiverActivity)

    }
    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()

    }

    fun startTimer() {

        if (isFinishCountdown) {
            initTimer()
            timer?.start()
        }
        else{
            timer?.start()
        }
    }

    private fun checkSpeaker() {
        if (PrefManager.getIsSpeakerTurnOn()) {
            imvNarration.setImageResource(R.drawable.ic_bell)
        } else {
            imvNarration.setImageResource(R.drawable.ic_bell_mute)
        }
    }



    val mGattUpdateReceiverActivity = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BleService.ACTION_GATT_CONNECTED == action) {

            } else if (BleService.ACTION_DATA_AVAILABLE == action) {
                val type = intent.getStringExtra(BleService.TYPE_DATA)
                when (type) {
                    Constants.TYPE_MEASURE_DATA -> {
                        val data =
                            intent.getSerializableExtra(BleService.EXTRA_DATA) as? RepaceMeasure
                        if (data != null) {
                            var rso2 = 0.0
                            if (data.rSO2 != null) {
                                rso2 = Functions.convertRSO2(data.rSO2!!)
                            }
                            //Save smo2 in last 10s
                            if (isSaveBaseLine == true) {
                                var dataSMO2 = PrefManager.getListSMO2()
                                if (dataSMO2.listSMO2 != null) {
                                    dataSMO2.listSMO2!!.add(rso2)
                                } else {
                                    dataSMO2.listSMO2 = arrayListOf(rso2)
                                }
                                PrefManager.saveListSMO2(dataSMO2)
                            }
                            //Save all smo2
                            var listSMO2 = PrefManager.getAllListSMO2()
                            if (listSMO2.listSMO2 != null) {
                                listSMO2.listSMO2!!.add(rso2)
                            } else {
                                listSMO2.listSMO2 = arrayListOf(rso2)
                            }
                            rawData = data.rawData
                            measureData = data
                            PrefManager.saveAllListSMO2(listSMO2)

                            //Save Smo2 in Stage
                            if (listSmo2InStage != ListSMO2Model.empty) {
                                listSmo2InStage.listSMO2?.add(rso2)
                            } else {
                                listSmo2InStage.listSMO2 = arrayListOf(rso2)
                            }
                        }
                    }
                    Constants.TYPE_BATTERY_LEVEL -> {}
                    Constants.TYPE_STATUS_VERSION -> {}
                    Constants.TYPE_ERROR -> {}
                }
            }
        }
    }

    fun initTimer(){
        timer = object : CountDownTimer(
            timeLeft,
            Constants.Countdown.TIME_INTERVAL.toLong()
        ) {
            override fun onTick(millisUntilFinished: Long) {

                timeLeft = millisUntilFinished
                isSaveBaseLine = convertLongtoSecondValue(millisUntilFinished) <= 10
                circularProgressBar.apply {
                    progress =
                        ((progressMax - timeLeft+900) * 0.8).toFloat()
                }

                tvTimeRemaining.text = convertLongtoMinuteSecondString(millisUntilFinished)

                if (measureData.rSO2 != null) {
                    tvSmO2.text = Functions.convertRSO2(measureData.rSO2!!).toString()
                    Functions.showLog(
                        TAG,
                        "Stage: ${stage}; Smo2: (${Functions.convertRSO2(measureData.rSO2!!)}, D); Heartrate: (${protocol.heartRate}, L); Start Speed: (${protocol.startSpeed})"
                    )
                }
                // Total distance = time * (default speed/5min)
                tvTotalDistance.text = String.format("%.1f", timeRun * (defaultSpeed / 3600))

                timeRun += 1
            }


            override fun onFinish() {
                timeLeft = Constants.Countdown.DURATION
                isFinishCountdown = true
                isSaveBaseLine = false
                //calculate currSmo2
                var arrayListSmo2 = PrefManager.getListSMO2()
                var currSmo2 = 0.0
                arrayListSmo2.listSMO2?.sum()?.toDouble()?.div(arrayListSmo2.listSMO2!!.size)?.let{
                    currSmo2 = it
                }

                PrefManager.saveListSMO2(ListSMO2Model.empty)

                // Get analysis from lb
                val analysis = Externer.getAnalysis(
                    stage,
                    protocol.protocol,
                    lactate.toDouble(),
                    PrefManager.getBaseLineSMO2().toDouble(),
                    currSmo2,
                    arrayListOf(rawData)
                )
                Functions.showLog(
                    TAG,
                    "Get Analysis || Speed: (${analysis.speed}, L); Lactate: (${analysis.lactate}, L); isNext: (${analysis.isNext}, L); "
                )

                //Check listAnalysis is not empty end save analysis
                val listAnalysisData = PrefManager.getAnalysis()
                if (listAnalysisData.listAnalyst != null) {
                    listAnalysisData.listAnalyst!!.add(analysis)
                } else {
                    listAnalysisData.listAnalyst = arrayListOf(analysis)
                }
                PrefManager.saveAnalysis(listAnalysisData)

                //Check list smo2 in stage using for chart
                if (listSmo2InStage.listSMO2 != null) {
                    var smo2InStage = listSmo2InStage.listSMO2?.average()
                    //convert smo2 stage, ex: 1->"A"
                    var scoreChart =
                        smo2InStage?.let { SmO2ChartModel(listStageConvert[stage - 1], it) }
                    val dataSavePrefListSmo2 = PrefManager.getListSmo2InStage()
                    if (dataSavePrefListSmo2.listSmo2 != null) {
                        scoreChart?.let { dataSavePrefListSmo2.listSmo2!!.add(it) }
                    } else {
                        dataSavePrefListSmo2.listSmo2 = scoreChart?.let { arrayListOf(it) }
                    }
                    PrefManager.saveListSmo2InStage(dataSavePrefListSmo2)
                    listSmo2InStage.listSMO2 = null
                }

                if (analysis.isNext == true) {
                    navigateScreen(0)
                    PrefManager.saveStage(stage + 1)
                    PrefManager.saveSpeed(analysis.speed.toFloat())
                    PrefManager.saveLactate(analysis.lactate.toFloat())
                } else {
                    navigateScreen(1)
                }
            }
        }
    }

    private fun navigateScreen(data: Int) {
        if (data == 0) {
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
            startActivity(PreLtTestTreadmillActivity.callingIntent(this@LtTestStartTreadmillActivity))
            finish()
        } else {
            val intent = Intent(this, LtTestCompleteActivity::class.java)
            intent.putExtra(Constants.type, Constants.LT_TEST_TREADMILL)
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
            startActivity(intent)
            finish()
        }
    }
}