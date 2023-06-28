package com.obelab.repace.features.exercise

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.dialog.ConfirmDialog
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.model.CalendarModel
import com.obelab.repace.model.ListSMO2Model
import com.obelab.repace.model.LocationModel
import com.obelab.repace.model.RepaceMeasure
import com.obelab.repace.service.ble.BleService
import kotlinx.android.synthetic.main.activity_free_exercise.*
import kotlinx.android.synthetic.main.header_back_bell.*
import java.util.*
import kotlin.concurrent.timer

class FreeExerciseStartOutdoorActivity : BaseActivity() {
    private val TAG = "FreeExerciseStartOutdoorActivity"
    private var startTime: Int = 0
    private var isRun = false
    private var measureData: RepaceMeasure = RepaceMeasure.empty
    private var listSmo2InTime = ListSMO2Model.empty
    private var locationList: ArrayList<LocationModel> = ArrayList()
    private var currentSpeed: Double = 0.0
    private var currentDistance: Double = 0.0
    private var lastPause: Long = 0
    private var isPause = false
    companion object {
        fun callingIntent(context: Context) = Intent(context, FreeExerciseStartOutdoorActivity::class.java)
    }

    private var locationManager: LocationManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Functions.resetData()
        setContentView(R.layout.activity_free_exercise)
        setUpView()
        simpleChronometer.setBase(SystemClock.elapsedRealtime());
        startTimer()
        isRun = true

        simpleChronometer.setOnChronometerTickListener{
            startTime ++
            if (measureData.rSO2 != null) {
                tvSm02.text = Functions.convertRSO2(measureData.rSO2!!).toString()
                Functions.showLog(TAG,"Smo2: (${Functions.convertRSO2(measureData.rSO2!!)}, D)")
                circularProgressBar.apply {
                    progress = (Functions.convertRSO2(measureData.rSO2!!) * 0.8.toFloat()).toFloat()
                }
            }
            Functions.showLog("Chromometer: OnTick:${SystemClock.elapsedRealtime() - it.getBase()}")
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        getLastLocation()
    }
    private fun stopTimer(){
        lastPause = SystemClock.elapsedRealtime();

        simpleChronometer.stop();
    }
    private fun startTimer(){
      simpleChronometer.start()
    }
    private fun resumeTimer(){
        simpleChronometer.setBase(simpleChronometer.getBase() + SystemClock.elapsedRealtime() - lastPause);
        simpleChronometer.start();
    }

    private fun setUpView() {
        imvBack.visibility = View.INVISIBLE
        imvBack.setOnClickListener {
            finish()
        }
        tvTitle.text = getText(R.string.free_exercise)
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
            PrefManager.saveFreeDuration(startTime)

            var confirmDialog = ConfirmDialog(this)
            confirmDialog.isShowTitle = false
            confirmDialog.content = getString(R.string.content_dialog_free_lt_test)
            confirmDialog.textButtonRight = getString(R.string.btn_cancel)
            confirmDialog.textButtonLeft = getString(R.string.btn_ok)
            confirmDialog.onClickButtonLeft = {
                finish()
                startActivity(ExerciseCompleteActivity.callingIntent(this))
                // stop location updating
                locationManager?.removeUpdates(locationListener)
            }
            confirmDialog.onClickButtonRight = {

            }
            confirmDialog.showPopup()
        }

        btnPausePlay.setOnClickListener {
            isRun = if (!isRun) {
                btnPausePlay.setImageResource(R.drawable.ic_pause)
               resumeTimer()
                true
            } else {
                btnPausePlay.setImageResource(R.drawable.ic_play)
               stopTimer()
                false
            }
        }

        var date = Calendar.getInstance()
        PrefManager.saveTimeStart(CalendarModel(date))
    }

    private fun getLastLocation() {
        try {
            // Request location updates
            locationManager?.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener )
        } catch (ex: SecurityException) {
            Functions.showLog(TAG, "Security Exception, no location available")
        }
    }

    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            var time = 0
            var latitude = location.latitude
            var longitude = location.longitude

            Functions.showLog(TAG, latitude.toString())
            Functions.showLog(TAG, longitude.toString())
            var item = LocationModel(location.latitude, location.longitude, location.time)

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
                    tvSpeed.text = Functions.getDouble1Decimal(currentSpeed).toString()
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

                    Functions.showLog("dataSpeedArray ${Functions.toJsonString(dataSpeedArray)}")
                    Functions.showLog("listLocation ${locationList}")
                }
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiverActivity, makeGattUpdateIntentFilter())
        val distance = PrefManager.getDistance()
        Functions.showLog("$distance")
        if (distance != "") {
            tvTotalDistance.text = distance
        } else {
            tvTotalDistance.text = "-"
        }
        checkSpeaker()

    }

    override fun onPause() {
        super.onPause()
        isPause = true

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
                }
            }
        }
    }

    override fun onDestroy() {
        locationManager?.removeUpdates(locationListener)
        val newLocationList = PrefManager.getExerciseLocationList() + locationList
        PrefManager.saveExerciseLocationList(newLocationList)
        simpleChronometer?.stop()
        super.onDestroy()
    }
}