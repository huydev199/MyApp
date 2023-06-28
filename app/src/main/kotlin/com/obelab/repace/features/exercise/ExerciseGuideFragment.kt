package com.obelab.repace.features.exercise

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.Display
import android.view.View
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.dialog.ConfirmDialog
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.ExerciseHelper
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.features.pairing.PairingActivity
import com.obelab.repace.model.LocationModel
import com.obelab.repace.service.ble.BleService
import kotlinx.android.synthetic.main.fragment_exercise_guide.*
import kotlinx.android.synthetic.main.header_back_bluetooth.*

class ExerciseGuideFragment : BaseFragment() {

    private lateinit var mService: BleService
    private var mBound: Boolean = false

    override fun layoutId(): Int {
        return R.layout.fragment_exercise_guide
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
    }

    override fun onResume() {
        activity?.registerReceiver(mGattUpdateReceiver, BaseActivity.makeGattUpdateIntentFilter())
        super.onResume()
    }

    override fun onDestroy() {
        activity?.unregisterReceiver(mGattUpdateReceiver)
        super.onDestroy()
    }

    val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BleService.ACTION_GATT_CONNECTED == action) {
                btn_bluetooth.setImageResource(R.drawable.ic_bluetooth_on);
            } else if (BleService.ACTION_GATT_DISCONNECTED == action) {
                btn_bluetooth.setImageResource(R.drawable.ic_bluetooth_off);
            }
        }
    }

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as BleService.LocalBinder
            mService = binder.getService()
            mBound = true
            if (mService.mConnectionState == 2) {
                btn_bluetooth.setImageResource(R.drawable.ic_bluetooth_on);
            } else {
                btn_bluetooth.setImageResource(R.drawable.ic_bluetooth_off);
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.bindService(
            Intent(activity, BleService::class.java),
            mServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun setUpView() {

        val mainActivity = activity as MainActivity
        val display: Display = activity?.windowManager!!.defaultDisplay
        val stageWidth: Int = display.getWidth()
        var width = Functions.convertPx(stageWidth, 64)

        img_low_exercise.layoutParams.width = width
        img_low_exercise.layoutParams.height = (width * 0.77).toInt()

        tvTitle.text = getText(R.string.rx_exercise)
        imvBack.setOnClickListener {
            mainActivity.goToExerciseSelectFragment(null)
        }

        if (mainActivity.typeScreen == Constants.RX_EXERCISE_TREADMILL) {
            if (mainActivity.typeExercise == Constants.HIGHT_INTENSITY_EXERCISE) {
                img_low_exercise.setImageResource(R.drawable.img_treadmill_hight)
            } else {
                img_low_exercise.setImageResource(R.drawable.img_treadmill_low)
            }
        } else {
            if (mainActivity.typeExercise == Constants.HIGHT_INTENSITY_EXERCISE) {
                img_low_exercise.setImageResource(R.drawable.img_outdoor_hight)
            } else {
                img_low_exercise.setImageResource(R.drawable.img_outdoor_low)
            }
        }

        val dataTodayExercise = ExerciseHelper.getTodayExercise()
        Functions.showLog("dataTodayExercise: $dataTodayExercise")

        if (dataTodayExercise.todaySession.session > 9) {
            tvSession.text = dataTodayExercise.todaySession.session.toString()
        } else {
            tvSession.text = "0${dataTodayExercise.todaySession.session}"
        }

        tvTime.text = dataTodayExercise.todaySession.time.toString()
        tvSpeed.text = dataTodayExercise.todaySession.speed.toString()
        tvHeartrate.text = dataTodayExercise.todaySession.heartRate.toString()

        Functions.showLog("" + dataTodayExercise.todaySession)

        btnExerciseStart.setOnClickListener {
            if (dataTodayExercise.todaySession.time == 0) {
                mainActivity.goToExerciseGuideRestFragment()
            } else {
                if (!dataTodayExercise.isPracticedToday || Constants.IS_TEST) {
                    if (mainActivity.typeScreen == Constants.RX_EXERCISE_TREADMILL) {
                        val intent = Intent(context, PreRxExerciseTreadmillActivity::class.java)
                        intent.putExtra(Constants.type, mainActivity.typeScreen)
                        intent.putExtra(Constants.typeExercise, mainActivity.typeExercise)
                        startActivity(intent)
                    } else if (mainActivity.typeScreen == Constants.RX_EXERCISE_OUTDOOR) {
                        if(Constants.IS_TEST) {
                            val intent = Intent(context, PreRxExerciseOutdoorActivity::class.java)
                            intent.putExtra(Constants.type, mainActivity.typeScreen)
                            intent.putExtra(Constants.typeExercise, mainActivity.typeExercise)
                            startActivity(intent)
                            PrefManager.saveExerciseLocationList(ArrayList<LocationModel>())
                        } else {
                            val intent = Intent(context, PreRxExerciseOutdoorActivity::class.java)
                            intent.putExtra(Constants.type, mainActivity.typeScreen)
                            intent.putExtra(Constants.typeExercise, mainActivity.typeExercise)
                            startActivity(intent)
                            PrefManager.saveExerciseLocationList(ArrayList<LocationModel>())
                        }
                    }
                } else {
                    var confirmDialog = context?.let { it1 -> ConfirmDialog(it1) }
                    if (confirmDialog != null) {
                        confirmDialog.isShowTitle = false
                        confirmDialog.content = getString(R.string.content_one_session_per_day)
                        confirmDialog.textButtonRight = getString(R.string.btn_cancel)
                        confirmDialog.textButtonLeft = getString(R.string.btn_ok)
                        confirmDialog.onClickButtonLeft = {
                        }
                        confirmDialog.onClickButtonRight = {
                        }
                        confirmDialog.showPopup()
                    }
                }
            }
        }

        btn_bluetooth.setOnClickListener {
            startActivity(context?.let { it1 -> PairingActivity.callingIntent(it1) })
        }
    }
}
