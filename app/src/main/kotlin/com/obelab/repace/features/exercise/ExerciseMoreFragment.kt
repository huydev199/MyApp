package com.obelab.repace.features.exercise

import android.os.Bundle
import android.view.View
import com.obelab.repace.R
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.features.main.MainActivity
import kotlinx.android.synthetic.main.fragment_exercise_more.*
import kotlinx.android.synthetic.main.header_back.*


class ExerciseMoreFragment : BaseFragment() {

    override fun layoutId(): Int {
        return R.layout.fragment_exercise_more
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
    }

    private fun setUpView() {
        imvBack.setOnClickListener {
            val mainActivity = activity as MainActivity
            mainActivity.goToExerciseSelectFragment(null)
        }
        if(MainActivity.instance?.isLowMore == true) {
            tvTitle.text = getText(R.string.title_low_exercise)
            imvMore.setBackgroundResource(R.drawable.img_low_exercise)
            tvTitleMore.text = getString(R.string.title_low_more)
            tvTitleContentBold.text = getString(R.string.content_low_bold_more)
            tvTitleContentRegular.text = getString(R.string.content_low_regular_more)
        } else {
            tvTitle.text = getText(R.string.high_intensity)
            imvMore.setBackgroundResource(R.drawable.img_high_exercise)
            tvTitleMore.text = getString(R.string.title_high_more)
            tvTitleContentBold.text = getString(R.string.content_high_bold_more)
            tvTitleContentRegular.text = getString(R.string.content_high_regular_more)
        }
    }
}