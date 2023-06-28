package com.obelab.repace.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.obelab.repace.R
import com.obelab.repace.model.LtTestPerformanceModel

class LtTestPerformanceAdapter(var listPerformance: ArrayList<LtTestPerformanceModel>) :
    RecyclerView.Adapter<LtTestPerformanceAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvStage: TextView
        var tvSpeed: TextView
        var tvDistance: TextView

        init {
            tvStage = itemView.findViewById(R.id.tvStage)
            tvSpeed = itemView.findViewById(R.id.tvSpeed)
            tvDistance = itemView.findViewById(R.id.tvDistance)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LtTestPerformanceAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_lt_test_performance, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: LtTestPerformanceAdapter.ViewHolder, position: Int) {
        val data = listPerformance[position]
        if (data.stage > 9) {
            holder.tvStage.text = data.stage.toString()
        } else {
            holder.tvStage.text = "0${data.stage}"
        }
        holder.tvSpeed.text =  String.format("%.1f", data.speed).replace(",", ".")
        holder.tvDistance.text = String.format("%.1f", data.distance).replace(",", ".")
    }

    override fun getItemCount(): Int {
        return listPerformance.size
    }
}