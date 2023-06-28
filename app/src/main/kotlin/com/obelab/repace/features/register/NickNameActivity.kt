package com.obelab.repace.features.register

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.model.*
import com.obelab.repace.viewModel.UserUpdateViewModel
import kotlinx.android.synthetic.main.activity_nickname.*
import kotlinx.android.synthetic.main.header_back.*

class NickNameActivity : BaseActivity() {
    companion object {
        private val KEY_EMAIL ="KEY_EMAIL"
        private val KEY_PASS_WORD ="KEY_PASSWORD"
        private val KEY_IS_SOCIAL = "KEY_IS_SOCIAL"
        fun callingIntent(context: Context, email: String?, password : String?,isSocial : Boolean): Intent {
            var intent = Intent(context, NickNameActivity::class.java)
            intent.putExtra(KEY_EMAIL,email)
            intent.putExtra(KEY_PASS_WORD,password)
            intent.putExtra(KEY_IS_SOCIAL,isSocial)
            return intent
        }

    }

    private val viewModel: UserUpdateViewModel by viewModels()
    private var isValidNickname: Boolean = false
    private var userEmail: String? = ""
    private var userPassword: String? = ""
    private var userNickName: String? = ""
    private var isSocial: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nickname)
        setUpView()
        getDataFromIntent()

        val token = PrefManager.getToken()
        Functions.showLog("tokenPrefNickName: $token")
    }

    private fun getDataFromIntent() {
        userEmail =  intent.getStringExtra(KEY_EMAIL)
        userPassword=  intent.getStringExtra(KEY_PASS_WORD)
        isSocial=  intent.getBooleanExtra(KEY_IS_SOCIAL,false)
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_nickname)
        //Check nickname is valid.
        edtNickname.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                tlNickname.setBackgroundResource(R.drawable.edit_text_bg_select)
                tlNickname.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorTextHit))
            } else {
                tlNickname.setBackgroundResource(R.drawable.edit_text_bg)
                tlNickname.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorText))
            }
        }

        edtNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var temp = edtNickname.text.toString()
                //Nickname have 4 to 15 character
                if (temp.length in 4..15) {
                    btnNickname.setBackgroundResource(R.drawable.btn_enable);
                    btnNickname.setTextColor(
                        ContextCompat.getColor(
                            this@NickNameActivity,
                            R.color.colorTextPrimary
                        )
                    )
                } else {
                    btnNickname.setBackgroundResource(R.drawable.btn_disable);
                    btnNickname.setTextColor(
                        ContextCompat.getColor(
                            this@NickNameActivity,
                            R.color.colorText
                        )
                    )
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tlNickname.setBackgroundResource(R.drawable.edit_text_bg_select)
                tlNickname.defaultHintTextColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this@NickNameActivity,
                        R.color.colorTextHit
                    )
                )
            }
        })

        imvBack.setOnClickListener {
            finish()
        }
        btnNickname.setOnClickListener {
            Functions.showLog("onclick=>>>>> ${isValidNickname}")
            if (isValidNickname == false) {
                val request = RequestUserUpdateModel()
                request.nickname = edtNickname.text.toString()

                with(viewModel) {
                    observe(resCheckMemberModel, ::renderPutUpdateUser)
                    failure(failure, ::handleFailure)
                }
                viewModel.checkUser(request)
            } else {

                startActivity(
                    AdditionalInfoActivity.callingIntent(this,email = userEmail,password = userPassword,nickname =userNickName,isSocial)

                )
            }
        }
    }

    private fun renderPutUpdateUser(resBaseModel: ResBaseModel?) {
        Functions.showLog("resBaseModel: " + resBaseModel?.let { Functions.toJsonString(it) })
        hideLoading()
        Functions.showLog("onclick=>>>>> ${resBaseModel?.success}")
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resLoginModel: ResLoginModel? = gson.fromJson(dataStr, ResLoginModel::class.java)
            Functions.showLog("resBaseModel success: " + resLoginModel?.let {
                Functions.toJsonString(
                    it
                )
            })
            userNickName = edtNickname.text.toString()
            btnNickname.setBackgroundResource(R.drawable.btn_enable);
            btnNickname.setTextColor(getColor(R.color.colorTextPrimary))
            tlNickname.setBackgroundResource(R.drawable.edit_text_bg)
            showToast(getString(R.string.available_nickname))
            edtNickname.setFocusableInTouchMode(false);
            edtNickname.setFocusable(false);
            tlNickname.defaultHintTextColor =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorText))
            isValidNickname = true
            btnNickname.setText(R.string.btn_next)
        } else {
            Functions.showLog("resBaseModel: " + resBaseModel?.success)
            btnNickname.setBackgroundResource(R.drawable.btn_disable);
            btnNickname.setTextColor(getColor(R.color.colorText))
            tlNickname.setBackgroundResource(R.drawable.edit_text_bg_error)
            tlNickname.defaultHintTextColor =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
            showToast(getString(R.string.invalid_nickname))
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("updateInfoError: " + failure.toString())
        btnNickname.setBackgroundResource(R.drawable.btn_disable);
        hideLoading()
    }

    // Touch out to disable keyboard
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}