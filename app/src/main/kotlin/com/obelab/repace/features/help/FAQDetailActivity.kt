package com.obelab.repace.features.help

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.model.FAQModel
import kotlinx.android.synthetic.main.activity_fqa_detail.*
import kotlinx.android.synthetic.main.activity_fqa_detail.btnSendEmail
import kotlinx.android.synthetic.main.activity_fqa_detail.tvEmailSupport
import kotlinx.android.synthetic.main.activity_notice_detail.tvTime
import kotlinx.android.synthetic.main.header_back.*

class FAQDetailActivity: BaseActivity() {

    companion object {
        fun callingIntent(context: Context) = Intent(context, FAQDetailActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fqa_detail)

        setUpView()
    }

    private fun setUpView(){
        tvTitle.text = getText(R.string.title_faq)
        imvBack.setOnClickListener {
            finish()
        }
        val data = intent.getSerializableExtra("data") as? FAQModel
        val mail = intent.getStringExtra("mail")
        tvTitleQuestion.text = data?.question.toString()
        tvTime.text = Functions.formatDateTime(data?.updatedAt.toString())
        tvAnswer.text = data?.answer.toString()
        tvEmailSupport.text = mail
        btnSendEmail.setOnClickListener {
            var intent = Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto",mail,null)
            )
            startActivity(Intent.createChooser(intent,"Send email..."))
        }
    }
}