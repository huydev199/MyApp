package com.obelab.repace.features.ltTest

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.Display
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.obelab.library.repace.Externer
import com.obelab.library.repace.data.LTPrescription
import com.obelab.library.repace.data.LTTraining
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.adapter.RecommendationLtTestAdapter
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import kotlinx.android.synthetic.main.activity_exercise_prescription_detail_share.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ExercisePrescriptionDetailShareActivity : BaseActivity() {

    companion object {
        fun callingIntent(context: Context) = Intent(context, ExercisePrescriptionDetailShareActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_prescription_detail_share)
        setUpView()
    }

    private fun setUpView() {

        val exercisePrescriptionType: Int = intent.getIntExtra(Constants.TYPE_EXERCISE_PRESCRIPTION,1)

        val prescriptionData = Externer.getPrescription(PrefManager.getProtocol().protocol,exercisePrescriptionType,PrefManager.getSpeed().toDouble())
        val prescriptionTableData = Externer.getPrescriptionTable(PrefManager.getProtocol().protocol,exercisePrescriptionType,PrefManager.getSpeed().toDouble())

        if (exercisePrescriptionType == 1) {
            setUpRecoveryAbility(prescriptionData)
        } else {
            setUpPolarizedTraining(prescriptionData, prescriptionTableData)
        }

        //SettingDate
        val date = getCurrentDateTime()
        val dateInString = date.toString("MMM dd, yyyy")
        tvTimeResult.text = dateInString

        //Setting profile
        tvUserName.text = PrefManager.getProfile().nickname

        // this is the card view whose screenshot
        // we will take in this article
        // get the view using fin view bt id
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        val display: Display = windowManager.defaultDisplay
        val stageWidth: Int = display.getWidth()
        var height = 0
        Handler().postDelayed({
            if (linearLayout != null) {
                height = linearLayout.measuredHeight
            }
        }, 50)
        Handler().postDelayed({
            val mBitmap = takeScreenshotOfView(linearLayout,height,stageWidth)
            if(mBitmap != null){
                saveMediaToStorage(mBitmap)
            }
        }, 100)
    }

    fun takeScreenshotOfView(view: View, height: Int, width: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap
    }

    //this method saves the image to gallery
    private fun saveMediaToStorage(bitmap: Bitmap) {
        //Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            this.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    shareImage(imageUri)
                }
                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }

            }
        } else {
            //These for devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            shareImage(image)
            fos = FileOutputStream(image)
        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    //Android < Q
    private fun shareImage(file: File) {
        //Android 10
        val uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
        //val uri = Uri.fromFile(file);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"))
        } catch (e: ActivityNotFoundException) {
            Functions.showLog("No App Available")
        }
        finish()
    }

    //Android >= Q
    private fun shareImage(uri: Uri) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"))
        } catch (e: ActivityNotFoundException) {
            Functions.showLog("No App Available")
        }
        finish()
    }

    private fun settingView(listData:  List<LTTraining>) {
        var layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rcvRecommendationLtTest.layoutManager = layoutManager
        var recomendationAdapter = RecommendationLtTestAdapter(listData)
        rcvRecommendationLtTest.adapter = recomendationAdapter
    }

    private fun setUpRecoveryAbility(prescriptionData: LTPrescription){
        llPolarizedTraining.visibility = View.GONE
        llStepsPolarizedTraining.visibility = View.GONE
        tvTitleLtTest.setText(R.string.title_low_exercise)
        tvDescription.setText(R.string.text_low_intensity)
        tvDescriptionContinue.setText(R.string.text_low_intensity_continue)
        tvLeTimeTop.text = prescriptionData.leTime.toString()
        tvLeMinTop.text = prescriptionData.leMin.toString()
        tvSpeedTop.text = prescriptionData.leSpeed.toString()
        tvLeTime.text = prescriptionData.leTime.toString()
        tvSpeed.text = prescriptionData.leSpeed.toString()
        tvLeMin.text = (Functions.getDouble1Decimal((prescriptionData.leMin/60).toDouble())).toString()
        tvLeWeek.text = prescriptionData.leWeek.toString()
        tvLeEndWeek.text = prescriptionData.leWeek.toString()
    }

    private fun setUpPolarizedTraining(prescriptionData: LTPrescription, prescriptionTableData: List<LTTraining>){
        llLowIntensity.visibility = View.GONE
        llStepsLowIntensity.visibility = View.GONE
        tvTitleLtTest.setText(R.string.polarized_training)
        tvDescription.setText(R.string.text_polarized_training)
        tvDescriptionContinue.setText(R.string.text_polarized_training_continue)
        tvLeWeekPolarized.text = prescriptionData.leWeek.toString()
        tvLeEndWeekPolarized.text = prescriptionData.leWeek.toString()
        settingView(prescriptionTableData)
    }
}