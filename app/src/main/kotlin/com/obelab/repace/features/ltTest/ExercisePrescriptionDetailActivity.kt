package com.obelab.repace.features.ltTest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.obelab.library.repace.Externer
import com.obelab.library.repace.data.LTPrescription
import com.obelab.library.repace.data.LTTraining
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.adapter.RecommendationLtTestAdapter
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.model.ExercisePrescriptionModel
import com.obelab.repace.model.ExerciseSessionModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.viewModel.ExercisePrescriptionDetailViewModel
import kotlinx.android.synthetic.main.activity_exercise_prescription_detail.*
import kotlinx.android.synthetic.main.header_back_share.*

class ExercisePrescriptionDetailActivity: BaseActivity() {
    private val TAG = "ExercisePrescriptionDetailActivity"
    companion object {
        fun callingIntent(context: Context) = Intent(context, ExercisePrescriptionDetailActivity::class.java)
    }

    var prescription = ExercisePrescriptionModel.empty
    private val viewModel: ExercisePrescriptionDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_prescription_detail)
        with(viewModel) {
            observe(resPostExercisePrescription, ::renderPostExercisePrescription)
            failure(failure, ::handleFailure)
        }
        setUpView()
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_exercise_prescription)

        imvBack.setOnClickListener {
            finish()
        }

        val exercisePrescription: Int = intent.getIntExtra(Constants.TYPE_EXERCISE_PRESCRIPTION,1)

        val prescriptionData = Externer.getPrescription(PrefManager.getProtocol().protocol,exercisePrescription,PrefManager.getSpeed().toDouble())
        val prescriptionTableData = Externer.getPrescriptionTable(PrefManager.getProtocol().protocol,exercisePrescription,PrefManager.getSpeed().toDouble())

        for(data in prescriptionTableData){
            Functions.showLog(TAG, "PrescriptionTableData[${data.section}] || section: (${data.section}, L), time: (${data.time}, L), speed: (${data.speed}, L), heartrate: (${data.heartRate}, L)")
            Functions.showLog("section: ${data.section}  time: ${data.time}   speed: ${data.speed}   heartrate: ${data.heartRate}")
        }

        // Repair data
        prescription.type = prescriptionData.type
        prescription.leTime = prescriptionData.leTime
        prescription.leMin = prescriptionData.leMin
        prescription.leSpeed = prescriptionData.leSpeed
        prescription.leWeek = prescriptionData.leWeek
        prescription.ptTime = prescriptionData.ptTime
        prescription.ptMin = prescriptionData.ptMin
        prescription.ptSpeed = prescriptionData.ptSpeed
        prescription.ptWeek = prescriptionData.ptWeek
        prescription.ptIndex = prescriptionData.ptIndex

        Functions.showLog(TAG, "PrescriptionData || " +
                "type: (${prescriptionData.type}, L), " +
                "leTime: (${prescriptionData.leTime}, L), " +
                "leMin: (${prescriptionData.leMin}, L), " +
                "leSpeed: (${prescriptionData.leSpeed}, L)"+
                "leWeek: (${prescriptionData.leWeek}, L)"+
                "ptMin: (${prescriptionData.ptMin}, L)"+
                "ptSpeed: (${prescriptionData.ptSpeed}, L)"+
                "ptWeek: (${prescriptionData.ptWeek}, L)"+
                "ptIndex: (${prescriptionData.ptIndex}, L)"
        )

        if (prescriptionData.type == 1) {
            setUpRecoveryAbility(prescriptionData)
        } else {
            setUpPolarizedTraining(prescriptionData, prescriptionTableData)
            val sessionList = ArrayList<ExerciseSessionModel>()
            prescriptionTableData.map {
                sessionList.add(ExerciseSessionModel(it.section,it.time,it.speed,it.heartRate))
            }
            prescription.session = sessionList
        }

        imvShare.setOnClickListener {
            val intent = Intent(this, ExercisePrescriptionDetailShareActivity::class.java)
            intent.putExtra(Constants.TYPE_EXERCISE_PRESCRIPTION, prescriptionData.type)
            startActivity(intent)
        }

        btnConfirm.setOnClickListener {
            prescription.session = prescription.session.sortedBy { it.session }
            PrefManager.saveExercisePrescription(prescription)
            viewModel.postExercisePrescriptionRequest(prescription)
            showLoading()
        }
    }

    private fun settingView(listData:  List<LTTraining>) {
        var layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rcvRecommendationLtTest.layoutManager = layoutManager
        var recomendationAdapter = RecommendationLtTestAdapter(listData)
        rcvRecommendationLtTest.adapter = recomendationAdapter
    }

    private fun setUpRecoveryAbility(prescriptionData: LTPrescription){
        llPolarizedTraining.visibility = View.GONE
        llStepsPolarizedTraining.visibility = View.GONE
        tvTitleLtTest.setText(R.string.title_low_exercise)
        tvDescription.setText(R.string.text_low_intensity)
        tvDescriptionContinue.setText(R.string.text_low_intensity_continue)
        tvLeTimeTop.text = prescriptionData.leTime.toString()
        tvLeMinTop.text = prescriptionData.leMin.toString()
        tvSpeedTop.text = prescriptionData.leSpeed.toString()
        tvLeTime.text = prescriptionData.leTime.toString()
        tvSpeed.text = prescriptionData.leSpeed.toString()
        tvLeMin.text = (Functions.getDouble1Decimal((prescriptionData.leMin/60).toDouble())).toString()
        tvLeWeek.text = prescriptionData.leWeek.toString()
        tvLeEndWeek.text = prescriptionData.leWeek.toString()
    }

    private fun setUpPolarizedTraining(prescriptionData: LTPrescription, prescriptionTableData: List<LTTraining>){
        llLowIntensity.visibility = View.GONE
        llStepsLowIntensity.visibility = View.GONE
        tvTitleLtTest.setText(R.string.polarized_training)
        tvDescription.setText(R.string.text_polarized_training)
        tvDescriptionContinue.setText(R.string.text_polarized_training_continue)
        tvLeWeekPolarized.text = prescriptionData.ptWeek.toString()
        tvLeEndWeekPolarized.text = prescriptionData.ptWeek.toString()
        settingView(prescriptionTableData)
    }

    private fun renderPostExercisePrescription(resBaseModel: ResBaseModel?) {
        hideLoading()
        Functions.showLog("renderPostExercisePrescription: " + resBaseModel?.let { Functions.toJsonString(it) }.toString())
        if(resBaseModel!!.success == true){
            // when click button confirm, reset value to false
            PrefManager.saveExercisePurpose(false)
            startActivity(MainActivity.callingIntent(this,true))
        } else {
            showToast(resBaseModel?.msg.toString())
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("PostExercisePrescriptionError: " + failure.toString())
        hideLoading()
    }
}