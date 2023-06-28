package com.obelab.repace.features.ltTest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.facebook.FacebookSdk
import com.google.gson.Gson
import com.obelab.library.repace.Externer
import com.obelab.library.repace.data.LTProtocol
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.dialog.InstructionsDialog
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.features.profilesetting.ProfileSettingActivity
import com.obelab.repace.model.LocationModel
import com.obelab.repace.model.LtTestSettingModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.viewModel.LtTestSettingViewModel
import kotlinx.android.synthetic.main.fragment_lt_test_summary.*

class LtTestSummaryFragment : BaseFragment() {

    private val viewModel: LtTestSettingViewModel by viewModels()
    private val TAG = "LtTestSummaryFragment"
    private var userInfo = PrefManager.getProfile()
    lateinit var display: Display

    override fun layoutId(): Int {
        return R.layout.fragment_lt_test_summary
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            observe(resLtTestSetting, ::renderLtTestSetting)
            failure(failure, ::handleFailure)
        }
        display = requireActivity().windowManager.defaultDisplay
        viewModel.getLtTestSetting()
        setUpView(view)
    }

    override fun onResume() {
        super.onResume()
        var protocol: LTProtocol? = null
        userInfo = PrefManager.getProfile()
        if (PrefManager.getProtocol() != LTProtocol()) {
            try {
                protocol = PrefManager.getProtocol()
                if (protocol != null) {
                    tvValueStartSpeed.text = protocol.startSpeed.toString()
                    PrefManager.saveSpeed(protocol.startSpeed.toFloat())
                } else {
                    btnTreadmill.setOnClickListener { }
                    btnTreadmill.setBackgroundResource(R.drawable.btn_disable)
                    tvSmTreadmill.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
                    tvBgTreadmill.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
                    btnOutdoor.setOnClickListener { }
                    btnOutdoor.setBackgroundResource(R.drawable.btn_disable)
                    tvSmOutdoor.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
                    tvBgOutdoor.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
                }
            } catch (e: Exception) {
                Functions.showLog("protocol error-> $e")
                btnTreadmill.setOnClickListener { }
                btnTreadmill.setBackgroundResource(R.drawable.btn_disable)
                tvSmTreadmill.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
                tvBgTreadmill.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
                btnOutdoor.setOnClickListener { }
                btnOutdoor.setBackgroundResource(R.drawable.btn_disable)
                tvSmOutdoor.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
                tvBgOutdoor.setTextColor(ContextCompat.getColor(FacebookSdk.getApplicationContext(), R.color.colorText))
            }
        }
    }

    private fun setUpView(view: View) {
        val mainActivity = activity as MainActivity
        Functions.resetData()
        btnSetProtocol.setOnClickListener {
            mainActivity.goToLtTestSettingFragment()
        }
        btnTreadmill.setOnClickListener {
            var isShowInstructions = PrefManager.getShowInstructions()
            if (!isShowInstructions) {
                var confirmDialog = InstructionsDialog(view.context)
                confirmDialog.onClickButtonNext = {
                    if (userInfo.birthday == null || userInfo.nickname == null || userInfo.gender == null || userInfo.height == null || userInfo.weight == null) {
                        startActivity(ProfileSettingActivity.callingIntent(view.context))
                    } else {
                        startActivity(PreLtTestTreadmillActivity.callingIntent(view.context))
                    }
                }
                confirmDialog.showPopup(display)
            } else {
                startActivity(PreLtTestTreadmillActivity.callingIntent(view.context))
            }
        }
        btnOutdoor.setOnClickListener {
            var isShowInstructions = PrefManager.getShowInstructions()

            if (!isShowInstructions) {
                var confirmDialog = InstructionsDialog(view.context)
                confirmDialog.onClickButtonNext = {
                    if (userInfo.birthday == null || userInfo.nickname == null || userInfo.gender == null || userInfo.height == null || userInfo.weight == null) {
                        startActivity(ProfileSettingActivity.callingIntent(view.context))
                    } else {
                        if (ContextCompat.checkSelfPermission( view.context, Manifest.permission.ACCESS_FINE_LOCATION ) !== PackageManager.PERMISSION_GRANTED ) {
                            ActivityCompat.requestPermissions(activity as MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1 )
                        } else {
                            PrefManager.saveLTTestLocationList(ArrayList<LocationModel>())
                            startActivity(PreLtTestOutdoorActivity.callingIntent(view.context))
                        }
                    }
                }
                confirmDialog.showPopup(display)
            } else {
                if (Constants.IS_TEST) {
                    PrefManager.saveLTTestLocationList(ArrayList<LocationModel>())
                    startActivity(PreLtTestOutdoorActivity.callingIntent(view.context))
                } else {
                    if (ContextCompat.checkSelfPermission( view.context, Manifest.permission.ACCESS_FINE_LOCATION ) !== PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions(activity as MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1 )
                    } else {
                        PrefManager.saveLTTestLocationList(ArrayList<LocationModel>())
                        startActivity(PreLtTestOutdoorActivity.callingIntent(view.context))
                    }
                }
            }
        }
    }

    private fun renderLtTestSetting(resBaseModel: ResBaseModel?) {
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val res: LtTestSettingModel? = gson.fromJson(dataStr, LtTestSettingModel::class.java)
            Functions.showLog("resLtTestSetting: " + res?.let {
                Functions.toJsonString(it)
            })
            var typeProtocol = setUserType(res?.userType)
            var userInfo = PrefManager.getProfile()
            Log.d("userInfo", userInfo.toString())
            var age: Long = 0
            if (userInfo?.birthday != null) {
                age = Functions.setAge(userInfo?.birthday.toString())
            }

            if (typeProtocol != null && userInfo?.birthday != null) {
                var protocol = Externer.getProtocol(
                    typeProtocol, age.toInt(),
                    res?.distance!!, res.number!!, res.time!!
                )
                Functions.showLog(TAG, "Get Protocol || Protocol: ${protocol.protocol}; Start Speed: ${protocol.startSpeed}; Heartrate: ${protocol.heartRate}")
                tvValueStartSpeed.text = protocol.startSpeed.toString()
                PrefManager.saveProtocol(protocol)
            }

        } else {
            resBaseModel?.msg?.let { Functions.showLog(TAG, it.toString()) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Get Lt Test Setting Fail: " + failure.toString())
        hideLoading()
    }

    private fun setUserType(userType: String?): Int? {
        if (userType == Constants.ORDINARY_USER) {
            return 0
        } else if (userType == Constants.RUNNING_USER) {
            return 1
        } else if (userType == Constants.ETC_USER) {
            return 2
        }
        return null
    }
}