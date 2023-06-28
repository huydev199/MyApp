package com.obelab.repace.features.exercise

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.obelab.repace.R
import com.obelab.repace.common.adapter.ExerciseStatisticAdapter
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.features.empty.StatisticByActivity
import com.obelab.repace.model.ExerciseStatisticSpeedModel
import com.obelab.repace.viewModel.GetExerciseResultViewModel
import kotlinx.android.synthetic.main.fragment_exercise_list_7days.*

class ExerciseList7daysFragment : BaseFragment() {
    private val viewModel: GetExerciseResultViewModel by viewModels()
    private lateinit var layoutManager: LinearLayoutManager
    private var mlistExercise: ArrayList<ExerciseStatisticSpeedModel> = arrayListOf()
    private var type = 1
    private var macitivity=""
    private var typeExercise=""
    private lateinit var mRecyclerView: RecyclerView

    companion object {
        fun callingIntent(context: Context) = Intent(context, ExerciseStatisticDetailActivity::class.java)
    }

    override fun layoutId(): Int {
        return R.layout.fragment_exercise_list_7days
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mAcitivity = activity as ExerciseStatisticDetailActivity
        mlistExercise = mAcitivity.statistic
        type = mAcitivity.currentType
        macitivity= mAcitivity.mAcitivity
        typeExercise=mAcitivity.mType
        settingView()
    }

    private fun settingView() {
        Functions.showLog("Data in setting vew: " + mlistExercise.toString())
        mRecyclerView = lv_statistic
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        lv_statistic.layoutManager = layoutManager
        var exerciseStatisticAdapter = ExerciseStatisticAdapter(mlistExercise, type)
        lv_statistic.adapter = exerciseStatisticAdapter
        ll_rcvStatistic.layoutParams.height = Functions.dpToPx(16 + mlistExercise.size * 36)
        exerciseStatisticAdapter.onClickDetail = {
            Functions.showLog("Data: " + it.toString())
            Functions.showLog("Data type: " + type)
            Functions.showLog("Data mType: "+ typeExercise)
            Functions.showLog("Data macitivity: " + macitivity)
            val intent = Intent(activity, StatisticByActivity::class.java)
            intent.putExtra("data", it)
            intent.putExtra("type", type)
            intent.putExtra("typeExercise",typeExercise )
            intent.putExtra("acitivity",macitivity )
            startActivity(intent)
        }
    }
}