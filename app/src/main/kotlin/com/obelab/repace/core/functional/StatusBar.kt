package com.obelab.repace.core.functional

import android.content.res.Resources
import android.util.Log
import com.google.gson.Gson
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.FileHelper
import com.obelab.repace.features.welcome.WelcomeActivity
import com.obelab.repace.model.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class StatusBar {
    companion object {
        fun StatusBarHeight(height: Int): Int {

            return if(height<80){
                0
            }else {
                 80
            }

        }
    }
}