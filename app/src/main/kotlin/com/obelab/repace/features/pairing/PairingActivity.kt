package com.obelab.repace.features.pairing

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.Display
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.obelab.repace.R
import com.obelab.repace.RepaceApplication
import com.obelab.repace.common.adapter.BluetoothDeviceAdapter
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.login.LoginActivity
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.features.notificationaccess.NotificationAccessActivity
import com.obelab.repace.model.BluetoothDeviceModel
import com.obelab.repace.model.DeviceInfoModel
import com.obelab.repace.service.ble.BleService
import kotlinx.android.synthetic.main.activity_pairing.*
import kotlinx.android.synthetic.main.header_back.*
import android.content.Intent
import android.provider.Settings
import android.widget.Toast


class PairingActivity : BaseActivity() {
    companion object {
        fun callingIntent(context: Context) = Intent(context, PairingActivity::class.java)
        fun callingIntentClearTask(context: Context): Intent {
            val i = Intent(context, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return i
        }

        var instance: PairingActivity? = null
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    private val timeScan = Constants.timeScan
    private var isPairing = false;
    private var isConnectionComplete = false;
    private lateinit var mRecyclerView: RecyclerView
    var listDevice: ArrayList<BluetoothDeviceModel> = arrayListOf<BluetoothDeviceModel>()
    var btnBluetoothAdapter = BluetoothDeviceAdapter(listDevice, -1, -1)
    private lateinit var mService: BleService
    private var mBound: Boolean = false
    private var isSearchClick = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pairing)

        // Check connect bluetooth

        checkAndRequestPermission()

        instance = this
        setUpView()
    }

    private fun requestBlueToothPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                arrayOf(

                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,

                ), 0
            )
        } else {
            val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, 0)

        }
    }

    private fun initBleService() {
        if (!RepaceApplication.isConnected) {
            bindBleService()
        }
    }

//    private val requestMultiplePermissions =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//            var isAllowAll = true
//            permissions.entries.forEach {
//                if (!it.value) {
//                    isAllowAll = false
//                }
//
//
//            }
//            if (isAllowAll) requestTurnOnBluetooth()
//
//        }

    private fun bindBleService() {
        val bindIntent = Intent(this, BleService::class.java)
        bindService(bindIntent, mServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onResume() {
        registerReceiver(mGattUpdateReceiverActivity, makeGattUpdateIntentFilter())
        super.onResume()
        if (BleService.connectionState == BleService.STATE_CONNECTED) {
            stopPairing()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestTurnOnBluetooth()
                }
                else{
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)) {
                        Toast.makeText(this, R.string.bluetooth_deined, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                if (grantResults.isNotEmpty()) {
                    var isAllPermissionAccept = true
                    grantResults.forEach {
                        if (it != PackageManager.PERMISSION_GRANTED) {
                            isAllPermissionAccept = false
                        }
                    }

                    if (isAllPermissionAccept) requestTurnOnBluetooth()
                    else {
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)) {
                            Toast.makeText(this, R.string.bluetooth_deined, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

            }

        }
        if (isSearchClick) isSearchClick = false
    }

    val mGattUpdateReceiverActivity = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BleService.ACTION_GATT_CONNECTED == action) {
                stopPairing()
            } else if (BleService.ACTION_GATT_SCAN_RESULT == action) {
                val data = intent.getSerializableExtra(BleService.EXTRA_DATA) as? DeviceInfoModel
                if (data != null) {
                    scanDevice(data.address, data.name)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            scanDevice()
        }
    }

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as BleService.LocalBinder
            mService = binder.getService()
            mBound = true
            mService.StartBLEScan()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    private fun setUpView() {
        val display: Display = windowManager.defaultDisplay
        val stageWidth: Int = display.getWidth()
        var width = Functions.convertPx(stageWidth, 86)

        ctl_bluetooth.layoutParams.width = width
        ctl_bluetooth.layoutParams.height = width

        civ_bluetooth.layoutParams.width = width
        civ_bluetooth.layoutParams.height = width

        civ_pairing.layoutParams.width = width
        civ_pairing.layoutParams.height = width

        tvTitle.text = getText(R.string.title_pairing)
        imvBack.setOnClickListener {
            finish()
        }

        btnSearchPairing.setOnClickListener {
            isSearchClick = true
            checkAndRequestPermission()
        }

        tvTroubleshoot.setOnClickListener {
            //id video in yt
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE)
                == PackageManager.PERMISSION_GRANTED

            ) {
                requestTurnOnBluetooth()
                isSearchClick = false
            } else {
                requestBlueToothPermission()
            }

        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                requestTurnOnBluetooth()
            } else {
                requestBlueToothPermission()
            }
        }

    }

    // Handle click button search device
    fun scanDevice(address: String, name: String) {
        var item = BluetoothDeviceModel(name, address)
        if (!listDevice.contains(item)) {
            listDevice.add(item)
        }

        mRecyclerView = findViewById(R.id.rcvListDevice)
        mRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mRecyclerView.layoutParams.height = RecyclerView.LayoutParams.WRAP_CONTENT
        if (listDevice.size > 0) {
            rcvListDevice.visibility = View.VISIBLE
            btnBluetoothAdapter.notifyDataSetChanged()
            mRecyclerView.adapter = btnBluetoothAdapter
            btnBluetoothAdapter.onClickDetail = {
                tvTitleStatus.text = getString(R.string.pairing_repace_device)
                tvTitleStatus.setTextColor(ContextCompat.getColor(this, R.color.colorTextPrimary))
            }
        }
    }

    // Animation scan device
    @RequiresApi(Build.VERSION_CODES.M)
    private fun startAnimationPairing() {
        isPairing = true
        tvTroubleshoot.visibility = View.INVISIBLE
        civ_bluetooth.setBackgroundResource(R.drawable.ic_bluetooth)
        civ_pairing.visibility = View.VISIBLE
        btnSearchPairing.setBackgroundResource(R.drawable.btn_disable);
        btnSearchPairing.setTextColor(getColor(R.color.colorText))
        var runable = object : Runnable {
            override fun run() {
                civ_pairing.animate().rotationBy(360F).withEndAction(this).setDuration(3000)
                    .setInterpolator(LinearInterpolator()).start()
            }
        }
        civ_pairing.animate().rotationBy(360F).withEndAction(runable).setDuration(3000)
            .setInterpolator(LinearInterpolator()).start()
        Handler().postDelayed({
            stopAnimationPairing()
            mService.onStop()
        }, timeScan.toLong())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun stopAnimationPairing() {
        isPairing = false
        civ_pairing.animate().cancel()
        tvTroubleshoot.visibility = View.VISIBLE
        civ_pairing.visibility = View.INVISIBLE
        btnSearchPairing.setBackgroundResource(R.drawable.btn_enable);
        btnSearchPairing.setTextColor(getColor(R.color.colorTextPrimary))
    }

    fun stopPairing() {
        runOnUiThread {
            stopAnimationPairing()
            rcvListDevice.visibility = View.GONE
            tvRemindPairing.visibility = View.GONE
            isConnectionComplete = true
            btnSearchPairing.text = getText(R.string.btn_next)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                btnSearchPairing.setTextColor(getColor(R.color.colorTextPrimary))
            }
            tvTitleStatus.text = getString(R.string.connection_complete)
            tvTitleStatus.setTextColor(ContextCompat.getColor(this, R.color.colorEdtFocus))
            btnSearchPairing.setBackgroundResource(R.drawable.btn_enable)
            civ_bluetooth.setBackgroundResource(R.drawable.ic_pairing_complete)
        }
    }

    // Stops scanning after 10 seconds.
    private val REQUEST_ENABLE_BT = 3

    //Connect device
    fun connect(address: String) {
        mService.connect(address)
    }

    override fun onDestroy() {
        unregisterReceiver(mGattUpdateReceiverActivity)
        super.onDestroy()
    }

    fun scanDevice() {
        btnBluetoothAdapter = BluetoothDeviceAdapter(listDevice, -1, -1)
        if (isConnectionComplete == false) {
            if (isPairing == false) {
                startAnimationPairing()
                if (listDevice.size > 0) {
                    listDevice.clear()
                    rcvListDevice.visibility = View.GONE
                    tvTitleStatus.text = getString(R.string.setup_device)
                    tvTitleStatus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.colorTextPrimary
                        )
                    )
                    mService.StartBLEScan()
                    btnBluetoothAdapter.notifyDataSetChanged()
                } else {
                    initBleService()
                }
            }
        }
        //Click button when connection complete
        else {
            if (intent.getStringExtra(Constants.type) == Constants.TUTORIAL_SCREEN) {
                startActivity(NotificationAccessActivity.callingIntent(this))
            } else {
                startActivity(MainActivity.callingIntent(this))
            }
        }
    }

    fun requestTurnOnBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            scanDevice()
        }

    }
}