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
import com.obelab.repace.features.empty.FriendsActivity
import com.obelab.repace.model.BluetoothDeviceModel
import com.obelab.repace.model.FriendListModel
import com.obelab.repace.model.FriendRequestModel
import com.obelab.repace.model.NoticeModel
import kotlinx.android.synthetic.main.activity_profilesetting.*

class FriendListAdapter(var friendList: List<FriendListModel>, private val context: Context) :
    RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var userName: TextView
        var avatar: ImageView
        var btnUnfriend: Button

        init {
            userName = itemView.findViewById(R.id.id_userName)
            avatar = itemView.findViewById(R.id.id_avatar)
            btnUnfriend = itemView.findViewById(R.id.btnUnfriend)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_friends_list, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataFriendList = friendList[position]
        holder.userName.text = dataFriendList.nickname
        Glide.with(context).load(dataFriendList.avatar)
            .placeholder(R.drawable.ic_avatar_default)
            .error(R.drawable.ic_avatar_default)
            .into(holder.avatar)

        holder.btnUnfriend.setOnClickListener {
            Functions.showLog("vooooodi ${dataFriendList}")
            onClickUnfriend?.invoke(dataFriendList)
        }

        holder.avatar.setOnClickListener {
            onClickFriend?.invoke(dataFriendList)
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    var onClickUnfriend: ((selectValue: FriendListModel) -> Unit)? = null

    var onClickFriend: ((selectValue: FriendListModel) -> Unit)? = null
}
