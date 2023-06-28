package com.obelab.repace.features.exercise

import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.speech.tts.TextToSpeech
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
import com.obelab.repace.core.util.ExerciseHelper
import com.obelab.repace.service.ble.BleService
import kotlinx.android.synthetic.main.activity_pre_free_exercise_treadmill.imvBtnStop
import kotlinx.android.synthetic.main.activity_pre_free_exercise_treadmill.imvTreadmill
import kotlinx.android.synthetic.main.activity_pre_free_exercise_treadmill.tvTimeStart
import kotlinx.android.synthetic.main.header_back_bell.*
import java.util.*

class PreFreeExerciseTreadmillAcitivity : BaseActivity() , TextToSpeech.OnInitListener  {
    private val TAG = "PreFreeExerciseTreadmillAcitivity"
    private var isFinishCountDown = true
    private var timeLeft = Constants.Countdown.DURATION
    var timer: CountDownTimer? = null
    private var tts: TextToSpeech? = null
    private var isGain = false

    companion object {
        fun callingIntent(context: Context) =
            Intent(context, PreFreeExerciseTreadmillAcitivity::class.java)
    }

    private lateinit var mService: BleService
    private var mBound: Boolean = false
    lateinit var confirmProgressBar: ConfirmProgressBar

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as BleService.LocalBinder
            mService = binder.getService()

            mBound = true
            Functions.showLog(TAG, "onServiceConnected: $mService")

            if (Constants.IS_TEST_MI) {
                mService.sendMiHeartRate()
            } else {
                mService.sendStartMeasureCmd()
                mService.sendGainDataTest()

                val display: Display = windowManager.defaultDisplay
                confirmProgressBar = instance?.let { ConfirmProgressBar(it) }!!
                confirmProgressBar?.showPopup(display)

//                showProgressDialog(getString(R.string.hold_steady))
//
//
//                Functions.showLog("TIME_OUT "+ Constants.Countdown.TIME_OUT)
//                Functions.showLog("TIME_OUT 1" + Constants.Countdown.TIME_INTERVAL)
                (object : CountDownTimer(
                    Constants.Countdown.TIME_OUT,
                    Constants.Countdown.TIME_INTERVAL
                ) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
//                        hideProgressDialog()
                        confirmProgressBar?.dismiss()
                    }
                }).start()

//                var confirmProgressBar = BaseActivity.instance?.let { ConfirmProgressBar(it) }
//                confirmProgressBar?.showPopup()

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
        setContentView(R.layout.activity_pre_free_exercise_treadmill)
        tts = TextToSpeech(this, this)
        initBleService()
        setUpView()
    }


    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiverActivity, makeGattUpdateIntentFilter())
        checkSpeaker()

    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mGattUpdateReceiverActivity)

    }

    private fun checkSpeaker(){
        if (PrefManager.getIsSpeakerTurnOn()) {
            imvNarration.setImageResource(R.drawable.ic_bell)
        } else {
            imvNarration.setImageResource(R.drawable.ic_bell_mute)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    fun initTimer(){
        timer = object : CountDownTimer(
            timeLeft,
            Constants.Countdown.TIME_INTERVAL.toLong()
        ) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                tvTimeStart.text = DateTimeUtil.convertLongtoSecondString(millisUntilFinished)
            }


            override fun onFinish() {
                isFinishCountDown = true
                timeLeft = Constants.Countdown.DURATION
                navigateScreen()
            }
        }
    }



    fun startTimer() {

        if (isFinishCountDown) {
            initTimer()
            timer?.start()
            isGain = true
        }
        else{
            timer?.start()
        }
    }
    val mGattUpdateReceiverActivity = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Functions.showLog(TAG, "On receive broadcast.action:${action}")
            if (BleService.ACTION_GATT_CONNECTED == action) {

            } else if (BleService.ACTION_DATA_AVAILABLE == action) {
                // When device send data -> app
                val type = intent.getStringExtra(BleService.TYPE_DATA)
                when (type) {
                    Constants.TYPE_MEASURE_DATA -> {}
                    Constants.TYPE_BATTERY_LEVEL -> {}
                    Constants.TYPE_STATUS_VERSION -> {}
                    Constants.TYPE_GAIN_DATA -> {
                        startTimer()
                        if (PrefManager.getIsSpeakerTurnOn()) {
                            tts!!.speak(
                                getString(R.string.tts_rx_exercise_pre_free),
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                            Functions.showLog("Narration: " +      getString(R.string.tts_rx_exercise_pre_free))
                        }
                        confirmProgressBar?.hidePopup()

//                        hideProgressDialog()
                    }
                    Constants.TYPE_ERROR -> {}
                }
            }
        }
    }




    private fun navigateScreen() {
        val type: String? = intent.getStringExtra(Constants.type)
        val intent = Intent(this, FreeExerciseStartTreadmillActivity::class.java)
        intent.putExtra(Constants.type, type)
        startActivity(intent)
        finish()
    }

    private fun setUpView() {
        imvBack.setOnClickListener {
            finish()
        }

        tvTitle.text = getText(R.string.free_exercise)
        imvBtnStop.setOnClickListener {
            var confirmDialog = ConfirmDialog(this)
            confirmDialog.isShowTitle = false
            confirmDialog.content = getString(R.string.content_dialog_free_lt_test)
            confirmDialog.textButtonRight = getString(R.string.btn_cancel)
            confirmDialog.textButtonLeft = getString(R.string.btn_ok)
            confirmDialog.onClickButtonLeft = {
                finish()
            }
            confirmDialog.onClickButtonRight = {
            }
            confirmDialog.showPopup()
        }

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