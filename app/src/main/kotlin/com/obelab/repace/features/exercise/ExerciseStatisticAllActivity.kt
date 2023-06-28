package com.obelab.repace.features.exercise

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Display
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import kotlinx.android.synthetic.main.activity_exercise_statistic_all.*
import kotlinx.android.synthetic.main.header.tvTitle
import kotlinx.android.synthetic.main.header_back.*

class ExerciseStatisticAllActivity : BaseActivity() {

    companion object {
        fun callingIntent(context: Context) = Intent(context, ExerciseStatisticAllActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_statistic_all)
        setUpView()
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_exercise_statistic)
        val display: Display = windowManager!!.defaultDisplay
        val stageWidth: Int = display.getWidth()
        var width = Functions.convertPx(stageWidth, 64)

        ivRx.layoutParams.height = width * 142 / 246

        ivFree.layoutParams.height = width * 142 / 246

        imvBack.setOnClickListener {
            finish()
        }
        ivRx.setOnClickListener {
            startActivity(ExerciseStatisticRxActivity.callingIntent(this))
        }
        ivFree.setOnClickListener {
            startActivity(ExerciseStatisticFreeActivity.callingIntent(this))
        }
    }
}