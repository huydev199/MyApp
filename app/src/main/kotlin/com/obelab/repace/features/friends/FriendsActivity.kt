package com.obelab.repace.features.empty

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.Display
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.gson.GsonBuilder
import com.obelab.repace.BuildConfig
import com.obelab.repace.R
import com.obelab.repace.common.adapter.FriendAdapter
import com.obelab.repace.common.adapter.FriendAddAdapter
import com.obelab.repace.common.adapter.FriendListAdapter
import com.obelab.repace.common.adapter.FriendsFacebookAdapter
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.functional.StatusBar
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.features.friends.FriendProfileActivity
import com.obelab.repace.model.*
import com.obelab.repace.service.SearchFriend
import com.obelab.repace.viewModel.*
import kotlinx.android.synthetic.main.activity_friends.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_pre_lt_test_outdoor.*
import kotlinx.android.synthetic.main.fragment_lt_test.*
import kotlinx.android.synthetic.main.header_back.*
import org.w3c.dom.Text
import java.util.*


class FriendsActivity : BaseActivity() {
    private lateinit var callbackManager: CallbackManager
    private lateinit var mRecyclerView: RecyclerView
//    private lateinit var listFriendsRequest: ArrayList<FriendListModel>

    private val viewModel: FriendsViewModel by viewModels()
    private val viewModelFriendList: FriendsListViewModel by viewModels()

    //    private val viewModelFriendList: FriendListModel by viewModels()
    private val viewModelRejectFriend: RejectFriendViewModel by viewModels()

    private val viewModelAcceptFriend: AcceptFriendViewModel by viewModels()

    // unfriend
    private val viewModelUnFriend: UnFriendViewModel by viewModels()

    //search friend
    private val viewModelSearchFriend: SearchFriendViewModel by viewModels()

    // add friend request
    private val viewModelAddFriend: AddFriendViewModel by viewModels()

    // add friend cancle request
    private val viewModelAddFriendCancle: cancleRequestFriendViewModel by viewModels()

    // social-connect
    private val viewModelSocialConnect: SocialConnectViewModel by viewModels()
    // friend facebokk
    private var isFacebook= false

    private var socialId=""
    private var socialToken=""
    companion object {
        fun callingIntent(context: Context) = Intent(context, FriendsActivity::class.java)
        private const val REQUEST_CODE_STT = 1
    }

    private var texSearch = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)
        setUpView()
        searchFriend()
        //connect facebook
        connectFacebook()
        //Invite friend
        Invite()

        // get api friend request
        with(viewModel) {
            observe(resFriendsRequestViewModel, ::renderAllFriendsRequest)
            failure(failure, ::handleFailure)
        }
        viewModel.getAllFriendsRequest()

        // get api friend list
        with(viewModelFriendList) {
            observe(resFriendsListViewModel, ::renderAllFriendList)
            failure(failure, ::handleFailure)
        }
        viewModelFriendList.getAllFriendsList()

        // get api friend list and get friend list
        with(viewModelRejectFriend) {
            observe(deleteFriendRequestModel, ::getAllFriendsRequest)
            failure(failure, ::handleFailure)
        }
        with(viewModelAcceptFriend) {
            observe(acceptFriendRequestModel, ::getAllFriendsRequest)
            failure(failure, ::handleFailure)
        }

        with(viewModelUnFriend) {
            observe(deleteUnfriendModel, ::getAllFriendsRequest)
            failure(failure, ::handleFailure)
        }

        //search Friend
        with(viewModelSearchFriend) {
            observe(searchFriendModel, ::searchFriendRequest)
            failure(failure, ::handleFailure)
        }

        //post add friend request
        with(viewModelAddFriend) {
            observe(addFriendRequestModel, ::addFriendRequest)
            failure(failure, ::handleFailure)
        }

//        // delete cancle request
        with(viewModelAddFriendCancle) {
            observe(deleteCancleRequestFriendModel, ::addFriendRequest)
            failure(failure, ::handleFailure)
        }

        //  social-connect

        with(viewModelSocialConnect) {
            observe(socialConnectRequestModel, ::socialConnectRes)
            failure(failure, ::handleFailure)
        }

//        viewModel.getAllFriends()
    }

    private fun Invite() {
        btnInvite.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "REPACE")
                var shareMessage = "REPACE\n"
                shareMessage =
                    """
                        ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}      
                        """.trimIndent()
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "REPACE"))
            } catch (e: Exception) {
                //e.toString();
            }
        }
    }

    private fun searchFriendRequest(resBaseModel: ResBaseModel?) {
        hideLoading()
        Functions.showLog("show ${resBaseModel}")
        if (resBaseModel?.success == true) {
            val gson = GsonBuilder().create()

            val dataStr = resBaseModel?.data?.let { Functions.toJsonString(it) }
            val resDistanceModel: SearchFriendModel? =
                gson.fromJson(dataStr, SearchFriendModel::class.java)

            Functions.showLog("resDistanceModel ${resDistanceModel}")
//
//            val dataList = resDistanceModel?.data
            val dataList = gson.fromJson(
                resDistanceModel?.data?.let { Functions.toJsonString(it) },
                Array<FriendAddModel>::class.java
            ).toList()
//            setFriendRequest(dataList)
            Functions.showLog("dataList ${dataList}")
            if (dataList != null) {
                setFriendAdd(dataList)
            }
        }
    }

    private fun getAllFriendsRequest(resBaseModel: ResBaseModel?) {

        showLoading()
        if(isFacebook==false) {
            viewModel.getAllFriendsRequest()
            viewModelFriendList.getAllFriendsList()
        }else{
            // load lai du lieu facebook
            viewModelSocialConnect.socialConnectRequest(RequestSocialConnectModel(socialId.toString(), socialToken.toString()))
        }
    }

    private fun renderAllFriendsRequest(resBaseModel: ResBaseModel?) {
        hideLoading()
        Functions.showLog("show ${resBaseModel}")
        if (resBaseModel?.success == true) {
            val gson = GsonBuilder().create()
            val dataList = gson.fromJson(
                resBaseModel.data?.let { Functions.toJsonString(it) },
                Array<FriendRequestModel>::class.java
            ).toList()
            setFriendRequest(dataList)
        }

    }

    private fun renderAllFriendList(resBaseModel: ResBaseModel?) {
        hideLoading()
        Functions.showLog("show11 ${resBaseModel}")
        if (resBaseModel?.success == true) {
            val gson = GsonBuilder().create()
            val dataList = gson.fromJson(
                resBaseModel.data?.let { Functions.toJsonString(it) },
                Array<FriendListModel>::class.java
            ).toList()
            setFriendList(dataList)
        }

    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Show notices error: " + failure.toString())
        hideLoading()
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 70) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return StatusBar.StatusBarHeight(result)
    }

    private fun searchFriend() {
        tv_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty()) {
                    isFacebook=false
                    Functions.showLog("after ${s}")
                    tvTitle.text = getText(R.string.title_add_friends)
                    id_view.visibility = View.GONE
                    rcv_friend_add.visibility = View.VISIBLE
                    viewModelSearchFriend.searchFriend(
                        SearchFriend.Params(
                            s.toString()
                        )
                    )
                    texSearch = s.toString()

                    facebook_friend.visibility = View.GONE
//                    tvTitle.visibility = View.GONE
                    rcv_friend_on_facebook.visibility = View.GONE
                } else {
                    tvTitle.text = getText(R.string.title_friends)
                    id_view.visibility = View.VISIBLE
                    rcv_friend_add.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun connectFacebook() {
        callbackManager = CallbackManager.Factory.create()
        btnSearchFace.setOnClickListener {
            val mBuilder = AlertDialog.Builder(this)
            mBuilder.setTitle("REPACE wants to use “facebook.com” to Log In")
            mBuilder.setMessage("This allows the app and the website to share information about you.")
            mBuilder.setPositiveButton(getString(R.string.btn_continue)) { dialog, which ->
//                    AlertDialog.BUTTON_POSITIVE

                loginWithFacebook()
//                dialog.dismiss()

            }
            mBuilder.setNegativeButton(getString(R.string.btn_cancel)) { dialog, which ->
//                viewModelRejectFriend.deleteFriendRequest(it.id)
                dialog.cancel()
            }
            val mDialog = mBuilder.create()
//            mDialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_gender)
            mDialog.show()
            val posBtn = mDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val negBtn = mDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            posBtn.setTextColor(ContextCompat.getColor(this, R.color.btnColorCommon))
            negBtn.setTextColor(ContextCompat.getColor(this, R.color.btnColorCommon))
        }
    }

    // login facebokk
    private fun loginWithFacebook() {
        tvTitle.text = getText(R.string.title_add_friends)
        id_view.visibility = View.GONE
        facebook_friend.visibility = View.VISIBLE


//        LoginManager.getInstance().logOut()
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, setOf("public_profile", "user_friends"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

            override fun onSuccess(result: LoginResult?) {
                Functions.showLog("result" + result?.let { Functions.toJsonString(it) })
                 socialId = result?.accessToken?.userId.toString()
                socialToken = result?.accessToken?.token.toString()
                showLoading()
                Functions.showLog("socialId socialToken"+ socialId +socialToken)
                viewModelSocialConnect.socialConnectRequest(RequestSocialConnectModel(socialId.toString(), socialToken.toString()))
            }

            override fun onCancel() {
                Functions.showLog("cancel")
                showToast(getString(R.string.fb_login_cancelled))
            }

            override fun onError(error: FacebookException?) {
                Functions.showLog("error" + error)
                showToast("${getString(R.string.fb_login_cancelled)}: ${error.toString()}")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Functions.showLog("requestCode 333" + requestCode + data + resultCode)
        //Result facebook signin
        callbackManager.onActivityResult(requestCode, resultCode, data)

        //nhan dien giong noi

        when (requestCode) {
            // Handle the result for our request code.
            REQUEST_CODE_STT -> {
                // Safety checks to ensure data is available.
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Retrieve the result array.
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    // Ensure result array is not null or empty to avoid errors.
                    if (!result.isNullOrEmpty()) {
                        // Recognized text is in the first position.
                        val recognizedText = result[0]
                        // Do what you want with the recognized text.

                        tv_search.setText(recognizedText)
                    }
                }
            }
        }
    }
//    private val textToSpeechEngine: TextToSpeech by lazy {
//        // Pass in context and the listener.
//        TextToSpeech(this,
//            TextToSpeech.OnInitListener { status ->
//                // set our locale only if init was success.
//                if (status == TextToSpeech.SUCCESS) {
//                    textToSpeechEngine.language = Locale.UK
//                }
//            })
//    }

    private fun setUpView() {
//        val type:String = intent.getStringExtra("type")
        tvTitle.text = getText(R.string.title_friends)
        imvBack.setOnClickListener {
            finish()
        }
        tx_friend_request.text = "${getText(R.string.friend_request)} (0)"
        tx_friend_list.text = "${getText(R.string.friend_list)} (0)"

        val display: Display = windowManager.defaultDisplay
        val stageWidth: Int = display.getWidth()

        val stageHeight: Int = display.getHeight()

        var barHeight = getStatusBarHeight()

        var width = Functions.convertPx(stageWidth, 96)
        var height = Functions.convertPx(stageHeight, (180 - barHeight))
        tv_search.layoutParams.width = (width).toInt()


        id_voice.setOnClickListener {
            // Get the Intent action
            val language = Locale.getDefault().language

            val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            // Language model defines the purpose, there are special models for other use cases, like search.
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, language)
            // Adding an extra language, you can use any language from the Locale class.
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,language)
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
            sttIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language);
            // Text that shows up on the Speech input prompt.
            sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")
            try {
                // Start the intent for a result, and pass in our request code.
                startActivityForResult(sttIntent, REQUEST_CODE_STT)
            } catch (e: ActivityNotFoundException) {
                // Handling error when the service is not available.
                e.printStackTrace()
                Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setFriendAdd(friendAdd: List<FriendAddModel>) {
        mRecyclerView = findViewById(R.id.rcv_friend_add)
        mRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        if (friendAdd.size > 0) {
            Functions.showLog("friendAdd ${friendAdd}")

            var friendAddAdapter = FriendAddAdapter(friendAdd, this)
            mRecyclerView.adapter = friendAddAdapter

            friendAddAdapter.onClickAvatarAdd = {
                val intent = Intent(this, FriendProfileActivity::class.java)
                intent.putExtra("nickname", it.nickname)
                intent.putExtra("id", it.id)
                intent.putExtra("avatar", it.avatar)
                startActivity(intent)
            }

            friendAddAdapter.onClickRequest = {
                isFacebook=false
                Functions.showLog("Request ${it.id}")
                showLoading()
                viewModelAddFriend.postAddFriend(RequestAddFriendModel(it.id))
            }

            friendAddAdapter.onClickCancleRequest = {
                isFacebook=false
                showLoading()
                viewModelAddFriendCancle.deleteCancleRequestFriend(it.id)
                Functions.showLog("CancleRequest")
            }
        }

//       FriendAddAdapter.
    }


    private fun socialConnectRes(resBaseModel: ResBaseModel?) {
        Functions.showLog("resBaseModel facebook " + resBaseModel)
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = GsonBuilder().create()

            val dataStr = resBaseModel?.data?.let { Functions.toJsonString(it) }
//            val resDistanceModel: SearchFriendModel? =
//                gson.fromJson(dataStr, SearchFriendModel::class.java)
////
////            Functions.showLog("resDistanceModel ${resDistanceModel}")
            val dataList = gson.fromJson(
                resBaseModel?.data?.let { Functions.toJsonString(it) },
                Array<FriendFacebookModel>::class.java
            ).toList()
            Functions.showLog("dataList ${dataStr}")
            Functions.showLog("resBaseModel ${resBaseModel?.data}")
            if (dataList != null) {
                setFriendToFacebook(dataList)
            }
        }
    }

    // friend facebook
    private fun setFriendToFacebook(friendAdd: List<FriendFacebookModel>) {
        mRecyclerView = findViewById(R.id.rcv_friend_on_facebook)
        mRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        if (friendAdd.size > 0) {
            Functions.showLog("friendAdd ${friendAdd}")
            rcv_friend_on_facebook.visibility = View.VISIBLE
//            rcv_friend_on_repace.visibility=Text.
            var friendsFacebookAdapter = FriendsFacebookAdapter(friendAdd, this)
            mRecyclerView.adapter = friendsFacebookAdapter

            friendsFacebookAdapter.onClickAvatarAdd = {
                val intent = Intent(this, FriendProfileActivity::class.java)
                intent.putExtra("nickname", it.nickname)
                intent.putExtra("id", it.id)
                intent.putExtra("avatar", it.avatar)
                intent.putExtra("status", it.status)
                startActivity(intent)
            }
//
            friendsFacebookAdapter.onClickFacebookRequest = {
                isFacebook=true
                Functions.showLog("Request ${it.id}")
                showLoading()
                viewModelAddFriend.postAddFriend(RequestAddFriendModel(it.id))
            }
//
            friendsFacebookAdapter.onClickCancleFacebookRequest = {
                Functions.showLog("CancleRequest")
                isFacebook=true
                showLoading()
                viewModelAddFriendCancle.deleteCancleRequestFriend(it.id)

            }
            // unfriend
            friendsFacebookAdapter.onClickUnfriendFacebookRequest={
                Functions.showLog("CancleRequest")
                isFacebook=true
                showLoading()
                viewModelUnFriend.deleteUnfriend(it.id)
            }

            //
            friendsFacebookAdapter.onClickRejectFacebookRequest={
                Functions.showLog("CancleRequest")
                isFacebook=true
                showLoading()
                viewModelRejectFriend.deleteFriendRequest(it.id)
            }
            //
            friendsFacebookAdapter.onClickAcceptFacebookRequest={
                Functions.showLog("CancleRequest")
                isFacebook=true
                showLoading()
                viewModelAcceptFriend.acceptFriendRequest(it.id)
            }
        }
    }

    private fun addFriendRequest(resBaseModel: ResBaseModel?) {
        Functions.showLog("resBaseModel fe "+resBaseModel)
        if (isFacebook==false) {
            viewModelSearchFriend.searchFriend(
                SearchFriend.Params(texSearch)
            )
        }else{
            // load lai du lieu facebook
            viewModelSocialConnect.socialConnectRequest(RequestSocialConnectModel(socialId.toString(), socialToken.toString()))
        }
    }

    private fun setFriendRequest(friendRequest: List<FriendRequestModel>) {

        mRecyclerView = findViewById(R.id.rcv_friend_Request)
        mRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
//        if(friendRequest.size==0){
//
//        }
        if (friendRequest.size > 0) {

//            tx_friend_request.layoutParams.height=Functions.dpToPx(80*friendRequest.size+40)
            tx_friend_request.text =
                "${getText(R.string.friend_request)} (${friendRequest.size})"
            id_did_not_friend.visibility = View.GONE
            rcv_friend_Request.visibility = View.VISIBLE
            var friendAdapter = FriendAdapter(friendRequest, this)
            mRecyclerView.adapter = friendAdapter
//Reject friend
            friendAdapter.onClickReject = {
                isFacebook=false
                val mBuilder = AlertDialog.Builder(this)
                mBuilder.setTitle(getString(R.string.reject))

                mBuilder.setMessage("Do you reject ${it.nickname} friend request?")
                mBuilder.setPositiveButton(getString(R.string.btn_cancel)) { dialog, which ->
//                    AlertDialog.BUTTON_POSITIVE
                    dialog.cancel()
                }
                mBuilder.setNegativeButton(getString(R.string.btn_ok)) { dialog, which ->
                    dialog.dismiss()
                    showLoading()
                    it.id?.let { it1 -> viewModelRejectFriend.deleteFriendRequest(it1) }

                }
                val mDialog = mBuilder.create()
//                mDialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_gender)
                mDialog.show()
//                viewModelRejectFriend.deleteFriendRequest(it.id)
                val posBtn = mDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                val negBtn = mDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                posBtn.setTextColor(ContextCompat.getColor(this, R.color.btnColorCommon))
                negBtn.setTextColor(ContextCompat.getColor(this, R.color.btnColorCommon))
            }
            //Accept friend
            friendAdapter.onClickAccept = {
                isFacebook=false
                showLoading()
                it.id?.let { it1 -> viewModelAcceptFriend.acceptFriendRequest(it1) }
            }

            //go to friend profile
            friendAdapter.onClickAvatarRequest = {
                Functions.showLog("show lieu ${it.id}")
                val intent = Intent(this, FriendProfileActivity::class.java)
                intent.putExtra("nickname", it.nickname)
                intent.putExtra("id", it.id)
                intent.putExtra("avatar", it.avatar)
                startActivity(intent)
            }
        } else {
            tx_friend_request.text =
                "${getText(R.string.friend_request)} (${friendRequest.size})"
            id_did_not_friend.visibility = View.VISIBLE
            rcv_friend_Request.visibility = View.GONE
        }
    }

    private fun setFriendList(dataList: List<FriendListModel>) {

        mRecyclerView = findViewById(R.id.rcv_friend_list)
        mRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        if (dataList.size > 0) {

//            rcv_friend_list.layoutParams.height=(80*dataList.size).toInt()
            rcv_friend_list.layoutParams.height = Functions.dpToPx(96 * dataList.size + 0)
            tx_friend_list.text =
                "${getText(R.string.friend_list)} (${dataList.size})"
            id_do_not_friend.visibility = View.GONE
            rcv_friend_list.visibility = View.VISIBLE

//            btnUnfriend.

            var friendListAdapter = FriendListAdapter(dataList, this)
            mRecyclerView.adapter = friendListAdapter

            // Unfriend
            friendListAdapter.onClickUnfriend = {
                Functions.showLog("voooooo")
                isFacebook=false

                val mBuilderUnfriend = AlertDialog.Builder(this)
                mBuilderUnfriend.setTitle("Unfriend")

                mBuilderUnfriend.setMessage("Are you sure you want to unfriend ${it.nickname}?")

                mBuilderUnfriend.setPositiveButton("Cancel") { dialog, which ->
//                    AlertDialog.BUTTON_POSITIVE
                    dialog.cancel()
                }
                mBuilderUnfriend.setNegativeButton("Ok") { dialog, which ->
                    Functions.showLog("du lieu ${it.id}")
                    showLoading()
                    viewModelUnFriend.deleteUnfriend(it.id)
                    dialog.dismiss()
                }
                val mDialog = mBuilderUnfriend.create()
                mDialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog_gender)
                mDialog.show()
            }
            // go to user Profile
            friendListAdapter.onClickFriend = {
                Functions.showLog("show lieu ${it.id}")

                val intent = Intent(this, FriendProfileActivity::class.java)
                intent.putExtra("nickname", it.nickname)
                intent.putExtra("id", it.id)
                intent.putExtra("avatar", it.avatar)
                startActivity(intent)
            }

        } else {
            tx_friend_list.text =
                "${getText(R.string.friend_list)} (${dataList.size})"
            id_do_not_friend.visibility = View.VISIBLE
            rcv_friend_list.visibility = View.GONE

        }

    }
}