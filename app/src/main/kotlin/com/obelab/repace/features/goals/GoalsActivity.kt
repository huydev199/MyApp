package com.obelab.repace.features.goals

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Display
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.obelab.repace.R
import com.obelab.repace.common.adapter.GoalsAdapter
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.model.GoalsModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.viewModel.GoalsViewModel
import kotlinx.android.synthetic.main.activity_goals.*
import kotlinx.android.synthetic.main.header_back_transparent.*

class GoalsActivity : BaseActivity() {

    companion object {
        fun callingIntent(context: Context) = Intent(context, GoalsActivity::class.java)
    }

    private val viewModel: GoalsViewModel by viewModels()
    private val totalItem = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)
        setUpView()
        with(viewModel) {
            observe(resGoal, ::renderGoals)
            failure(failure, ::handleFailure)
        }

        viewModel.getGoals()
    }

    private fun renderGoals(resBaseModel: ResBaseModel?) {
        Functions.showLog("resUpdateMemberSetting: " + resBaseModel?.let { Functions.toJsonString(it) })
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel?.data?.let { Functions.toJsonString(it) }
            val goals = gson.fromJson(dataStr, GoalsModel::class.java)
            settingView(goals.trophy_cnt, goals.medal_cnt, goals.badge_cnt)
        } else {
            resBaseModel?.msg?.let { Functions.showLog(it) }
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Show goals error: " + failure.toString())
        hideLoading()
    }

    private fun setUpView() {
        imvBack.setOnClickListener {
            finish()
        }
        tvTitle.text = getString(R.string.title_goals)
        val display: Display = windowManager.defaultDisplay
        val width: Int = display.getWidth()
        animation_view.layoutParams.height = width * 3 / 4
        llBgGoals.layoutParams.height = width * 3 / 4
        settingView(0, 0, 0)
    }

    private fun settingView(trophy_cnt: Int, medal_cnt: Int, badge_cnt: Int) {
        // Type 0 is trophy, type 1 is medal, type 2 is badge
        // Set up recylerview
        var trophyRecyclerView: RecyclerView
        var trophyLayoutManager: LinearLayoutManager
        trophyRecyclerView = findViewById(R.id.rcvTrophy)
        trophyLayoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        trophyRecyclerView.layoutManager = trophyLayoutManager
        var trophyAdapter = GoalsAdapter(0, totalItem, trophy_cnt)
        trophyRecyclerView.adapter = trophyAdapter

        var medalRecyclerView: RecyclerView
        var medalLayoutManager: LinearLayoutManager
        medalRecyclerView = findViewById(R.id.rcvMedal)
        medalLayoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        medalRecyclerView.layoutManager = medalLayoutManager
        var medalAdapter = GoalsAdapter(1, totalItem, medal_cnt)
        medalRecyclerView.adapter = medalAdapter

        var badgeRecyclerView: RecyclerView
        var badgeLayoutManager: LinearLayoutManager
        badgeRecyclerView = findViewById(R.id.rcvBadge)
        badgeLayoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        badgeRecyclerView.layoutManager = badgeLayoutManager
        var badgeAdapter = GoalsAdapter(1, totalItem, badge_cnt)
        badgeRecyclerView.adapter = badgeAdapter
    }
}