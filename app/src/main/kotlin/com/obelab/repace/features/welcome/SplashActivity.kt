package com.obelab.repace.features.welcome

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.dialog.ConfirmDialog
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.features.main.MainActivity

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (!PrefManager.getIsNotFirstOpenApp()) {
            PrefManager.saveIsNotFirstOpenApp(true)
            PrefManager.saveIsSpeakerTurnOn(true)
        }
        if (isNetworkWorking()) {
            Handler().postDelayed({
                if (PrefManager.getToken().isNotEmpty()) {
                    // startActivity(MainActivity.callingIntent(this))
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                } else {
                    //   startActivity(WelcomeActivity.callingIntent(this))
                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)

                }
                finish()
            }, 2000)
        } else {
            var confirmDialog = ConfirmDialog(this)
            confirmDialog.isShowTitle = false
            confirmDialog.isShowLeftButton = false
            confirmDialog.textButtonRight = getString(R.string.btn_ok)
            confirmDialog.content = getString(R.string.failure_network_connection)
            confirmDialog.onClickButtonRight = {
                finish()
            }
            confirmDialog.showPopup()
        }
    }

    private fun isNetworkWorking(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }
}