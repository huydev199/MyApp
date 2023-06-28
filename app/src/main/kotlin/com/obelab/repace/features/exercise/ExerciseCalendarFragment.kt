package com.obelab.repace.features.exercise

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
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
import com.obelab.repace.common.customview.UntouchableRecyclerView
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.ExerciseHelper
import com.obelab.repace.features.main.MainActivity
import com.obelab.repace.model.DayExerciseModel
import com.obelab.repace.model.ExerciseResultModel
import com.obelab.repace.model.RequestCalendarModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.viewModel.CalendarExerciseViewModel
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

class ExerciseCalendarFragment : BaseFragment() {

    private val viewModel: CalendarExerciseViewModel by viewModels()

    private var selectedDate: LocalDate? = null
    private var monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mDayRecyclerView: UntouchableRecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    var currentMonth = YearMonth.now()
    override fun layoutId(): Int {
        return R.layout.fragment_exercise_calendar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            observe(resCalendarExercise, ::renderCalendarExercise)
            failure(failure, ::handleFailure)
        }
        showLoading()
        viewModel.getCalendarExercise(RequestCalendarModel(currentMonth.monthValue, currentMonth.year))
        setUpView()
    }

    private fun setUpView() {
        val mainActivity = activity as MainActivity
        tvTitle.text = getText(R.string.rx_exercise)
        imvBack.setOnClickListener {
            val mainActivity = activity as MainActivity
            mainActivity.goToExerciseFragment()
        }
        btnExerciseNext.setOnClickListener {
            if (mainActivity.typeScreen == Constants.RX_EXERCISE_TREADMILL) {
                val mainActivity = activity as MainActivity
                mainActivity.goToExerciseSelectFragment(Constants.RX_EXERCISE_TREADMILL)
            } else if (mainActivity.typeScreen == Constants.RX_EXERCISE_OUTDOOR) {
                val mainActivity = activity as MainActivity
                mainActivity.goToExerciseSelectFragment(Constants.RX_EXERCISE_OUTDOOR)
            }
        }
        setUpButtonNext()
    }

    private fun renderCalendarExercise(resBaseModel: ResBaseModel?) {
        Functions.showLog("Test: " + resBaseModel?.data)
        hideLoading()
        try {
            if (resBaseModel?.success == true){
                val dataList = DatabaseHelper.instance.getDayExercisesByMonthYear(currentMonth.monthValue, currentMonth.year)
                setUpCalendar(dataList)
            } else {

            }
        } catch (e:Exception){

        }
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("getCalendarExerciseError: " + failure.toString())
        val dataList = DatabaseHelper.instance.getDayExercisesByMonthYear(currentMonth.monthValue, currentMonth.year)
        setUpCalendar(dataList)
    }

    private fun setUpCalendar(listCalendar: List<DayExerciseModel>) {
        Functions.showLog("Calendar: " + listCalendar)
        exFiveRv.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }

        val daysOfWeek = daysOfWeekFromLocale()

        exFiveCalendar.setup(
            currentMonth.minusMonths(10),
            currentMonth.plusMonths(10),
            daysOfWeek.first()
        )

        // Disable event scroll
        exFiveCalendar.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return if (e.action == MotionEvent.ACTION_MOVE) {
                    true
                } else {
                    super.onInterceptTouchEvent(rv, e)
                }
            }
        })

        exFiveCalendar.scrollToMonth(currentMonth)
        exFiveCalendar.maxRowCount = 6
        exFiveCalendar.daySize = CalendarView.sizeAutoWidth(Functions.dpToPx(80))

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
                    layout.setBackgroundResource(if (selectedDate == day.date) R.drawable.calendar_selected_bg else 0)
                    if (listCalendar.size > day.date.dayOfMonth - 1) {
                        var dataInDay = listCalendar[day.date.dayOfMonth - 1].exercise
                        if (dataInDay != null) {
                            mDayRecyclerView = container.view.rcvCalendarExercise
                            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                            mDayRecyclerView.layoutManager = layoutManager
                            var excerciseCalendarAdapter = ExerciseCalendarAdapter(dataInDay as ArrayList<ExerciseResultModel>, false)
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
            exFiveMonthYearText?.text = title
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
                viewModel.getCalendarExercise(RequestCalendarModel(it.yearMonth.next.monthValue, it.yearMonth.next.year))
            }
        }

        exFivePreviousMonthImage.setOnClickListener {
            exFiveCalendar.findFirstVisibleMonth()?.let {
                exFiveCalendar.smoothScrollToMonth(it.yearMonth.previous)
                currentMonth = it.yearMonth.previous
                Functions.showLog("DATE: ${it.yearMonth.previous.monthValue} ${it.yearMonth.previous.year} next")
                viewModel.getCalendarExercise(RequestCalendarModel(it.yearMonth.previous.monthValue, it.yearMonth.previous.year))
            }
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

    @SuppressLint("ResourceAsColor")
    fun setUpButtonNext() {
        val mainActivity = activity as MainActivity
        val dataTodayExercise = ExerciseHelper.getTodayExercise()
        if (dataTodayExercise.type != 0) {
            btnExerciseNext.isEnabled = true
            btnExerciseNext.setOnClickListener {
                if (mainActivity.typeScreen == Constants.RX_EXERCISE_TREADMILL) {
                    val mainActivity = activity as MainActivity
                    mainActivity.goToExerciseSelectFragment(Constants.RX_EXERCISE_TREADMILL)
                } else if (mainActivity.typeScreen == Constants.RX_EXERCISE_OUTDOOR) {
                    val mainActivity = activity as MainActivity
                    mainActivity.goToExerciseSelectFragment(Constants.RX_EXERCISE_OUTDOOR)
                }
            }
        } else {
            btnExerciseNext.setBackgroundResource(R.drawable.btn_disable)
            btnExerciseNext.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorText
                )
            )
            btnExerciseNext.isEnabled = false
        }
    }
}