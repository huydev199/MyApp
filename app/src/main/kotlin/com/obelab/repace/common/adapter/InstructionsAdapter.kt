package com.obelab.repace.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.obelab.repace.R
import com.obelab.repace.model.InstructionModel

class InstructionsAdapter (var listInstruction: ArrayList<InstructionModel>) : RecyclerView.Adapter<InstructionsAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvContent: TextView
        var imvInstruction: ImageView
        init {
            tvContent = itemView.findViewById(R.id.tvInstruction)
            imvInstruction = itemView.findViewById(R.id.imvInstruction)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_instruction,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listInstruction[position]
        holder.tvContent.text = data.content
        holder.imvInstruction.setImageResource(data.imgSource)
    }

    override fun getItemCount(): Int {
        if (listInstruction!=null){
            return listInstruction.size
        }
        return 0
    }
}