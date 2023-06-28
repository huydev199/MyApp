package com.obelab.repace.features.exercise

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import com.obelab.repace.DBManager.DatabaseHelper
import com.obelab.repace.R
import com.obelab.repace.common.adapter.ExerciseCalendarAdapter
import com.obelab.repace.common.adapter.RxExerciseDetailHistoryAdapter
import com.obelab.repace.common.customview.UntouchableRecyclerView
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.model.DayExerciseModel
import com.obelab.repace.model.ExerciseResultModel
import com.obelab.repace.model.RequestCalendarModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.viewModel.CalendarExerciseViewModel
import kotlinx.android.synthetic.main.activity_exercise_history.*
import kotlinx.android.synthetic.main.activity_exercise_history.exFiveCalendar
import kotlinx.android.synthetic.main.activity_exercise_history.exFiveMonthYearText
import kotlinx.android.synthetic.main.activity_exercise_history.exFiveNextMonthImage
import kotlinx.android.synthetic.main.activity_exercise_history.exFivePreviousMonthImage
import kotlinx.android.synthetic.main.activity_exercise_history.exFiveRv
import kotlinx.android.synthetic.main.calendar_day.view.*
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import kotlinx.android.synthetic.main.fragment_exercise_calendar.*
import kotlinx.android.synthetic.main.header_back.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

class ExerciseHistoryActivity : BaseActivity() {

    private val viewModel: CalendarExerciseViewModel by viewModels()

    private var selectedDate: LocalDate? = null
    private var monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)
//    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mDayRecyclerView: UntouchableRecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    var currentMonth = YearMonth.now()

    companion object {
        fun callingIntent(context: Context) = Intent(context, ExerciseHistoryActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_history)
        with(viewModel) {
            observe(resCalendarExercise, ::renderCalendarExercise)
            failure(failure, ::handleFailure)
        }
        /** Fix bug #74, disable loading for this screen */
        //showLoading()
        viewModel.getCalendarExercise(
            RequestCalendarModel(
                currentMonth.monthValue,
                currentMonth.year
            )
        )
        setUpView()
    }

    fun setUpView() {
        imvBack.setOnClickListener {
            finish()
        }
        tvTitle.text = getString(R.string.title_exercise_history)
    }

    private fun renderCalendarExercise(resBaseModel: ResBaseModel?) {
        hideLoading()
        val dataList = DatabaseHelper.instance.getDayExercisesByMonthYear(
            currentMonth.monthValue,
            currentMonth.year
        )
        setUpCalendar(dataList)
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("getCalendarExerciseError: " + failure.toString())
        val dataList = DatabaseHelper.instance.getDayExercisesByMonthYear(
            currentMonth.monthValue,
            currentMonth.year
        )
        setUpCalendar(dataList)
    }

    private fun setUpCalendar(listCalendar: List<DayExerciseModel>) {
        exFiveRv.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        }

        val daysOfWeek = daysOfWeekFromLocale()

        exFiveCalendar.setup(
            currentMonth.minusMonths(10),
            currentMonth.plusMonths(10),
            daysOfWeek.first()
        )


        exFiveCalendar.scrollToMonth(currentMonth)
        exFiveCalendar.maxRowCount = 6
        exFiveCalendar.daySize = CalendarView.sizeAutoWidth(Functions.dpToPx(80))

//        exFiveCalendar.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
//            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
//                return rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING
//            }
//        })
//
        exFiveCalendar.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return if (e.action == MotionEvent.ACTION_MOVE) {
                    true
                } else {
                    super.onInterceptTouchEvent(rv, e)
                }
            }
        })

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            exFiveCalendar.notifyDateChanged(day.date)
                            oldDate?.let { exFiveCalendar.notifyDateChanged(it) }
                        }
                    }
                }
            }
        }


        exFiveCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                val textView = container.view.exFiveDayText
                val layout = container.view.exFiveDayLayout
                container.day = day


                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.setTextColorRes(R.color.textGray)
                    if (selectedDate == day.date) {
                        layout.setBackgroundResource(R.drawable.calendar_selected_bg)
                        settingDetailHistory(listCalendar, day)
                    } else {
                        layout.setBackgroundResource(0)
                    }
                    if (listCalendar.size > day.date.dayOfMonth - 1) {
                        var dataInDay = listCalendar[day.date.dayOfMonth - 1].exercise

                        if (dataInDay != null) {
                            mDayRecyclerView = container.view.rcvCalendarExercise
                            layoutManager = LinearLayoutManager(
                                this@ExerciseHistoryActivity,
                                RecyclerView.VERTICAL,
                                false
                            )
                            mDayRecyclerView.layoutManager = layoutManager
                            var excerciseCalendarAdapter = ExerciseCalendarAdapter(
                                dataInDay as ArrayList<ExerciseResultModel>,
                                true
                            )
                            mDayRecyclerView.adapter = excerciseCalendarAdapter

                        }
                    }
                } else {
                    textView.setTextColorRes(R.color.textGrayLight)
                    layout.background = null
                }


            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = view.legendLayout
        }

        exFiveCalendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                // Setup each header day text if we have not done that already.
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }
                        .forEachIndexed { index, tv ->
                            tv.text =
                                daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                                    .toUpperCase(Locale.ENGLISH)
                            if(index ==0||index==6){
                                tv.setTextColorRes(R.color.textGray)
                            }
                            else{
                                tv.setTextColorRes(R.color.colorTextPrimary)
                            }

                        }
                    month.yearMonth
                }
            }
        }

        exFiveCalendar.monthScrollListener = { month ->
            val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
            exFiveMonthYearText.text = title
            Functions.showLog("monthScrollListener")
            selectedDate?.let {
                // Clear selection if we scroll to a new month.
                selectedDate = null
                exFiveCalendar.notifyDateChanged(it)
                viewModel.getCalendarExercise(RequestCalendarModel(month.month, month.year))
            }
        }

        exFiveNextMonthImage.setOnClickListener {
            exFiveCalendar.findFirstVisibleMonth()?.let {
                exFiveCalendar.smoothScrollToMonth(it.yearMonth.next)
                currentMonth = it.yearMonth.next
                Functions.showLog("DATE: ${it.yearMonth.next.monthValue} ${it.yearMonth.next.year} next")
                viewModel.getCalendarExercise(
                    RequestCalendarModel(
                        it.yearMonth.next.monthValue,
                        it.yearMonth.next.year
                    )
                )
            }
        }

        exFivePreviousMonthImage.setOnClickListener {
            exFiveCalendar.findFirstVisibleMonth()?.let {
                exFiveCalendar.smoothScrollToMonth(it.yearMonth.previous)
                currentMonth = it.yearMonth.previous
                Functions.showLog("DATE: ${it.yearMonth.previous.monthValue} ${it.yearMonth.previous.year} next")
                viewModel.getCalendarExercise(
                    RequestCalendarModel(
                        it.yearMonth.previous.monthValue,
                        it.yearMonth.previous.year
                    )
                )
            }
        }

    }

    fun settingDetailHistory(listCalendar: List<DayExerciseModel>, day: CalendarDay) {
        var dataInDay = listCalendar[day.date.dayOfMonth - 1].exercise
        if (listCalendar.size > day.date.dayOfMonth - 1) {


            Functions.partDateTimeDetailShort(day.date.toString())?.let {
                tvDetailHistory.text = it
            }

            llDetailHistory.visibility = View.VISIBLE
            Functions.showLog("" + dataInDay)
            if (dataInDay.size > 0) {
                rcvDetailHistory.visibility = View.VISIBLE
                llDetailHistoryNoData.visibility = View.GONE
            } else {
                rcvDetailHistory.visibility = View.GONE
                llDetailHistoryNoData.visibility = View.VISIBLE
            }
//            mRecyclerView = findViewById(R.id.rcvDetailHistory)
//            val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
//            mRecyclerView.layoutManager = layoutManager
            val detailHistoryAdapter = RxExerciseDetailHistoryAdapter(dataInDay)
            rcvDetailHistory.adapter = detailHistoryAdapter
            detailHistoryAdapter.notifyDataSetChanged()

            detailHistoryAdapter.onClickDetail = {
                val intent = Intent(this, RxExerciseResultActivity::class.java)
                intent.putExtra(Constants.data, it)
                startActivity(intent)
            }

            // setting height rcv
            /*var size = dataInDay.size
            if (size == 1) {
                rcvDetailHistory.layoutParams.height = 104.dp
            } else if (size > 1) {
                rcvDetailHistory.layoutParams.height =
                    (16 + 86 * size).dp
            }*/
        }
    }

    fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    internal fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)
    internal fun TextView.setTextColorRes(@ColorRes color: Int) =
        setTextColor(context.getColorCompat(color))

    //Convert to dp
    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}