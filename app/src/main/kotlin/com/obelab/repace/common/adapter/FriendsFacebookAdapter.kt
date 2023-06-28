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

class FriendsFacebookAdapter(var friendRequest: List<FriendFacebookModel>, private val context: Context) :
    RecyclerView.Adapter<FriendsFacebookAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var userName: TextView
        var avatar: ImageView

        var btnRequest: Button
        var btnCancleRequest: Button
        var btnReject: Button
        var btnAccept: Button
        var btnUnfriend: Button

        init {
            userName = itemView.findViewById(R.id.id_userName)
            avatar = itemView.findViewById(R.id.id_avatar)
            btnRequest = itemView.findViewById(R.id.btnRequest)
            btnCancleRequest = itemView.findViewById(R.id.btnCancleRequest)
            btnReject = itemView.findViewById(R.id.btnReject)
            btnAccept = itemView.findViewById(R.id.btnAccept)

            btnUnfriend = itemView.findViewById(R.id.btnUnfriend)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_friends_facebook, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = friendRequest[position]

        Functions.showLog("friendRequest ${data}")
        Functions.showLog("friendRequest ${data.status}")
        holder.userName.text = data.nickname
//        if (data.status==="none") {
//            holder.btnCancleRequest.visibility = View.VISIBLE
//            holder.btnRequest.visibility = View.GONE
////            holder.btnUnfriend.visibility = View.VISIBLE
//        } else {
//            holder.btnCancleRequest.visibility = View.GONE
//            holder.btnRequest.visibility = View.VISIBLE
//        }
        when (data.status) {
            "none" -> {

                holder.btnRequest.visibility = View.VISIBLE
//          holder.btnUnfriend.visibility = View.VISIBLE
            }
            "friend" -> {
                holder.btnUnfriend.visibility = View.VISIBLE
            }
            "accept" -> {
                holder.btnReject.visibility = View.VISIBLE
                holder.btnAccept.visibility = View.VISIBLE

            }
            "requested" -> {
                holder.btnCancleRequest.visibility = View.VISIBLE
            }
            else -> { // Note the block


            }
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
// gui yeu cau ket ban
        holder.btnRequest.setOnClickListener {
            onClickFacebookRequest?.invoke(data)
        }
//canle yeu cau ket ban
        holder.btnCancleRequest.setOnClickListener {
            onClickCancleFacebookRequest?.invoke(data)
        }
        // unfriend
        holder.btnUnfriend.setOnClickListener {
            onClickUnfriendFacebookRequest?.invoke(data)
        }
        // khong dong y ket ban
        holder.btnReject.setOnClickListener{
            onClickRejectFacebookRequest?.invoke(data)
        }
        // Accept ban face book
        holder.btnAccept.setOnClickListener{
            onClickAcceptFacebookRequest?.invoke(data)
        }

    }

    override fun getItemCount(): Int {
        return friendRequest.size
    }

    var onClickAvatarAdd: ((selectValue: FriendFacebookModel) -> Unit)? = null
    var onClickFacebookRequest: ((selectValue: FriendFacebookModel) -> Unit)? = null
    var onClickCancleFacebookRequest: ((selectValue: FriendFacebookModel) -> Unit)? = null
    var onClickUnfriendFacebookRequest: ((selectValue: FriendFacebookModel) -> Unit)? = null

    var onClickRejectFacebookRequest: ((selectValue: FriendFacebookModel) -> Unit)? = null

    var onClickAcceptFacebookRequest:((selectValue: FriendFacebookModel) -> Unit)? = null
}
