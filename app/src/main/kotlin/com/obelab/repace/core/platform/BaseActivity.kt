package com.obelab.repace.core.platform

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.loadingview.LoadingDialog
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.dialog.ConfirmDialog
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.pairing.PairingActivity
import com.obelab.repace.service.ble.BleService
import com.tapadoo.alerter.Alerter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.toolbar.*
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    companion object {
        var instance: BaseActivity? = null

        fun makeGattUpdateIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BleService.ACTION_GATT_CONNECTED)
            intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED)
            intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED)
            intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE)
            intentFilter.addAction(BleService.ACTION_GATT_SCAN_RESULT)
            return intentFilter
        }
    }

    private var dialogLoading: LoadingDialog? = null
    private var progressDialog: AlertDialog? = null
    private var confirmDialog: ConfirmDialog? = null
    var listStageConvert = arrayListOf<String>("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.activity_open_exit);
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit)
        AppCenter.start(application, getString(R.string.app_center_key), Analytics::class.java, Crashes::class.java)
        setSupportActionBar(toolbar)
        instance = this
        dialogLoading = LoadingDialog[this]

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            window.apply {
//                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    decorView.systemUiVisibility =
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//                } else {
//                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                }
//
//            }
//        }
    }

    override fun onResume() {
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        super.onResume()
        Functions.setLocale(PrefManager.getLanguage())
    }

    override fun onDestroy() {
        unregisterReceiver(mGattUpdateReceiver)
        super.onDestroy()
    }

    open val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BleService.ACTION_GATT_DISCONNECTED == action) {
                showPopupDisconnectedDevice()
            }
        }
    }

    internal fun showLoading() {
        try {
            dialogLoading?.show()
        } catch (e: Exception) {
        }
    }

    internal fun hideLoading() {
        try {
            dialogLoading?.hide()
        } catch (e: Exception) {
        }
    }

    internal fun showToast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    internal fun showSuccessAlert(message: String) = Alerter.create(this)
        .setTitle("Success")
        .setText(message)
        .setBackgroundColorInt(getColor(R.color.colorSuccess))
        .also { alerter ->
            val alerterView = alerter.getLayoutContainer()
            val param = alerterView?.layoutParams as ViewGroup.MarginLayoutParams
            param.topMargin = -80
        }
        .show()

    internal fun showWarnAlert(message: String) = Alerter.create(this)
        .setTitle("Warn")
        .setText(message)
        .setBackgroundColorInt(getColor(R.color.colorWarn))
        .also { alerter ->
            val alerterView = alerter.getLayoutContainer()
            val param = alerterView?.layoutParams as ViewGroup.MarginLayoutParams
            param.topMargin = -80
        }
        .show()

    internal fun showErrorAlert(message: String) = Alerter.create(this)
        .setTitle("Error")
        .setText(message)
        .setBackgroundColorInt(getColor(R.color.colorError))
        .also { alerter ->
            val alerterView = alerter.getLayoutContainer()
            val param = alerterView?.layoutParams as ViewGroup.MarginLayoutParams
            param.topMargin = -80
        }
        .show()

    internal fun showPopupDisconnectedDevice() {
        if(confirmDialog == null){
            confirmDialog = ConfirmDialog(this)
        }
        confirmDialog!!.isShowTitle = true
        confirmDialog!!.title = getString(R.string.title_disconnected)
        confirmDialog!!.content = getString(R.string.content_disconnected)
        confirmDialog!!.isShowLeftButton = false
        confirmDialog!!.textButtonRight = getString(R.string.btn_ok)
        confirmDialog!!.onClickButtonRight = {
            startActivity(PairingActivity.callingIntent(this))
        }
        Functions.showLog("showPopupDisconnectedDevice")
        confirmDialog!!.showPopup()
    }

    internal fun showPopupDisconnectedDevice(context: Context) {
        var confirmDialog = ConfirmDialog(context)
        confirmDialog.isShowTitle = true
        confirmDialog.title = getString(R.string.title_disconnected)
        confirmDialog.content = getString(R.string.content_disconnected)
        confirmDialog.isShowLeftButton = false
        confirmDialog.textButtonRight = getString(R.string.btn_ok)
        confirmDialog.onClickButtonRight = {
            startActivity(PairingActivity.callingIntent(this))
        }
        confirmDialog.showPopup()
    }

    internal fun Date.toString(format: String, locale: Locale = Locale.ENGLISH): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    internal fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    open fun showProgressDialog(title: String = "Loading...") {
        val llPadding = 30
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam
        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam
        progressBar.indeterminateTintList = ColorStateList.valueOf(Color.WHITE)
        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(this)
        tvText.text = title
        tvText.setTextColor(Color.WHITE)
        tvText.textSize = 16f
        tvText.layoutParams = llParam
        ll.addView(progressBar)
        ll.addView(tvText)
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(ll)
        progressDialog = builder.create()
        progressDialog?.show()
        val window = progressDialog?.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(window.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.gravity = Gravity.BOTTOM
            window.attributes = layoutParams
            window.setBackgroundDrawableResource(android.R.color.background_dark)
        }
    }

    open fun hideProgressDialog() {
        progressDialog?.dismiss()
    }


    open fun showProgressBarDialog(title: String = "Loading...", titleNext: String = "Loading...") {
        val llPadding = 30
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam
        progressBar.indeterminateTintList = ColorStateList.valueOf(Color.WHITE)

        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(this)



        tvText.text = title
        (object : CountDownTimer( Constants.Countdown.TIME_OUT/3, Constants.Countdown.TIME_INTERVAL ) {
            override fun onTick(millisUntilFinished: Long) { }
            override fun onFinish() {

                tvText.text =titleNext
            }
        }).start()
        tvText.setTextColor(Color.WHITE)
        tvText.textSize = 16f
        tvText.layoutParams = llParam
        ll.addView(progressBar)
        ll.addView(tvText)
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(ll)
        progressDialog = builder.create()
        progressDialog?.show()
        val window = progressDialog?.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(window.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.gravity = Gravity.BOTTOM
            window.attributes = layoutParams
            window.setBackgroundDrawableResource(android.R.color.background_dark)
        }
    }

    open fun hideProgressBarDialog() {
        progressDialog?.dismiss()
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
