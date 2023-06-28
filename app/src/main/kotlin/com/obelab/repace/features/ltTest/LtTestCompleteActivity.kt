package com.obelab.repace.features.ltTest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import kotlinx.android.synthetic.main.activity_lt_test_complete.*
import kotlinx.android.synthetic.main.header.*
import java.util.*

class LtTestCompleteActivity : BaseActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    companion object {
        fun callingIntent(context: Context) = Intent(context, LtTestCompleteActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lt_test_complete)
        tts = TextToSpeech(this, this)
        setUpView()
    }

    private fun setUpView(){
        tvTitle.text = getText(R.string.lt_test)
        if (PrefManager.getIsSpeakerTurnOn()) {
            Handler().postDelayed({
                tts!!.speak(
                    getString(R.string.tts_lt_test_complete),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            }, 500)
        }
        val type: String? = intent.getStringExtra(Constants.type)

        btnLtTestResult.setOnClickListener {
            if (type==Constants.LT_TEST_OUTDOOR){
                val intent = Intent(this, LtTestOutdoorResultActivity::class.java)
                intent.putExtra(Constants.moveFragment,Constants.LT_TEST_OUTDOOR)
                startActivity(intent)
            } else{
                val intent = Intent(this, LtTestTreadmillResultActivity::class.java)
                intent.putExtra(Constants.moveFragment,Constants.LT_TEST_TREADMILL)
                startActivity(intent)
            }
        }
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.ENGLISH)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Functions.showLog("Text to speech -> Device not support text to speech")
                PrefManager.saveIsSpeakerTurnOn(false)
            }
        } else {
            PrefManager.saveIsSpeakerTurnOn(false)
            Functions.showLog("Text to speech -> false")
        }
    }
}