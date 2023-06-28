package com.obelab.repace.common.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.util.Constants
import com.obelab.repace.model.ExerciseResultModel

class RxExerciseDetailHistoryAdapter(var listExercise: List<ExerciseResultModel>) :
    RecyclerView.Adapter<RxExerciseDetailHistoryAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imvItem: ImageView
        var tvTitle: TextView
        var tvDuration: TextView
        var tvTimeCreate: TextView

        init {
            imvItem = itemView.findViewById(R.id.imvRxExerciseHistoryItem)
            tvTitle = itemView.findViewById(R.id.tvTitleRxExercise)
            tvDuration = itemView.findViewById(R.id.tvHistoryDuration)
            tvTimeCreate = itemView.findViewById(R.id.tvHistoryCreateTime)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RxExerciseDetailHistoryAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detail_history, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return listExercise.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listExercise[position]
        if (data.typeId == Constants.rx_exercise) {
            if (data.activityId == Constants.ex_treadmill) {
                holder.imvItem.setBackgroundResource(R.drawable.ic_history_rx_treadmill_running)
                holder.tvTitle.setText(R.string.treadmill_running)
            } else {
                holder.imvItem.setBackgroundResource(R.drawable.ic_history_rx_outdoor_running)
                holder.tvTitle.setText(R.string.outdoor_running)
            }
        } else {
            when (data.activityId) {
                Constants.ex_treadmill -> {
                    holder.imvItem.setBackgroundResource(R.drawable.ic_history_free_treadmill_running)
                    holder.tvTitle.setText(R.string.treadmill_running)
                }
                Constants.ex_outdoor -> {
                    holder.imvItem.setBackgroundResource(R.drawable.ic_history_free_outdoor_running)
                    holder.tvTitle.setText(R.string.outdoor_running)
                }
                Constants.ex_cycling -> {
                    holder.imvItem.setBackgroundResource(R.drawable.ic_history_free_cycling)
                    holder.tvTitle.setText(R.string.cycling)
                }
                Constants.ex_climbing -> {
                    holder.imvItem.setBackgroundResource(R.drawable.ic_history_free_climbing)
                    holder.tvTitle.setText(R.string.moutain_climbing)
                }
            }
        }
        holder.tvDuration.setText(convertTime(data.totalDuration*60))
        holder.tvTimeCreate.setText(data.createdAt?.let { Functions.partDateTimeDetail(it).toUpperCase() })

        holder.itemView.setOnClickListener {
            onClickDetail?.invoke(data)
        }
    }

    var onClickDetail: ((selectValue: ExerciseResultModel) -> Unit)? = null

    private fun convertTime(time: Int): String {
        var hours = (time / 3600)
        var minute = ((time % 3600) / 60).toInt()
        var second = (time % 60).toInt()
        var result = "00 : 00"
        if (hours > 9) {
            if (minute > 9) {
                if (second > 9) {
                    result = "$hours:$minute:$second"
                } else {
                    result = "$hours:$minute:0$second"
                }
            } else {
                if (second > 9) {
                    result = "$hours:0$minute:$second"
                } else {
                    result = "$hours:0$minute:0$second"
                }
            }
        } else {
            if (minute > 9) {
                if (second > 9) {
                    result = "0$hours:$minute:$second"
                } else {
                    result = "0$hours:$minute:0$second"
                }
            } else {
                if (second > 9) {
                    result = "0$hours:0$minute:$second"
                } else {
                    result = "0$hours:0$minute:0$second"
                }
            }
        }
        return result
    }

    fun View.margin(
        left: Int? = null,
        top: Int? = null,
        right: Int? = null,
        bottom: Int? = null
    ) {
        layoutParams<ViewGroup.MarginLayoutParams> {
            left?.run { leftMargin = dpToPx(this) }
            top?.run { topMargin = dpToPx(this) }
            right?.run { rightMargin = dpToPx(this) }
            bottom?.run { bottomMargin = dpToPx(this) }
        }
    }

    inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
        if (layoutParams is T) block(layoutParams as T)
    }

    fun View.dpToPx(dp: Int): Int = context.dpToPx(dp)
    fun Context.dpToPx(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
}