package com.obelab.repace.core.functional

import android.content.res.Resources
import android.util.Log
import com.google.gson.Gson
import com.obelab.library.repace.data.LTAnalysis
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.FileHelper
import com.obelab.repace.features.welcome.WelcomeActivity
import com.obelab.repace.model.*
import okhttp3.internal.and
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.math.roundToInt


class Functions {
    companion object {
        fun isEmailValid(email: String): Boolean {
            return Pattern.compile(
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
            ).matcher(email).matches()
        }

        fun isPasswordValid(password: String): Boolean {
            return Pattern.compile(
                "^(?=.*[0-9])(?=.*[a-z])(?=\\S+\$).{8,10}"
            ).matcher(password).matches()
        }

        fun toJsonString(data: Any): String {
            return Gson().toJson(data)
        }

        fun showLog(message: String) {
            if (Constants.IS_SHOWLOG == true) {
                Log.d("REPACE_LOG", message)
            }
        }

        fun showLog(tag: String, message: String) {
            if (Constants.IS_SHOWLOG) {
                showLog("$tag -> $message")
            }
            if (Constants.IS_WRITE_LOG) {
                FileHelper.saveLog(tag, message)
            }
        }

        fun sqlDateToHumanDate(sqlDate: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SQL_DATE_FORMAT)
                val formatter = SimpleDateFormat("MMM dd, yyyy")
                val output = formatter.format(parser.parse(sqlDate))
                output
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun simpleDatetoHumanDate(sqlDate: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT)
                val formatter = SimpleDateFormat("MMM dd, yyyy")
                val output = formatter.format(parser.parse(sqlDate))
                output
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun partDateTimeDetail(sqlDate: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT)
                val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss")
                val output = formatter.format(parser.parse(sqlDate))
                output
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        // Output: AUG 15, 2021
        fun partDateTimeDetailShort(sqlDate: String): String? {
            return try {
                val parser = SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT)
                val formatter = SimpleDateFormat("MMM dd, yyyy")
                val output = formatter.format(parser.parse(sqlDate))
                output
            } catch (e: Exception) {
                showLog(e.toString())
                null
            }
        }

        fun dateToDayInDay(date: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT)

                val dueDate = SimpleDateFormat("MMM dd, yyyy")

                val output2 = dueDate.format(parser.parse(date))
                val out = "${output2}"
                out
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun dateInDay(date: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT)
                val dueDate = SimpleDateFormat("yyyy-MM-dd")
                val output2 = dueDate.format(parser.parse(date))
                output2
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun dateToDayInWeek(start: String, end: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT)

                val startDate = SimpleDateFormat("MMM dd")
                val endDate = SimpleDateFormat("MMM dd,yyyy")
                val output1 = startDate.format(parser.parse(start))
                val output2 = endDate.format(parser.parse(end))
                val out = "${output1}~${output2}"
                out
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun dateToDayInMonth(start: String): String {
            return try {
//                val c1 = Calendar.getInstance()
                val parser = SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT)

                val startDate = SimpleDateFormat("MM/dd")
                val endDate = SimpleDateFormat("dd")
                val output1 = startDate.format(parser.parse(start))
                val output2 = endDate.format(parser.parse(start))


                val month = output2.get(Calendar.DATE) + 6

                Functions.showLog("month, ${month}")

                val out = "${output1}~${month}"
                out
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun simpleDateToDayInWeek(date: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT)
                val formatter = SimpleDateFormat("MMM dd")
                val dueDate = SimpleDateFormat("MMM dd, yyyy")
                val output1 = formatter.format(parser.parse(date))
                val output2 = dueDate.format(parser.parse(date)).plus(7)
                val out = "${output1}~${output2}"
                out
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun simpleDateToDayInMonth(date: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT)
                val formatter = SimpleDateFormat("MMM dd")
                val dueDate = SimpleDateFormat("MMM dd, yyyy")
                val output1 = formatter.format(parser.parse(date))
                val output2 = dueDate.format(parser.parse(date)).plus(30)
                val out = "${output1}~${output2}"
                out
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun simpleDatetoMD(date: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT)
                val formatter = SimpleDateFormat("MM/dd")
                val output = formatter.format(parser.parse(date))
                output
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun simpleDateToWeek(date: String, dateEnd: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT)
                val formatter = SimpleDateFormat("MM/dd")
                val toDate = SimpleDateFormat("dd")
                val output = formatter.format(parser.parse(date))
                val output2 = toDate.format(parser.parse(dateEnd))
                val out = "${output}~${output2}"
                out
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun simpleDatetoMM(sqlDate: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT)
                val formatter = SimpleDateFormat("MM")
                val output = formatter.format(parser.parse(sqlDate))
                output
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun formatDateTime(sqlDate: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SQL_DATE_FORMAT)
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val output = formatter.format(parser.parse(sqlDate))
                output
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }
        fun formatDateKoreaTime(sqlDate: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.DATE_FORMAT_KOREA)
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val output = formatter.format(parser.parse(sqlDate))
                output
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun formatDateToMoth(sqlDate: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SQL_DATE_FORMAT)
                val formatter = SimpleDateFormat("MMM dd")
                val output = formatter.format(parser.parse(sqlDate))
                output
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun formatDateToYear(sqlDate: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SQL_DATE_FORMAT)
                val formatter = SimpleDateFormat("MMM dd, yyyy")
                val output = formatter.format(parser.parse(sqlDate))
                output
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun formatCalendarToString(cal: Calendar): String {
            return try {
                val date = "${getYearFromCalendar(cal)}-${cal.time.month + 1}-${formatNumber(cal.time.date)}"
                date
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun formatDateToMY(sqlDate: String): String {
            return try {
                val parser = SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT)
                val formatter = SimpleDateFormat("MMM, yyyy")
                val output = formatter.format(parser.parse(sqlDate))
                output
            } catch (e: Exception) {
                showLog(e.toString())
                ""
            }
        }

        fun formatStringToCalendar(sqlDate: String): Calendar {
            return try {
                val formatter = SimpleDateFormat("yyyy-MM-dd")
                val date: Date = formatter.parse(sqlDate);
                val calender = Calendar.getInstance()
                calender.time = date
                calender
            } catch (e: Exception) {
                Calendar.getInstance()
            }
        }

        fun sqlDateToDateTime(sqlDate: String): Calendar {
            return try {
                val df: DateFormat = SimpleDateFormat(Constants.SQL_DATE_FORMAT)
                val cal = Calendar.getInstance()
                cal.time = df.parse(sqlDate)
                cal
            } catch (e: Exception) {
                Calendar.getInstance()
            }
        }


        fun formatSecondToHumanTime(second: Double): String {
            val hour = (second / 3600).toInt()
            val min = ((second - (hour * 3600)) / 60).toInt()
            val sec = (second - hour * 3600 - min * 60).toInt()
            val strHour: String
            val strMin: String
            val strSec: String
            if (hour < 10)
                strHour = "0${hour}"
            else
                strHour = hour.toString()
            if (min < 10)
                strMin = "0${min}"
            else
                strMin = min.toString()
            if (sec < 10)
                strSec = "0${min}"
            else
                strSec = sec.toString()
            return "${strHour}:${strMin}:${strSec}"
        }

        fun formatNumber(number: Int): String {
            return if (number > 9) {
                number.toString()
            } else {
                "0$number"
            }
        }

        fun getMonthNameFromCalendar(cal: Calendar): String {
            //var monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)
            val month_date = SimpleDateFormat("MMM", Locale.ENGLISH)
            val month_name = month_date.format(cal.getTime())
            return month_name
        }

        fun getYearFromCalendar(cal: Calendar): String {
            val month_date = SimpleDateFormat("yyyy")
            val month_name = month_date.format(cal.getTime())
            return month_name
        }

        fun getHeightUnitName(): String {
            val unit = PrefManager.getUnit()
            return if (unit == Constants.UNIT_METRIC) {
                Constants.HEIGHT_METRIC
            } else {
                Constants.HEIGHT_IMPERIAL
            }
        }

        fun getWeightUnitName(): String {
            val unit = PrefManager.getUnit()
            return if (unit == Constants.UNIT_METRIC) {
                Constants.WEIGHT_METRIC
            } else {
                Constants.WEIGHT_IMPERIAL
            }
        }

        fun getDouble1Decimal(number: Double): Double {
            val number3digits: Double = (number * 1000.0).roundToInt() / 1000.0
            val number2digits: Double = (number3digits * 100.0).roundToInt() / 100.0
            return (number2digits * 10.0).roundToInt() / 10.0
        }

        fun getFloatDecimal(number: Float): Double {

            val number3digits: Double = (number * 1000.0).roundToInt() / 1000.0
            val number2digits: Double = (number3digits * 100.0).roundToInt() / 100.0
            return (number2digits * 10.0).roundToInt() / 10.0
        }

        fun getHeightUnitValue(cm: Double?): Double? {
            if (cm != null) {
                return getDouble1Decimal(cm)
            }
            return 0.0
        }

        fun getImperialHeightValue(cm: Double?): String {
            if (cm != null) {

                var feetValue = convertCmToFeet(cm)
                if (feetValue != null) {
                    if (convertFeetToInch(feetValue - feetValue.toInt()) == 12){
                        return "${feetValue.toInt() + 1} ft  0 in"
                    } else return "${feetValue.toInt()} ft  ${convertFeetToInch(feetValue - feetValue.toInt())} in"
                }
            }
            return ""
        }

        fun revertImperialHeight(cm: Double?): ImperialModel? {
            val unit = PrefManager.getUnit()
            if (cm != null) {
                val feetValue = convertCmToFeet(cm)
                if (feetValue != null) {
                    return convertFeetToInch(feetValue - feetValue.toInt())?.let { ImperialModel(feetValue.toInt(), it) }
                }
            }
            return ImperialModel.empty
        }

        fun getHeightFromIn(inUnit: Double?): Double? {
            val unit = PrefManager.getUnit()
            if (inUnit != null) {
                return if (unit == Constants.UNIT_METRIC) {
                    inUnit
                } else {
                    getDouble1Decimal(inUnit / 0.393701)
                }
            }
            return 0.0
        }

        fun convertCmToFeet(cm: Double?): Double? {
            val unit = PrefManager.getUnit()
            if (cm != null) {
                return if (unit == Constants.UNIT_METRIC) {
                    cm
                } else {
                    cm * 0.032808
                }
            }
            return 0.0
        }

        fun convertFeetToCm(feet: Int?): Double? {
            val unit = PrefManager.getUnit()
            if (feet != null) {
                return if (unit == Constants.UNIT_METRIC) {
                    getDouble1Decimal(feet.toDouble())
                } else {
                    getDouble1Decimal(feet / 0.032808)
                }
            }
            return 0.0
        }

        fun convertInchToCm(inch: Int?): Double? {
            val unit = PrefManager.getUnit()
            if (inch != null) {
                return if (unit == Constants.UNIT_METRIC) {
                    getDouble1Decimal(inch.toDouble())
                } else {
                    getDouble1Decimal(inch / 0.39370)
                }
            }
            return 0.0
        }

        fun convertCmToInch(cm: Double?): Double? {
            val unit = PrefManager.getUnit()
            if (cm != null) {
                return if (unit == Constants.UNIT_METRIC) {
                    getDouble1Decimal(cm)
                } else {
                    getDouble1Decimal(cm * 0.39370)
                }
            }
            return 0.0
        }

        fun convertFeetToInch(feet: Double?): Int? {
            val unit = PrefManager.getUnit()
            if (feet != null) {
                return if (unit == Constants.UNIT_METRIC) {
                    Math.round(feet).toInt()
                } else {
                    Math.round(feet * 12).toInt()
                }
            }
            return 0
        }

        fun convertInchToFeet(feet: Int?): Double {
            val unit = PrefManager.getUnit()
            if (feet != null) {
                return if (unit == Constants.UNIT_METRIC) {
                    feet.toDouble()
                } else {
                    getDouble1Decimal((feet / 12).toDouble())
                }
            }
            return 0.0
        }

        fun getWeightUnitValue(kg: Double?): Double? {
            val unit = PrefManager.getUnit()
            if (kg != null) {
                return if (unit == Constants.UNIT_METRIC) {
                    kg
                } else {
                    getDouble1Decimal(kg * 2.205)
                }
            }
            return 0.0
        }

        fun getWeightFromLbs(lbsUnit: Double?): Double? {
            val unit = PrefManager.getUnit()
            if (lbsUnit != null) {
                return if (unit == Constants.UNIT_METRIC) {
                    lbsUnit
                } else {
                    getDouble1Decimal(lbsUnit / 2.205)
                }
            }
            return 0.0
        }

        fun logOut() {
            PrefManager.saveToken("");
            BaseActivity.instance?.startActivity(BaseActivity.instance?.let {
                WelcomeActivity.callingIntent(
                    it
                )
            })
        }

        fun setLocale(languageCode: String?) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val resources: Resources? = BaseActivity.instance?.resources
            val config = resources?.configuration
            config?.setLocale(locale)
            resources?.updateConfiguration(config, resources.displayMetrics)
        }

        fun pxToDp(px: Int): Int {
            return (px / Resources.getSystem().displayMetrics.density).toInt()
        }

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        fun convertPx(px: Int, margin: Int): Int {
            val convertToDp = (px / Resources.getSystem().displayMetrics.density).toInt()
            val currentDisplay = convertToDp - margin
            return (currentDisplay * Resources.getSystem().displayMetrics.density).toInt()
        }

        fun setAge(startDate: String): Long {
            if (startDate != null) {
                try {
                    val simpleDateFormat: DateFormat = SimpleDateFormat(Constants.SQL_DATE_FORMAT)
                    val cal: Calendar = Calendar.getInstance()
                    cal.setTime(Date())
                    cal.add(Calendar.HOUR, 24)
                    val currentDate = simpleDateFormat.format(Date())
                    var date1: Date? = null
                    var date2: Date? = null
                    // calculating the difference b/w startDate and endDate
                    date1 = simpleDateFormat.parse(startDate) as Date?
                    date2 = simpleDateFormat.parse(currentDate) as Date?
                    val getDiff = date2!!.time - date1!!.time

                    // using TimeUnit class from java.util.concurrent package
                    val getDaysDiff: Long = TimeUnit.MILLISECONDS.toDays(getDiff) / 365
                    return getDaysDiff
                } catch (e: Exception) {
                    return 0
                }

            } else {
                return 0
            }
        }

        fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }

        @ExperimentalUnsignedTypes
        fun toHexString(arr: ByteArray) =
            arr.asUByteArray().joinToString(" ") { "0x" + it.toString(16).padStart(2, '0') }

        fun dateToString(date: Date, format: String, locale: Locale = Locale.getDefault()): String {
            val formatter = SimpleDateFormat(format, locale)
            return formatter.format(date)
        }

        fun convertRSO2(rso2: ArrayList<Byte>): Double {
            var result = (rso2[0] and 0xFF shl 8 or (rso2[1] and 0xFF)) / 10.0
            return if (result >100) 100.0 else getDouble1Decimal(result.toDouble())
        }

        fun getCurrentDateTime(): Date {
            return Calendar.getInstance().time
        }

        fun getCurrentSqlDate(): String {
            return dateToString(Calendar.getInstance().time, Constants.SQL_DATE_FORMAT)
        }

        fun resetData() {
            PrefManager.saveStage(1)
            PrefManager.saveListSmo2InStage(ListSmo2LineChartModel.empty)
            PrefManager.saveListSMO2(ListSMO2Model.empty)
            PrefManager.saveListLtTestPerformance(ListLtTestPerformance.empty)
            PrefManager.saveBaseLineSMO2(0.0F)
            PrefManager.saveAnalysis(LTAnalysis())
            PrefManager.saveLactate(0.0F)
            PrefManager.setTotalDistance(0.0.toFloat())
            PrefManager.saveListExerciseSpeed(ExerciseArraySpeedModel.empty)
        }

        fun getLtRawData(data: ByteArray): ArrayList<Byte> {
            val rawData = arrayListOf<Byte>()
            for (i in 9..56) {
                rawData.add(data[i])
            }
            return rawData
        }

        fun knotsToKm(number: Double): Double {
            return number * 1.852
        }

        fun convertMsToKm(distance: Double): Double {
            return (distance * 3.6) / 10
        }

        fun distanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val theta = lon1 - lon2
            var dist =
                Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(
                    deg2rad(lat2)
                ) * Math.cos(deg2rad(theta))
            dist = Math.acos(dist)
            dist = rad2deg(dist)
            dist = dist * 60 * 1.1515
            dist = dist * 1.609344
            return dist
        }

        private fun deg2rad(deg: Double): Double {
            return deg * Math.PI / 180.0
        }

        private fun rad2deg(rad: Double): Double {
            return rad * 180.0 / Math.PI
        }

        fun timeInHour(time: Double): Double {
            return time / 360
        }

        fun convertStringState(data: Int): String {
            if (data > 9) {
                return data.toString()
            } else {
                return "0${data}"
            }
        }

        fun getTodayId(): String {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH) + 1
            val day = c.get(Calendar.DAY_OF_MONTH)
            return "${year}_${month}_${day}"
        }
        fun convertTimeSecondary(time: Int): String {
            var minute = (time / 60)
            var second = (time % 60)
            var result = "-"
            if (minute > 9) {
                if (second > 9) {
                    result = "$minute’ $second’’"
                } else {
                    result = "$minute’ 0$second’’"
                }
            } else {
                if (second > 9) {
                    result = "0$minute’ $second’’"
                } else {
                    result = "0$minute’ 0$second’’"
                }
            }
            return result
        }
    }
}