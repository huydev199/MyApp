package com.obelab.repace.features.empty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.obelab.repace.R
import com.obelab.repace.core.platform.BaseActivity

class EmptyActivity : BaseActivity() {

    companion object {
        fun callingIntent(context: Context) = Intent(context, FriendsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty)
    }
}