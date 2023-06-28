package com.obelab.repace.features.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.obelab.repace.R
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.features.login.LoginActivity
import com.obelab.repace.features.register.AdditionalInfoActivity
import com.obelab.repace.features.register.RegisterActivity
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : BaseActivity() {
    companion object {
        fun callingIntent(context: Context): Intent {
            val i = Intent(context, WelcomeActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return i
        }
    }

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        btnLogin.setOnClickListener {
            startActivity(LoginActivity.callingIntent(this))
        }

        btnRegister.setOnClickListener {
//            startActivity(AdditionalInfoActivity.callingIntent(this))
            startActivity(RegisterActivity.callingIntent(this))
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        showToast(getString(R.string.double_back_to_exit))
        Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}