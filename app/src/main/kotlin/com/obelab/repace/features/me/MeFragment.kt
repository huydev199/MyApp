package com.obelab.repace.features.me

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.obelab.repace.BuildConfig
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.help.HelpActivity
import com.obelab.repace.features.narrationguide.NarrationGuideActivity
import com.obelab.repace.features.notices.NoticesActivity
import com.obelab.repace.features.notification.NotificationActivity
import com.obelab.repace.features.preference.PreferenceActivity
import com.obelab.repace.features.profilesetting.ProfileSettingActivity
import com.obelab.repace.features.register.PrivacyPolicyActivity
import com.obelab.repace.features.register.TermsOfUseActitity
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.UserInfoModel
import com.obelab.repace.viewModel.HomeViewModel
import kotlinx.android.synthetic.main.fragment_me.*


class MeFragment : BaseFragment() {

    override fun layoutId(): Int {
        return R.layout.fragment_me
    }

    private val viewModel: HomeViewModel by viewModels()
    private val versionName : String = BuildConfig.VERSION_NAME;

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            observe(resProfileModel, ::renderProfile)
            failure(failure, ::handleFailure)
        }

        viewModel.getProfile()
        setUpView(view)
        swipeContainer.setOnRefreshListener {
            viewModel.getProfile()
            setUpView(view)
            timeOut()
        }
    }

    fun timeOut(){
        Handler().postDelayed(Runnable { // Stop animation (This will be after 3 seconds)
            swipeContainer.isRefreshing = false
        }, 4000)
    }

    private fun setUpView(view: View) {
        viewModel.getProfile()
        tvVersion.text = "${getText(R.string.app_version)} $versionName"
        ivSetting.setOnClickListener {
            startActivity(ProfileSettingActivity.callingIntent(view.context))
        }
        btnNotification.setOnClickListener {
            val intent = Intent(view.context, NotificationActivity::class.java)
            intent.putExtra(Constants.type, Constants.me_activity)
            startActivity(intent)
        }
        btnNarrationGuide.setOnClickListener {
            startActivity(NarrationGuideActivity.callingIntent(view.context))
        }
        btnPreference.setOnClickListener {
            startActivity(PreferenceActivity.callingIntent(view.context, isRegister = false))
        }
        btnNotice.setOnClickListener {
            startActivity(NoticesActivity.callingIntent(view.context))
        }
        btnHelp.setOnClickListener {
            startActivity(HelpActivity.callingIntent(view.context))
        }
        btnTermsOfUs.setOnClickListener {view
            startActivity(TermsOfUseActitity.callingIntent(view.context))
        }
        btnPrivacyPolicy.setOnClickListener {
            startActivity(PrivacyPolicyActivity.callingIntent(view.context))
        }
    }

    override fun onResume() {
        viewModel.getProfile()
        super.onResume()
    }

    private fun renderProfile(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resProfile: UserInfoModel? = gson.fromJson(dataStr, UserInfoModel::class.java)
            Functions.showLog("resProfileUser: " + resProfile?.let { Functions.toJsonString(it) })
            setProfile(resProfile)
        } else {
            resBaseModel?.msg?.let { Functions.showLog(it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Get Profile Error: " + failure.toString())
        hideLoading()
    }

    private fun setProfile(data: UserInfoModel?) {
        tvEmail.text = data?.email.toString()

        if (data?.nickname != null) {
            tvUserName.text = data?.nickname.toString()
        }

        if (data?.birthday != null) {
            tvBirthdate.text = Functions.sqlDateToHumanDate(data?.birthday.toString())
        }

        if (data?.gender != "") {
            val gender = data?.gender.toString()
            tvGender.text = gender.substring(0, 1).toUpperCase() + gender.substring(1)
        }

        if (data?.height != null) {
            if (PrefManager.getUnit() == Constants.UNIT_METRIC){
                tvHeight.text = "${
                    Functions.getHeightUnitValue(data?.height).toString()
                } ${Functions.getHeightUnitName()}"
            } else {
                tvHeight.text = Functions.getImperialHeightValue(data?.height)
            }
        }

        if (data?.weight != null) {
            tvWeight.text = "${
                Functions.getWeightUnitValue(data?.weight).toString()
            } ${Functions.getWeightUnitName()}"
        }
        //Set avatar
        Glide.with(this).load(data?.avatar)
            .placeholder(R.drawable.ic_avatar_default)
            .error(R.drawable.ic_avatar_default)
            .into(civAvatar)
    }
}