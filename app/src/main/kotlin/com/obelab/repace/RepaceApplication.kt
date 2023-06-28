/**
 * Copyright (C) 2020 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.obelab.repace

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.service.ble.BleService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RepaceApplication : Application() {
    private val TAG = "PreLtTestTreadmillActivity"
    protected lateinit var mApplication: RepaceApplication
    val ChanelID = "repace_push_id"


    companion object{
        lateinit var mService : BleService
        var mBound: Boolean = false
        var isConnected = false
    }
    fun getInstance(): RepaceApplication? {
        return mApplication
    }

    fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BleService.ACTION_GATT_SCAN_RESULT)
        return intentFilter
    }

    override fun onCreate() {
        super.onCreate()
        mApplication = this
        val intent = Intent(mApplication, BleService::class.java)
        createChanelNotification()
        try {
            Handler().postDelayed({
                try {
                    startService(intent)
                } catch (e: Exception){
                    Functions.showLog("startServiceError -> ${e.toString()}")
                }
            }, 1000)
        } catch (e: Exception) {
            Functions.showLog(TAG,"Error start service: $e")
        }

        init()
    }

    @SuppressLint("StringFormatInvalid")
    private fun createChanelNotification() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.app_name, token)
            Functions.showLog(TAG, msg)
        })
    }

    private fun init(){
        registerReceiver()
    }

    fun registerReceiver(){
        try {
            LocalBroadcastManager.getInstance(mApplication)
                .registerReceiver(
                    mGattUpdateReceiver, makeGattUpdateIntentFilter()
                )
        } catch (e: Exception) {
            Functions.showLog("Register receiver error")
        }
    }

    open val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            Functions.showLog(TAG, "gatt status: ${action}")
            if (BleService.ACTION_GATT_DISCONNECTED == action) {
                isConnected = false
                //showPopupDisconnectedDevice()
            } else if (BleService.ACTION_GATT_CONNECTED == action){
                isConnected = true
            }
        }
    }

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as BleService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }
}
