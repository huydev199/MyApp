package com.obelab.repace.common.dialog

import android.app.Dialog
import android.content.Context
import android.os.CountDownTimer
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.util.Constants

class ConfirmProgressBar(context: Context) : Dialog(context) {
    init {
        dialog = Dialog(context)
    }

    companion object {
        var dialog: Dialog? = null
    }

    private lateinit var dialogView: View;
    var content = {}
    var progressHorizontal = 0


    fun showPopup(display: Display) {
        dialogView = LayoutInflater.from(context).inflate(R.layout.progress_bar_confirm, null, false)
        dialog?.setCancelable(true)
        dialog?.setContentView(dialogView)
        dialog?.getWindow()?.setBackgroundDrawableResource(R.color.smsp_transparent_color)
        dialog?.setCanceledOnTouchOutside(false)
        val stageWidth: Int = display.getWidth()
        var width = Functions.convertPx(stageWidth, 40)
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setGravity(Gravity.BOTTOM)
        val tvContent = dialogView.findViewById<TextView>(R.id.textView)
        tvContent.text = context.getString(R.string.hold_steady)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        val progressBarHorizontal = dialogView.findViewById<ProgressBar>(R.id.progressBarHorizontal)

        dialog?.show()

        (object : CountDownTimer(Constants.Countdown.TIME_OUT, Constants.Countdown.TIME_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                Functions.showLog("Long  " + Functions.toJsonString(millisUntilFinished))
                if (millisUntilFinished < Constants.Countdown.TIME_OUT - 2000) {
                    tvContent.text = context.getString(R.string.please_wait)
                    progressBarHorizontal.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    if (progressHorizontal < 80) {
                        progressHorizontal = progressHorizontal + 4
                    }
                    progressBarHorizontal.setProgress(progressHorizontal)
                }
            }

            override fun onFinish() {
                dialog?.hide()
            }
        }).start()
    }

    fun hidePopup() {
        dialog?.dismiss()
    }
}