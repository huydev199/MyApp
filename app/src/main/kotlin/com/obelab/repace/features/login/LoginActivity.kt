package com.obelab.repace.features.login

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.obelab.repace.features.forgetPassword.ForgetPasswordActivity
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.features.register.NickNameActivity
import com.obelab.repace.features.register.RC_SIGN_IN
import com.obelab.repace.features.register.RegisterSnsActivity
import com.obelab.repace.model.*
import com.obelab.repace.service.PostUserLogin
import com.obelab.repace.viewModel.LoginRegisterSocialViewModel
import com.obelab.repace.viewModel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.header_back.*
import kotlinx.android.synthetic.main.header_home.*


@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userSocial: RequestSocialLoginModel

    companion object {
        fun callingIntent(context: Context) = Intent(context, LoginActivity::class.java)
        fun callingIntentClearTask(context: Context): Intent {
            val i = Intent(context, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return i
        }
    }

    private val viewModel: LoginViewModel by viewModels()
    private val viewModelSocial: LoginRegisterSocialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

                viewModel.postUserLogin(
                    RequestLoginModel(
                        "qts.vip.pro1@gmail.com",
                        "123456",
//                        PrefManager.getFireBaseToken()
                    )
                )

//      btnBack.setOnClickListener(){
//        Log.d("LoginActivity", "hello")
//      }


//        setUpView()
        with(viewModel) {
            observe(_resLoginModel, ::renderResLogin)
            failure(failure, ::handleFailure)
        }
//        with(viewModelSocial) {
//            observe(resSignInSocialModel, ::renderPostSignInSocial)
//            failure(failure, ::handleFailure)
//        }
    }

    private fun handleFailure(failure: Failure?) {
    }
    private fun renderResLogin(resBaseModel: ResBaseMicroModel?) {
        Log.d("huydev","resBaseModel"+resBaseModel)
        hideLoading()
        if (resBaseModel?.status == true) {
//            finish()
        } else {
//            resBaseModel?.msg?.let { showToast(it) }
        }
    }


//    private fun setUpView() {
//        tvTitle.text = getText(R.string.title_login)
//        btnLogin.setOnClickListener {
//            if (isValidData()) {
//                showLoading()
//                viewModel.postUserLogin(
//                    RequestLoginModel(
//                        edtEmail.text.toString(),
//                        edtPassword.text.toString(),
//                        PrefManager.getFireBaseToken()
//                    )
//                )
//            }
//        }
//        imvBack.setOnClickListener {
//            finish()
//        }
//
//
//
//        edtEmail.addTextChangedListener(object : TextWatcher {
//
//            override fun afterTextChanged(s: Editable?) {
//                if (s!!.isNotEmpty()) {
//                    tlEmail.setPadding(0, 5.dp, 0, 0)
//
//                }
//
//                checkEnableButton()
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//
//
//        })
//        edtPassword.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//
//                if (edtPassword.text.toString().length > 0) {
//                    tlPassword.setPadding(0, 5.dp, 0, 0)
//                }
//
//                checkEnableButton()
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//        })
//        tvForgetPassword.setOnClickListener {
//            startActivity(ForgetPasswordActivity.callingIntent(this))
//        }
//        btnSocialGoogle.setOnClickListener {
//            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build()
//            googleSignInClient = GoogleSignIn.getClient(this, gso)
//            googleSignInClient.signOut()
//
//            var confirmDialog = ConfirmDialog(this)
//            confirmDialog.textButtonRight = getString(R.string.btn_continue)
//            confirmDialog.title = getString(R.string.title_dialog_google_login)
//            confirmDialog.content = getString(R.string.content_dialog_google)
//            confirmDialog.onClickButtonRight = {
//                signIn()
//            }
//            confirmDialog.showPopup()
//        }
//
//        firebaseAuth = FirebaseAuth.getInstance()
//        callbackManager = CallbackManager.Factory.create()
//
//        btnSocialFacebook.setOnClickListener {
//            var confirmDialog = ConfirmDialog(this)
//            confirmDialog.textButtonRight = getString(R.string.btn_continue)
//            confirmDialog.title = getString(R.string.title_dialog_facebook_login)
//            confirmDialog.content = getString(R.string.content_dialog_google)
//            confirmDialog.onClickButtonRight = {
//                loginWithFacebook()
//            }
//            confirmDialog.showPopup()
//
//        }
//
//        edtEmail.setOnFocusChangeListener { _, hasFocus ->
//
//            if (hasFocus) {
//                tlEmail.setPadding(0, 5.dp, 0, 0)
//                tlEmail.background = getDrawable(R.drawable.edit_text_bg_select)
//                tlEmail.defaultHintTextColor =
//                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorTextHit))
//            } else if (!hasFocus && !Functions.isEmailValid(edtEmail.text.toString())) {
//
//                tlEmail.background = getDrawable(R.drawable.edit_text_bg_error)
//                tlEmail.defaultHintTextColor =
//                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
//                showToast(getString(R.string.invalid_email))
//
//                if (edtEmail.text.toString().length > 0) {
//                    tlEmail.setPadding(0, 5.dp, 0, 0)
//                } else {
//                    tlEmail.setPadding(0, 0.dp, 0, 0)
//                }
//
//            } else {
//
//                tlEmail.background = getDrawable(R.drawable.edit_text_bg)
//                tlEmail.defaultHintTextColor =
//                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorText))
//            }
//
//
//        }
//
//        edtPassword.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                tlPassword.background = getDrawable(R.drawable.edit_text_bg_select)
//                tlPassword.defaultHintTextColor =
//                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorTextHit))
//            } else if (!hasFocus && !Functions.isPasswordValid(edtPassword.text.toString())) {
//                tlPassword.background = getDrawable(R.drawable.edit_text_bg_error)
//                tlPassword.defaultHintTextColor =
//                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
//                showToast(getString(R.string.invalid_password))
//                if (edtPassword.text.toString().length > 0) {
//                    tlPassword.setPadding(0, 5.dp, 0, 0)
//                } else {
//                    tlPassword.setPadding(0, 0.dp, 0, 0)
//                }
//
//            } else {
//                tlPassword.background = getDrawable(R.drawable.edit_text_bg)
//                tlPassword.defaultHintTextColor =
//                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorText))
//            }
//        }
//    }
//
//    private fun signIn() {
//        val signInIntent = googleSignInClient.signInIntent
//        startActivityForResult(signInIntent, RC_SIGN_IN)
//    }
//
//    private fun loginWithFacebook() {
//        LoginManager.getInstance().logOut()
//        callbackManager = CallbackManager.Factory.create()
//        LoginManager.getInstance().logInWithReadPermissions(this, setOf("email", "public_profile"))
//        LoginManager.getInstance().registerCallback(callbackManager, object :
//            FacebookCallback<LoginResult> {
//            override fun onSuccess(result: LoginResult?) {
//                Functions.showLog("success result: " + result!!.accessToken.token)
//                Functions.showLog("success: " + result?.accessToken?.applicationId + " " + result?.accessToken?.userId)
//                handleFacebookAccessToken(result!!.accessToken)
//            }
//
//            override fun onCancel() {
//                Functions.showLog("cancel")
//                showToast(getString(R.string.fb_login_cancelled))
//            }
//
//            override fun onError(error: FacebookException?) {
//                Functions.showLog("error")
//                showToast("${getString(R.string.fb_login_cancelled)}: ${error.toString()}")
//            }
//
//        })
//    }
//
//    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
//        val credential: AuthCredential = FacebookAuthProvider.getCredential(accessToken!!.token)
//        Functions.showLog("accessToken 222 " + accessToken!!.token)
//        firebaseAuth!!.signInWithCredential(credential)
//            .addOnFailureListener { e ->
//                Functions.showLog(e.message.toString())
//            }
//            .addOnSuccessListener { result ->
//                Functions.showLog(result.user!!.email.toString() + "id: " + result.user!!.uid)
//                userSocial = RequestSocialLoginModel(
//                    result.user!!.uid,
//                    "facebook",
//                    result.user!!.email,
//                    Constants.OS_ANDROID,
//                    accessToken!!.token,
//                    PrefManager.getFireBaseToken()
//                )
//                viewModelSocial.postSignInSocial(
//                    RequestSocialLoginModel(
//                        result.user!!.uid,
//                        "facebook",
//                        result.user!!.email,
//                        Constants.OS_ANDROID,
//                        accessToken!!.token,
//                        PrefManager.getFireBaseToken()
//                    )
//                )
//            }
//    }
//
//    private fun renderPostSignInSocial(resBaseModel: ResBaseModel?) {
//        hideLoading()
//        if (resBaseModel?.success == true) {
//            val gson = Gson()
//            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
//            val resLoginModel: ResLoginModel? = gson.fromJson(dataStr, ResLoginModel::class.java)
//            Functions.showLog("resLoginSocialModel: " + resLoginModel?.let {
//                Functions.toJsonString(
//                    it
//                )
//            })
//            resLoginModel?.token?.let { PrefManager.saveToken(it) }
//            resLoginModel?.user?.let { PrefManager.saveProfile(it) }
//            val intent = Intent(this, MainActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//            finish()
//            startActivity(MainActivity.callingIntent(this))
//        } else {
//            val intent = Intent(this, RegisterSnsActivity::class.java)
//            intent.putExtra("data", userSocial)
//            startActivity(intent)
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            val exception = task.exception
//            if (task.isSuccessful) {
//                try {
//                    // Google Sign In was successful, authenticate with Firebase
//                    val account = task.getResult(ApiException::class.java)!!
//
//                    Functions.showLog("data account  " + Functions.toJsonString(account))
//                    Functions.showLog("data  " + data?.let { Functions.toJsonString(it) })
//                    Functions.showLog("data  task " + Functions.toJsonString(task))
//                    userSocial = RequestSocialLoginModel(
//                        account.id,
//                        "google",
//                        account.email,
//                        Constants.OS_ANDROID,
//                        account.id,
//                        PrefManager.getFireBaseToken()
//                    )
//                    viewModelSocial.postSignInSocial(
//                        RequestSocialLoginModel(
//                            account.id,
//                            "google",
//                            account.email,
//                            Constants.OS_ANDROID,
//                            account.id,
//                            PrefManager.getFireBaseToken()
//                        )
//                    )
//                } catch (e: ApiException) {
//                    // Google Sign In failed, update UI appropriately
//                    Functions.showLog(e.toString())
//                }
//            } else {
//                Functions.showLog(exception.toString())
//            }
//        }
//
//        //Result facebook signin
//        callbackManager.onActivityResult(requestCode, resultCode, data)
//    }
//
//    private fun isNotEmpty(): Boolean {
//        var isValid = true
//        if (edtEmail.text.toString().isEmpty()) {
//            isValid = false
//
//        }
//        if (edtPassword.text.toString().isEmpty()) {
//            isValid = false
//        }
//        return isValid
//    }
//
//    private fun isValidData(): Boolean {
//        var isValid = true
//        if (!Functions.isEmailValid(edtEmail.text.toString())) {
//
//            tlEmail.background = getDrawable(R.drawable.edit_text_bg_error)
//            tlEmail.defaultHintTextColor =
//                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
//            showToast(getString(R.string.invalid_email))
//            isValid = false
//        }
//        if (!Functions.isPasswordValid(edtPassword.text.toString())) {
//            tlPassword.background = getDrawable(R.drawable.edit_text_bg_error)
//            tlPassword.defaultHintTextColor =
//                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
//            showToast(getString(R.string.invalid_password))
//            isValid = false
//        }
//        return isValid
//    }
//
//    private fun checkEnableButton() {
//        if (isNotEmpty()) {
//            btnLogin.isEnabled = true
//            btnLogin.setBackgroundResource(R.drawable.btn_enable)
//            btnLogin.setTextColor(
//                ContextCompat.btnloginmay tu dong vao che do (
//                    this,
//                    R.color.colorTextPrimary
//                )
//            )
//        } else {
//            btnLogin.isEnabled = false
//            btnLogin.setBackgroundResource(R.drawable.btn_disable)
//            btnLogin.setTextColor(
//                ContextCompat.getColor(
//                    this,
//                    R.color.colorText
//                )
//            )
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        viewModel.loadUserList()
//    }
//
//    private fun renderUserList(userList: List<UserModel>?) {
//        Functions.showLog("userList: " + userList?.size)
//    }
//
//    private fun renderPostUserLogin(resBaseModel: ResBaseModel?) {
//        Functions.showLog("resBaseModel: " + resBaseModel?.let { Functions.toJsonString(it) })
//        hideLoading()
//        if (resBaseModel?.success == true) {
//            try {
//                val gson = Gson()
//                val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
//                val resLoginModel: ResLoginModel? =
//                    gson.fromJson(dataStr, ResLoginModel::class.java)
//                Functions.showLog("resLoginModel: " + resLoginModel?.let { Functions.toJsonString(it) })
//                resLoginModel?.token?.let { PrefManager.saveToken(it) }
//                resLoginModel?.user?.let { PrefManager.saveProfile(it) }
//                if (resLoginModel?.user?.nickname?.isNotEmpty() == true) {
//
//                   // startActivity(MainActivity.callingIntent(this))
//                    val intent = Intent(this, MainActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(intent)
//                } else {
////                    startActivity(NickNameActivity.callingIntent(this))
//                    val intent = Intent(this, NickNameActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(intent)
//
//                }
//                finish()
//            } catch (e: Exception) {
//                tlEmail.background = getDrawable(R.drawable.edit_text_bg_error)
//                tlEmail.defaultHintTextColor =
//                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
//                tlPassword.background = getDrawable(R.drawable.edit_text_bg_error)
//                tlPassword.defaultHintTextColor =
//                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
//                showToast(getString(R.string.failure_exception))
//
//            }
//        } else {
//            tlEmail.background = getDrawable(R.drawable.edit_text_bg_error)
//            tlEmail.defaultHintTextColor =
//                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
//            tlPassword.background = getDrawable(R.drawable.edit_text_bg_error)
//            tlPassword.defaultHintTextColor =
//                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEdtError))
//            resBaseModel?.msg?.let { showToast(it) }
//        }
//    }
//
//    private fun handleFailure(failure: Failure?) {
//        Functions.showLog("loginError: " + failure.toString())
//        hideLoading()
//        showToast(getString(R.string.failure_network_connection))
//    }
//
//    //Convert to dp
//    val Int.dp: Int
//        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}
