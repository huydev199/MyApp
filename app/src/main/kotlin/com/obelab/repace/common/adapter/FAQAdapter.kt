package com.obelab.repace.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.obelab.repace.R
import com.obelab.repace.model.FAQModel

class FAQAdapter(var listFAQ: ArrayList<FAQModel>) : RecyclerView.Adapter<FAQAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView
        var itemQuestion: ConstraintLayout

        init {
            tvTitle = itemView.findViewById(R.id.tvTitleQuestion)
            itemQuestion = itemView.findViewById(R.id.itemQuestion)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_faq,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listFAQ[position]
        holder.tvTitle.text = data.question
        holder.itemQuestion.setOnClickListener {
            onClickDetail?.invoke(data)
        }
    }

    override fun getItemCount(): Int {
        return listFAQ.size
    }
    var onClickDetail: ((selectValue: FAQModel) -> Unit)? = null
}