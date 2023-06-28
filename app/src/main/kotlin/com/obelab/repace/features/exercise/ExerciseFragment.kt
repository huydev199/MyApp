package com.obelab.repace.features.exercise

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.Display
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.core.util.Constants
import com.obelab.repace.features.ltTest.ExercisePrescriptionActivity
import com.obelab.repace.features.ltTest.LtTestFragment
import com.obelab.repace.features.ltTest.PreLtTestOutdoorActivity
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.model.LocationModel
import com.obelab.repace.model.LtTestResultModel
import com.obelab.repace.model.ResAllLtTestHistoryModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.viewModel.GetExerciseResultViewModel
import com.obelab.repace.viewModel.LtTestHistoryViewModel
import kotlinx.android.synthetic.main.fragment_exercise.*

class ExerciseFragment: BaseFragment() {

    private val TAG = "ExerciseFragment"
    private  val viewModel: LtTestHistoryViewModel by viewModels()
    private val viewModelExcise: GetExerciseResultViewModel by viewModels()

    companion object {
        fun callingIntent(context: Context) = Intent(context, LtTestFragment::class.java)
    }
    override fun layoutId(): Int {
        return R.layout.fragment_exercise
    }

    override fun onResume() {
        super.onResume()
        onResetView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel){
            observe(resLtTestHistoryModel, ::renderAllHistory)
            failure(failure, ::handleFailure)
        }

        with(viewModelExcise) {
            observe(resLtTestResultModel, ::renderLtTestResult)
            failure(failure, ::handleFailure)
        }

        //viewModel.getAllLtTestHistory()
        viewModelExcise.getLtTestResult(true)

        setUpView()
        val display: Display = requireActivity().windowManager.defaultDisplay
        val stageWidth: Int = display.getWidth()

        var width = Functions.convertPx(stageWidth, 96)

        btn_treadmill.layoutParams.width = (width/2)
        btn_treadmill.layoutParams.height = (width/2)

        btn_outdoor.layoutParams.width = (width/2)
        btn_outdoor.layoutParams.height = (width/2)

        btn_free_exercise_treadmill.layoutParams.width = (width/2)
        btn_free_exercise_treadmill.layoutParams.height = (width/2)

        btn_free_exercise_outdoor.layoutParams.width =(width/2)
        btn_free_exercise_outdoor.layoutParams.height = (width/2)

        btn_free_cycling.layoutParams.width = (width/2)
        btn_free_cycling.layoutParams.height = (width/2)

        btn_free_mountain.layoutParams.width = (width/2)
        btn_free_mountain.layoutParams.height = (width/2)

    }

    private fun setUpView() {

        val isExercisePurpose = PrefManager.getExercisePurpose()


        btn_treadmill.setOnClickListener{
            btn_treadmill.setBackgroundResource(R.drawable.ic_treadmill_run_boder)
            btn_outdoor.setBackgroundResource(R.drawable.ic_outdoor_run)
            btn_free_exercise_treadmill.setBackgroundResource(R.drawable.btn_exercise_treadmill)
            btn_free_exercise_outdoor.setBackgroundResource(R.drawable.btn_exercise_outdoor)
            PrefManager.saveTypeId(Constants.rx_exercise)
            PrefManager.saveActivityId(Constants.ex_treadmill)
            Handler().postDelayed({
                if(isExercisePurpose == true){
                    startActivity(context?.let { it1 ->
                        ExercisePrescriptionActivity.callingIntentFromExercise(
                            it1,true)
                    })
                } else {
                    val mainActivity = activity as MainActivity
                    mainActivity.goToExerciseCalendarFragment(Constants.RX_EXERCISE_TREADMILL)
                }
            }, 50)
        }

        btn_outdoor.setOnClickListener{
            btn_outdoor.setBackgroundResource(R.drawable.ic_outdoor_run_border)
            btn_treadmill.setBackgroundResource(R.drawable.ic_treadmill_run)
            btn_free_exercise_treadmill.setBackgroundResource(R.drawable.btn_exercise_treadmill)
            btn_free_exercise_outdoor.setBackgroundResource(R.drawable.btn_exercise_outdoor)
            PrefManager.saveTypeId(Constants.rx_exercise)
            PrefManager.saveActivityId(Constants.ex_outdoor)
            Handler().postDelayed({
                if(isExercisePurpose == true){
                    startActivity(context?.let { it1 ->
                        ExercisePrescriptionActivity.callingIntentFromExercise(
                            it1,true)
                    })
                } else {
                    if (view?.context?.let { it1 -> ContextCompat.checkSelfPermission(it1, Manifest.permission.ACCESS_FINE_LOCATION ) } !== PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions(activity as MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1 )
                    } else {
                        val mainActivity = activity as MainActivity
                        mainActivity.goToExerciseCalendarFragment(Constants.RX_EXERCISE_OUTDOOR)
                    }
                }
                //startActivity(context?.let { it1 -> PreRxExerciseOutdoorActivity.callingIntent(it1) })
            }, 50)
        }

        btn_free_exercise_treadmill.setOnClickListener {
            PrefManager.saveTypeId(Constants.free_exercise)
            PrefManager.saveActivityId(Constants.ex_treadmill)
            btn_free_exercise_treadmill.setBackgroundResource(R.drawable.btn_exercise_treadmill_border)
            btn_outdoor.setBackgroundResource(R.drawable.ic_outdoor_run)
            btn_treadmill.setBackgroundResource(R.drawable.ic_treadmill_run)
            btn_free_exercise_outdoor.setBackgroundResource(R.drawable.btn_exercise_outdoor)
            val intent = Intent(context, PreFreeExerciseTreadmillAcitivity::class.java)
            intent.putExtra(Constants.type, Constants.FREE_EXERCISE_TREADMILL)
            startActivity(intent)
        }

        btn_free_exercise_outdoor.setOnClickListener {
            PrefManager.saveTypeId(Constants.free_exercise)
            PrefManager.saveActivityId(Constants.ex_outdoor)
            btn_free_exercise_outdoor.setBackgroundResource(R.drawable.btn_free_exercise_outdoor_border)
            btn_outdoor.setBackgroundResource(R.drawable.ic_outdoor_run)
            btn_treadmill.setBackgroundResource(R.drawable.ic_treadmill_run)
            btn_free_exercise_treadmill.setBackgroundResource(R.drawable.btn_exercise_treadmill)
            val intent = Intent(context, PreFreeExerciseOutdoorActivity::class.java)
            intent.putExtra(Constants.type, Constants.FREE_EXERCISE_OUTDOOR)
            if (view?.context?.let { it1 -> ContextCompat.checkSelfPermission(it1, Manifest.permission.ACCESS_FINE_LOCATION ) } !== PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(activity as MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1 )
            } else {
                startActivity(intent)
            }
            PrefManager.saveExerciseLocationList(ArrayList<LocationModel>())
        }
    }

    private fun onResetView(){
        btn_treadmill.setBackgroundResource(R.drawable.ic_treadmill_run)
        btn_outdoor.setBackgroundResource(R.drawable.ic_outdoor_run)
        btn_free_exercise_treadmill.setBackgroundResource(R.drawable.btn_exercise_treadmill)
        btn_free_exercise_outdoor.setBackgroundResource(R.drawable.btn_exercise_outdoor)
    }

    private fun renderAllHistory(resBaseModel: ResBaseModel?) {
        hideLoading()
        Functions.showLog("resBaseModel: "+ resBaseModel?.let { Functions.toJsonString(it) })
        if (resBaseModel?.success == true) {
            val gson = GsonBuilder().create()
            Functions.showLog("dataStr: "+ resBaseModel.data?.let { Functions.toJsonString(it) })
            val dataList = gson.fromJson(resBaseModel.data?.let { Functions.toJsonString(it) },Array<ResAllLtTestHistoryModel>::class.java).toList()
            if (dataList.isNotEmpty()){
                settingLtTest()
            } else {
                settingLtTestNoData()
            }
        } else {
            resBaseModel?.msg?.let { Functions.showLog(TAG,it) }
        }
    }

    private fun renderLtTestResult(resBaseModel: ResBaseModel?) {
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resLtTestResult: LtTestResultModel? =
                gson.fromJson(dataStr, LtTestResultModel::class.java)
            if(resLtTestResult != null){
                if (resLtTestResult.totalStages != 0) {
                    settingLtTest()
                } else {
                    settingLtTestNoData()
                }
            }
        } else {
            resBaseModel?.msg?.let { Functions.showLog(TAG, it) }
        }
    }

    private fun settingLtTestNoData(){
        llLtTestNoData.visibility = View.VISIBLE
        clRxExercise.visibility = View.GONE
        btnGoToLtTest.setOnClickListener {
            MainActivity.instance?.goToLtTestFragment()
        }
        imvRxExerciseHistory.setImageResource(R.drawable.ic_history_exercise_disable)
        imvFreeExerciseHistory.setImageResource(R.drawable.ic_history_exercise_disable)
    }

    private fun settingLtTest(){
        llLtTestNoData.visibility = View.GONE
        clRxExercise.visibility = View.VISIBLE
        imvRxExerciseHistory.setImageResource(R.drawable.ic_history_exercise_enable)
        imvFreeExerciseHistory.setImageResource(R.drawable.ic_history_exercise_enable)
        imvRxExerciseHistory.setOnClickListener {
            startActivity(context?.let { it1 -> ExerciseHistoryActivity.callingIntent(it1) })
        }
        imvFreeExerciseHistory.setOnClickListener {
            startActivity(context?.let { it1 -> ExerciseHistoryActivity.callingIntent(it1) })
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Show all history error: " + failure.toString())
        hideLoading()
    }
}