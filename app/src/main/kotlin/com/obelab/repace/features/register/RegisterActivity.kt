package com.obelab.repace.features.register

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.TextView.BufferType
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.dialog.ConfirmDialog
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.model.RequestRegisterModel
import com.obelab.repace.model.RequestSocialLoginModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.ResLoginModel
import com.obelab.repace.viewModel.LoginRegisterSocialViewModel
import com.obelab.repace.viewModel.RegisterViewModel
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_me.*
import kotlinx.android.synthetic.main.header_back.*

const val RC_SIGN_IN = 123

class RegisterActivity : BaseActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userSocial: RequestSocialLoginModel

    companion object {
        fun callingIntent(context: Context) = Intent(context, RegisterActivity::class.java)
    }

    private val viewModel: RegisterViewModel by viewModels()
    private val viewModelSocial: LoginRegisterSocialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        customTextView(tvPolicySub)
        acceptPolicy()
        checkDataValid()
        setUpView()
        with(viewModel) {
            observe(resLoginModel, ::renderPostUserRegister)
            failure(failure, ::handleFailure)
        }

        with(viewModelSocial) {
            observe(resSignInSocialModel, ::renderPostSignInSocial)
            failure(failure, ::handleFailure)
        }

        //Login Facebook
        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()
        btnSocialFacebook.setOnClickListener {
            var confirmDialog = ConfirmDialog(this)
            confirmDialog.textButtonRight = getString(R.string.btn_continue)
            confirmDialog.title = getString(R.string.title_dialog_facebook_login)
            confirmDialog.content = getString(R.string.content_dialog_google)
            confirmDialog.onClickButtonRight = {
                loginWithFacebook()
            }
            confirmDialog.showPopup()

        }
    }

    private fun loginWithFacebook() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, setOf("email", "public_profile"))
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    Functions.showLog("success: " + result?.accessToken?.applicationId + " " + result?.accessToken?.userId)
                    handleFacebookAccessToken(result!!.accessToken)
                }

                override fun onCancel() {
                    Functions.showLog("cancel")
                    showToast(getString(R.string.fb_login_cancelled))
                }

                override fun onError(error: FacebookException?) {
                    Functions.showLog("error")
                    showToast("${getString(R.string.fb_login_cancelled)}: ${error.toString()}")
                }

            })
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        val credential: AuthCredential = FacebookAuthProvider.getCredential(accessToken!!.token)
        firebaseAuth!!.signInWithCredential(credential)
            .addOnFailureListener { e ->
                Functions.showLog(e.message.toString())
            }
            .addOnSuccessListener { result ->
                Functions.showLog(result.user!!.email.toString() + "id: " + result.user!!.uid)
                userSocial = RequestSocialLoginModel(
                    result.user!!.uid,
                    "facebook",
                    result.user!!.email,
                    Constants.OS_ANDROID,
                    accessToken!!.token,
                    PrefManager.getFireBaseToken()
                )
                viewModelSocial.postSignInSocial(
                    RequestSocialLoginModel(
                        result.user!!.uid,
                        "facebook",
                        result.user!!.email,
                        Constants.OS_ANDROID,
                        accessToken!!.token,
                        PrefManager.getFireBaseToken()
                    )
                )
            }
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_signup)

        btnSocialGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            btnSocialGoogle.setBackgroundResource(R.drawable.btn_social_select)
            googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut()

            var confirmDialog = ConfirmDialog(this)
            confirmDialog.textButtonRight = getString(R.string.btn_continue)
            confirmDialog.title = getString(R.string.title_dialog_google_signup)
            confirmDialog.content = getString(R.string.content_dialog_google)
            confirmDialog.onClickButtonRight = {
                signIn()
            }
            confirmDialog.showPopup()
        }

        btnSignup.setOnClickListener {
            var isChecked = cbPolicy.isChecked
            if (!Functions.isEmailValid(edtUsername.text.toString())) {
                showToast(getString(R.string.invalid_email))
                tlUsername.setBackgroundResource(R.drawable.edit_text_bg_error)
                tlUsername.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
            } else if (!Functions.isPasswordValid(edtPassword.text.toString())) {
                showToast(getString(R.string.invalid_password))
                tlPassword.setBackgroundResource(R.drawable.edit_text_bg_error)
                tlPassword.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
            } else if (edtPassword.text.toString() != edtConfirmPassword.text.toString()) {
                showToast(getString(R.string.invalid_confirm_password))
                tlConfirmPassword.setBackgroundResource(R.drawable.edit_text_bg_error)
                tlConfirmPassword.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
            } else if (isChecked) {
                viewModel.checkEmail(
                    RequestRegisterModel(
                        edtUsername.text.toString(),
                        edtPassword.text.toString()
                    )
                )
            }
        }

        imvBack.setOnClickListener {
            finish()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if (task.isSuccessful) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    userSocial = RequestSocialLoginModel(
                        account.id,
                        "google",
                        account.email,
                        Constants.OS_ANDROID,
                        account.id,
                        PrefManager.getFireBaseToken()
                    )
                    viewModelSocial.postSignInSocial(
                        RequestSocialLoginModel(
                            account.id,
                            "google",
                            account.email,
                            Constants.OS_ANDROID,
                            account.id,
                            PrefManager.getFireBaseToken()
                        )
                    )
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Functions.showLog(e.toString())
                }
            } else {
                Functions.showLog(exception.toString())
            }
        }

        //Result facebook signin
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    //Multiple clickable links in textview (Privacy Policy,Terms of Use)
    private fun customTextView(view: TextView) {
        val spanTxt = SpannableStringBuilder(
            getString(R.string.tv_sub_policy_1)
        )
        spanTxt.append(getString(R.string.btn_terms_of_us))
        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(p0: View) {
                startActivity(TermsOfUseActitity.callingIntent(this@RegisterActivity))
            }
        }, spanTxt.length - getString(R.string.btn_terms_of_us).length, spanTxt.length, 0)
        spanTxt.append(getString(R.string.tv_sub_policy_2))
        spanTxt.append(getString(R.string.tv_sub_policy_3))
        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(p0: View) {
                startActivity(PrivacyPolicyActivity.callingIntent(this@RegisterActivity))
            }
        }, spanTxt.length - getString(R.string.tv_sub_policy_3).length, spanTxt.length, 0)
        view.movementMethod = LinkMovementMethod.getInstance()
        view.setText(spanTxt, BufferType.SPANNABLE)
    }

    //Check data validity when registering
    private fun checkDataValid() {
        edtUsername.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                tlUsername.setBackgroundResource(R.drawable.edit_text_bg_select)
                tlUsername.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorTextHit))
            } else if (!hasFocus && !Functions.isEmailValid(edtUsername.text.toString())) {
                tlUsername.setBackgroundResource(R.drawable.edit_text_bg_error)
                tlUsername.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
                showToast(getString(R.string.invalid_email))
            } else {
                tlUsername.setBackgroundResource(R.drawable.edit_text_bg)
                tlUsername.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorText))
            }
        }
        edtPassword.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                tlPassword.setBackgroundResource(R.drawable.edit_text_bg_select)
                tlPassword.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorTextHit))
            } else if (!hasFocus && !Functions.isPasswordValid(edtPassword.text.toString())) {
                tlPassword.setBackgroundResource(R.drawable.edit_text_bg_error)
                tlPassword.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
                showToast(getString(R.string.invalid_password))
            } else {
                tlPassword.setBackgroundResource(R.drawable.edit_text_bg)
                tlPassword.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorText))
            }
        }
        edtConfirmPassword.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                tlConfirmPassword.setBackgroundResource(R.drawable.edit_text_bg_select)
                tlConfirmPassword.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorTextHit))
            } else if (!hasFocus && edtPassword.text.toString() != edtConfirmPassword.text.toString()) {
                tlConfirmPassword.setBackgroundResource(R.drawable.edit_text_bg_error)
                tlConfirmPassword.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
                showToast(getString(R.string.invalid_confirm_password))
            } else {
                tlConfirmPassword.setBackgroundResource(R.drawable.edit_text_bg)
                tlConfirmPassword.defaultHintTextColor =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorText))
            }
        }
    }

    //Function to change state for button
    private fun acceptPolicy() {
        cbPolicy.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                btnSignup.setBackgroundResource(R.drawable.btn_enable);
                btnSignup.setTextColor(ContextCompat.getColor(this, R.color.colorTextPrimary))
            } else {
                btnSignup.setBackgroundResource(R.drawable.btn_disable);
                btnSignup.setTextColor(ContextCompat.getColor(this, R.color.colorText))
            }
        }
    }

    private fun renderPostUserRegister(resBaseModel: ResBaseModel?) {
        Functions.showLog("resBaseModel: " + resBaseModel?.let { Functions.toJsonString(it) })
        hideLoading()
        if (resBaseModel?.success == true) {
            startActivity(NickNameActivity.callingIntent(this,edtUsername.text.toString(),edtPassword.text.toString(),false))

        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun renderPostSignInSocial(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resLoginModel: ResLoginModel? = gson.fromJson(dataStr, ResLoginModel::class.java)
            Functions.showLog("resLoginSocialModel: " + resLoginModel?.let {
                Functions.toJsonString(
                    it
                )
            })
            resLoginModel?.token?.let {
                PrefManager.saveToken(it)
            }
            startActivity(MainActivity.callingIntent(this))
        } else {
            val intent = Intent(this, RegisterSnsActivity::class.java)
            intent.putExtra(Constants.data, userSocial)
            startActivity(intent)
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("loginError: " + failure.toString())
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

