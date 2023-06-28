package com.obelab.repace.features.ltTest

import android.animation.ObjectAnimator
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.functional.StatusBar
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.main.MainActivity
import kotlinx.android.synthetic.main.fragment_lt_test.*

class LtTestFragment : BaseFragment() {

    override fun layoutId(): Int {
        return R.layout.fragment_lt_test
    }

    override fun onResume() {
        super.onResume()
        changeConnect()
    }


    fun changeConnect() {
        var connect = MainActivity.instance?.isConnect
        if (Constants.IS_TEST) {
            enableButton()
        } else {
            if (connect == false) {
                disableButton()
                Handler().postDelayed({
                    context?.let { showPopupDisconnectedDevice(it) }
                }, 500)
            } else {
                enableButton()
            }
        }

    }

    private var status = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val display: Display = requireActivity().windowManager.defaultDisplay

        Handler().postDelayed({
            var height = 0
            if (imvBgLtTest != null) {
                height = imvBgLtTest.measuredHeight
            }

            val magrinTop: Float = height.toFloat() + 64.dp
            val objectAnimator = ObjectAnimator.ofFloat(id_frame, "translationY", 0f, magrinTop)
            objectAnimator.duration = 0
            objectAnimator.start()
        }, 100)

        setUpView()

        val stageWidth: Int = display.getWidth()
        var width = Functions.convertPx(stageWidth, 32)
        imvBgLtTest.layoutParams.width = width
        imvBgLtTest.layoutParams.height = (width * 0.89).toInt()
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 70) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return StatusBar.StatusBarHeight(result)
    }

    private fun setUpView() {

        val display: Display = requireActivity().windowManager.defaultDisplay
        val stageHeight: Int = display.getHeight()
        var barHeight = getStatusBarHeight()
        var height = Functions.convertPx(stageHeight, (180 - barHeight))
        id_FrLayout.layoutParams.height = height

        ic_help.setOnClickListener {
            if (status == 0) {
                status = 1
                ic_help.setBackgroundResource(R.drawable.ic_x_circle)
                tvSortText.visibility = View.GONE
                llMoreText.visibility = View.VISIBLE

                val height = imvBgLtTest.measuredHeight
                val magrinTop: Float = height.toFloat() + 64.dp
                val padding: Float = 0f + 25.dp

                val objectAnimator =
                    ObjectAnimator.ofFloat(id_frame, "translationY", magrinTop, padding)
                objectAnimator.duration = 1500
                objectAnimator.start()
            } else {
                status = 0
                ic_help.setBackgroundResource(R.drawable.ic_help_circle)
                tvSortText.visibility = View.VISIBLE
                llMoreText.visibility = View.GONE
                val height = imvBgLtTest.measuredHeight
//
                val magrinTop: Float = height.toFloat() + 64.dp
                val objectAnimator = ObjectAnimator.ofFloat(id_frame, "translationY", 0f, magrinTop)
                objectAnimator.duration = 1500
                objectAnimator.start()
            }
        }
    }

    fun disableButton() {
        btnStart.setBackgroundResource(R.drawable.btn_disable)
        btnStart.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorText))
        btnStart.setOnClickListener { }
    }

    fun enableButton() {
        btnStart.setBackgroundResource(R.drawable.btn_enable)
        btnStart.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
        btnStart.setOnClickListener {
            val mainActivity = activity as MainActivity
            mainActivity.goToLtTestSummaryFragment()
        }
    }

    //Convert to dp
    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun View.setMargins(marginLeft: Int, marginTop: Int, marginRight: Int, marginBottom: Int) {
        val params: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        params.setMargins(marginLeft.dp, marginTop, marginRight.dp, marginBottom.dp)
        this.layoutParams = params
    }
}