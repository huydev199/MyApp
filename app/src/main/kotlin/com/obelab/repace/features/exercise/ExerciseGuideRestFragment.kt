package com.obelab.repace.features.exercise

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import com.obelab.repace.R
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.core.util.ExerciseHelper
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.features.pairing.PairingActivity
import com.obelab.repace.service.ble.BleService
import kotlinx.android.synthetic.main.fragment_exercise_guide_rest.*
import kotlinx.android.synthetic.main.header_back_bluetooth.*


class ExerciseGuideRestFragment : BaseFragment() {

    private lateinit var mService: BleService
    private var mBound: Boolean = false

    override fun layoutId(): Int {
        return R.layout.fragment_exercise_guide_rest
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
        tvTitle.text = getText(R.string.rx_exercise)
        imvBack.setOnClickListener {
            mainActivity.goToExerciseGuideFragment(null)
        }

        val dataTodayExercise = ExerciseHelper.getTodayExercise()

        if(dataTodayExercise.todaySession.session >9){
            tvSession.text = dataTodayExercise.todaySession.session.toString()
            tvCurrentSession.text = dataTodayExercise.todaySession.session.toString()
        } else {
            tvSession.text = "0${dataTodayExercise.todaySession.session}"
            tvCurrentSession.text = "0${dataTodayExercise.todaySession.session}"
        }



        btnExerciseRest.setOnClickListener {
            MainActivity.instance?.goToExerciseFragment()
        }

        btn_bluetooth.setOnClickListener {
            startActivity(context?.let { it1 -> PairingActivity.callingIntent(it1) })
        }
    }
}