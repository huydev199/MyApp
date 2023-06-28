package com.obelab.repace.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.obelab.library.repace.data.LTTraining
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions

class RecommendationLtTestAdapter (var listData: List<LTTraining>) : RecyclerView.Adapter<RecommendationLtTestAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvSession: TextView
        var tvTime: TextView
        var tvSpeed: TextView
        var tvHeartRate: TextView
        var tvUnitTime: TextView
        init {
            tvSession = itemView.findViewById(R.id.tvSession)
            tvTime = itemView.findViewById(R.id.tvTime)
            tvSpeed = itemView.findViewById(R.id.tvSpeed)
            tvHeartRate = itemView.findViewById(R.id.tvHeartRate)
            tvUnitTime = itemView.findViewById(R.id.tvUnitTime)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecommendationLtTestAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_recommendation_lt_test,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(
        holder: RecommendationLtTestAdapter.ViewHolder,
        position: Int
    ) {
        val data = listData[position]
        holder.tvSession.text = data.section.toString()
        Functions.showLog("session: "+ data.section+", time: "+data.time)
        if (data.time == null || data.time == 0){
            Functions.showLog("Success")
            holder.tvTime.text =  "REST"
            holder.tvUnitTime.visibility = View.GONE
        } else {
            Functions.showLog("Error")
            holder.tvTime.text = data.time.toString()
        }

        if(data.speed == null || data.speed == 0.0){
            holder.tvSpeed.text = "-"
        } else {
            holder.tvSpeed.text = data.speed.toString()
        }

        if(data.heartRate == null || data.heartRate == 0){
            holder.tvHeartRate.text = "-"
//            holder.tvHeartRate.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
//            holder.tvHeartRate.setPadding(Functions.dpToPx(20),0,0,0)
        } else {
            holder.tvHeartRate.text = data.heartRate.toString()
        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }
}