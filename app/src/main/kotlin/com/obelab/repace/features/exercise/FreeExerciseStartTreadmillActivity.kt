package com.obelab.repace.features.exercise

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.View
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.dialog.ConfirmDialog
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.model.CalendarModel
import com.obelab.repace.model.ListSMO2Model
import com.obelab.repace.model.RepaceMeasure
import com.obelab.repace.service.ble.BleService
import kotlinx.android.synthetic.main.activity_free_exercise.*
import kotlinx.android.synthetic.main.header_back_bell.*
import java.util.*

class FreeExerciseStartTreadmillActivity : BaseActivity() {
    private val TAG = "FreeExerciseStartTreadmillActivity"
    private var startTime: Int = 0
    private var isRun = true
    private var measureData: RepaceMeasure = RepaceMeasure.empty
    private var listSmo2InTime = ListSMO2Model.empty
    private var lastPause: Long = 0
    private var isPause = false
    companion object {
        fun callingIntent(context: Context) =
            Intent(context, FreeExerciseStartTreadmillActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_free_exercise)

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
        setUpView()

        checkSpeaker()
    }

    override fun onPause() {
        super.onPause()
        isPause = true

    }
    override fun onDestroy() {
        super.onDestroy()
        simpleChronometer?.stop()
    }

    private fun checkSpeaker(){
        if (PrefManager.getIsSpeakerTurnOn()) {
            imvNarration.setImageResource(R.drawable.ic_bell)
        } else {
            imvNarration.setImageResource(R.drawable.ic_bell_mute)
        }
    }

    private fun setUpView() {
        imvBack.visibility = View.INVISIBLE
        imvBack.setOnClickListener {
            finish()
        }

        tvTitle.text = getText(R.string.free_exercise)

        var date = Calendar.getInstance()
        PrefManager.saveTimeStart(CalendarModel(date))

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

                startActivity(FreeExerciseDistanceActivity.callingIntent(this))
                finish()
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

        circularProgressBar.apply {
            progressMax = 100.toFloat()
        }
    }

    val mGattUpdateReceiverActivity = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BleService.ACTION_DATA_AVAILABLE == action) {
                val type = intent.getStringExtra(BleService.TYPE_DATA)
                when (type) {
                    Constants.TYPE_MEASURE_DATA -> {
                        val data = intent.getSerializableExtra(BleService.EXTRA_DATA) as? RepaceMeasure
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
}