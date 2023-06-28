package com.obelab.repace.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.model.NoticeModel

class NoticeAdapter(var listNotice: ArrayList<NoticeModel>) : RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvTitle: TextView
        var tvTime: TextView
        var imvDetail: ImageView
        var itemNotice: ConstraintLayout
        init {
            tvTitle = itemView.findViewById(R.id.tvTitleNotice)
            tvTime = itemView.findViewById(R.id.tvTime)
            imvDetail = itemView.findViewById(R.id.imvDetail)
            itemNotice = itemView.findViewById(R.id.itemNotice)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_notice,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listNotice[position]
        holder.tvTitle.text = data.title
        holder.tvTime.text = Functions.formatDateTime(data.updatedAt)
        holder.itemNotice.setOnClickListener {
            onClickDetail?.invoke(data)
        }
    }

    override fun getItemCount(): Int {
        return listNotice.size
    }
    var onClickDetail: ((selectValue: NoticeModel) -> Unit)? = null
}