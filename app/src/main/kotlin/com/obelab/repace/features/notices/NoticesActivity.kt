package com.obelab.repace.features.notices

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.obelab.repace.R
import com.obelab.repace.common.adapter.NoticeAdapter
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.model.NoticeModel
import com.obelab.repace.model.ResAllNoticesModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.viewModel.NoticesViewModel
import kotlinx.android.synthetic.main.header_back.*

class NoticesActivity : BaseActivity() {

    private val viewModel: NoticesViewModel by viewModels()
    private lateinit var layoutManager: LinearLayoutManager
    private var mListNotice : ArrayList<NoticeModel> = arrayListOf()
    companion object {
        fun callingIntent(context: Context) = Intent(context, NoticesActivity::class.java)
    }

    private lateinit var mRecyclerView: RecyclerView
    private var page = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notices)
        setUpView()

        with(viewModel) {
            observe(resNoticesModel, ::renderAllNotices)
            failure(failure, ::handleFailure)
        }

        viewModel.getAllNotices(page)
    }

    private fun setUpView() {
        tvTitle.text = getText(R.string.title_notices)
        imvBack.setOnClickListener {
            finish()
        }
        var swipeLayout: SwipeRefreshLayout = findViewById(R.id.swipe_container)
        swipeLayout.setOnRefreshListener {
            page = 0
            mListNotice.clear()
            viewModel.getAllNotices(page)
            swipeLayout.isRefreshing = false
        }
    }

    private fun renderAllNotices(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel?.data?.let { Functions.toJsonString(it) }
            val listData = gson.fromJson(dataStr, ResAllNoticesModel::class.java)
            Functions.showLog("Data: " + listData.toString())
            settingView(listData)
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun settingView(listNotice: ResAllNoticesModel) {
        for(item in listNotice.data){
            mListNotice.add(item)
        }
        mRecyclerView = findViewById(R.id.rcvListNotice)
        layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mRecyclerView.layoutManager = layoutManager
        var noticeAdapter = NoticeAdapter(mListNotice)
        mRecyclerView.adapter = noticeAdapter

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(dy>0){
                    val visibleItemCount: Int = layoutManager.childCount
                    val pastVisibleItem: Int = layoutManager.findFirstCompletelyVisibleItemPosition()
                    val total: Int = noticeAdapter.itemCount
                    if ((visibleItemCount+pastVisibleItem)>= total && page+1 <listNotice.totalPages){
                        page++
                        viewModel.getAllNotices(page)
                    }
                }
            }
        })

        noticeAdapter.onClickDetail = {
            Functions.showLog("Data: " + it.toString())
            val intent = Intent(this, NoticeDetailActivity::class.java)
            intent.putExtra("data", it)
            startActivity(intent)
        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Show notices error: " + failure.toString())
        hideLoading()
    }
}