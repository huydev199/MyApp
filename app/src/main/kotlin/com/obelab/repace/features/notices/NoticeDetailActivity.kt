package com.obelab.repace.features.notices

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.model.NoticeModel
import kotlinx.android.synthetic.main.activity_notice_detail.*
import kotlinx.android.synthetic.main.header_back.*

class NoticeDetailActivity : BaseActivity() {

    companion object {
        fun callingIntent(context: Context) = Intent(context, NoticeDetailActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_detail)
        setUpView()
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_notices)
        imvBack.setOnClickListener {
            finish()
        }
        val data = intent.getSerializableExtra("data") as? NoticeModel
        Functions.showLog("data detail: " + data?.htmlContent.toString())
        tvTitleNotice.text = data?.title.toString()
        tvTime.text = Functions.formatDateTime(data?.updatedAt.toString())
        tvContent.setText(Html.fromHtml(data?.htmlContent.toString()))
    }

}