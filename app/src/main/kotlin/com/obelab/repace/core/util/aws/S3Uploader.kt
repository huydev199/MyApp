package com.obelab.repace.core.util.aws

import android.util.Log
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.model.ObjectMetadata
import com.obelab.repace.core.platform.BaseActivity
import java.io.File

class S3Uploader(private val fileName: String) {
    private val transferUtility: TransferUtility? = BaseActivity.instance?.let { AmazonUtil.getTransferUtility(it) }
    private var s3UploadInterface: S3UploadInterface? = null
    fun initUpload(filePath: String?) {
        val file = File(filePath)
        val myObjectMetadata = ObjectMetadata()
        myObjectMetadata.contentType = "image/png"
        val observer = transferUtility!!.upload(AWSKeys.BUCKET_NAME, fileName, file)
        observer.setTransferListener(UploadListener())
    }

    private inner class UploadListener : TransferListener {
        // Simply updates the UI list when notified.
        override fun onError(id: Int, e: Exception) {
            Log.e(TAG, "Error during upload: $id", e)
            s3UploadInterface!!.onUploadError(e.toString())
            s3UploadInterface!!.onUploadError("Error")
        }

        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
            Log.d(
                TAG, String.format(
                    "onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent
                )
            )
        }

        override fun onStateChanged(id: Int, newState: TransferState) {
            Log.d(TAG, "onStateChanged: $id, $newState")
            if (newState == TransferState.COMPLETED) {
                s3UploadInterface!!.onUploadSuccess("Success")
            }
        }
    }

    fun setOns3UploadDone(s3UploadInterface: S3UploadInterface?) {
        this.s3UploadInterface = s3UploadInterface
    }

    interface S3UploadInterface {
        fun onUploadSuccess(response: String?)
        fun onUploadError(response: String?)
    }

    companion object {
        private const val TAG = "S3Uploader"
    }

}