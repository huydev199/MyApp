package com.obelab.repace.features.exercise

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Display
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import kotlinx.android.synthetic.main.activity_exercise_statistic_rx.*
import kotlinx.android.synthetic.main.header.tvTitle
import kotlinx.android.synthetic.main.header_back.*

class ExerciseStatisticRxActivity : BaseActivity() {

    companion object {
        fun callingIntent(context: Context) = Intent(context, ExerciseStatisticRxActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_statistic_rx)
        setUpView()
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_exercise_statistic)
        imvBack.setOnClickListener {
            finish()
        }

        val display: Display = windowManager!!.defaultDisplay
        val stageWidth: Int = display.getWidth()
        var width = Functions.convertPx(stageWidth, 64)

        ibAll.layoutParams.height = width * 80 / 296

        ibTreadmill.layoutParams.height = width * 80 / 296

        ibOutdoor.layoutParams.height = width * 80 / 296

        ibAll.setOnClickListener{
            val intent = Intent(this, ExerciseStatisticDetailActivity::class.java)
            intent.putExtra(Constants.typeId, Constants.rx_exercise)
            intent.putExtra(Constants.activityId, "")
            startActivity(intent)
        }
        ibTreadmill.setOnClickListener{
            val intent = Intent(this, ExerciseStatisticDetailActivity::class.java)
            intent.putExtra(Constants.typeId, Constants.rx_exercise)
            intent.putExtra(Constants.activityId, Constants.ex_treadmill)
            startActivity(intent)
        }
        ibOutdoor.setOnClickListener {
            val intent = Intent(this, ExerciseStatisticDetailActivity::class.java)
            intent.putExtra(Constants.typeId, Constants.rx_exercise)
            intent.putExtra(Constants.activityId, Constants.ex_outdoor)
            startActivity(intent)
        }
    }
}