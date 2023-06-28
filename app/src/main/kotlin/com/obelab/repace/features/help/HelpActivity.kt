package com.obelab.repace.features.help

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.obelab.repace.R
import com.obelab.repace.common.adapter.FAQAdapter
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.model.FAQModel
import com.obelab.repace.model.MailSupportModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.ResFAQsModel
import com.obelab.repace.viewModel.FAQsViewModel
import com.obelab.repace.viewModel.MailSupportViewModel
import kotlinx.android.synthetic.main.activity_help.*
import kotlinx.android.synthetic.main.header_back.*

class HelpActivity : BaseActivity() {

    companion object {
        fun callingIntent(context: Context) = Intent(context, HelpActivity::class.java)
    }

    private val viewModel: FAQsViewModel by viewModels()
    private val viewModelMail: MailSupportViewModel by viewModels()
    private lateinit var mRecyclerView: RecyclerView
    private var mailSupport: String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        with(viewModel) {
            observe(resFAQModel, ::renderFAQs)
            failure(failure, ::handleFailure)
        }

        with(viewModelMail) {
            observe(resMailSupportModel, ::renderMailSupport)
            failure(failure, ::handleFailure)
        }
        viewModelMail.getMailSupport()
        viewModel.getFAQs()

        setUpView()
    }

    private fun settingView(listData: ArrayList<FAQModel>) {
        mRecyclerView = findViewById(R.id.rcvListFAQ)
        var layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mRecyclerView.layoutManager = layoutManager
        var faqAdapter = FAQAdapter(listData)
        mRecyclerView.adapter = faqAdapter

        faqAdapter.onClickDetail = {
            val intent = Intent(this, FAQDetailActivity::class.java)
            intent.putExtra("data", it)
            intent.putExtra("mail", mailSupport)
            startActivity(intent)
        }
    }

    private fun renderFAQs(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel?.let { Functions.toJsonString(it) }
            val listData = gson.fromJson(dataStr, ResFAQsModel::class.java)
            settingView(listData.data)
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun renderMailSupport(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel?.data?.let { Functions.toJsonString(it) }
            val resMailSupport = gson.fromJson(dataStr, MailSupportModel::class.java)
            tvEmailSupport.text = resMailSupport.value
            mailSupport = resMailSupport.value
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Show FAQs error: " + failure.toString())
        hideLoading()
    }

    private fun setUpView() {
        imvBack.setOnClickListener {
            finish()
        }
        tvTitle.text = getText(R.string.btn_help)
        btnSendEmail.setOnClickListener {
            var intent = Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto",mailSupport,null)
            )
            startActivity(Intent.createChooser(intent,"Send email..."))
        }
    }
}