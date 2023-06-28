package com.obelab.repace.features.ltTest

import android.content.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.view.View
import com.obelab.library.repace.Externer
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.dialog.ConfirmDialog
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.DateTimeUtil
import com.obelab.repace.core.util.DateTimeUtil.convertLongtoMinuteSecondString
import com.obelab.repace.model.*
import com.obelab.repace.service.ble.BleService
import kotlinx.android.synthetic.main.activity_lt_test_start_outdoor.*
import kotlinx.android.synthetic.main.activity_lt_test_start_outdoor.circularProgressBar
import kotlinx.android.synthetic.main.activity_lt_test_start_outdoor.imvBtnStop
import kotlinx.android.synthetic.main.activity_lt_test_start_outdoor.tvSmO2
import kotlinx.android.synthetic.main.activity_lt_test_start_outdoor.tvStage
import kotlinx.android.synthetic.main.activity_lt_test_start_outdoor.tvTimeRemaining
import kotlinx.android.synthetic.main.header_back_bell.*
import java.util.*

class LtTestStartOutdoorActivity : BaseActivity(), TextToSpeech.OnInitListener {
    private val TAG = "LtTestStartOutdoorActivity"
    private var currentDistance: Double = 0.0
    private var listRSO2: ArrayList<ListSMO2Model> = ArrayList()
    private var currentSpeed: Double = 0.0
    private var isFinishCountdown = true
    private var isSaveBaseLine = false
    private var rawData: ByteArray? = null
    private var measureData: RepaceMeasure = RepaceMeasure.empty
    private var defaultSpeed =
        String.format("%.1f", PrefManager.getSpeed().toDouble()).replace(",", ".").toDouble()
    private var locationList: MutableList<LocationModel> = ArrayList()
    private var listSmo2InStage = ListSMO2Model.empty
    private var protocol = PrefManager.getProtocol()
    private var arrayListSmo2 = PrefManager.getListSMO2()
    private var stage = PrefManager.getStage()
    private var lactate = PrefManager.getLactate()
    private var tts: TextToSpeech? = null
    private var timeLeft = Constants.Countdown.DURATION_START
    var timer: CountDownTimer? = null
    private var time: Int = 0
    private var preStatus: Int = -1
    private var currentStatus: Int = -2
    private var isOpenSpeak = false

    companion object {
        fun callingIntent(context: Context) =
            Intent(context, LtTestStartOutdoorActivity::class.java)
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

    private var locationManager: LocationManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lt_test_start_outdoor)
        tts = TextToSpeech(this, this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        setUpView()
        getLastLocation()
        id_total_distance.text =
            Functions.getFloatDecimal(PrefManager.getTotalDistance()).toString()
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

    private fun getLastLocation() {
        try {
            // Request location updates
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                locationListener
            )
        } catch (ex: SecurityException) {
            Functions.showLog("myTag", "Security Exception, no location available")
        }
    }

    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            var time = 0
            var item = LocationModel(location.latitude, location.longitude, location.time)
            locationList.add(item)
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
                    tvCurrentSpeed.text = Functions.getDouble1Decimal(currentSpeed).toString()
                    Functions.showLog(
                        TAG,
                        "Current Speed: ${Functions.getDouble1Decimal(currentSpeed)};"
                    )
                    id_total_distance.text =
                        Functions.getFloatDecimal(PrefManager.getTotalDistance()).toString()
                    currentDistance = +distanceCurrent
                    val distance = PrefManager.getTotalDistance() + distanceCurrent

                    PrefManager.setTotalDistance(distance.toFloat())
                    Functions.showLog("currentSpeed -> $currentSpeed")
                }
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun setUpView() {
        tvCurrentSpeed.text = currentSpeed.toString()
        imvBack.visibility = View.INVISIBLE
        currentStatus = settingSpeed()
        imvBack.setOnClickListener {
            finish()
        }
        if (stage == 1) {
            //Base line
            var baseLineSMO2 =
                arrayListSmo2.listSMO2?.sum()?.toDouble()?.div(arrayListSmo2.listSMO2!!.size)
            baseLineSMO2?.let { PrefManager.saveBaseLineSMO2(it.toFloat()) }
        }
        //Reset list SMO2
        PrefManager.saveListSMO2(ListSMO2Model.empty)

        tvTitle.text = getText(R.string.lt_test)
        tvStage.text = Functions.convertStringState(stage)
        //Set speed
        tvDefaultSpeed.text = defaultSpeed.toString()
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

        imvBtnStop.setOnClickListener {
            //isRun = false
            var confirmDialog = ConfirmDialog(this)
            confirmDialog.isShowTitle = false
            confirmDialog.content = getString(R.string.content_dialog_lt_test_stop)
            confirmDialog.textButtonRight = getString(R.string.btn_cancel)
            confirmDialog.textButtonLeft = getString(R.string.btn_ok)
            confirmDialog.onClickButtonLeft = {
                locationManager?.removeUpdates(locationListener)
                PrefManager.setTotalDistance(0.0.toFloat())
                val intent = Intent(this@LtTestStartOutdoorActivity, BleService::class.java)
                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
                finish()
            }
            confirmDialog.onClickButtonRight = {

            }
            confirmDialog.showPopup()
            confirmDialog.setCanceledOnTouchOutside(false)
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
                            if (isSaveBaseLine) {
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
                    Constants.TYPE_BATTERY_LEVEL -> {
                    }
                    Constants.TYPE_STATUS_VERSION -> {
                    }
                    Constants.TYPE_ERROR -> {
                    }
                }
            }
        }
    }

    fun initTimer() {
        timer = object : CountDownTimer(
            timeLeft,
            Constants.Countdown.TIME_INTERVAL.toLong()
        ) {
            override fun onTick(millisUntilFinished: Long) {
                preStatus = currentStatus
                Functions.showLog("onTick: $time")
                settingTextSpeed()
                if (time == 15 && preStatus == settingSpeed()) {
                    Functions.showLog("onTick: Open")
                    if (!isOpenSpeak) {
                        isOpenSpeak = true
                        openSpeaker(preStatus)
                        Handler().postDelayed({
                            isOpenSpeak = false
                        }, 4000)
                    }

                } else {
                    if (preStatus == settingSpeed()) {
                        currentStatus = settingSpeed()
                        time++
                        Functions.showLog("onTick: ++ $time")
                    } else {
                        currentStatus = settingSpeed()
                        time = 0
                        Functions.showLog("onTick: reset")
                    }
                }

                timeLeft = millisUntilFinished
                //Call function get analysis and setting view
                isSaveBaseLine = DateTimeUtil.convertLongtoSecondValue(millisUntilFinished) <= 10
                circularProgressBar.apply {
                    progress =
                        ((progressMax - timeLeft+900) * 0.8).toFloat()
                }
                tvTimeRemaining.text = convertLongtoMinuteSecondString(millisUntilFinished)
                if (measureData.rSO2 != null) {
                    tvSmO2.text = Functions.convertRSO2(measureData.rSO2!!).toString()
                    Functions.showLog(
                        TAG,
                        "Stage: ${stage}; Smo2: (${Functions.convertRSO2(measureData.rSO2!!)}, D); Protocol: (${protocol.heartRate}, L); Start Speed: (${protocol.startSpeed})"
                    )
                    val rSO2 = Functions.convertRSO2(measureData.rSO2!!)
                    listRSO2 = (listRSO2 + listOf(rSO2)) as ArrayList<ListSMO2Model>
                    Functions.showLog("measureData.rSO2 ${measureData.rSO2}")
                }
            }


            override fun onFinish() {
                timeLeft = Constants.Countdown.DURATION
                isFinishCountdown = true
                isSaveBaseLine = false
                var arrayListSmo2 = PrefManager.getListSMO2()
                var currSmo2 = 0.0
                if (arrayListSmo2.listSMO2 != null) {
                    arrayListSmo2.listSMO2?.sum()?.toDouble()?.div(arrayListSmo2.listSMO2!!.size)
                        ?.let {
                            currSmo2 = it
                        }
                }

                PrefManager.saveListSMO2(ListSMO2Model.empty)

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
                    "Get Analysis || Speed: (${analysis.speed}, L); Lactate: (${analysis.lactate}, L); isNext: (${analysis.isNext}, L)"
                )
                //Save performance each stage
                var dataLtTestPerformance = PrefManager.getListLtTestPerformance()
                var data = LtTestPerformanceModel(
                    stage,
                    defaultSpeed,
                    Functions.getFloatDecimal(PrefManager.getTotalDistance())
                )
                if (dataLtTestPerformance.listLtTestPerformance != null) {
                    dataLtTestPerformance.listLtTestPerformance!!.add(data)
                } else {
                    dataLtTestPerformance.listLtTestPerformance = arrayListOf(data)
                }
                PrefManager.saveListLtTestPerformance(dataLtTestPerformance)

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
                    var smo2InStage = listSmo2InStage.listSMO2?.average()?.toInt()
                    //convert smo2 stage, ex: 1->"A"
                    var scoreChart =
                        smo2InStage?.let { SmO2ChartModel(listStageConvert[stage - 1],
                            it.toDouble()
                        ) }
                    val dataSavePrefListSmo2 = PrefManager.getListSmo2InStage()
                    if (dataSavePrefListSmo2.listSmo2 != null) {
                        scoreChart?.let { dataSavePrefListSmo2.listSmo2!!.add(it) }
                    } else {
                        dataSavePrefListSmo2.listSmo2 = scoreChart?.let { arrayListOf(it) }
                    }
                    PrefManager.saveListSmo2InStage(dataSavePrefListSmo2)
                    listSmo2InStage.listSMO2 = null
                }
                if (analysis.isNext) {
                    navigateScreen(0)
                    PrefManager.saveStage(stage + 1)
                    PrefManager.saveSpeed(analysis.speed.toFloat())
                    PrefManager.saveLactate(analysis.lactate.toFloat())
                } else {
                    navigateScreen(1)
                }
                this.cancel()
            }
        }
    }

    private fun navigateScreen(data: Int) {
        if (data == 0) {
            locationManager?.removeUpdates(locationListener)
            startActivity(PreLtTestOutdoorActivity.callingIntent(this@LtTestStartOutdoorActivity))
            finish()
        } else {
            locationManager?.removeUpdates(locationListener)
            val intent = Intent(this, LtTestCompleteActivity::class.java)
            intent.putExtra(Constants.type, Constants.LT_TEST_OUTDOOR)
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
            startActivity(intent)
            finish()
        }
    }

    private fun settingSpeed(): Int {
        if (currentSpeed > defaultSpeed * 1.1) {
            return 1
        } else if (currentSpeed < defaultSpeed * 0.9) {
            return -1
        } else {
            return 0
        }
    }

    fun openSpeaker(status: Int) {
        if (PrefManager.getIsSpeakerTurnOn()) {
            if (status == 1) {
                tts!!.speak(getString(R.string.speed_down), TextToSpeech.QUEUE_FLUSH, null, "")
            } else if (status == -1) {
                tts!!.speak(getString(R.string.speed_up), TextToSpeech.QUEUE_FLUSH, null, "")
            }
        }
    }

    private fun settingTextSpeed() {
        if (currentSpeed > defaultSpeed * 1.1) {
            Functions.showLog("onTick: speed down")
            llWarning.visibility = View.VISIBLE
            tvWarning.text = getString(R.string.speed_down)
            tvCurrentSpeed.setTextColor(resources.getColor(R.color.colorEdtError))
        } else if (currentSpeed < defaultSpeed * 0.9) {
            Functions.showLog("onTick: speed up")
            llWarning.visibility = View.VISIBLE
            tvWarning.text = getString(R.string.speed_up)
            tvCurrentSpeed.setTextColor(resources.getColor(R.color.colorEdtError))
        } else {
            llWarning.visibility = View.INVISIBLE
            tvCurrentSpeed.setTextColor(resources.getColor(R.color.colorTextPrimary))
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
        val newLocationList = PrefManager.getLTTestLocationList() + locationList
        PrefManager.saveLTTestLocationList(newLocationList)
        timer?.cancel()
        super.onDestroy()
    }

}