package com.obelab.repace.common.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.obelab.repace.R
import com.obelab.repace.core.util.Constants
import com.obelab.repace.model.ExerciseResultModel

class ExerciseCalendarAdapter(var listExercise: List<ExerciseResultModel>, var isHistory: Boolean) :
    RecyclerView.Adapter<ExerciseCalendarAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var viewItem: View

        init {
            viewItem = itemView.findViewById(R.id.vDayExercise)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_exercise, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listExercise[position]
        if (!isHistory) {
            if (data.intensityId == Constants.low_intensity) {
                holder.viewItem.setBackgroundResource(R.drawable.bg_low_exercise)
            } else if (data.intensityId == Constants.high_intensity) {
                holder.viewItem.setBackgroundResource(R.drawable.bg_high_exercise)
            }
        } else {
            if (data.typeId == Constants.rx_exercise) {
                holder.viewItem.setBackgroundResource(R.drawable.bg_rx_exercise)
            } else if (data.typeId == Constants.free_exercise) {
                holder.viewItem.setBackgroundResource(R.drawable.bg_free_exercise)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (listExercise.size > 6) 6 else listExercise.size
    }
}