package com.obelab.repace.features.profilesetting

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.adapter.WheelPickerAdapter
import com.obelab.repace.common.adapter.WheelPickerFeetAdapter
import com.obelab.repace.common.dialog.DatePickerCustomCallback
import com.obelab.repace.common.dialog.DatePickerCustomDialog
import com.obelab.repace.common.dialog.WheelPickerDialog
import com.obelab.repace.common.dialog.WheelPickerImperialDialog
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.FileHelper
import com.obelab.repace.core.util.aws.S3Uploader
import com.obelab.repace.core.util.aws.S3Uploader.S3UploadInterface
import com.obelab.repace.core.util.aws.S3Utils.generateS3PublicUrl
import com.obelab.repace.model.*
import com.obelab.repace.viewModel.HeightWeightViewModel
import com.obelab.repace.viewModel.HomeViewModel
import com.obelab.repace.viewModel.UserUpdateViewModel
import kotlinx.android.synthetic.main.activity_profilesetting.*
import kotlinx.android.synthetic.main.header_back.*
import java.text.SimpleDateFormat
import java.util.*

class ProfileSettingActivity : BaseActivity() {

    private val viewModel: HomeViewModel by viewModels()
    private val viewModelUpdate: UserUpdateViewModel by viewModels()
    private val viewModelInfo: HeightWeightViewModel by viewModels()

    private var listHeightValue: MutableList<Double> = arrayListOf()
    private var listWeightValue: MutableList<Double> = arrayListOf()

    private var birthDate: String? = null
    private var gender: String? = null
    private var height: Double? = null
    private var weight: Double? = null
    private var avatar: String? = null
    private var isNewbirthDate: Boolean = false

    companion object {
        fun callingIntent(context: Context) = Intent(context, ProfileSettingActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profilesetting)

        setUpView()

        with(viewModel) {
            observe(resProfileModel, ::renderProfile)
            failure(failure, ::handleFailure)
        }

        with(viewModelUpdate) {
            observe(resUpdateUserModel, ::renderPutUpdateUser)
            failure(failure, ::handleFailure)
        }

        with(viewModelInfo) {
            observe(resHeightWeightModel, ::renderGetHeightWeight)
            failure(failure, ::handleFailure)
        }

        viewModelInfo.getHeightWeight()
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_profile_setting)
        imvBack.setOnClickListener {
            finish()
        }
        viewModel.getProfile()
        tvLogout.setOnClickListener {
            Functions.logOut()
        }
        edtHeight.setOnClickListener {
            chooseHeight(Functions.getHeightUnitName())
        }
        edtWeight.setOnClickListener {
            chooseWeight(Functions.getWeightUnitName())
        }

        btnSave.setOnClickListener {
            val request = RequestUserUpdateModel()
//            val formattedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
//            val date = formattedDate.parse(birthDate)
////
//            Functions.showLog("date   "+ date)

            val birthDateFomat = birthDate?.let { it1 -> Functions.formatDateKoreaTime(it1) }

            request.gender = gender
            if (birthDateFomat != "") {
                request.birthday = birthDateFomat
            } else {
                request.birthday = birthDate
            }
            request.height = height
            request.weight = weight
            request.avatar = avatar
            request.nickname = edtNickname.text.toString()
            if (request.nickname.toString().length in 4..15) {
                viewModelUpdate.putUserUpdate(request)
            } else {
                showToast(getString(R.string.invalid_nickname))
            }
            Functions.showLog("birthday giang3 " + birthDate?.let { it1 -> Functions.formatDateKoreaTime(it1) })
            Functions.showLog("Date: $birthDate, gender: $gender, height: $height, weight: $weight, nickname: ${edtNickname.text}")

            Functions.showLog("request   " + request)

        }

        civChangeAvatar.setOnClickListener {
            val checkSelfPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            } else {
                ImagePicker.with(this).start()
            }
        }
    }

    private fun uploadImageToS3(imageUri: Uri) {
        val path = FileHelper.getFilePathFromURI(imageUri)
        val fileName = FileHelper.getS3ImageFileName()
        Functions.showLog("path: $path")
        showLoading()
        val s3uploaderObj = S3Uploader(fileName)
        s3uploaderObj.initUpload(path)
        s3uploaderObj.setOns3UploadDone(object : S3UploadInterface {
            override fun onUploadSuccess(response: String?) {
                if (response.equals("Success", ignoreCase = true)) {
                    hideLoading()
                    val urlFromS3 = generateS3PublicUrl(fileName)
                    avatar = urlFromS3
                    if (urlFromS3.isNotEmpty()) {
                        Functions.showLog("Uploaded : $urlFromS3")
                    }
                }
            }

            override fun onUploadError(response: String?) {
                hideLoading()
                Functions.showLog("Error : $response")
            }
        })
    }

    private fun renderProfile(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resProfile: UserInfoModel? = gson.fromJson(dataStr, UserInfoModel::class.java)
            Functions.showLog("resProfileSetting: " + resProfile?.let { Functions.toJsonString(it) })
            setProfile(resProfile)
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun renderPutUpdateUser(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            finish()
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun renderGetHeightWeight(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel?.data?.let { Functions.toJsonString(it) }
            val resHeightWeightModel: ResHeightWeightModel? =
                gson.fromJson(dataStr, ResHeightWeightModel::class.java)
            if (resHeightWeightModel != null) {
                listHeightValue = resHeightWeightModel.heightList
                listWeightValue = resHeightWeightModel.weightList
            }
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Get Profile Error: " + failure.toString())
        hideLoading()
    }

    private fun setProfile(data: UserInfoModel?) {
        edtEmail.setText(data?.email.toString())

        if (data?.nickname != null) {
            edtNickname.setText(data?.nickname.toString())
        }

        if (data?.birthday != null) {
            edtBirthdate.setText(Functions.sqlDateToHumanDate(data?.birthday.toString()).toUpperCase())
            birthDate = Functions.sqlDateToHumanDate(data?.birthday.toString())

            val zonedTime = Functions.sqlDateToDateTime(data?.birthday.toString())
            chooseBirthDate(zonedTime)


        } else {
            edtBirthdate.setText("")
            val zonedTime = Functions.sqlDateToDateTime("2000-01-01T07:12:10.000Z")
            chooseBirthDate(zonedTime)
        }

        if (!data?.gender.isNullOrEmpty()) {
            var genderUser = ""
            if (data?.gender == "male") {
                genderUser = getText(R.string.male).toString()
                edtGender.setText(genderUser.substring(0, 1).toUpperCase() + genderUser.substring(1))
                chooseGender(data?.gender)
                gender = data?.gender

            } else if(data?.gender == "female") {
                genderUser = getText(R.string.female).toString()
                edtGender.setText(genderUser.substring(0, 1).toUpperCase() + genderUser.substring(1))
                chooseGender(data?.gender)
                gender = data?.gender
            } else if(data?.gender == "other"){
                genderUser = getText(R.string.other).toString()
                edtGender.setText(genderUser.substring(0, 1).toUpperCase() + genderUser.substring(1))
                chooseGender(data?.gender)
                gender = data?.gender
            }
        } else {
            chooseGender(Constants.GENDER_MALE)
        }

        if (data?.height != null) {
            if (PrefManager.getUnit() == Constants.UNIT_METRIC) {
                edtHeight.setText("${Functions.getHeightUnitValue(data?.height).toString()} ${Functions.getHeightUnitName()}")
            } else {
                edtHeight.setText(Functions.getImperialHeightValue(data?.height))
            }

            height = Functions.getHeightUnitValue(data?.height)
        }

        if (data?.weight != null) {
            edtWeight.setText("${Functions.getWeightUnitValue(data?.weight).toString()} ${Functions.getWeightUnitName()}")
            weight = Functions.getWeightUnitValue(data?.weight)
        }
        //Set avatar
        Glide.with(this).load(data?.avatar)
            .placeholder(R.drawable.ic_avatar_black)
            .error(R.drawable.ic_avatar_black)
            .into(civAvatar)
    }

    private fun chooseBirthDate(data: Calendar) {
        Functions.showLog("data  " + data)
        //Choose date of birth
        var selectDate: Calendar = Calendar.getInstance()
        val formatDate = SimpleDateFormat("MMM dd, yyyy")
        val newFormat = SimpleDateFormat("yyyy-MM-dd")
        //Default value birthdate

        if (birthDate == null) {
            selectDate.set(Calendar.YEAR, 2000)
            selectDate.set(Calendar.MONTH, 1)
            selectDate.set(Calendar.DAY_OF_MONTH, 1)
        } else {
            selectDate.set(Calendar.YEAR, data.get(Calendar.YEAR))
            selectDate.set(Calendar.MONTH, data.get(Calendar.MONTH))
            selectDate.set(Calendar.DAY_OF_MONTH, data.get(Calendar.DAY_OF_MONTH))
        }
        edtBirthdate.setOnClickListener {

            val dialogDatePicker = DatePickerCustomDialog(
                context = this,
                calendarIn = selectDate,
                callback = DatePickerCustomCallback { calender ->

                    selectDate = calender

                    val date: String = formatDate.format(selectDate.time)
                    val newDate: String = newFormat.format(selectDate.time)
                    birthDate = newDate
                    edtBirthdate.setText(date.toUpperCase())

                }
            )
            dialogDatePicker.showPopup()

/*            val datePicker = DatePickerDialog(
                this,
//                android.R.style.Theme_DeviceDefault_Light_Dialog,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    Functions.showLog("year " + year + " monthOfYear " + monthOfYear + " dayOfMonth" + dayOfMonth)
                    // Display Selected date in textbox
                    selectDate.set(Calendar.YEAR, year)
                    selectDate.set(Calendar.MONTH, monthOfYear)
                    selectDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    var date: String = formatDate.format(selectDate.time)
                    var newDate: String = newFormat.format(selectDate.time)
                    birthDate = newDate
                    edtBirthdate.setText(date.toUpperCase())
                },
                // Default value
                selectDate.get(Calendar.YEAR),
                selectDate.get(Calendar.MONTH),
                selectDate.get(Calendar.DAY_OF_MONTH)
            )
            val dateNow: Calendar = Calendar.getInstance()
            datePicker.datePicker.maxDate = dateNow.timeInMillis
            datePicker.window?.setBackgroundDrawableResource(android.R.color.transparent);
            datePicker.show()*/

        }
    }

    @SuppressLint("ResourceType")
    private fun chooseGender(data: String) {
        var defaultValue = -1;
        var tempValue = -1;
        defaultValue = if (data == getText(R.string.male)) {
            0
        } else 1
        tempValue = if (data == getText(R.string.male)) {
            0
        } else 1

        //Choose gender
        edtGender.setOnClickListener {
            val male = getText(R.string.male).substring(0, 1)
                .toUpperCase() + Constants.GENDER_MALE.substring(1)
            val female = getText(R.string.female).substring(0, 1)
                .toUpperCase() + Constants.GENDER_FEMALE.substring(1)
            val other = getText(R.string.other).substring(0, 1)
                .toUpperCase() + Constants.GENDER_OTHER.substring(1)
            val listItems = arrayOf(male, female, other)
            val listGenders = arrayOf(Constants.GENDER_MALE, Constants.GENDER_FEMALE, Constants.GENDER_OTHER)


            gender = listGenders[defaultValue]

            val mBuilder = AlertDialog.Builder(this)
            mBuilder.setTitle(getString(R.string.choose_gender))
            mBuilder.setSingleChoiceItems(listItems, defaultValue) { dialogInterface, i ->
                tempValue = i
                gender = listGenders[i]
            }

            mBuilder.setPositiveButton(getString(R.string.btn_cancel)) { dialog, which ->
                AlertDialog.BUTTON_POSITIVE
                dialog.cancel()
            }
            mBuilder.setNegativeButton(getString(R.string.btn_ok)) { dialog, which ->
                defaultValue = tempValue
                edtGender.setText(listItems[tempValue])
                dialog.dismiss()
            }
            val mDialog = mBuilder.create()
//            mDialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_gender)
            mDialog.show()

            val posBtn = mDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val negBtn = mDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            posBtn.setTextColor(ContextCompat.getColor(this, R.color.btnColorCommon))
            negBtn.setTextColor(ContextCompat.getColor(this, R.color.btnColorCommon))
        }
    }

    private fun chooseHeight(unit: String) {
        val pickerModel = WheelPickerModel(unit, listHeightValue);

        if (PrefManager.getUnit() == Constants.UNIT_METRIC) {
            val wheelPickerAdapter = WheelPickerAdapter(pickerModel)
            wheelPickerAdapter.unitType = Constants.UNIT_TYPE_HEIGHT
            val wheelPickerDialog = WheelPickerDialog(this)
            wheelPickerDialog.wheelPickerAdapter = wheelPickerAdapter
            wheelPickerDialog.selectedValue = edtHeight.text.toString().replace(" $unit", "").toDouble()
            wheelPickerDialog.showPopup()
            wheelPickerDialog.onClickButtonRight = {
                val result = it.toBigDecimal().toPlainString()
                edtHeight.setText("$result $unit")
                height = result.toDouble()
            }
        } else {
            val listFeetValue: MutableList<Int> = mutableListOf()
            listHeightValue.map {
                Functions.convertCmToFeet(it)?.let { it1 -> listFeetValue.add(it1.toInt()) }
            }
            val wheelPickerAdapter = WheelPickerFeetAdapter(listFeetValue.distinct())
            val wheelPickerDialog = WheelPickerImperialDialog(this)
            var convertValue = Functions.convertCmToFeet(height)!!
            wheelPickerDialog.wheelPickerFeetAdapter = wheelPickerAdapter
            if (Functions.convertFeetToInch(convertValue - convertValue.toInt())!! == 12) {
                wheelPickerDialog.selectedValueFeet = convertValue.toInt() + 1
                wheelPickerDialog.selectedValueInch = 0
            } else {
                wheelPickerDialog.selectedValueFeet = convertValue.toInt()
                wheelPickerDialog.selectedValueInch = Functions.convertFeetToInch(convertValue - convertValue.toInt())!!
            }
            wheelPickerDialog.showPopup()
            wheelPickerDialog.onClickButtonRight = {
                val result = it.toBigDecimal().toDouble()
                edtHeight.setText(Functions.getImperialHeightValue(result))
                height = result
            }
        }
    }

    private fun chooseWeight(unit: String) {
        val pickerModel = WheelPickerModel(unit, listWeightValue);
        val wheelPickerAdapter = WheelPickerAdapter(pickerModel)
        wheelPickerAdapter.unitType = Constants.UNIT_TYPE_WEIGHT
        val wheelPickerDialog = WheelPickerDialog(this)
        wheelPickerDialog.wheelPickerAdapter = wheelPickerAdapter
        if (edtWeight.text.toString().isNotEmpty()) {
            if (unit == Constants.UNIT_METRIC) {
                wheelPickerDialog.selectedValue =
                    edtWeight.text.toString().replace(" $unit", "").toDouble()
                wheelPickerDialog.onClickButtonRight = {
                    val result = it.toBigDecimal().toPlainString()
                    edtWeight.setText("$result $unit")
                    weight = result.toDouble()
                }
            } else {
                wheelPickerDialog.selectedValue =
                    Functions.getWeightFromLbs(edtWeight.text.toString().replace(" $unit", "").toDouble())!!
                wheelPickerDialog.onClickButtonRight = {
                    val result = it.toBigDecimal().toPlainString()
                    edtWeight.setText("$result $unit")
                    weight = Functions.getWeightFromLbs(result.toDouble())
                }
            }
        }

        wheelPickerDialog.showPopup()

    }

    // Touch out to disable keyboard
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data!!
            civAvatar.setImageURI(uri)
            uploadImageToS3(uri)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            showToast(ImagePicker.getError(data))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantedResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)
        when (requestCode) {
            1 -> if (grantedResults.isNotEmpty() && grantedResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePicker.with(this).start()
            }
        }
    }
}