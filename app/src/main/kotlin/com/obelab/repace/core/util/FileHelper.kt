package com.obelab.repace.core.util

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FileHelper {
    companion object {
        private fun getImageFileName(): String {
            val sdf = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss")
            val sub = sdf.format(Date())
            val profile = PrefManager.getProfile()
            return "user_${profile.id}_$sub.png"
        }

        fun getS3ImageFileName(): String {
            val profile = PrefManager.getProfile()
            return "app/user_${profile.id}/${getImageFileName()}"
        }

        fun getFilePathFromURI(selectedImageUri: Uri): String {
            var filePath = ""
            if (selectedImageUri.path?.contains("/storage") == true) {
                filePath = selectedImageUri.path!!
            } else {
                val wholeID = DocumentsContract.getDocumentId(selectedImageUri)

                // Split at colon, use second item in the array
                val id = wholeID.split(":".toRegex()).toTypedArray()[1]
                val column = arrayOf(MediaStore.Images.Media.DATA)

                // where id is equal to
                val sel = MediaStore.Images.Media._ID + "=?"
                val cursor = BaseActivity.instance?.contentResolver?.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, arrayOf(id), null)
                val columnIndex = cursor!!.getColumnIndex(column[0])
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex)
                }
                cursor.close()
            }
            return filePath
        }

        fun saveLog(title: String, content: String) {
            try {
                // Check direction
                val extStorageDirectory = Environment.getExternalStorageDirectory().toString() + "/Download/Repace"
                val dir = File(extStorageDirectory)
                if (!dir.exists())
                    dir.mkdirs()
                // Append text to file
                val date = Functions.getCurrentDateTime()
                val dateNow = Functions.dateToString(date,"yyyy_MM_dd")
                val myExternalFile = File(extStorageDirectory, "RepaceLog_$dateNow.txt")
                val fileOutPutStream = FileOutputStream(myExternalFile, true)
                val dateTimeNow = Functions.dateToString(date,"yyyy/MM/dd HH:mm:ss")
                fileOutPutStream.write("\n".toByteArray() + dateTimeNow.toByteArray() + ": ".toByteArray() + title.toByteArray() + "-> ".toByteArray() + content.toByteArray())
                fileOutPutStream.close()
            } catch (e: Exception) {
                Functions.showLog("saveLogError: $e")
            }
        }

        fun saveLogData(content: String) {
            try {
                // Check direction
                val extStorageDirectory = Environment.getExternalStorageDirectory().toString() + "/Download/Repace"
                val dir = File(extStorageDirectory)
                if (!dir.exists())
                    dir.mkdirs()
                // Append text to file
                val date = Functions.getCurrentDateTime()
                val dateNow = Functions.dateToString(date,"yyyyMMdd")
                val myExternalFile = File(extStorageDirectory, "LTTest_$dateNow.txt")
                val fileOutPutStream = FileOutputStream(myExternalFile, true)
                val dateTimeNow = Functions.dateToString(date,"yyyy/MM/dd HH:mm:ss")
                fileOutPutStream.write("\n".toByteArray() + dateTimeNow.toByteArray() + " Received: ".toByteArray() + content.toByteArray())
                fileOutPutStream.close()
            } catch (e: Exception) {
                Functions.showLog("saveLogError: $e")
            }
        }

        fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
            val width: Int = bm.width
            val height: Int = bm.height
            val scaleWidth = newWidth.toFloat() / width
            val scaleHeight = newHeight.toFloat() / height
            val matrix = Matrix()
            matrix.postScale(scaleWidth, scaleHeight)
            val resizedBitmap: Bitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false
            )
            bm.recycle()
            return resizedBitmap
        }
    }
}