package com.obelab.repace.features.exercise

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.view.View
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.dialog.ConfirmDialog
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.DateTimeUtil
import com.obelab.repace.core.util.ExerciseHelper
import com.obelab.repace.model.CalendarModel
import com.obelab.repace.model.ListSMO2Model
import com.obelab.repace.model.RepaceMeasure
import com.obelab.repace.model.SmO2ChartModel
import com.obelab.repace.service.ble.BleService
import kotlinx.android.synthetic.main.activity_lt_test_start_treadmill.*
import kotlinx.android.synthetic.main.activity_rx_exercise_start_treadmill.*
import kotlinx.android.synthetic.main.activity_rx_exercise_start_treadmill.circularProgressBar
import kotlinx.android.synthetic.main.activity_rx_exercise_start_treadmill.imvBtnStop
import kotlinx.android.synthetic.main.activity_rx_exercise_start_treadmill.tvSmO2
import kotlinx.android.synthetic.main.activity_rx_exercise_start_treadmill.tvSpeed
import kotlinx.android.synthetic.main.activity_rx_exercise_start_treadmill.tvTimeRemaining
import kotlinx.android.synthetic.main.activity_rx_exercise_start_treadmill.tvTotalDistance
import kotlinx.android.synthetic.main.header_back_bell.*
import java.util.*

class RxExerciseStartTreadmillActivity : BaseActivity(), TextToSpeech.OnInitListener {
    private val TAG = "RxExerciseStartTreadmillActivity"
    val dataTodayExercise = ExerciseHelper.getTodayExercise()
    private var time = if (Constants.IS_TEST) 30 else dataTodayExercise.todaySession.time * 60
    private var isFinishCountdown = true
    private var measureData: RepaceMeasure = RepaceMeasure.empty
    private var typeExercise: String? = ""
    private var listSmo2InTime = ListSMO2Model.empty
    private var tts: TextToSpeech? = null
    private var timeLeft =
        if (Constants.IS_TEST) 30.toLong() * 1000 else dataTodayExercise.todaySession.time.toLong() * 60 * 1000
    var timer: CountDownTimer? = null

    companion object {
        fun callingIntent(context: Context) =
            Intent(context, RxExerciseStartTreadmillActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rx_exercise_start_treadmill)
        typeExercise = intent.getStringExtra(Constants.typeExercise)
        Functions.resetData()
        tts = TextToSpeech(this, this)
        if (PrefManager.getIsSpeakerTurnOn()) {
            tts!!.speak(
                getString(R.string.tts_start_running),
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        }
        setUpView()
        startTimer()
    }

    private fun setUpView() {
        imvBack.visibility = View.INVISIBLE
        imvBack.setOnClickListener {
            finish()
        }

        tvTitle.text = getText(R.string.rx_exercise)

        var date = Calendar.getInstance()
        PrefManager.saveTimeStart(CalendarModel(date))

        if (dataTodayExercise.todaySession.session > 9) {
            tvSession.text = dataTodayExercise.todaySession.session.toString()
        } else {
            tvSession.text = "0${dataTodayExercise.todaySession.session}"
        }

        tvSpeed.text = dataTodayExercise.todaySession.speed.toString()

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

        circularProgressBar.apply {
            progressMax = time.toFloat() * 1000
        }

        imvBtnStop.setOnClickListener {
            var confirmDialog = ConfirmDialog(this)
            confirmDialog.isShowTitle = false
            confirmDialog.content = getString(R.string.content_dialog_lt_test_stop)
            confirmDialog.textButtonRight = getString(R.string.btn_cancel)
            confirmDialog.textButtonLeft = getString(R.string.btn_ok)
            confirmDialog.onClickButtonLeft = {
                if (PrefManager.getIsSpeakerTurnOn()) {
                    tts!!.speak(
                        getString(R.string.tts_workout_stopped),
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                }
                Handler().postDelayed({
                    finish()
                }, 1500)

            }
            confirmDialog.onClickButtonRight = {
            }
            confirmDialog.showPopup()
            confirmDialog.setCanceledOnTouchOutside(false)
        }
        btnPausePlay.setOnClickListener {
            if (isFinishCountdown) {
                btnPausePlay.setImageResource(R.drawable.ic_play)
                if (PrefManager.getIsSpeakerTurnOn()) {
                    tts!!.speak(
                        getString(R.string.tts_workout_paused),
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                }
                isFinishCountdown = false
                stopTimer()
            } else {
                if (PrefManager.getIsSpeakerTurnOn()) {
                    tts!!.speak(
                        getString(R.string.tts_workout_resumed),
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                }
                btnPausePlay.setImageResource(R.drawable.ic_pause)
                isFinishCountdown = true
                startTimer()
            }
        }
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

    override fun onDestroy() {
        timer?.cancel()

        super.onDestroy()
    }

    fun stopTimer() {
        timer?.cancel()
    }

    fun startTimer() {
        if (isFinishCountdown) {
            initTimer()
            timer?.start()
        } else {
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

    fun initTimer() {
        timer = object : CountDownTimer(
            timeLeft,
            Constants.Countdown.TIME_INTERVAL.toLong()
        ) {
            override fun onTick(millisUntilFinished: Long) {

                timeLeft = millisUntilFinished

//                if (PrefManager.getIsSpeakerTurnOn()) {
//                    if (typeExercise == Constants.high_intensity) {
//                        if (time == 60) {
//                            if (DateTimeUtil.convertLongtoMinuteSecondString(millisUntilFinished) == "55:00") {
//                                tts!!.speak(
//                                    getString(R.string.tts_good_start),
//                                    TextToSpeech.QUEUE_FLUSH,
//                                    null,
//                                    ""
//                                )
//                            }
//                        }
//                    }
//                }

                circularProgressBar.apply {
                    progress =
                        ((time.toLong() * 1000 - timeLeft.toFloat()) * 0.8).toFloat()
                }
                if (measureData.rSO2 != null) {
                    tvSmO2.text = Functions.convertRSO2(measureData.rSO2!!).toString()
                    Functions.showLog(
                        TAG,
                        "Smo2: (${Functions.convertRSO2(measureData.rSO2!!)}, D)"
                    )
                }
                tvTimeRemaining.text =
                    DateTimeUtil.convertLongtoMinuteSecondString(millisUntilFinished)
                tvTotalDistance.text = String.format(
                    "%.1f",
                    (dataTodayExercise.todaySession.speed) * (time - timeLeft / 1000) / 3600
                ).replace(",", ".")
            }


            override fun onFinish() {
                isFinishCountdown = true
                timeLeft = Constants.Countdown.DURATION
                navigateScreen()
            }
        }
    }

    val mGattUpdateReceiverActivity = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BleService.ACTION_DATA_AVAILABLE == action) {
                val type = intent.getStringExtra(BleService.TYPE_DATA)
                when (type) {
                    Constants.TYPE_MEASURE_DATA -> {
                        val data =
                            intent.getSerializableExtra(BleService.EXTRA_DATA) as? RepaceMeasure

                        if (data != null) {
                            measureData = data
                        }

                        if (data != null) {
                            var rso2 = 0.0
                            if (data.rSO2 != null) {
                                rso2 = Functions.convertRSO2(data.rSO2!!)
                            }

                            //Save all smo2
                            var listSMO2 = PrefManager.getAllListSMO2()
                            if (listSMO2.listSMO2 != null) {
                                listSMO2.listSMO2!!.add(rso2)
                            } else {
                                listSMO2.listSMO2 = arrayListOf(rso2)
                            }
                            PrefManager.saveAllListSMO2(listSMO2)

                            if (listSmo2InTime != ListSMO2Model.empty) {
                                listSmo2InTime.listSMO2!!.add(rso2)
                            } else {
                                listSmo2InTime.listSMO2 = arrayListOf(rso2)
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


    private fun navigateScreen() {
        val type: String? = intent.getStringExtra(Constants.type)
        val intent = Intent(this, ExerciseCompleteActivity::class.java)
        intent.putExtra(Constants.type, type)
        intent.putExtra(Constants.typeExercise, typeExercise)
        finish()
        startActivity(intent)
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