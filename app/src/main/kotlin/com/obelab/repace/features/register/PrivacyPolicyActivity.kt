package com.obelab.repace.features.register

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.View
import android.webkit.WebSettings
import androidx.activity.viewModels
import com.google.gson.Gson
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.model.CommonCodeModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.viewModel.PolicyViewModel
import kotlinx.android.synthetic.main.activity_privacy_policy.*
import kotlinx.android.synthetic.main.activity_privacy_policy.webView
import kotlinx.android.synthetic.main.activity_terms_of_use.*
import kotlinx.android.synthetic.main.header_back.*

class PrivacyPolicyActivity : BaseActivity() {

    companion object {
        fun callingIntent(context: Context) = Intent(context, PrivacyPolicyActivity::class.java)
    }

    private val viewModel: PolicyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        with(viewModel) {
            observe(resPolicyModel, ::renderGetPolicy)
            failure(failure, ::handleFailure)
        }
        viewModel.getPolicy("privacy")
        setUpView()
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_privacy_policy)
        imvBack.setOnClickListener {
            finish()
        }
    }

    private fun renderGetPolicy(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel?.data?.let { Functions.toJsonString(it) }
            val commonCodeModel = gson.fromJson(dataStr, CommonCodeModel::class.java)
//            tvPrivacyPolicy.setText(Html.fromHtml(commonCodeModel.value))
            applyWebView(commonCodeModel.value)

        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }
    private fun applyWebView(data: String) {
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.loadData(data, "text/html", "UTF-8")
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.visibility = View.VISIBLE
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Show common error: " + failure.toString())
        hideLoading()
    }
}