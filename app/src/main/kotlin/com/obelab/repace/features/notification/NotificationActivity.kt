package com.obelab.repace.features.notification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.google.gson.Gson
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.ResMemberSettingModel
import com.obelab.repace.viewModel.PreferenceViewModel
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.android.synthetic.main.header_back.*

class NotificationActivity : BaseActivity() {

    private val viewModel: PreferenceViewModel by viewModels()
    private var memberSetting = ResMemberSettingModel()
    private var type = ""

    companion object {
        fun callingIntent(context: Context) = Intent(context, NotificationActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        with(viewModel) {
            observe(resMemberSetting, ::renderMemberSetting)
            observe(resUpdateMemberSetting, ::renderUpdateMemberSetting)
            failure(failure, ::handleFailure)
        }
        viewModel.getMemberSetting()
        type = intent.getStringExtra(Constants.type).toString()

        setUpView()
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_notification)
        if(type == Constants.me_activity){
            btnSave.visibility = View.GONE
        }
        imvBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            viewModel.putUpdateMemberSetting(memberSetting)
        }

        btnPushNotifications.setOnClickListener {
            if (memberSetting.pushNotice == 1) {
                btnPushNotifications.setImageResource(R.drawable.ic_switch_off)
                memberSetting.pushNotice = 0
            } else {
                btnPushNotifications.setImageResource(R.drawable.ic_switch_on)
                memberSetting.pushNotice = 1
            }
            viewModel.putUpdateMemberSetting(memberSetting)
        }

        btnEmailNotifications.setOnClickListener {
            if (memberSetting.mailNotice == 1) {
                btnEmailNotifications.setImageResource(R.drawable.ic_switch_off)
                memberSetting.mailNotice = 0
            } else {
                btnEmailNotifications.setImageResource(R.drawable.ic_switch_on)
                memberSetting.mailNotice = 1
            }
            viewModel.putUpdateMemberSetting(memberSetting)
        }
    }

    private fun settingView() {
        if (memberSetting.pushNotice == 1) {
            btnPushNotifications.setImageResource(R.drawable.ic_switch_on)
        } else {
            btnPushNotifications.setImageResource(R.drawable.ic_switch_off)
        }

        if (memberSetting.mailNotice == 1) {
            btnEmailNotifications.setImageResource(R.drawable.ic_switch_on)
        } else {
            btnEmailNotifications.setImageResource(R.drawable.ic_switch_off)
        }
    }

    private fun renderMemberSetting(resBaseModel: ResBaseModel?) {
        Functions.showLog("resBaseModel: " + resBaseModel?.let { Functions.toJsonString(it) })
        hideLoading()
        if (resBaseModel?.success == true) {
            try {
                val gson = Gson()
                val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
                memberSetting = gson.fromJson(dataStr, ResMemberSettingModel::class.java)
                Functions.showLog("resMemberSettingModel: " + memberSetting?.let { Functions.toJsonString(it) })
                settingView()
            } catch (e: Exception) {
                Functions.showLog("renderMemberSetting: $e")
            }
        } else {
            resBaseModel?.msg?.let { Functions.showLog(it) }
        }
    }

    private fun renderUpdateMemberSetting(resBaseModel: ResBaseModel?) {
        Functions.showLog("resUpdateMemberSetting: " + resBaseModel?.let { Functions.toJsonString(it) })
        hideLoading()
        if (resBaseModel?.success == true) {
            if (type == Constants.me_activity){
//                finish()
            } else {
                startActivity(MainActivity.callingIntent(this))
            }
        } else {
            resBaseModel?.msg?.let { Functions.showLog(it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("preferenceError: " + failure.toString())
        hideLoading()
        showToast(getString(R.string.failure_network_connection))
    }

}