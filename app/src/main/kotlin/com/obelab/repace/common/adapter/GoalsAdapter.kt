package com.obelab.repace.common.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.obelab.repace.R
import de.hdodenhof.circleimageview.CircleImageView

class GoalsAdapter(var type: Int, var totalItem: Int, var currentItem: Int) : RecyclerView.Adapter<GoalsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvNumber: TextView
        var tvContent: TextView
        var civGoal: CircleImageView

        init {
            tvNumber = itemView.findViewById(R.id.tvNumber)
            tvContent = itemView.findViewById(R.id.tvContent)
            civGoal = itemView.findViewById(R.id.civGoal)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalsAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_goals, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: GoalsAdapter.ViewHolder, position: Int) {
        if (position < currentItem) {
            when (type) {
                0 -> {
                    holder.civGoal.setImageResource(R.drawable.ic_trophy)
                    holder.tvNumber.text = "${position + 1}"
                }
                1 -> {
                    holder.civGoal.setImageResource(R.drawable.ic_medal)
                    holder.tvContent.text = "times"
                    holder.tvNumber.text = "${(position + 1) * 4}"
                }
                2 -> {
                    holder.civGoal.setImageResource(R.drawable.ic_badge)
                    holder.tvContent.text = "times"
                    holder.tvNumber.text = "${(position + 1) * 4}"
                }
            }
            holder.tvNumber.setTextColor(Color.parseColor("#FFFFFF"));
            holder.tvContent.setTextColor(Color.parseColor("#FFFFFF"))
        } else {
            when (type) {
                0 -> {
                    holder.civGoal.setImageResource(R.drawable.ic_trophy_disable)
                    holder.tvNumber.text = "${position + 1}"
                }
                1 -> {
                    holder.civGoal.setImageResource(R.drawable.ic_medal_disable)
                    holder.tvContent.text = "times"
                    holder.tvNumber.text = "${(position + 1) * 4}"
                }
                2 -> {
                    holder.civGoal.setImageResource(R.drawable.ic_badge_disable)
                    holder.tvContent.text = "times"
                    holder.tvNumber.text = "${(position + 1) * 4}"
                }
            }

            holder.tvNumber.setTextColor(Color.parseColor("#8C92AC"))
            holder.tvContent.setTextColor(Color.parseColor("#8C92AC"))
        }
    }

    override fun getItemCount(): Int {
        return totalItem
    }
}