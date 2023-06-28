package com.obelab.repace.features.narrationguide

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.google.gson.Gson
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.ResMemberSettingModel
import com.obelab.repace.viewModel.PreferenceViewModel
import kotlinx.android.synthetic.main.activity_narration_guide.*
import kotlinx.android.synthetic.main.header_back.*

class NarrationGuideActivity : BaseActivity() {

    private val viewModel: PreferenceViewModel by viewModels()
    private var memberSetting = ResMemberSettingModel()


    companion object {
        fun callingIntent(context: Context) = Intent(context, NarrationGuideActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_narration_guide)
        with(viewModel) {
            observe(resMemberSetting, ::renderMemberSetting)
            observe(resUpdateMemberSetting, ::renderUpdateMemberSetting)
            failure(failure, ::handleFailure)
        }
        viewModel.getMemberSetting()
        setUpView()
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_narration_guide)

        imvBack.setOnClickListener {
            finish()
        }

        btnNarrationGuide.setOnClickListener {
            if (memberSetting.guide == 1) {
                btnNarrationGuide.setImageResource(R.drawable.ic_switch_off)
                memberSetting.guide = 0
            }else{
                btnNarrationGuide.setImageResource(R.drawable.ic_switch_on)
                memberSetting.guide = 1
            }
            viewModel.putUpdateMemberSetting(memberSetting)
        }
    }

    private fun settingView() {
        if (memberSetting.guide == 1) {
            btnNarrationGuide.setImageResource(R.drawable.ic_switch_on)
        }else{
            btnNarrationGuide.setImageResource(R.drawable.ic_switch_off)
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
                Functions.showLog("resMemberSettingModel: " + memberSetting?.let {
                    Functions.toJsonString(
                        it
                    )
                })
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
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("preferenceError: " + failure.toString())
        hideLoading()
        showToast(getString(R.string.failure_network_connection))
    }

}