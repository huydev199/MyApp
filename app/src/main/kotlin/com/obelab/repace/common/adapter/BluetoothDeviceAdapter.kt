package com.obelab.repace.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.features.pairing.PairingActivity
import com.obelab.repace.model.BluetoothDeviceModel


class BluetoothDeviceAdapter(var listDevice: ArrayList<BluetoothDeviceModel>, var connectingIndex: Int, var connectedIndex:Int) :
    RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvNameDevice: TextView
        var tvMACAddress: TextView
        var tvConnect: TextView
        var btnConnect: LinearLayout
        var llConnect: LinearLayout
        init {
            tvNameDevice = itemView.findViewById(R.id.tvNameDevice)
            tvMACAddress = itemView.findViewById(R.id.tvMACAddress)
            tvConnect = itemView.findViewById(R.id.tvConnect)
            btnConnect = itemView.findViewById(R.id.llItemDevice)
            llConnect = itemView.findViewById(R.id.llConnect)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listDevice[position]
        holder.tvNameDevice.text = data.name
        holder.tvMACAddress.text = data.MACAddress
        if (connectingIndex == -1){
            holder.llConnect.setBackgroundResource(R.drawable.btn_enable)
            holder.btnConnect.setOnClickListener {
                val pairingActivity = PairingActivity.instance
                pairingActivity?.connect(data.MACAddress)
                holder.tvConnect.setText(R.string.btn_connecting)
                connectingIndex = position
                notifyDataSetChanged()
                onClickDetail?.invoke()
            }
        } else {
            if (position == connectingIndex){
                holder.llConnect.setBackgroundResource(R.drawable.btn_enable)
                holder.tvConnect.setText(R.string.btn_connecting)
            } else {
                holder.llConnect.setBackgroundResource(R.drawable.btn_disable)
                holder.tvConnect.setText(R.string.btn_connect)
                holder.btnConnect.setOnClickListener {}
            }
        }
        if(connectedIndex == position){
            holder.tvConnect.setText(R.string.btn_connected)
        }
    }

    override fun getItemCount(): Int {
        return listDevice.size
    }

    var onClickDetail: (() -> Unit)? = null}