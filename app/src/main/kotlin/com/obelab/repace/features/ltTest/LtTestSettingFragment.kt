package com.obelab.repace.features.ltTest

import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.view.Display
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.obelab.library.repace.Externer
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.model.LtTestSettingModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.viewModel.LtTestSettingViewModel
import kotlinx.android.synthetic.main.fragment_lt_test_setting.*
import kotlinx.android.synthetic.main.header_back.*

class LtTestSettingFragment : BaseFragment() {
    private val TAG = "LtTestSettingFragment"
    override fun layoutId(): Int {
        return R.layout.fragment_lt_test_setting
    }

    private val viewModel: LtTestSettingViewModel by viewModels()
    private lateinit var userType: String
    private var showNoteTime = false
    private var showNoteDistance = false
    private var showNoteNumber = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            observe(resLtTestSetting, ::renderLtTestSetting)
            observe(resPutLtTestSetting, ::renderPutLtTestSetting)
            failure(failure, ::handleFailure)
        }

        viewModel.getLtTestSetting()
        val display: Display = requireActivity().windowManager.defaultDisplay
        val stageWidth: Int = display.getWidth()
        btnOrdinaryPerson.layoutParams.width = (stageWidth - 60.dp) / 3
        btnSportsmanRunning.layoutParams.width = (stageWidth - 60.dp) / 3
        btnSportsmanEtc.layoutParams.width = (stageWidth - 60.dp) / 3
        setUpView()
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_LT_Test_Setting)

        imvBack.setOnClickListener {
            val mainActivity = activity as MainActivity
            mainActivity.goToLtTestSummaryFragment()
        }

        btnOrdinaryPerson.setOnClickListener {
            btnOrdinaryEnable()
            hideNote()
        }

        btnSportsmanRunning.setOnClickListener {
            btnRunningEnable()
            hideNote()
        }

        btnSportsmanEtc.setOnClickListener {
            btnEtcEnable()
            hideNote()
        }

        ic_help_time.setOnClickListener {
            if (showNoteTime == true) {
                showNoteTime = false
                cvShowNoteTime.visibility = View.INVISIBLE
            } else {
                showNoteTime = true
                cvShowNoteTime.visibility = View.VISIBLE
            }
        }

        ic_help_distance.setOnClickListener {
            if (showNoteDistance == true) {
                showNoteDistance = false
                cvShowNoteDistance.visibility = View.INVISIBLE
            } else {
                showNoteDistance = true
                cvShowNoteDistance.visibility = View.VISIBLE
            }
        }

        ic_help_number.setOnClickListener {
            if (showNoteNumber == true) {
                showNoteNumber = false
                cvShowNoteNumber.visibility = View.INVISIBLE
            } else {
                showNoteNumber = true
                cvShowNoteNumber.visibility = View.VISIBLE
            }
        }

        llParent.setOnClickListener {
            hideNote()
        }

        var userInfo = PrefManager.getProfile()
        var age = 0

        if(userInfo.birthday!=null){
            age = Functions.setAge(userInfo.birthday.toString()).toInt()
        }

        btnSave.setOnClickListener {
            val time = Math.round((seekBarTime.progress.toDouble() / 2) * 1000.0) / 1000.0
            val distance = seekBarDistance.progress
            val number = seekBarNumber.progress
            var typeProtocol = setUserType(userType)

            if (typeProtocol != null && userInfo.birthday != null) {
                var protocol = Externer.getProtocol(typeProtocol, age, distance, number, time)
                PrefManager.saveProtocol(protocol)
                Functions.showLog(TAG,"Get Protocol || Protocol: ${protocol.protocol}; Start Speed: ${protocol.startSpeed}; Heartrate: ${protocol.heartRate}")
            }

            viewModel.putLtTestSetting(
                LtTestSettingModel(
                    null,
                    null,
                    null,
                    userType,
                    time,
                    distance,
                    number
                )
            )
        }
    }

    private fun settingSeekbar(
        seekBar: SeekBar,
        min: Int,
        max: Int,
        process: Any?,
        tvProcess: TextView,
        type: Int
    ) {
        //Type 0 use to seekbar have type Double
        //Type 1 use to seekbar have type Int

        //Setup defaul value of seekbar
        Handler().postDelayed({
            if (type == 0) {
                seekBar.max = max * 2
                seekBar.setProgress((process.toString().toDouble() * 2).toInt());
            } else {
                seekBar.max = max
                seekBar.setProgress(process.toString().toInt());
            }
        }, 100)

        seekBar.setOnClickListener {
            hideNote()
        }

        // seekbar change value
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                hideNote()
                //Get the thumb bound and get its center value
                val x = p0?.getThumb()?.getBounds()?.centerX();

                if (type == 0) {
                    var process = (Math.round((p1.toDouble() / 2) * 1000.0) / 1000.0).toString()
                    if (p1 == max * 2) {
                        tvProcess.setText((p1 / 2).toString() + "+");
                    } else if (process.contains(".0")) {
                        tvProcess.setText((p1 / 2).toString());
                    } else {
                        tvProcess.setText("" + process);
                    }
                } else {
                    if (p1 == max) {
                        tvProcess.setText((p1).toString() + "+");
                    } else {
                        tvProcess.setText(p1.toString());
                    }
                }
                tvProcess.x = x!!.toFloat() - (tvProcess.width) / 2 + 16.dp
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                hideNote()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
    }

    private fun setUserType(userType: String): Int? {
        if (userType == Constants.ORDINARY_USER) {
            return 0
        } else if (userType == Constants.RUNNING_USER) {
            return 1
        } else if (userType == Constants.ETC_USER) {
            return 2
        }
        return null
    }

    private fun hideNote(){
        cvShowNoteNumber.visibility = View.INVISIBLE
        cvShowNoteDistance.visibility = View.INVISIBLE
        cvShowNoteTime.visibility = View.INVISIBLE
    }

    private fun settingView(resSettingLtTest: LtTestSettingModel?) {
        settingSeekbar(seekBarTime, 0, 5, resSettingLtTest!!.time, tvSeekbarTime, 0)
        settingSeekbar(seekBarDistance, 0, 12, resSettingLtTest!!.distance, tvSeekbarDistance, 1)
        settingSeekbar(seekBarNumber, 0, 10, resSettingLtTest!!.number, tvSeekbarNumber, 1)
        val resUserType = resSettingLtTest.userType
        userType = resUserType!!
        if (resUserType == Constants.ORDINARY_USER) {
            btnOrdinaryEnable()
        } else if (resUserType == Constants.RUNNING_USER) {
            btnRunningEnable()
        } else {
            btnEtcEnable()
        }

    }

    private fun renderLtTestSetting(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resSettingLtTest: LtTestSettingModel? =
                gson.fromJson(dataStr, LtTestSettingModel::class.java)
            Functions.showLog("resLtTestSetting: " + resSettingLtTest?.let {
                Functions.toJsonString(
                    it
                )
            })
            settingView(resSettingLtTest)
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun renderPutLtTestSetting(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val mainActivity = activity as MainActivity
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resSettingLtTest: LtTestSettingModel? =
                gson.fromJson(dataStr, LtTestSettingModel::class.java)
            Functions.showLog("resPutLtTestSetting: " + resSettingLtTest?.let {
                Functions.toJsonString(
                    it
                )
            })
            mainActivity.goToLtTestSummaryFragment()
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Get Lt Test Setting Fail: " + failure.toString())
        hideLoading()
    }

    private fun btnOrdinaryEnable() {
        btnOrdinaryPerson.setBackgroundResource(R.drawable.btn_enable)
        btnOrdinaryPerson.setTextColor(resources.getColor(R.color.colorTextPrimary))
        btnSportsmanRunning.setBackgroundResource(R.drawable.btn_disable)
        btnSportsmanRunning.setTextColor(resources.getColor(R.color.colorText))
        btnSportsmanEtc.setBackgroundResource(R.drawable.btn_disable)
        btnSportsmanEtc.setTextColor(resources.getColor(R.color.colorText))
        userType = Constants.ORDINARY_USER
    }

    private fun btnRunningEnable() {
        btnSportsmanRunning.setBackgroundResource(R.drawable.btn_enable)
        btnSportsmanRunning.setTextColor(resources.getColor(R.color.colorTextPrimary))
        btnOrdinaryPerson.setBackgroundResource(R.drawable.btn_disable)
        btnOrdinaryPerson.setTextColor(resources.getColor(R.color.colorText))
        btnSportsmanEtc.setBackgroundResource(R.drawable.btn_disable)
        btnSportsmanEtc.setTextColor(resources.getColor(R.color.colorText))
        userType = Constants.RUNNING_USER
    }

    private fun btnEtcEnable() {
        btnSportsmanEtc.setBackgroundResource(R.drawable.btn_enable)
        btnSportsmanEtc.setTextColor(resources.getColor(R.color.colorTextPrimary))
        btnOrdinaryPerson.setBackgroundResource(R.drawable.btn_disable)
        btnOrdinaryPerson.setTextColor(resources.getColor(R.color.colorText))
        btnSportsmanRunning.setBackgroundResource(R.drawable.btn_disable)
        btnSportsmanRunning.setTextColor(resources.getColor(R.color.colorText))
        userType = Constants.ETC_USER
    }

    //Convert to dp
    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}