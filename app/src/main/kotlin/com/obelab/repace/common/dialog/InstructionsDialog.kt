package com.obelab.repace.common.dialog

import android.app.Dialog
import android.content.Context
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.adapter.InstructionsAdapter
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.model.InstructionModel
import me.relex.circleindicator.CircleIndicator3

class InstructionsDialog(context: Context) : Dialog(context) {
    init {
//        dialog = Dialog(context)
        dialog = Dialog(context, R.style.FullWidthDialog)

    }


    private lateinit var instructionsAdapter: InstructionsAdapter
    var listInstruction = ArrayList<InstructionModel>()


    companion object {
        var dialog: Dialog? = null
    }

    private lateinit var dialogView: View;

    fun showPopup(display: Display) {


//        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_instructions, null, false)
//        dialog?.setCancelable(true)
//        dialog?.setContentView(dialogView)
//        val stageWidth: Int = display.getWidth()
//        var width = Functions.convertPx(stageWidth, 45)
//
//        dialog?.window?.setLayout(
//            width ,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )

        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_instructions, null, false)
//            dialog?.setCancelable(true)
        dialog?.setContentView(dialogView)
//            dialog?.setCanceledOnTouchOutside(false)
//            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        ConfirmDialog.dialog?.show()


        val mViewPager = dialogView.findViewById<ViewPager2>(R.id.vpInstructions);
        val mCircleIndicator = dialogView.findViewById<CircleIndicator3>(R.id.ciInstructions)
        val btnNext = dialogView.findViewById<TextView>(R.id.btnNext)
        val checkBox = dialogView.findViewById<CheckBox>(R.id.cbDoNotShowAgain)
        val llDialog = dialogView.findViewById<LinearLayout>(R.id.llDialog)

        listInstruction.add(
            InstructionModel(
                "\n" + context.getString(R.string.instruction_content_1),
                R.drawable.ic_instruction_1
            )
        )
        listInstruction.add(
            InstructionModel(
                "\n" + context.getString(R.string.instruction_content_2),
                R.drawable.ic_instruction_2
            )
        )
        listInstruction.add(
            InstructionModel(
                "\n" + context.getString(R.string.instruction_content_3),
                R.drawable.ic_instruction_3
            )
        )
        listInstruction.add(
            InstructionModel(
                "\n" + context.getString(R.string.instruction_content_4),
                R.drawable.ic_instruction_4
            )
        )

        instructionsAdapter = InstructionsAdapter(listInstruction!!)
        mViewPager.adapter = instructionsAdapter
        mCircleIndicator.setViewPager(mViewPager)
        dialog?.show()
        btnNext.setOnClickListener {
            var isCheck = checkBox.isChecked
            PrefManager.saveShowInstructions(isCheck)
            dialog!!.dismiss()
            onClickButtonNext?.invoke()
        }

        llDialog.setBackgroundResource(R.color.smsp_transparent_color)
    }

    var onClickButtonNext: (() -> Unit)? = null
}