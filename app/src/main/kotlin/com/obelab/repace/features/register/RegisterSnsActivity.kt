package com.obelab.repace.features.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.model.RequestSocialLoginModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.ResLoginModel
import com.obelab.repace.viewModel.LoginRegisterSocialViewModel
import kotlinx.android.synthetic.main.activity_register_sns.*
import kotlinx.android.synthetic.main.header_back.*

class RegisterSnsActivity : BaseActivity() {

    private val viewModelSocial: LoginRegisterSocialViewModel by viewModels()

    companion object {
        fun callingIntent(context: Context) = Intent(context, RegisterSnsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_sns)

        with(viewModelSocial) {
            observe(resRegisterSocialModel, ::renderPostRegisterSocial)
            failure(failure, ::handleFailure)
        }
        acceptPolicy()
        customTextView(tvPolicySub)
        tvTitle.text = getText(R.string.title_signup)

        val data = intent.getSerializableExtra("data") as? RequestSocialLoginModel

        btnSignup.setOnClickListener {
            val isChecked = cbPolicy.isChecked
            if (isChecked) {
                data?.let { it1 -> viewModelSocial.postRegisterSocial(it1) }
            }
        }

        imvBack.setOnClickListener {
            finish()
        }
    }

    private fun renderPostRegisterSocial(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resLoginModel: ResLoginModel? = gson.fromJson(dataStr, ResLoginModel::class.java)
            Functions.showLog("resRegisterModel: " + resLoginModel?.let { Functions.toJsonString(it) })
            resLoginModel?.token?.let {
                PrefManager.saveToken(it)
            }
            startActivity(NickNameActivity.callingIntent(this,null, null,true))
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("loginError: " + failure.toString())
        hideLoading()
    }

    private fun acceptPolicy() {
        cbPolicy.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                btnSignup.setBackgroundResource(R.drawable.btn_enable);
                btnSignup.setTextColor(ContextCompat.getColor(this, R.color.colorTextPrimary))
            } else {
                btnSignup.setBackgroundResource(R.drawable.btn_disable);
                btnSignup.setTextColor(ContextCompat.getColor(this, R.color.colorText))
            }
        }
    }

    //Multiple clickable links in textview (Privacy Policy,Terms of Use)
    private fun customTextView(view: TextView) {
        val spanTxt = SpannableStringBuilder(
            "By signing up, you agree to REPACE’s "
        )
        spanTxt.append("Terms of Use")
        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(p0: View) {
                startActivity(TermsOfUseActitity.callingIntent(view.context))
            }
        }, spanTxt.length - "Terms of Use".length, spanTxt.length, 0)
        spanTxt.append(" and ")
        spanTxt.append("Privacy Policy.")
        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(p0: View) {
                startActivity(PrivacyPolicyActivity.callingIntent(view.context))
            }
        }, spanTxt.length - "Privacy Policy.".length, spanTxt.length, 0)
        view.movementMethod = LinkMovementMethod.getInstance()
        view.setText(spanTxt, TextView.BufferType.SPANNABLE)
    }
}