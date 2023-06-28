package com.obelab.repace.features.exercise

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.view.View
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.dialog.ConfirmDialog
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.DateTimeUtil
import com.obelab.repace.core.util.DateTimeUtil.convertLongtoMinuteSecondString
import com.obelab.repace.core.util.ExerciseHelper
import com.obelab.repace.model.CalendarModel
import com.obelab.repace.model.ListSMO2Model
import com.obelab.repace.model.LocationModel
import com.obelab.repace.model.RepaceMeasure
import com.obelab.repace.service.ble.BleService
import kotlinx.android.synthetic.main.activity_rx_exercise_start_outdoor.*
import kotlinx.android.synthetic.main.activity_rx_exercise_start_outdoor.btnPausePlay
import kotlinx.android.synthetic.main.activity_rx_exercise_start_outdoor.circularProgressBar
import kotlinx.android.synthetic.main.activity_rx_exercise_start_outdoor.imvBtnStop
import kotlinx.android.synthetic.main.activity_rx_exercise_start_outdoor.tvSession
import kotlinx.android.synthetic.main.activity_rx_exercise_start_outdoor.tvSmO2
import kotlinx.android.synthetic.main.activity_rx_exercise_start_outdoor.tvSpeed
import kotlinx.android.synthetic.main.activity_rx_exercise_start_outdoor.tvTimeRemaining
import kotlinx.android.synthetic.main.activity_rx_exercise_start_outdoor.tvTotalDistance
import kotlinx.android.synthetic.main.activity_rx_exercise_start_treadmill.*
import kotlinx.android.synthetic.main.header_back_bell.*
import java.util.*

class RxExerciseStartOutdoorActivity : BaseActivity(), TextToSpeech.OnInitListener {
    private val TAG = "RxExerciseStartOutdoorActivity"
    val dataTodayExercise = ExerciseHelper.getTodayExercise()
    private var time = if (Constants.IS_TEST) 30 else dataTodayExercise.todaySession.time * 60
    private var startTime: Int = time
    private var isFinishCountdown = true
    private var measureData: RepaceMeasure = RepaceMeasure.empty
    private var listSmo2InTime = ListSMO2Model.empty
    private var locationList: ArrayList<LocationModel> = ArrayList()
    private var currentSpeed: Double = 0.0
    private var currentDistance: Double = 0.0
    private var tts: TextToSpeech? = null
    private var timeLeft =
        if (Constants.IS_TEST) 30.toLong() * 1000 else dataTodayExercise.todaySession.time.toLong() * 60 * 1000

    var timer: CountDownTimer? = null

    companion object {
        fun callingIntent(context: Context) =
            Intent(context, RxExerciseStartOutdoorActivity::class.java)
    }

    private var locationManager: LocationManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rx_exercise_start_outdoor)
        Functions.resetData()

        tts = TextToSpeech(this, this)
        setUpView()

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        getLastLocation()
        tvTotalDistance.text = Functions.getFloatDecimal(PrefManager.getTotalDistance()).toString()
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
                finish()
            }
            confirmDialog.onClickButtonRight = {
            }
            confirmDialog.showPopup()
            confirmDialog.setCanceledOnTouchOutside(false)
        }

        btnPausePlay.setOnClickListener {
            if (isFinishCountdown) {
                btnPausePlay.setImageResource(R.drawable.ic_play)
                isFinishCountdown = false
                stopTimer()
            } else {
                btnPausePlay.setImageResource(R.drawable.ic_pause)
                isFinishCountdown = true
                startTimer()
            }
        }
    }

    private fun getLastLocation() {
        Functions.showLog("current1 voo")
        try {
            // Request location updates
            Functions.showLog("current1 voo55")
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                locationListener
            )
        } catch (ex: SecurityException) {
            Functions.showLog("Security Exception, no location available")
        }

    }

    //define the listener
    private val locationListener: LocationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            var time = 0
            val item = LocationModel(location.latitude, location.longitude, location.time)

            locationList = (locationList + listOf(item)) as ArrayList<LocationModel>
            if (locationList.size >= 2) {
                time =
                    (((locationList[(locationList.size - 1)].time - locationList[(locationList.size - 2)].time)) / 1000).toInt()
                if (time > 0) {

                    var distanceCurrent = Functions.distanceInKm(
                        locationList[(locationList.size - 1)].latitude,
                        locationList[(locationList.size - 1)].longitude,
                        locationList[(locationList.size - 2)].latitude,
                        locationList[(locationList.size - 2)].longitude
                    )
                    var timeCurrent = Functions.timeInHour(time.toDouble())
                    currentSpeed = (distanceCurrent) / timeCurrent
                    tvExerciseCurrentSpeed.text =
                        Functions.getDouble1Decimal(currentSpeed).toString()
                    tvTotalDistance.text =
                        Functions.getFloatDecimal(PrefManager.getTotalDistance()).toString()
                    currentDistance = +distanceCurrent
                    val distance = PrefManager.getTotalDistance() + distanceCurrent
                    var dataSpeedArray = PrefManager.getListExerciseSpeed()

                    if (dataSpeedArray.exerciseArraySpeedModel != null) {
                        dataSpeedArray.exerciseArraySpeedModel!!.add(currentSpeed)
                    } else {
                        dataSpeedArray.exerciseArraySpeedModel = arrayListOf(currentSpeed)
                    }
                    PrefManager.saveListExerciseSpeed(dataSpeedArray)
                    PrefManager.setTotalDistance(distance.toFloat())
                    Functions.showLog("currentSpeed -> $currentSpeed")
                    openSpeaker()
                }
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
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
        val typeExercise: String? = intent.getStringExtra(Constants.typeExercise)
        val intent = Intent(this, ExerciseCompleteActivity::class.java)
        intent.putExtra(Constants.type, type)
        intent.putExtra(Constants.typeExercise, typeExercise)
        finish()
        startActivity(intent)
    }

    private fun convertTime(time: Int): String {
        val minute = (time / 60).toInt()
        val second = (time % 60).toInt()
        var result = "00 : 00"
        result = if (minute > 9) {
            if (second > 9) {
                "$minute : $second"
            } else {
                "$minute : 0$second"
            }
        } else {
            if (second > 9) {
                "0$minute : $second"
            } else {
                "0$minute : 0$second"
            }
        }
        return result
    }

    fun openSpeaker() {
        Functions.showLog("${PrefManager.getIsSpeakerTurnOn()}")
        if (PrefManager.getIsSpeakerTurnOn()) {
            if (currentSpeed > dataTodayExercise.todaySession.speed) {
                tts!!.speak(getString(R.string.speed_down), TextToSpeech.QUEUE_FLUSH, null, "")
            } else if (currentSpeed < dataTodayExercise.todaySession.speed) {
                tts!!.speak(getString(R.string.speed_up), TextToSpeech.QUEUE_FLUSH, null, "")
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

    private fun settingSpeed() {
        val defaultSpeed = dataTodayExercise.todaySession.speed
        if (currentSpeed > defaultSpeed) {
            llWarning.visibility = View.VISIBLE
            tvWarning.text = getString(R.string.speed_down)
            tvExerciseCurrentSpeed.setTextColor(resources.getColor(R.color.colorEdtError))
        } else if (currentSpeed < defaultSpeed) {
            llWarning.visibility = View.VISIBLE
            tvWarning.text = getString(R.string.speed_up)
            tvExerciseCurrentSpeed.setTextColor(resources.getColor(R.color.colorEdtError))
        } else {
            llWarning.visibility = View.INVISIBLE
            tvExerciseCurrentSpeed.setTextColor(resources.getColor(R.color.colorTextPrimary))
        }
    }

    fun initTimer() {
        timer = object : CountDownTimer(
            timeLeft,
            Constants.Countdown.TIME_INTERVAL.toLong()
        ) {
            override fun onTick(millisUntilFinished: Long) {
                settingSpeed()
                timeLeft = millisUntilFinished
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
            }


            override fun onFinish() {
                isFinishCountdown = true
                timeLeft =
                    if (Constants.IS_TEST) 30.toLong() * 1000 else dataTodayExercise.todaySession.time.toLong() * 60 * 1000
                navigateScreen()
                // stop location updating
                locationManager?.removeUpdates(locationListener);
            }
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

    override fun onDestroy() {
        locationManager?.removeUpdates(locationListener)
        val newLocationList = PrefManager.getExerciseLocationList() + locationList
        PrefManager.saveExerciseLocationList(newLocationList)
        timer?.cancel()
        super.onDestroy()
    }
}