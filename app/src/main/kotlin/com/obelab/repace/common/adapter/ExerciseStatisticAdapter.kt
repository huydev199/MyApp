package com.obelab.repace.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.model.ExerciseStatisticSpeedModel
import java.util.*


class ExerciseStatisticAdapter(var listExercise: ArrayList<ExerciseStatisticSpeedModel>, var type: Int) : RecyclerView.Adapter<ExerciseStatisticAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvExerciseTime: TextView
        var itemExercise: ConstraintLayout

        init {
            tvExerciseTime = itemView.findViewById(R.id.tv_exercise_time)
            itemExercise = itemView.findViewById(R.id.itemExerciseList)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise_statistic, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = listExercise[position]
        if (type == 1) {
            val day = Functions.simpleDatetoHumanDate(data.date)
            holder.tvExerciseTime.text = day
        } else if (type == 2) {
            val date: Calendar = Functions.formatStringToCalendar(data.date)
            val dutedate: Calendar = Functions.formatStringToCalendar(data.date)
            dutedate.add(Calendar.DAY_OF_MONTH, 6)
            if (date.time.year != dutedate.time.year) {
                holder.tvExerciseTime.text = "${Functions.getMonthNameFromCalendar(date)} ${Functions.formatNumber(date.time.date)}, ${Functions.getYearFromCalendar(date)} ~ ${Functions.getMonthNameFromCalendar(dutedate)} ${Functions.formatNumber(dutedate.time.date)}, ${Functions.getYearFromCalendar(dutedate)}"
            } else {
                holder.tvExerciseTime.text = "${Functions.getMonthNameFromCalendar(date)} ${Functions.formatNumber(date.time.date)} ~ ${Functions.getMonthNameFromCalendar(dutedate)} ${Functions.formatNumber(dutedate.time.date)}, ${Functions.getYearFromCalendar(dutedate)}"
            }
        } else {
            val day = Functions.formatDateToMY(data.date)
            holder.tvExerciseTime.text = day
        }

        holder.itemExercise.setOnClickListener {
            onClickDetail?.invoke(data)
        }
    }

    override fun getItemCount(): Int {
        Functions.showLog("listExercise.size in setting view: " + listExercise.size.toString())
        return listExercise.size
    }

    var onClickDetail: ((selectValue: ExerciseStatisticSpeedModel) -> Unit)? = null
}