package com.obelab.repace.features.preference

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import com.obelab.repace.features.register.AdditionalInfoActivity
import com.obelab.repace.features.welcome.SplashActivity
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.ResMemberSettingModel
import com.obelab.repace.viewModel.PreferenceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_preference.*
import kotlinx.android.synthetic.main.header_back.*


@AndroidEntryPoint
class PreferenceActivity : BaseActivity() {

    companion object {
        const val KEY_IS_REGISTER = "KEY_IS_REGISTER"
        fun callingIntent(
            context: Context,
            isRegister: Boolean
        ): Intent {
            var intent = Intent(context, PreferenceActivity::class.java)
            intent.putExtra(KEY_IS_REGISTER, isRegister)
            return intent
        }
    }

    private var isRegister: Boolean = false


    private val viewModel: PreferenceViewModel by viewModels()

    private val languageValueList = mutableListOf(Constants.LANGUAGE_EN, Constants.LANGUAGE_KO)
    private val unitValueList = mutableListOf(Constants.UNIT_METRIC, Constants.UNIT_IMPERIAL)
    private val profileValueList = mutableListOf(
        Constants.PROFILE_DISCLOSURE_ALL,
        Constants.PROFILE_DISCLOSURE_FRIEND,
        Constants.PROFILE_DISCLOSURE_NONE
    )

    private var memberSetting = ResMemberSettingModel()
    private var isUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        setUpView()
        getDataFromIntent()
        with(viewModel) {
            observe(resMemberSetting, ::renderMemberSetting)
            observe(resUpdateMemberSetting, ::renderUpdateMemberSetting)
            failure(failure, ::handleFailure)
        }
        viewModel.getMemberSetting()
        /** Fix bug #74, disable loading for this screen */
        //showLoading()
    }

    private fun getDataFromIntent() {
        isRegister = intent.getBooleanExtra(KEY_IS_REGISTER, false)
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.btn_preference)
        imvBack.setOnClickListener {
            finish()
        }

        ll_language_setting.setOnClickListener {
            if (ll_dropdown_language.visibility == View.VISIBLE) {
                dropDownClose(0)
                ll_language_setting.setBackgroundResource(R.drawable.bg_spinner)
                imv_language_setting.setImageResource(R.drawable.ic_dropdown)
            } else {
                dropDownOpen(0)
                ll_language_setting.setBackgroundResource(R.drawable.bg_spinner_select)
                imv_language_setting.setImageResource(R.drawable.ic_up)
            }
        }

        ll_unit.setOnClickListener {
            if (ll_dropdown_unit.visibility == View.VISIBLE) {
                dropDownClose(1)
                ll_unit.setBackgroundResource(R.drawable.bg_spinner)
                imv_unit.setImageResource(R.drawable.ic_dropdown)
            } else {
                dropDownOpen(1)
                ll_unit.setBackgroundResource(R.drawable.bg_spinner_select)
                imv_unit.setImageResource(R.drawable.ic_up)
            }
        }

        ll_profile.setOnClickListener {
            if (ll_dropdown_profile.visibility == View.VISIBLE) {
                dropDownClose(2)
                ll_profile.setBackgroundResource(R.drawable.bg_spinner)
                imv_profile.setImageResource(R.drawable.ic_dropdown)
            } else {
                dropDownOpen(2)
                ll_profile.setBackgroundResource(R.drawable.bg_spinner_select)
                imv_profile.setImageResource(R.drawable.ic_up)
            }
        }


        tvEnglish.setOnClickListener {
            memberSetting.language = languageValueList[0]
            viewModel.putUpdateMemberSetting(memberSetting)
            settingViewLanguage(0)
            deactiveButton(0)
            restartApp()
            closeAllDropDown()
        }

        tvKorea.setOnClickListener {
            memberSetting.language = languageValueList[1]
            viewModel.putUpdateMemberSetting(memberSetting)
            settingViewLanguage(1)
            deactiveButton(0)
            restartApp()
            closeAllDropDown()
        }

        tvMetric.setOnClickListener {
            memberSetting.unit = unitValueList[0]
            viewModel.putUpdateMemberSetting(memberSetting)
            settingViewUnit(0)
            deactiveButton(1)
            closeAllDropDown()
        }

        tvImperial.setOnClickListener {
            memberSetting.unit = unitValueList[1]
            viewModel.putUpdateMemberSetting(memberSetting)
            settingViewUnit(1)
            deactiveButton(1)
            closeAllDropDown()
        }

        tvAll.setOnClickListener {
            memberSetting.profileDisclosure = profileValueList[0]
            viewModel.putUpdateMemberSetting(memberSetting)
            settingViewProfile(0)
            deactiveButton(2)
            closeAllDropDown()
        }

        tvFriends.setOnClickListener {
            memberSetting.profileDisclosure = profileValueList[1]
            viewModel.putUpdateMemberSetting(memberSetting)
            settingViewProfile(1)
            deactiveButton(2)
            closeAllDropDown()
        }

        tvNone.setOnClickListener {
            memberSetting.profileDisclosure = profileValueList[2]
            viewModel.putUpdateMemberSetting(memberSetting)
            settingViewProfile(2)
            deactiveButton(2)
            closeAllDropDown()
        }

    }

    private fun setUpMemberSetting() {
        for (i in 0 until languageValueList.size) {
            if (languageValueList[i] == memberSetting.language) {
                settingViewLanguage(i)
            }
        }
        for (i in 0 until unitValueList.size) {
            if (unitValueList[i] == memberSetting.unit) {
                settingViewUnit(i)
            }
        }
        for (i in 0 until profileValueList.size) {
            if (profileValueList[i] == memberSetting.profileDisclosure) {
                settingViewProfile(i)
            }
        }
    }

    fun settingViewLanguage(i: Int) {
        if (i == 0) {
            tvValueLanguage.text = getString(R.string.language_en)
            llEnglish.setBackgroundResource(R.drawable.bg_spinner_selected_top)
            llKorea.setBackgroundResource(R.drawable.smsp_transparent_color)
        } else {
            tvValueLanguage.text = getString(R.string.language_ko)
            llKorea.setBackgroundResource(R.drawable.bg_spinner_selected_bottom)
            llEnglish.setBackgroundResource(R.drawable.smsp_transparent_color)
        }
    }

    fun settingViewUnit(i: Int) {
        if (i == 0) {
            tvValueUnit.text = getString(R.string.unit_metric)
            llMetric.setBackgroundResource(R.drawable.bg_spinner_selected_top)
            llImperial.setBackgroundResource(R.drawable.smsp_transparent_color)
        } else {
            tvValueUnit.text = getString(R.string.unit_imperial)
            llImperial.setBackgroundResource(R.drawable.bg_spinner_selected_bottom)
            llMetric.setBackgroundResource(R.drawable.smsp_transparent_color)
        }
    }

    fun settingViewProfile(i: Int) {
        if (i == 0) {
            tvValueProfile.text = getString(R.string.profile_disclosure_all)
            llAll.setBackgroundResource(R.drawable.bg_spinner_selected_top)
            llFriends.setBackgroundResource(R.drawable.smsp_transparent_color)
            llNone.setBackgroundResource(R.drawable.smsp_transparent_color)
        } else if (i == 1) {
            tvValueProfile.text = getString(R.string.profile_disclosure_friends)
            llFriends.setBackgroundResource(R.drawable.bg_spinner_selected_center)
            llAll.setBackgroundResource(R.drawable.smsp_transparent_color)
            llNone.setBackgroundResource(R.drawable.smsp_transparent_color)
        } else {
            tvValueProfile.text = getString(R.string.profile_disclosure_none)
            llNone.setBackgroundResource(R.drawable.bg_spinner_selected_bottom)
            llAll.setBackgroundResource(R.drawable.smsp_transparent_color)
            llFriends.setBackgroundResource(R.drawable.smsp_transparent_color)
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
                setUpMemberSetting()
                isUpdate = true
            } catch (e: Exception) {
                Functions.showLog("renderMemberSetting: $e")
            }
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun dropDownClose(type: Int) {
        if (type == 0) {
            ll_dropdown_language.visibility = View.GONE
        } else if (type == 1) {
            ll_dropdown_unit.visibility = View.GONE
        } else {
            ll_dropdown_profile.visibility = View.GONE
        }
    }

    private fun closeAllDropDown() {
        ll_dropdown_language.visibility = View.GONE
        ll_dropdown_profile.visibility = View.GONE
        ll_dropdown_unit.visibility = View.GONE
    }

    private fun dropDownOpen(type: Int) {
        if (type == 0) {
            Handler().postDelayed({
                ll_dropdown_language.visibility = View.VISIBLE
            }, 120)
            deactiveButton(1)
            deactiveButton(2)
            ll_dropdown_profile.visibility = View.GONE
            ll_dropdown_unit.visibility = View.GONE
        } else if (type == 1) {
            Handler().postDelayed({
                ll_dropdown_unit.visibility = View.VISIBLE
            }, 120)
            deactiveButton(0)
            deactiveButton(2)
            ll_dropdown_language.visibility = View.GONE
            ll_dropdown_profile.visibility = View.GONE
        } else {
            Handler().postDelayed({
                ll_dropdown_profile.visibility = View.VISIBLE
            }, 120)
            deactiveButton(0)
            deactiveButton(1)
            ll_dropdown_language.visibility = View.GONE
            ll_dropdown_unit.visibility = View.GONE
        }
    }

    private fun deactiveButton(type: Int) {
        if (type == 0) {
            ll_language_setting.setBackgroundResource(R.drawable.bg_spinner)
            imv_language_setting.setImageResource(R.drawable.ic_dropdown)
        } else if (type == 1) {
            ll_unit.setBackgroundResource(R.drawable.bg_spinner)
            imv_unit.setImageResource(R.drawable.ic_dropdown)
        } else {
            ll_profile.setBackgroundResource(R.drawable.bg_spinner)
            imv_profile.setImageResource(R.drawable.ic_dropdown)
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

    private fun restartApp() {
        if (!isRegister){
            val intent = Intent(this, SplashActivity::class.java)
            finishAffinity()
            Handler().postDelayed({
                this.startActivity(intent)
            }, 100)
        }
    }
}