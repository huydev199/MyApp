package com.obelab.repace.features.ltTest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.obelab.library.repace.Externer
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import kotlinx.android.synthetic.main.activity_exercise_prescription.*
import kotlinx.android.synthetic.main.header_back.*

class ExercisePrescriptionActivity : BaseActivity() {
    private val TAG = "ExercisePrescriptionActivity"
    companion object {
        var isNavigateFromExercise = false
        fun callingIntent(context: Context) =
            Intent(context, ExercisePrescriptionActivity::class.java)
        fun callingIntentFromExercise(context: Context, isNavigate: Boolean) : Intent {
            isNavigateFromExercise = isNavigate
            return Intent(context, ExercisePrescriptionActivity::class.java)
        }
    }

    private var isActiveRecoveryAbility = false
    private var isActiveWeightLost = false
    private var isActiveMaximalAerobicPerformance = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_prescription)
        setUpView()
    }

    override fun onResume() {
        super.onResume()
        setUpView()
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_exercise_prescription)

        imvBack.setOnClickListener {
            finish()
        }

        var purpose = Externer.getPurpose(
            PrefManager.getProtocol().protocol,
            PrefManager.getSpeed().toDouble()
        )
        Functions.showLog(TAG, "Purpose: ($purpose, L)")
        if (isNavigateFromExercise == false){
            val currentSpeed = PrefManager.getSpeed()
            PrefManager.saveSpeedPurpose(currentSpeed)
        } else{
            purpose = Externer.getPurpose(
                PrefManager.getProtocol().protocol,
                PrefManager.getSpeedPurpose().toDouble()
            )
        }

        // setting value exercise purpose is true when open this screen, In Fragment Exercise,
        // when selected Exericse Button, if value is true - go to Exercise Prescription Detail
        PrefManager.saveExercisePurpose(true)

        for (it in purpose) {
            when (it) {
                1 -> {
                    isActiveRecoveryAbility = true
                }
                2 -> {
                    isActiveWeightLost = true
                }
                3 -> {
                    isActiveMaximalAerobicPerformance = true
                }
            }
        }

        if (isActiveRecoveryAbility == true) {
            enableView(
                flRecoveryAbility,
                R.drawable.bg_recovery_ability,
                tvTitleRecoveryAbility,
                tvContentRecoveryAbility,
                imvRecoveryAbility,
                R.drawable.img_recovery_ability
            )
            flRecoveryAbility.setOnClickListener {
                val intent = Intent(this, ExercisePrescriptionDetailActivity::class.java)
                intent.putExtra(Constants.TYPE_EXERCISE_PRESCRIPTION, Constants.RECOVERY_ABILITY)
                startActivity(intent)
            }
        } else {
            disableView(
                flRecoveryAbility,
                tvTitleRecoveryAbility,
                tvContentRecoveryAbility,
                imvRecoveryAbility,
                R.drawable.img_recovery_ability_disable
            )
        }

        if (isActiveWeightLost == true) {
            enableView(
                flWeightLoss,
                R.drawable.bg_weight_loss,
                tvTitleWeightLoss,
                tvContentWeightLoss,
                imvWeightLoss,
                R.drawable.img_weight_loss
            )
            flWeightLoss.setOnClickListener {
                val intent = Intent(this, ExercisePrescriptionDetailActivity::class.java)
                intent.putExtra(Constants.TYPE_EXERCISE_PRESCRIPTION, Constants.LOW_INTENSITY)
                startActivity(intent)
            }
        } else {
            disableView(
                flWeightLoss,
                tvTitleWeightLoss,
                tvContentWeightLoss,
                imvWeightLoss,
                R.drawable.img_weight_loss_disable
            )
        }

        if (isActiveMaximalAerobicPerformance == true) {
            enableView(
                flMaximalAerobicPerformance,
                R.drawable.bg_maximal_earobic_performance,
                tvTitleMaximalAerobicPerformance,
                tvContentMaximalAerobicPerformance,
                imvMaximalAerobicPerformance,
                R.drawable.img_maximal_aerobic_performance
            )
            flMaximalAerobicPerformance.setOnClickListener {
                val intent = Intent(this, ExercisePrescriptionDetailActivity::class.java)
                intent.putExtra(Constants.TYPE_EXERCISE_PRESCRIPTION, Constants.POLARIZED_TRAINING)
                startActivity(intent)
            }
        } else {
            disableView(
                flMaximalAerobicPerformance,
                tvTitleMaximalAerobicPerformance,
                tvContentMaximalAerobicPerformance,
                imvMaximalAerobicPerformance,
                R.drawable.img_maximal_earobic_performance_disable
            )
        }
    }

    private fun enableView(
        flContainer: FrameLayout,
        bgContainer: Int,
        tvTitle: TextView,
        tvContent: TextView,
        imvBg: ImageView,
        imgBg: Int
    ) {
        flContainer.setBackgroundResource(bgContainer)
        tvTitle.setTextColor(resources.getColor(R.color.colorTextPrimary))
        tvContent.setTextColor(resources.getColor(R.color.colorTextPrimary))
        imvBg.setImageResource(imgBg)
    }

    private fun disableView(
        flContainer: FrameLayout,
        tvTitle: TextView,
        tvContent: TextView,
        imvBg: ImageView,
        imgBg: Int
    ) {
        flContainer.setBackgroundResource(R.drawable.bg_disable)
        tvTitle.setTextColor(resources.getColor(R.color.colorText))
        tvContent.setTextColor(resources.getColor(R.color.colorText))
        imvBg.setImageResource(imgBg)
    }
}