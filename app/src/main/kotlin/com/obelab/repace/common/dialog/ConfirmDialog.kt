package com.obelab.repace.common.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions

class ConfirmDialog(context: Context) : Dialog(context) {
    init {
        dialog = Dialog(context, R.style.FullWidthDialog)
    }

    companion object {
        var dialog: Dialog? = null
    }

    private lateinit var dialogView: View;
    var title = ""
    var content = ""
    var textButtonLeft = context.getText(R.string.btn_cancel)
    var textButtonRight = context.getText(R.string.btn_continue)
    var isShowTitle = true
    var isShowLeftButton = true

    fun showPopup() {
        try {
            dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null, false)
            dialog?.setContentView(dialogView)
            dialog?.show()

            val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
            val tvContent = dialogView.findViewById<TextView>(R.id.tvContent)
            val btnLeft = dialogView.findViewById<TextView>(R.id.btnLeft)
            val btnRight = dialogView.findViewById<TextView>(R.id.btnRight)

            tvTitle.text = title
            tvContent.text = content
            btnLeft.text = textButtonLeft
            btnRight.text = textButtonRight

            if (!isShowTitle) {
                tvTitle.visibility = View.GONE
            }
            if (!isShowLeftButton) {
                btnLeft.visibility = View.GONE
            }
            btnLeft.setOnClickListener {
                dialog?.dismiss()
                onClickButtonLeft?.invoke()
            }
            btnRight.setOnClickListener {
                dialog?.dismiss()
                onClickButtonRight?.invoke()
            }
        } catch (e: Exception) {
            Functions.showLog("Show ConfirmDialog Error -> $e")
        }
    }

    var onClickButtonLeft: (() -> Unit)? = null
    var onClickButtonRight: (() -> Unit)? = null
}