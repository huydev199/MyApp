package com.obelab.repace.features.forgetPassword

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.obelab.repace.R
import com.obelab.repace.common.dialog.ConfirmDialog
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.model.RequestForgetPasswordModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.viewModel.ForgetPasswordViewModel
import kotlinx.android.synthetic.main.activity_forget_password.*
import kotlinx.android.synthetic.main.activity_forget_password.edtEmail
import kotlinx.android.synthetic.main.header_back.*

class ForgetPasswordActivity : BaseActivity() {

    companion object {
        fun callingIntent(context: Context) = Intent(context, ForgetPasswordActivity::class.java)
    }

    private val viewModel: ForgetPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)
        with(viewModel) {
            observe(resForgetPasswordModel, ::renderForgetPassword)
            failure(failure, ::handleFailure)
        }
        setUpView()
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_enter_email)
        imvBack.setOnClickListener {
            finish()
        }
        edtEmail.setOnFocusChangeListener { view, b ->
            if (b) {
                tlEmail.setBackgroundResource(R.drawable.edit_text_bg_select)
                tlEmail.defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorTextHit))
            } else {
                tlEmail.setBackgroundResource(R.drawable.edit_text_bg)
                tlEmail.defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorText))
            }
        }
        edtEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkEnableButton()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tlEmail.setBackgroundResource(R.drawable.edit_text_bg_select)
                tlEmail.defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this@ForgetPasswordActivity, R.color.colorTextHit))
            }
        })
        btnSend.setOnClickListener {
            if (isValidData()) {
                viewModel.putForgetPassword(RequestForgetPasswordModel(edtEmail.text.toString()))
            }
        }
    }

    private fun checkEnableButton() {
        if (edtEmail.text.toString().isNotEmpty()) {
            btnSend.isEnabled = true
            btnSend.setTextColor(
                    ContextCompat.getColor(
                            this,
                            R.color.colorTextPrimary
                    )
            )
            btnSend.setBackgroundResource(R.drawable.btn_enable)
        } else {
            btnSend.isEnabled = false
            btnSend.setBackgroundResource(R.drawable.btn_disable)
            btnSend.setTextColor(
                    ContextCompat.getColor(
                            this,
                            R.color.colorText
                    )
            )
        }
    }

    private fun isValidData(): Boolean {
        var isValid = true
        if (!Functions.isEmailValid(edtEmail.text.toString())) {
            tlEmail.setBackgroundResource(R.drawable.edit_text_bg_error)
            tlEmail.defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
            showToast(getString(R.string.invalid_email))
            isValid = false
        }
        return isValid
    }

    private fun renderForgetPassword(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            var confirmDialog = ConfirmDialog(this)
            confirmDialog.isShowTitle = false
            confirmDialog.isShowLeftButton = false
            confirmDialog.textButtonRight = getString(R.string.btn_continue)
            confirmDialog.content = getString(R.string.invalid_send_email)
            confirmDialog.onClickButtonRight = {
                finish()
            }
            confirmDialog.showPopup()
        } else {
            tlEmail.setBackgroundResource(R.drawable.edit_text_bg_error)
            tlEmail.defaultHintTextColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
            //showToast(getString(R.string.invalid_email))
            showToast(resBaseModel?.msg.toString())
        }

    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("ForgetPasswordError: " + failure.toString())
        hideLoading()
    }
}