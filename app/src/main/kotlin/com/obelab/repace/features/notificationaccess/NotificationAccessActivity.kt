package com.obelab.repace.features.notificationaccess

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.features.notification.NotificationActivity
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.viewModel.PreferenceViewModel
import kotlinx.android.synthetic.main.activity_notification_access.*
import kotlinx.android.synthetic.main.header_back.*

class NotificationAccessActivity : BaseActivity() {

    private val viewModel: PreferenceViewModel by viewModels()
    companion object {
        fun callingIntent(context: Context) = Intent(context, NotificationAccessActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_access)

        with(viewModel) {
            observe(resUpdateMemberSetting, ::renderUpdateMemberSetting)
            failure(failure, ::handleFailure)
        }
        setUpView()
    }

    private fun setUpView(){
        tvTitle.text = getText(R.string.title_notification_access)

        imvBack.setOnClickListener {
            finish()
        }
        btnAllowAccess.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            intent.putExtra(Constants.type, Constants.notification_access)
            startActivity(intent)
        }
        tvLate.setOnClickListener {
            startActivity(MainActivity.callingIntent(this))
        }
    }

    private fun renderUpdateMemberSetting(resBaseModel: ResBaseModel?) {
        Functions.showLog("resUpdateMemberSetting: " + resBaseModel?.let { Functions.toJsonString(it) })
        hideLoading()
        if (resBaseModel?.success == true) {
            startActivity(MainActivity.callingIntent(this))
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