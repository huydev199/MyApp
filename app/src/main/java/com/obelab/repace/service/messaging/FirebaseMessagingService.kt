package com.obelab.repace.service.messaging

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.features.main.MainActivity

open class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        sendRegistrationToServer(token)
        Functions.showLog("token FCM: ${token}")
    }

    private fun sendRegistrationToServer(token: String?) {
        token?.let {
            PrefManager.saveFireBaseToken(token)

        }
        // TODO: Implement this method to send token to your app server.
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = getString(R.string.project_id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            handleNotificationAndroidO(notificationManager, channelId)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
            .setContentTitle(getString(R.string.app_name))
            .setContentText(remoteMessage.notification?.body)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_app)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1000, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleNotificationAndroidO(
        notificationManager: NotificationManager,
        channelId: String
    ) {
        createNotificationChannel(notificationManager, channelId)
        notificationManager
            .getNotificationChannel(channelId)
            ?.canBypassDnd()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        notificationManager: NotificationManager,
        channelId: String
    ) {
        val notificationChannel =
            NotificationChannel(channelId, getString(R.string.app_name), IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}