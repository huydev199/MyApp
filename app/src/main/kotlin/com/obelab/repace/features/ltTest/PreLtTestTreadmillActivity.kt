package com.obelab.repace.features.ltTest

import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.view.ActionMode
import android.view.Display
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.RepaceApplication
import com.obelab.repace.common.dialog.ConfirmDialog
import com.obelab.repace.common.dialog.ConfirmProgressBar
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.DateTimeUtil
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.model.RepaceMeasure
import com.obelab.repace.service.ble.BleService
import kotlinx.android.synthetic.main.activity_pre_lt_test_treadmill.*
import kotlinx.android.synthetic.main.header_back_bell.*
import java.util.*

class PreLtTestTreadmillActivity : BaseActivity(), TextToSpeech.OnInitListener {

    private var isSaveBaseLine = false
    private var dataDevice = RepaceMeasure.empty
    private var isFinishCoundown = true
    private var isGain = false
    private val TAG = "PreLtTestTreadmillActivity"
    private var timeLeft = Constants.Countdown.DURATION
    private var tts: TextToSpeech? = null
    var timer: CountDownTimer? = null

    companion object {
        fun callingIntent(context: Context) =
            Intent(context, PreLtTestTreadmillActivity::class.java)
    }

    private lateinit var mService: BleService
    private var mBound: Boolean = false
    var confirmProgressBar: ConfirmProgressBar? = null

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as BleService.LocalBinder
            mService = binder.getService()

            mBound = true
            Functions.showLog(TAG, "onServiceConnected: $mService")

            if (PrefManager.getStage() == 1) {
                if (Constants.IS_TEST_MI) {
                    mService.sendMiHeartRate()
                } else {
                    mService.sendStartMeasureCmd()
                    mService.sendGainDataTest()

                    val display: Display = windowManager.defaultDisplay
                    confirmProgressBar = instance?.let { ConfirmProgressBar(it) }!!
                    confirmProgressBar?.showPopup(display)

                    (object : CountDownTimer(
                        Constants.Countdown.TIME_OUT,
                        Constants.Countdown.TIME_INTERVAL
                    ) {
                        override fun onTick(millisUntilFinished: Long) {}
                        override fun onFinish() {
                            confirmProgressBar?.dismiss()
                        }
                    }).start()
                }
            } else {
                startTimer()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    private fun initBleService() {
        Functions.showLog(TAG, "ble status:${RepaceApplication.isConnected}")
        if (!RepaceApplication.isConnected) {
            bindBleService()
        }
    }

    private fun bindBleService() {
        val bindIntent = Intent(this, BleService::class.java)
        bindService(bindIntent, mServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_lt_test_treadmill)
        initBleService()
        tts = TextToSpeech(this, this)
        setUpView()
    }


    fun initTimer() {
        timer = object : CountDownTimer(
            timeLeft,
            Constants.Countdown.TIME_INTERVAL.toLong()
        ) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                isSaveBaseLine = DateTimeUtil.convertLongtoSecondValue(millisUntilFinished) <= 10
                tvTimeStart.text = DateTimeUtil.convertLongtoSecondString(millisUntilFinished)
                Functions.showLog("TimerOnTick")
            }


            override fun onFinish() {
                timeLeft = Constants.Countdown.DURATION
                isSaveBaseLine = false
                isFinishCoundown = true
                var dataSMO2 = PrefManager.getListSMO2()
                if (dataDevice != RepaceMeasure.empty && dataSMO2 != null) {
                    startActivity(LtTestStartTreadmillActivity.callingIntent(this@PreLtTestTreadmillActivity))
                    finish()
                } else {
                    Functions.showLog(TAG, "No data from device")
                    var confirmDialog = ConfirmDialog(this@PreLtTestTreadmillActivity)
                    confirmDialog.isShowTitle = false
                    confirmDialog.content = getString(R.string.no_data_from_device)
                    confirmDialog.isShowLeftButton = false
                    confirmDialog.textButtonRight = getString(R.string.btn_ok)
                    confirmDialog.onClickButtonRight = {
                        startActivity(
                            MainActivity.callingIntentGoToLtTest(
                                this@PreLtTestTreadmillActivity,
                                true
                            )
                        )
                    }
                    confirmDialog.showPopup()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiverActivity, makeGattUpdateIntentFilter())
        checkSpeaker()
//        timer?.let {
//            startTimer()
//        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mGattUpdateReceiverActivity)
        //     stopTimer()
    }


    private fun checkSpeaker() {
        if (PrefManager.getIsSpeakerTurnOn()) {
            imvNarration.setImageResource(R.drawable.ic_bell)
        } else {
            imvNarration.setImageResource(R.drawable.ic_bell_mute)
        }
    }


    fun stopTimer() {
        timer?.cancel()
        isGain = false

    }

    fun startTimer() {
        Handler().postDelayed({
            if (isFinishCoundown) {
                initTimer()
                timer?.start()
                isGain = true
            } else {

                timer?.start()

            }
        }, 500)

    }



    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
    private val mGattUpdateReceiverActivity = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Functions.showLog(TAG, "On receive broadcast.action:${action}")
            if (BleService.ACTION_DATA_AVAILABLE == action) {
                // When device send data -> app
                when (intent.getStringExtra(BleService.TYPE_DATA)) {
                    Constants.TYPE_MEASURE_DATA -> {
                        val data =
                            intent.getSerializableExtra(BleService.EXTRA_DATA) as? RepaceMeasure
                        if (data != null) {
                            //check data read from device
                            dataDevice = data

                            var rso2 = 0.0
                            if (data.rSO2 != null) {
                                rso2 = Functions.convertRSO2(data.rSO2!!)
                            }
                            //Save smo2 in last 10s
                            if (isSaveBaseLine == true && data != null) {
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
                            PrefManager.saveAllListSMO2(listSMO2)
                        }
                    }
                    Constants.TYPE_BATTERY_LEVEL -> {}
                    Constants.TYPE_STATUS_VERSION -> {}
                    Constants.TYPE_GAIN_DATA -> {
                        startTimer()
                        if (PrefManager.getIsSpeakerTurnOn()) {
                            var stage = PrefManager.getStage()
                            var speed = PrefManager.getSpeed()
                            tts!!.speak(
                                getString(R.string.tts_lt_test_pre_treadmill, stage, speed),
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }
                        confirmProgressBar?.hidePopup()
                    }
                    Constants.TYPE_ERROR -> {}
                }
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun setUpView() {
        var stage = PrefManager.getStage()
        var speed = PrefManager.getSpeed()
        if (stage > 1 && PrefManager.getIsSpeakerTurnOn()) {
            var lastStage = stage - 1

            Handler().postDelayed({
                tts!!.speak(
                    getString(R.string.tts_lt_test_pre_treadmill_second, lastStage),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            }, 500)
            Handler().postDelayed({
                tts!!.speak(
                    getString(R.string.tts_lt_test_pre_treadmill_third, stage),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            }, 3000)
            Handler().postDelayed({
                tts!!.speak(
                    getString(R.string.tts_lt_test_pre_treadmill_four, speed),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            }, 5500)
        }
        imvBack.setOnClickListener {
            Functions.resetData()

            finish()
        }

        tvTreadmillSpeed.text = String.format("%.1f", speed).replace(",", ".")

        tvTitle.text = getText(R.string.lt_test)
        imvBtnStop.setOnClickListener {

            var confirmDialog = ConfirmDialog(this)
            confirmDialog.isShowTitle = false
            confirmDialog.content = getString(R.string.content_dialog_lt_test_stop)
            confirmDialog.textButtonRight = getString(R.string.btn_cancel)
            confirmDialog.textButtonLeft = getString(R.string.btn_ok)
            confirmDialog.onClickButtonLeft = {
                Functions.resetData()
                finish()
            }
            confirmDialog.onClickButtonRight = {

            }
            confirmDialog.showPopup()
        }
        tvStage.text = Functions.convertStringState(stage)

        imvNarration.setOnClickListener {
            if (PrefManager.getIsSpeakerTurnOn()) {
                PrefManager.saveIsSpeakerTurnOn(false)
                imvNarration.setImageResource(R.drawable.ic_bell_mute)
                tts!!.stop()
            } else {
                PrefManager.saveIsSpeakerTurnOn(true)
                imvNarration.setImageResource(R.drawable.ic_bell)
            }
        }

        if (PrefManager.getProfile().gender == Constants.GENDER_MALE) {
            imvTreadmill.setImageResource(R.drawable.img_treadmill_man)
        } else {
            imvTreadmill.setImageResource(R.drawable.img_treadmill_woman)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.ENGLISH)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Functions.showLog("Text to speech -> Device not support text to speech")
                PrefManager.saveIsSpeakerTurnOn(false)
            }
        } else {
            PrefManager.saveIsSpeakerTurnOn(false)
            Functions.showLog("Text to speech -> false")
        }
    }


}