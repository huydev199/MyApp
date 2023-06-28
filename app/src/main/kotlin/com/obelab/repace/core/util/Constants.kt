package com.obelab.repace.core.util

import com.obelab.repace.core.functional.Functions

class Constants {
    companion object {
        val SQL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
        val SIMPLE_DATE_FORMAT = "yyyy-MM-dd"
        val DATE_FORMAT_KOREA="MMM dd,yyyy"

        val GENDER_MALE = "male"
        val GENDER_FEMALE = "female"
        val GENDER_OTHER = "other"

        val LANGUAGE_EN = "en" // English
        val LANGUAGE_KO = "ko" // Korean

        val PROFILE_DISCLOSURE_ALL = "all"
        val PROFILE_DISCLOSURE_FRIEND = "ko"
        val PROFILE_DISCLOSURE_NONE = "none"

        val UNIT_METRIC = "metric"
        val UNIT_IMPERIAL = "imperial"

        val UNIT_TYPE_HEIGHT = "UNIT_TYPE_HEIGHT"
        val UNIT_TYPE_WEIGHT = "UNIT_TYPE_WEIGHT"

        val OS_IOS = "iOS"
        val OS_ANDROID = "android"

        val HEIGHT_METRIC = "cm"
        val HEIGHT_IMPERIAL = "in"

        val WEIGHT_METRIC = "kg"
        val WEIGHT_IMPERIAL = "lbs"

        val LIMIT_NOTICES = 10
        val MAX_IMAGE_SIZE = 1024 // 1mb

        //Time pairing
        val timeScan = 30000

        val RUNNING_USER = "running_user"
        val ETC_USER = "ect_user"
        val ORDINARY_USER = "ordinary_user"
        val DEFAULT_USERNAME = "USER NAME"

        val LT_TEST_OUTDOOR = "LT_TEST_OUTDOOR"
        val LT_TEST_TREADMILL = "LT_TEST_TREADMILL"

        val RX_EXERCISE_OUTDOOR = "RX_EXERCISE_OUTDOOR"
        val RX_EXERCISE_TREADMILL = "RX_EXERCISE_TREADMILL"

        val RX_EXERCISE_OUTDOOR_RESULT = "RX_EXERCISE_OUTDOOR_RESULT"
        val RX_EXERCISE_TREADMILL_RESULT = "RX_EXERCISE_TREADMILL_RESULT"

        val LOW_INTENSITY_EXERCISE = "LOW_INTENSITY_EXERCISE"
        val HIGHT_INTENSITY_EXERCISE = "HIGHT_INTENSITY_EXERCISE"

        val FREE_EXERCISE_OUTDOOR = "FREE_EXERCISE_OUTDOOR"
        val FREE_EXERCISE_TREADMILL = "FREE_EXERCISE_TREADMILL"

        val TUTORIAL_SCREEN = "TUTORIAL_SCREEN"
        val MAIN_SCREEN = "MAIN_SCREEN"

        val FREE_EXERCISE_OUTDOOR_RESULT = "FREE_EXERCISE_OUTDOOR_RESULT"
        val FREE_EXERCISE_TREADMILL_RESULT = "FREE_EXERCISE_TREADMILL_RESULT"

        //TYPE DATA SERVICE
        val TYPE_MEASURE_DATA = "TYPE_MEASURE_DATA"
        val TYPE_BATTERY_LEVEL = "TYPE_BATTERY_LEVEL"
        val TYPE_STATUS_VERSION = "TYPE_STATUS_VERSION"
        val TYPE_GAIN_DATA = "TYPE_GAIN_DATA"
        val TYPE_ERROR = "TYPE_ERROR"

        //Constant status
        val IS_SHOWLOG = true
        val IS_TEST = true
        val IS_TEST_EMULATOR = false
        val IS_TEST_MI = false
        val IS_FILTER = false
        val IS_WRITE_LOG = false
        val IS_WRITE_LOG_LTTEST = false

        //TYPE EXERCISE PRESCRIPTION
        val TYPE_EXERCISE_PRESCRIPTION = "TYPE_EXERCISE_PRESCRIPTION"
        val RECOVERY_ABILITY = 1
        val LOW_INTENSITY = 2
        val POLARIZED_TRAINING = 3

        //PUT DATA SHARE RESULT
        val LAST_STAGE = "LAST_STAGE"
        val MAX_SPEED = "MAX_SPEED"
        val TOTAL_DISTANCE = "TOTAL_DISTANCE"
        val TOTAL_DURATION = "TOTAL_DURATION"
        val ONSET = "ONSET"
        val THRESHOLD = "THRESHOLD"
        val CREATE_AT ="CREATE_AT"
        val LISTSMO2="LISTSMO2"
        val LISTHEART_RATE="LISTHEART_RATE"

        //testTypeId
        val treadmill_test = "treadmill_test"
        val outdoor_test = "outdoor_test"
        val status_lt_test_result = 1
        val low_intensity = "low_intensity"
        val high_intensity = "high_intensity"

        val data = "data"
        val notification_access = "notification_access"
        val me_activity = "me_activity"


        //Type Intent
        val type = "type"
        val typeExercise = "typeExercise"
        val moveFragment = "moveFragment"

        // Type Execise Result
        val typeId = "typeId"
        val intensityId = "intensityId"
        val activityId = "activityId"

        // typeId
        val free_exercise = "free_exercise"
        val rx_exercise = "rx_exercise"

        // Type activityId
        val ex_climbing = "ex_climbing"
        val ex_cycling = "ex_cycling"
        val ex_outdoor = "ex_outdoor"
        val ex_treadmill = "ex_treadmill"

        // Mapbox
        const val LAT_DEFAULT: Double = 37.5650172
        const val LON_DEFAULT: Double = 126.8494651
        val ICON_MAP_MARKER_SIZE: Int = Functions.dpToPx(18)
    }

    object LtTestConstant {
        val START_LTTEST_DURATION = if (IS_TEST) 30 else 30
        val LT_TEST_DURATION = if (IS_TEST) 15 else 5000
    }

    object ExerciseConstant {
        val START_EXERCISE_DURATION = if (IS_TEST) 15 else 30
        val EXERCISE_DURATION = 300

        val EXERCISE_LOW_TYPE = 1
        val EXERCISE_POLARIZED_TYPE = 2
    }

    object Countdown {
        val DURATION: Long = if (IS_TEST) 30000 else  30000
        val DURATION_START: Long = if (IS_TEST) 30000 else  300000
        val TIME_INTERVAL: Long = 1000
        val TIME_OUT: Long = if (IS_TEST) 30000 else 30000
    }

}