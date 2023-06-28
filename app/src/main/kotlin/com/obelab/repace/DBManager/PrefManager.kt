package com.obelab.repace.DBManager

import com.google.gson.Gson
import com.obelab.library.repace.data.LTProtocol
import com.obelab.repace.DBManager.PreferenceHelper.get
import com.obelab.repace.DBManager.PreferenceHelper.set
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.model.*

object PrefManager {
    const val TOKEN = "TOKEN"
    const val FIRE_BASE_TOKEN = "FIRE_BASE_TOKEN"
    const val UNIT = "UNIT"
    const val LANGUAGE = "LANGUAGE"
    const val PROFILE = "PROFILE"
    const val INSTRUCTIONS = "INSTRUCTIONS"
    const val DISTANCE = "DISTANCE"
    const val PROTOCOL = "PROTOCOL"
    const val LTANALYSIS = "LTANALYSIS"
    const val LISTRSO2 = "LISTRSO2"
    const val ALLLISTSMO2 = "ALLLISTSMO2"
    const val BASELINERSO2 = "BASELINERSO2"
    const val SAVEDEVICE = "SAVEDEVICE"
    const val LACTATE = "LACTATE"
    const val STAGE = "STAGE"
    const val SPEED = "SPEED"
    const val SPEED_PURPOSE = "SPEED_PURPOSE"
    const val TOTALDISTANCE = "TOTALDISTANCE"
    const val LTTESTPERFORMANCE = "LTTESTPERFORMANCE"
    const val LISTSMO2INSTAGE = "LISTSMO2INSTAGE"
    const val EXERCISE_PURPOSE = "EXERCISE_PURPOSE"
    const val EXERCISE_PRESCRIPTION = "EXERCISE_PRESCRIPTION"
    const val TIME_START = "TIME_START"
    const val FREE_DURATION = "FREE_DURATION"
    const val EXERCISE_SPEED = "EXERCISE_SPEED"
    const val IS_SPEAKER_TURN_ON = "IS_SPEAKER_TURN_ON"
    const val IS_NOT_FIRST_OPEN_APP = "IS_NOT_FIRST_OPEN_APP"
    const val TL_TEST_LOCATION_LIST = "TL_TEST_LOCATION_LIST"
    const val EXERCISE_LOCATION_LIST = "EXERCISE_LOCATION_LIST"

    val gson = Gson()

    fun saveTypeId(value: String) = PreferenceHelper.defaultPrefs().set(Constants.typeId, value)
    fun getTypeId(): String = PreferenceHelper.defaultPrefs()[Constants.typeId]

    fun saveIntensityId(value: String) = PreferenceHelper.defaultPrefs().set(Constants.intensityId, value)
    fun getIntensityId(): String = PreferenceHelper.defaultPrefs()[Constants.intensityId]

    fun saveActivityId(value: String) = PreferenceHelper.defaultPrefs().set(Constants.activityId, value)
    fun getActivityId(): String = PreferenceHelper.defaultPrefs()[Constants.activityId]

    fun saveToken(token: String) = PreferenceHelper.defaultPrefs().set(TOKEN, token)
    fun getToken(): String = PreferenceHelper.defaultPrefs()[TOKEN]

    fun saveFireBaseToken(firebaseToken: String) = PreferenceHelper.defaultPrefs().set(FIRE_BASE_TOKEN, firebaseToken)
    fun getFireBaseToken(): String = PreferenceHelper.defaultPrefs()[FIRE_BASE_TOKEN]


    fun saveDistance(distance: String) = PreferenceHelper.defaultPrefs().set(DISTANCE, distance)
    fun getDistance(): String = PreferenceHelper.defaultPrefs()[DISTANCE]

    fun saveDeviceMacAddress(isSave: String) = PreferenceHelper.defaultPrefs().set(SAVEDEVICE, isSave)
    fun getDeviceMacAddress(): String = PreferenceHelper.defaultPrefs()[SAVEDEVICE]

    fun saveShowInstructions(isChecked: Boolean) = PreferenceHelper.defaultPrefs().set(INSTRUCTIONS, isChecked)
    fun getShowInstructions(): Boolean = PreferenceHelper.defaultPrefs()[INSTRUCTIONS]

    fun saveExercisePurpose(data: Boolean) = PreferenceHelper.defaultPrefs().set(EXERCISE_PURPOSE, data)
    fun getExercisePurpose(): Boolean = PreferenceHelper.defaultPrefs()[EXERCISE_PURPOSE]

    fun saveUnit(unit: String) = PreferenceHelper.defaultPrefs().set(UNIT, unit)
    fun getUnit(): String {
        var unit: String = PreferenceHelper.defaultPrefs()[UNIT]
        if (unit.isEmpty()) {
            unit = Constants.UNIT_METRIC
        }
        return unit
    }

    fun saveLanguage(language: String) = PreferenceHelper.defaultPrefs().set(LANGUAGE, language)
    fun getLanguage(): String {
        var language: String = PreferenceHelper.defaultPrefs()[LANGUAGE]
        if (language.isEmpty()) {
            language = Constants.LANGUAGE_EN
        }
        return language
    }

    fun saveProfile(profile: Any) {
        PreferenceHelper.defaultPrefs()[PROFILE] = Functions.toJsonString(profile)
    }

    fun getProfile(): UserInfoModel {
        return try {
            val profileStr: String = PreferenceHelper.defaultPrefs()[PROFILE]
            gson.fromJson(profileStr, UserInfoModel::class.java)
        } catch (e: Exception) {
            UserInfoModel.empty
        }
    }

    fun saveTimeStart(data: Any) = PreferenceHelper.defaultPrefs().set(TIME_START, Functions.toJsonString(data))
    fun getTimeStart(): CalendarModel {
        return try {
            val data: String = PreferenceHelper.defaultPrefs()[TIME_START]
            gson.fromJson(data, CalendarModel::class.java)
        } catch (e: Exception) {
            CalendarModel.empty
        }
    }

    fun saveListSMO2(rso2: Any) = PreferenceHelper.defaultPrefs().set(LISTRSO2, Functions.toJsonString(rso2))
    fun getListSMO2(): ListSMO2Model {
        return try {
            val rso2: String = PreferenceHelper.defaultPrefs()[LISTRSO2]
            gson.fromJson(rso2, ListSMO2Model::class.java)
        } catch (e: Exception) {
            ListSMO2Model.empty
        }
    }

    fun saveAllListSMO2(rso2: Any) = PreferenceHelper.defaultPrefs().set(ALLLISTSMO2, Functions.toJsonString(rso2))
    fun getAllListSMO2(): ListSMO2Model {
        return try {
            val rso2: String = PreferenceHelper.defaultPrefs()[ALLLISTSMO2]
            gson.fromJson(rso2, ListSMO2Model::class.java)
        } catch (e: Exception) {
            ListSMO2Model.empty
        }
    }

    fun saveListSmo2InStage(data: Any) = PreferenceHelper.defaultPrefs().set(LISTSMO2INSTAGE, Functions.toJsonString(data))
    fun getListSmo2InStage(): ListSmo2LineChartModel {
        return try {
            val rso2: String = PreferenceHelper.defaultPrefs()[LISTSMO2INSTAGE]
            gson.fromJson(rso2, ListSmo2LineChartModel::class.java)
        } catch (e: Exception) {
            ListSmo2LineChartModel.empty
        }
    }

    fun saveListLtTestPerformance(data: Any) = PreferenceHelper.defaultPrefs().set(LTTESTPERFORMANCE, Functions.toJsonString(data))
    fun getListLtTestPerformance(): ListLtTestPerformance {
        return try {
            val rso2: String = PreferenceHelper.defaultPrefs()[LTTESTPERFORMANCE]
            gson.fromJson(rso2, ListLtTestPerformance::class.java)
        } catch (e: Exception) {
            ListLtTestPerformance.empty
        }
    }

    fun saveStage(stage: Int) = PreferenceHelper.defaultPrefs().set(STAGE, stage)
    fun getStage(): Int {
        var stage: Int = PreferenceHelper.defaultPrefs()[STAGE]
        if (stage == -1) {
            stage = 0
        }
        return stage
    }

    fun saveFreeDuration(duration: Int) = PreferenceHelper.defaultPrefs().set(FREE_DURATION, duration)
    fun getFreeDuration(): Int {
        var duration: Int = PreferenceHelper.defaultPrefs()[FREE_DURATION]
        if (duration == -1) {
            duration = 0
        }
        return duration
    }

    fun saveBaseLineSMO2(baseline: Float) = PreferenceHelper.defaultPrefs().set(BASELINERSO2, baseline)
    fun getBaseLineSMO2(): Float {
        var baseline: Float = PreferenceHelper.defaultPrefs()[BASELINERSO2]
        if (baseline == -1f) {
            baseline = 0.0F
        }
        return baseline
    }

    fun saveLactate(lactate: Float) = PreferenceHelper.defaultPrefs().set(LACTATE, lactate)
    fun getLactate(): Float {
        var lactate: Float = PreferenceHelper.defaultPrefs()[LACTATE]
        if (lactate == -1f) {
            lactate = 0.0F
        }
        return lactate
    }

    fun saveSpeed(lactate: Float) = PreferenceHelper.defaultPrefs().set(SPEED, lactate)
    fun getSpeed(): Float {
        var speed: Float = PreferenceHelper.defaultPrefs()[SPEED]
        if (speed == -1f) {
            speed = 0.0F
        }
        return speed
    }

    fun saveSpeedPurpose(lactate: Float) = PreferenceHelper.defaultPrefs().set(SPEED_PURPOSE, lactate)
    fun getSpeedPurpose(): Float {
        var speed: Float = PreferenceHelper.defaultPrefs()[SPEED_PURPOSE]
        if (speed == -1f) {
            speed = 0.0F
        }
        return speed
    }

    fun setTotalDistance(totalDistance: Float) = PreferenceHelper.defaultPrefs().set(TOTALDISTANCE, totalDistance)
    fun getTotalDistance(): Float {
        return PreferenceHelper.defaultPrefs()[TOTALDISTANCE]
    }

    fun saveProtocol(protocol: Any) = PreferenceHelper.defaultPrefs().set(PROTOCOL, Functions.toJsonString(protocol))
    fun getProtocol(): LTProtocol {
        return try {
            val protocol: String = PreferenceHelper.defaultPrefs()[PROTOCOL]
            gson.fromJson(protocol, LTProtocol::class.java)
        } catch (e: Exception) {
            LTProtocol()
        }
    }

    fun saveAnalysis(analysis: Any) = PreferenceHelper.defaultPrefs().set(LTANALYSIS, Functions.toJsonString(analysis))
    fun getAnalysis(): ListAnalysisModel {
        return try {
            val analysis: String = PreferenceHelper.defaultPrefs()[LTANALYSIS]
            gson.fromJson(analysis, ListAnalysisModel::class.java)
        } catch (e: Exception) {
            ListAnalysisModel.empty
        }
    }

    fun saveExercisePrescription(exercisePrescription: Any) = PreferenceHelper.defaultPrefs().set(EXERCISE_PRESCRIPTION, Functions.toJsonString(exercisePrescription))
    fun getExercisePrescription(): ExercisePrescriptionModel {
        return try {
            val exercisePrescriptionStr: String =
                PreferenceHelper.defaultPrefs()[EXERCISE_PRESCRIPTION]
            val prescription = gson.fromJson(exercisePrescriptionStr, ExercisePrescriptionModel::class.java)
            prescription.session = prescription.session.sortedBy { it.session }
            prescription
        } catch (e: Exception) {
            ExercisePrescriptionModel.empty
        }
    }

    fun saveListExerciseSpeed(data: Any) = PreferenceHelper.defaultPrefs().set(EXERCISE_SPEED, Functions.toJsonString(data))
    fun getListExerciseSpeed(): ExerciseArraySpeedModel {
        return try {
            val exerciseSpeed: String = PreferenceHelper.defaultPrefs()[EXERCISE_SPEED]
            gson.fromJson(exerciseSpeed, ExerciseArraySpeedModel::class.java)
        } catch (e: Exception) {
            ExerciseArraySpeedModel.empty
        }
    }

    fun saveIsSpeakerTurnOn(turnOn: Boolean){
        val result = ResMemberSettingModel()
        if(turnOn){
            result.guide = 1
        } else  result.guide = 0
        MainActivity.instance?.preferenceViewModel?.putUpdateMemberSetting(result)
        PreferenceHelper.defaultPrefs()[IS_SPEAKER_TURN_ON] = turnOn
    }
    fun getIsSpeakerTurnOn(): Boolean = PreferenceHelper.defaultPrefs()[IS_SPEAKER_TURN_ON]

    fun saveIsNotFirstOpenApp(turnOn: Boolean) = PreferenceHelper.defaultPrefs().set(IS_NOT_FIRST_OPEN_APP, turnOn)
    fun getIsNotFirstOpenApp(): Boolean = PreferenceHelper.defaultPrefs()[IS_NOT_FIRST_OPEN_APP]

    fun saveLTTestLocationList(data: Any) = PreferenceHelper.defaultPrefs().set(TL_TEST_LOCATION_LIST, Functions.toJsonString(data))
    fun getLTTestLocationList(): MutableList<LocationModel> {
        return try {
            val data: String = PreferenceHelper.defaultPrefs()[TL_TEST_LOCATION_LIST]
            gson.fromJson(data, Array<LocationModel>::class.java).toMutableList()
        } catch (e: Exception) {
            ArrayList()
        }
    }

    fun saveExerciseLocationList(data: Any) = PreferenceHelper.defaultPrefs().set(EXERCISE_LOCATION_LIST, Functions.toJsonString(data))
    fun getExerciseLocationList(): MutableList<LocationModel> {
        return try {
            val data: String = PreferenceHelper.defaultPrefs()[EXERCISE_LOCATION_LIST]
            gson.fromJson(data, Array<LocationModel>::class.java).toMutableList()
        } catch (e: Exception) {
            ArrayList()
        }
    }
}
