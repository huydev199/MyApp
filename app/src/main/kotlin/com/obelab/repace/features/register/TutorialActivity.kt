package com.obelab.repace.features.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.obelab.repace.R
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.features.pairing.PairingActivity
import kotlinx.android.synthetic.main.activity_tutorial.*
import kotlinx.android.synthetic.main.header_back.*


class TutorialActivity : BaseActivity() {

    companion object {
        fun callingIntent(context: Context) = Intent(context, TutorialActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
        tvTitle.text = getText(R.string.title_additional_info)
        setUpView()
    }

    private fun setUpView() {
        imvBack.setOnClickListener {
            finish()
        }
        btnPairing.setOnClickListener {
            val intent = Intent(this, PairingActivity::class.java)
            intent.putExtra(Constants.type, Constants.TUTORIAL_SCREEN)
            startActivity(intent)
        }
        tvLate.setOnClickListener {
            showLoading()
            startActivity(MainActivity.callingIntent(this))
        }
    }
}