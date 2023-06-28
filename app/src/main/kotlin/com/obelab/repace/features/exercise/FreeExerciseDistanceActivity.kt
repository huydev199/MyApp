package com.obelab.repace.features.exercise

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.google.gson.Gson
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.ResDistanceModel
import com.obelab.repace.viewModel.DistanceViewModel
import kotlinx.android.synthetic.main.activity_free_exercise_distance.*
import kotlinx.android.synthetic.main.header_back_bell.*

class FreeExerciseDistanceActivity : BaseActivity() {

    private val viewModelDistance: DistanceViewModel by viewModels()
    private lateinit var distance: String
    private lateinit var listDistance: Array<String>

    companion object {
        fun callingIntent(context: Context) = Intent(
            context,
            FreeExerciseDistanceActivity::class.java
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_free_exercise_distance)
        with(viewModelDistance) {
            observe(resDistance, ::renderDistance)
            failure(failure, ::handleFailure)
        }
        viewModelDistance.getDistance()
        setUpView()
    }

    private fun setUpView() {
        imvBack.visibility = View.INVISIBLE
        tvTitle.text = getText(R.string.free_exercise)

        if (PrefManager.getProfile().gender == Constants.GENDER_MALE) {
            imvTreadmill.setImageResource(R.drawable.img_treadmill_man)
        } else {
            imvTreadmill.setImageResource(R.drawable.img_treadmill_woman)
        }
    }

    private fun renderDistance(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel?.data?.let { Functions.toJsonString(it) }
            val resDistanceModel: ResDistanceModel? =
                gson.fromJson(dataStr, ResDistanceModel::class.java)
            if (resDistanceModel != null) {
                listDistance = resDistanceModel.distanceList
                val defaultDistance = listDistance[listDistance.size/2]
                distance = defaultDistance
                if (listDistance.size > 0) {
                    number_picker.setMinValue(0);
                    number_picker.setMaxValue(listDistance.size - 1)
                    number_picker.setDisplayedValues(null);
                    if (defaultDistance != null) {
                        number_picker.setValue(listDistance.indexOf(defaultDistance))
                    }
                    number_picker.setDisplayedValues(listDistance);
                    number_picker.wrapSelectorWheel = true
                    number_picker.setOnValueChangedListener { picker, oldVal, newVal ->
                        distance = listDistance[newVal]
                    }
                }
            }
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
        btnFinish.setOnClickListener {
            PrefManager.saveDistance(distance)
            startActivity(ExerciseCompleteActivity.callingIntent(this))
            finish()
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Get Profile Error: " + failure.toString())
        hideLoading()
    }

}