package com.obelab.repace.common.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.obelab.repace.R
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.model.*
import kotlinx.android.synthetic.main.activity_profilesetting.*

class FriendAddAdapter(var friendRequest: List<FriendAddModel>, private val context: Context) :
    RecyclerView.Adapter<FriendAddAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var userName: TextView
        var avatar: ImageView

        var btnRequest: Button
        var btnCancleRequest: Button

        init {
            userName = itemView.findViewById(R.id.id_userName)
            avatar = itemView.findViewById(R.id.id_avatar)
            btnRequest = itemView.findViewById(R.id.btnRequest)
            btnCancleRequest = itemView.findViewById(R.id.btnCancleRequest)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_friends_add, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = friendRequest[position]

        Functions.showLog("friendRequest ${data}")
        holder.userName.text = data.nickname
        if (data.requested) {
            holder.btnCancleRequest.visibility = View.VISIBLE
            holder.btnRequest.visibility = View.GONE
//            holder.btnUnfriend.visibility = View.VISIBLE
        } else {
            holder.btnCancleRequest.visibility = View.GONE
            holder.btnRequest.visibility = View.VISIBLE
        }
        Glide.with(context).load(data.avatar)
            .placeholder(R.drawable.ic_user_avatar)
            .error(R.drawable.ic_user_avatar)
            .into(holder.avatar)

        Functions.showLog("data.avatar ${data.avatar}")

        holder.avatar.setOnClickListener {
            Functions.showLog("aaaaaaaaaa")
            onClickAvatarAdd?.invoke(data)
        }

        holder.btnRequest.setOnClickListener {
            onClickRequest?.invoke(data)
        }

        holder.btnCancleRequest.setOnClickListener {
            onClickCancleRequest?.invoke(data)
        }

    }

    override fun getItemCount(): Int {
        return friendRequest.size
    }

    var onClickAvatarAdd: ((selectValue: FriendAddModel) -> Unit)? = null
    var onClickRequest: ((selectValue: FriendAddModel) -> Unit)? = null
    var onClickCancleRequest: ((selectValue: FriendAddModel) -> Unit)? = null

}
