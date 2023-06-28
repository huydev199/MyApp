package com.obelab.repace.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.model.BluetoothDeviceModel
import com.obelab.repace.model.LTTestHistoryOnsetModel
import com.obelab.repace.model.NoticeModel
import com.obelab.repace.model.ResAllLtTestHistoryModel
import kotlinx.android.synthetic.main.fragment_lt_test_setting.*

class LtTestHistoryAdapter(var listHistory: ArrayList<ResAllLtTestHistoryModel>) : RecyclerView.Adapter<LtTestHistoryAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var dateTimeHistory: TextView

        var valueStage: TextView
        var valueOnset: TextView
        var valueSmo2: TextView
        var moreLtTestHistory: Button

        init {
            dateTimeHistory = itemView.findViewById(R.id.date_Time_History)
            valueStage = itemView.findViewById(R.id.value_stage)
            valueOnset = itemView.findViewById(R.id.valueOnset)
            valueSmo2 = itemView.findViewById(R.id.valueSmo2)
            moreLtTestHistory = itemView.findViewById(R.id.more_lt_test)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_lt_test_history,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listHistory[position]

    Functions.showLog("data"+data)
        holder.valueSmo2.text =Functions.getDouble1Decimal(data.smO2Avg).toString()
//        (Math.round((data.smO2Avg) * 10.0) / 10.0).toString()
        holder.dateTimeHistory.text= Functions.formatDateToYear( data.date.toString())
        holder.valueStage.text=data.stage.toString()
        holder.valueOnset.text = Functions.getDouble1Decimal(data.lactateOnset).toString()
//        (Math.round((data.lactateOnset) * 10.0) / 10.0).toString()
        holder.moreLtTestHistory.setOnClickListener {
            onClickMoreLtTestDetail?.invoke(data)
        }
    }

    override fun getItemCount(): Int {
        return listHistory.size
    }
    var onClickMoreLtTestDetail: ((data:ResAllLtTestHistoryModel) -> Unit)? = null
}