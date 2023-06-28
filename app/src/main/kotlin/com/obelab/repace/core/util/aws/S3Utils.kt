package com.obelab.repace.core.util.aws

import android.util.Log
import android.webkit.MimeTypeMap
import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ResponseHeaderOverrides
import com.obelab.repace.core.platform.BaseActivity
import java.util.*


object S3Utils {
    /**
     * Method to generate a presignedurl for the image
     * @param applicationContext context
     * @param path image path
     * @return presignedurl
     */
    @JvmStatic
    fun generates3ShareUrl( path: String?, fileName: String): String {
        val s3client: AmazonS3 = AmazonUtil.getS3Client(BaseActivity.instance)!!
        val expiration = Date()
        var msec = expiration.time
        msec += 7 * 24 * 1000 * 60 * 60.toLong() // 7 days.
        expiration.time = msec
        val overrideHeader = ResponseHeaderOverrides()
        overrideHeader.contentType = getMimeType(path)
        val generatePresignedUrlRequest = GeneratePresignedUrlRequest(AWSKeys.BUCKET_NAME, fileName)
        generatePresignedUrlRequest.method = HttpMethod.GET // Default.
        generatePresignedUrlRequest.expiration = expiration
        generatePresignedUrlRequest.responseHeaders = overrideHeader
        val url = s3client.generatePresignedUrl(generatePresignedUrlRequest)
        Log.e("Generated Url - ", url.toString())
        return url.toString()
    }

    fun generateS3PublicUrl(fileName: String): String {
        return "https://${AWSKeys.BUCKET_NAME}.s3.${AWSKeys.MY_REGION.getName()}.amazonaws.com/${fileName}"
    }

    private fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }
}