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
import com.obelab.repace.model.BluetoothDeviceModel
import com.obelab.repace.model.FriendListModel
import com.obelab.repace.model.FriendRequestModel
import com.obelab.repace.model.NoticeModel
import kotlinx.android.synthetic.main.activity_profilesetting.*

class FriendAdapter(var friendRequest: List<FriendRequestModel>, private val context: Context) :
    RecyclerView.Adapter<FriendAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var userName: TextView
        var avatar: ImageView
        var btnReject: Button
        var btnAccept: Button
//        var btnUnfriend: Button

        init {
            userName = itemView.findViewById(R.id.id_userName)
            avatar = itemView.findViewById(R.id.id_avatar)
            btnReject = itemView.findViewById(R.id.btnReject)
            btnAccept = itemView.findViewById(R.id.btnAccept)
//            btnUnfriend = itemView.findViewById(R.id.btnUnfriend)

        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_friends_request, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = friendRequest[position]
//        val dataFriendList = listFriend[position]
        holder.userName.text = data.nickname
//        if (friendList) {
//            holder.btnReject.visibility = View.GONE
//            holder.btnAccept.visibility = View.GONE
////            holder.btnUnfriend.visibility = View.VISIBLE
//        }
        Glide.with(context).load(data.avatar)
            .placeholder(R.drawable.ic_avatar_default)
            .error(R.drawable.ic_avatar_default)
            .into(holder.avatar)
//        holder.id_avatar.data(data.avatar)
//        holder.avatar.setImageResource(civAvatar)
//        val uri: Uri = data?.avatar
//        holder.avatar.setImageURI(uri)
        Functions.showLog("data.avatar ${data.avatar}")
//        holder.avatar.setImageResource(data.avatar.uri)
//        holder.itemNotice.setOnClickListener {
//            onClickDetail?.invoke(data)
//        }

        holder.btnReject.setOnClickListener {
            onClickReject?.invoke(data)
        }
        holder.btnAccept.setOnClickListener {
            onClickAccept?.invoke(data)
        }
//        holder.btnUnfriend.setOnClickListener {
//            Functions.showLog("vooooodi ${data}")
////            onClickUnfriend?.invoke(dataFriendList)
//        }
        holder.avatar.setOnClickListener {
            onClickAvatarRequest?.invoke(data)
        }

    }

    override fun getItemCount(): Int {
        return friendRequest.size
    }

    var onClickReject: ((selectValue: FriendRequestModel) -> Unit)? = null

    var onClickAccept: ((selectValue: FriendRequestModel) -> Unit)? = null

    var onClickAvatarRequest: ((selectValue: FriendRequestModel) -> Unit)? = null
//    var onClickUnfriend: ((selectValue: FriendListModel) -> Unit)? = null
}
