package com.obelab.repace.core.util

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.obelab.repace.core.platform.BaseActivity
import java.lang.String

class PermissionHelper {
    companion object {
        const val PERMISSION_STORAGE_REQUEST_CODE = 200
        fun checkStoragePermission(): Boolean {
            return if (SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                val result = BaseActivity.instance?.let { ContextCompat.checkSelfPermission(it, READ_EXTERNAL_STORAGE) }
                val result1 = BaseActivity.instance?.let { ContextCompat.checkSelfPermission(it, WRITE_EXTERNAL_STORAGE) }
                result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
            }
        }

        fun requestStoragePermission() {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(String.format("package:%s", BaseActivity.instance?.getPackageName()))
                    BaseActivity.instance?.startActivityForResult(intent, PERMISSION_STORAGE_REQUEST_CODE)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    BaseActivity.instance?.startActivityForResult(intent, PERMISSION_STORAGE_REQUEST_CODE)
                    BaseActivity.instance?.startActivityForResult(intent, PERMISSION_STORAGE_REQUEST_CODE)
                }
            } else {
                //below android 11
                BaseActivity.instance?.let { ActivityCompat.requestPermissions(it, arrayOf(WRITE_EXTERNAL_STORAGE), PERMISSION_STORAGE_REQUEST_CODE) }
                BaseActivity.instance?.let { ActivityCompat.requestPermissions(it, arrayOf(WRITE_EXTERNAL_STORAGE), PERMISSION_STORAGE_REQUEST_CODE) }
            }
        }
    }
}