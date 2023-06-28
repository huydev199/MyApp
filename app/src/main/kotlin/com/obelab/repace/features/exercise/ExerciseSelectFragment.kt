package com.obelab.repace.features.exercise

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.ExerciseHelper
import com.obelab.repace.features.main.MainActivity
import kotlinx.android.synthetic.main.fragment_exercise_select.*
import kotlinx.android.synthetic.main.header_back.*

class ExerciseSelectFragment : BaseFragment() {

    override fun layoutId(): Int {
        return R.layout.fragment_exercise_select
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView(view)
    }

    private fun setUpView(view: View) {
        val mainActivity = activity as MainActivity
        tvTitle.text = getText(R.string.rx_exercise)

        imvBack.setOnClickListener {
            mainActivity.goToExerciseCalendarFragment(null)
        }

        btnMoreExerciseLow.setOnClickListener {
            mainActivity.goToExerciseMoreFragment(true)
        }

        btnMoreExerciseHigh.setOnClickListener {
            mainActivity.goToExerciseMoreFragment(false)
        }
        val dataTodayExercise = ExerciseHelper.getTodayExercise()
        if (dataTodayExercise.type == 1) {
            enableLowLayout()
            disableHighLayout()
        } else if (dataTodayExercise.type == 2) {
            disableLowLayout()
            enableHighLayout()
        }
    }

    private fun enableLowLayout() {
        layout_exercise_low.isEnabled = true
        layout_exercise_low.setOnClickListener {
            PrefManager.saveIntensityId(Constants.low_intensity)
            MainActivity.instance?.goToExerciseGuideFragment(Constants.LOW_INTENSITY_EXERCISE)
        }
        tvTitleLow.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextPrimary
            )
        )
        tvContentLow.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextPrimary
            )
        )
        btnMoreExerciseLow.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextPrimary
            )
        )
        layout_exercise_low.setBackgroundResource(R.drawable.view_enable)
        img_low.setBackgroundResource(R.drawable.img_low_exercise)
    }

    private fun disableLowLayout() {
        layout_exercise_low.isEnabled = false
        tvTitleLow.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorText
            )
        )
        tvContentLow.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorText
            )
        )
        btnMoreExerciseLow.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorText
            )
        )
        layout_exercise_low.setBackgroundResource(R.drawable.bg_disable)
        img_low.setBackgroundResource(R.drawable.img_low_exercise_disable)
    }

    private fun enableHighLayout() {
        layout_exercise_hight.isEnabled = true
        layout_exercise_hight.setOnClickListener {
            PrefManager.saveIntensityId(Constants.high_intensity)
            MainActivity.instance?.goToExerciseGuideFragment(Constants.HIGHT_INTENSITY_EXERCISE)
        }
        tvTitleHigh.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextPrimary
            )
        )
        tvContentHigh.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextPrimary
            )
        )
        btnMoreExerciseHigh.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorTextPrimary
            )
        )
        layout_exercise_hight.setBackgroundResource(R.drawable.view_enable_high)
        img_hight.setBackgroundResource(R.drawable.img_high_exercise)
    }

    private fun disableHighLayout(){
        layout_exercise_hight.isEnabled = false
        tvTitleHigh.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorText
            )
        )
        tvContentHigh.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorText
            )
        )
        btnMoreExerciseHigh.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorText
            )
        )
        layout_exercise_hight.setBackgroundResource(R.drawable.bg_disable)
        img_hight.setBackgroundResource(R.drawable.img_high_exercise_disable)
    }
}