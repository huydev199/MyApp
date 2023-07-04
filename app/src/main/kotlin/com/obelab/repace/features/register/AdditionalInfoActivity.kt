package com.obelab.repace.features.register

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.common.adapter.WheelPickerAdapter
import com.obelab.repace.common.adapter.WheelPickerFeetAdapter
import com.obelab.repace.common.dialog.ConfirmDialog
import com.obelab.repace.common.dialog.WheelPickerDialog
import com.obelab.repace.common.dialog.WheelPickerImperialDialog
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.Constants.Companion.GENDER_FEMALE
import com.obelab.repace.core.util.Constants.Companion.GENDER_MALE
import com.obelab.repace.core.util.Constants.Companion.GENDER_OTHER
import com.obelab.repace.features.exercise.ExerciseCompleteActivity
import com.obelab.repace.features.preference.PreferenceActivity
import com.obelab.repace.model.*
import com.obelab.repace.viewModel.HeightWeightViewModel
import com.obelab.repace.viewModel.UserUpdateViewModel
import kotlinx.android.synthetic.main.activity_additional_info.*
import kotlinx.android.synthetic.main.activity_additional_info.edtBirthdate
import kotlinx.android.synthetic.main.activity_additional_info.edtGender
import kotlinx.android.synthetic.main.activity_additional_info.edtHeight
import kotlinx.android.synthetic.main.activity_additional_info.edtWeight
import kotlinx.android.synthetic.main.activity_profilesetting.*
import kotlinx.android.synthetic.main.header_back.*
import java.text.SimpleDateFormat
import java.util.*

class AdditionalInfoActivity : BaseActivity() {
    companion object {
        const val KEY_EMAIL = "KEY_EMAIL"
        const val KEY_PASS_WORD = "KEY_PASSWORD"
        const val KEY_NICKNAME = "KEY_NICK_NAME"
        const val KEY_IS_SOCIAL = "KEY_IS_SOCIAL"
        fun callingIntent(
            context: Context,
            email: String?,
            password: String?,
            nickname: String?,
            isSocial: Boolean
        ): Intent {
            var intent = Intent(context, AdditionalInfoActivity::class.java)
            intent.putExtra(KEY_EMAIL, email)
            intent.putExtra(KEY_PASS_WORD, password)
            intent.putExtra(KEY_NICKNAME, nickname)
            intent.putExtra(KEY_IS_SOCIAL, isSocial)

            return intent
        }
    }

    private val viewModel: UserUpdateViewModel by viewModels()
    private val viewModelInfo: HeightWeightViewModel by viewModels()

    private var listHeightValue: MutableList<Double> = arrayListOf()
    private var listWeightValue: MutableList<Double> = arrayListOf()

    private var birthDate: String? = null
    private var gender: String? = null
    private var height: Double? = null
    private var weight: Double? = null
    private var nickname: String? = null
    private var email: String? = null
    private var password: String? = null
    private var isSocial: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_additional_info)
        setUpView()
        getDataFromIntent()

        with(viewModel) {
            observe(resRegisUserModel, ::renderPutRegistUser)
            failure(failure, ::handleFailure)
        }

        with(viewModelInfo) {
            observe(resHeightWeightModel, ::renderGetHeightWeight)
            failure(failure, ::handleFailure)
        }

        with(viewModel) {
            observe(resUpdateUserModel, ::renderPutRegistUser)
            failure(failure, ::handleFailure)
        }

        viewModelInfo.getHeightWeight()

        checkDataValid()
        val token = PrefManager.getToken()
        Functions.showLog("tokenPrefAdditionalInfo: " + token)
    }

    private fun getDataFromIntent() {
        nickname = intent.getStringExtra(KEY_NICKNAME)
        email = intent.getStringExtra(KEY_EMAIL)
        password = intent.getStringExtra(KEY_PASS_WORD)
        isSocial = intent.getBooleanExtra(KEY_IS_SOCIAL, false)
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_additional_info)

        btnSetPreference.setOnClickListener {
            startActivity(PreferenceActivity.callingIntent(this, isRegister = true))
        }

        //Button Update Info
        btnNext.setOnClickListener {

            if (!isSocial && checkDataValid()) {
                val request = RequestRegisterModel()
                request.gender = gender
                request.birthday = birthDate
                request.height = height
                request.weight = weight
                request.nickname = nickname
                request.email = email
                request.password = password
                request.fcmToken = PrefManager.getFireBaseToken()
//                viewModel.postUserRegister(request)

            }
            if(!isSocial && !checkDataValid()){
                Toast.makeText(this, "Insufficient Information Please Enter Info",Toast.LENGTH_LONG).show()
            }
            if (checkDataValid() == true) {
                val request = RequestUserUpdateModel()
                request.gender = gender
                request.birthday = birthDate
                request.height = height
                request.weight = weight
                request.nickname = nickname
                request.email = email
                request.password = password

                viewModel.putUserUpdate(request)

            }
        }

        tvSkip.setOnClickListener {
            if (!isSocial) {
                val request = RequestRegisterModel()
                request.nickname = nickname
                request.email = email
                request.password = password
                request.fcmToken = PrefManager.getFireBaseToken()
//                viewModel.postUserRegister(request)
            } else {
                val request = RequestUserUpdateModel()
                request.nickname = nickname
                request.email = email
                request.password = password
                viewModel.putUserUpdate(request)
            }


        }

        imvBack.setOnClickListener {
            finish()
        }

        chooseBirthDate()

        chooseGender()

        edtHeight.setOnClickListener {
            chooseHeight(Functions.getHeightUnitName())
        }

        edtWeight.setOnClickListener {
            chooseWeight(Functions.getWeightUnitName())
        }

    }

    private fun checkDataValid(): Boolean {
        if (birthDate != null && gender != null && weight != null && height != null) {
            btnNext.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorTextPrimary
                )
            )
            btnNext.setBackgroundResource(R.drawable.btn_enable);
            return true
        } else {
            btnNext.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorText
                )
            )
            btnNext.setBackgroundResource(R.drawable.btn_disable);
            return false
        }
    }

    private fun renderPutRegistUser(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resLoginModel: ResLoginModel? = gson.fromJson(dataStr, ResLoginModel::class.java)
            Functions.showLog("tokenPrefAdditionalInfo: " + "resUpdateInfoModel: " + resLoginModel?.let {
                Functions.toJsonString(it)
            })
            resLoginModel?.token?.let { PrefManager.saveToken(it) }
            startActivity(TutorialActivity.callingIntent(this))
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
            Functions.showLog("List Height:" + Functions.toJsonString(listHeightValue))
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("updateInfoError: " + failure.toString())
        hideLoading()
    }

    private fun chooseHeight(unit: String) {
        val pickerModel = WheelPickerModel(unit, listHeightValue);

        if (PrefManager.getUnit() == Constants.UNIT_METRIC) {
            val wheelPickerAdapter = WheelPickerAdapter(pickerModel)
            wheelPickerAdapter.unitType = Constants.UNIT_TYPE_HEIGHT
            val wheelPickerDialog = WheelPickerDialog(this)
            wheelPickerDialog.wheelPickerAdapter = wheelPickerAdapter
            if (edtHeight.text.toString() != "") {
                wheelPickerDialog.selectedValue =
                    edtHeight.text.toString().replace(" $unit", "").toDouble()
            }
            wheelPickerDialog.showPopup()
            wheelPickerDialog.onClickButtonRight = {
                val result = it.toBigDecimal().toPlainString()
                edtHeight.setText("$result $unit")
                height = result.toDouble()
                checkDataValid()
                id_height.setPadding(0, 5.dp, 0, 0)
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
                wheelPickerDialog.selectedValueInch =
                    Functions.convertFeetToInch(convertValue - convertValue.toInt())!!
            }
            wheelPickerDialog.showPopup()
            wheelPickerDialog.onClickButtonRight = {
                val result = it.toBigDecimal().toDouble()
                edtHeight.setText(Functions.getImperialHeightValue(result))
                height = result
                checkDataValid()
                id_height.setPadding(0, 5.dp, 0, 0)
            }
        }
    }

    private fun chooseWeight(unit: String) {
        val pickerModel = WheelPickerModel(unit, listWeightValue);
        var wheelPickerAdapter = WheelPickerAdapter(pickerModel)
        wheelPickerAdapter.unitType = Constants.UNIT_TYPE_WEIGHT
        val wheelPickerDialog = WheelPickerDialog(this)
        wheelPickerDialog.wheelPickerAdapter = wheelPickerAdapter
        if (edtWeight.text.toString().isNotEmpty()) {
            wheelPickerDialog.selectedValue =
                edtWeight.text.toString().replace(" $unit", "").toDouble()

        }
        wheelPickerDialog.showPopup()
        wheelPickerDialog.onClickButtonRight = {
            val result = it.toBigDecimal().toPlainString()
            edtWeight.setText("$result $unit")
            weight = result.toDouble()
            checkDataValid()
            id_weight.setPadding(0, 5.dp, 0, 0)
        }
    }

    private fun chooseBirthDate() {
        //Choose date of birth
        val selectDate: Calendar = Calendar.getInstance()
        val formatDate = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
        var newFormat = SimpleDateFormat("yyyy-MM-dd")

        var setDate = false
        edtBirthdate.setOnClickListener {

            if (!setDate) {
                selectDate.set(Calendar.YEAR, 2000)
                selectDate.set(Calendar.MONTH, 1)
                selectDate.set(Calendar.DAY_OF_MONTH, 1)
            }
            val datepicker = DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    // Display Selected date in textbox
                    selectDate.set(Calendar.YEAR, year)
                    selectDate.set(Calendar.MONTH, monthOfYear)
                    selectDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    var date: String = formatDate.format(selectDate.time)
                    var newDate: String = newFormat.format(selectDate.time)
                    birthDate = newDate
                    setDate = true
                    edtBirthdate.setText(date.toUpperCase())
                    checkDataValid()
                    birthdate.setPadding(0, 5.dp, 0, 0)
                },
                // Default value
                selectDate.get(Calendar.YEAR),
                selectDate.get(Calendar.MONTH),
                selectDate.get(Calendar.DAY_OF_MONTH)
            )
            val dateNow: Calendar = Calendar.getInstance()
            datepicker.datePicker.maxDate = dateNow.timeInMillis
            datepicker.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent);
            datepicker.setTitle("Birth Date");
            datepicker.show()
        }
    }

    private fun chooseGender() {
        var defaultValue = -1;
        var tempGender = gender
        var tempText = edtGender.text.toString()
        //Choose gender
        edtGender.setOnClickListener {
            val male = GENDER_MALE.substring(0, 1).toUpperCase() + GENDER_MALE.substring(1)
            val female = GENDER_FEMALE.substring(0, 1).toUpperCase() + GENDER_FEMALE.substring(1)
            val other = GENDER_OTHER.substring(0, 1).toUpperCase() + GENDER_OTHER.substring(1)
            val listItems = arrayOf(male, female, other)
            val listGenders = arrayOf(GENDER_MALE, GENDER_FEMALE, GENDER_OTHER)
            val mBuilder = AlertDialog.Builder(this)
            mBuilder.setTitle(getString(R.string.choose_gender))
            mBuilder.setSingleChoiceItems(listItems, defaultValue) { dialogInterface, i ->
                tempGender = listGenders[i]
                tempText = listItems[i]
                defaultValue = i
                checkDataValid()
                id_gender.setPadding(0, 5.dp, 0, 0)
            }
            mBuilder.setPositiveButton(getString(R.string.btn_cancel)) { dialog, which ->
                dialog.cancel()
            }
            mBuilder.setNegativeButton(getString(R.string.btn_ok)) { dialog, which ->
                edtGender.setText(tempText)
                gender = tempGender
                dialog.dismiss()
            }
            val mDialog = mBuilder.create()
            mDialog.show()

            val posBtn = mDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val negBtn = mDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            posBtn.setTextColor(ContextCompat.getColor(this, R.color.btnColorCommon))
            negBtn.setTextColor(ContextCompat.getColor(this, R.color.btnColorCommon))
        }
    }

    // Touch out to disable keyboard
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    //Convert to dp
    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}