package com.obelab.repace.features.main

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.obelab.repace.DBManager.PrefManager
import com.obelab.repace.R
import com.obelab.repace.core.exception.Failure
import com.obelab.repace.core.extension.failure
import com.obelab.repace.core.extension.observe
import com.obelab.repace.core.functional.Functions
import com.obelab.repace.core.platform.BaseActivity
import com.obelab.repace.core.util.Constants
import com.obelab.repace.core.util.PermissionHelper
import com.obelab.repace.features.empty.FriendsActivity
import com.obelab.repace.features.exercise.*
import com.obelab.repace.features.goals.GoalsActivity
import com.obelab.repace.features.home.HomeFragment
import com.obelab.repace.features.ltTest.LtTestFragment
import com.obelab.repace.features.ltTest.LtTestHistoryActivity
import com.obelab.repace.features.ltTest.LtTestSettingFragment
import com.obelab.repace.features.ltTest.LtTestSummaryFragment
import com.obelab.repace.features.me.MeFragment
import com.obelab.repace.features.notices.NoticesActivity
import com.obelab.repace.features.pairing.PairingActivity
import com.obelab.repace.model.LtTestSettingModel
import com.obelab.repace.model.ResBaseModel
import com.obelab.repace.model.ResMemberSettingModel
import com.obelab.repace.model.ResMemberTokenModel
import com.obelab.repace.service.ble.BleService
import com.obelab.repace.viewModel.FcmViewModel
import com.obelab.repace.viewModel.LtTestSettingViewModel
import com.obelab.repace.viewModel.PreferenceViewModel
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {

    private var doubleBackToExitPressedOnce = false
    var mainViewPagerAdapter: MainViewPagerAdapter? = null
    var currentTab = 0
    var fragmentList = ArrayList<Fragment>()
    lateinit var typeScreen: String
    lateinit var typeExercise: String
    var isConnect = false
    var isLowMore = true
    private val viewModel: LtTestSettingViewModel by viewModels()
    val fcmViewModel: FcmViewModel by viewModels()
    val preferenceViewModel: PreferenceViewModel by viewModels()
    private var memberSetting = ResMemberSettingModel()

    private lateinit var mService: BleService
    private var mBound: Boolean = false

    companion object {

        var instance: MainActivity? = null
        var isStartExercise = false
        var isGoLtTest = false

        fun callingIntent(context: Context): Intent {
            var i = Intent(context, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return i
        }

        fun callingIntent(context: Context, isNavigate: Boolean): Intent {
            isStartExercise = isNavigate
            var i = Intent(context, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return i
        }

        fun callingIntentGoToLtTest(context: Context, isNavigate: Boolean): Intent {
            isGoLtTest = isNavigate
            var i = Intent(context, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return i
        }
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as BleService.LocalBinder
            mService = binder.getService()
            mBound = true
            if (mService.mConnectionState == 2) {
                turnOnBluetooth()
            } else {
                turnOffBluetooth()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        with(viewModel) {
            observe(resLtTestSetting, ::renderLtTestSetting)
            failure(failure, ::handleFailure)
        }
        with(preferenceViewModel) {
            observe(resMemberSetting, ::renderMemberSetting)
            observe(resUpdateMemberSetting, ::renderUpdateMemberSetting)
            failure(failure, ::handleFailure)
        }
        instance = this

        viewModel.getLtTestSetting()
        Handler().postDelayed({
            preferenceViewModel.getMemberSetting()
        }, 100)
        setUpView()
        setUpDrawer()
        actionView()

        val intent = Intent(this, BleService::class.java)
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)

        if (intent.getStringExtra("op") != null) {
            typeExercise = intent.getStringExtra(Constants.typeExercise)!!
        }
        if (!PermissionHelper.checkStoragePermission()) {
            PermissionHelper.requestStoragePermission()
        }
        //testSaveFile()
        llDrawer.setOnClickListener(View.OnClickListener {
        })
    }

    fun sendFcmToken(token: String?) {
        val body = ResMemberTokenModel(token)
        with(fcmViewModel) {
            observe(resMemberToken, ::renderFcmToken)
            failure(failure, ::handleFailure)
        }
        fcmViewModel.putUpdateMemberToken(body)
    }

    fun turnOffBluetooth() {
        ivBluetooth.setImageResource(R.drawable.ic_bluetooth_off);
        ivBluetoothHome.setImageResource(R.drawable.ic_bluetooth_off);
        isConnect = false
    }

    fun turnOnBluetooth() {
        ivBluetooth.setImageResource(R.drawable.ic_bluetooth_on);
        ivBluetoothHome.setImageResource(R.drawable.ic_bluetooth_on);
        isConnect = true
    }

    private fun setUpView() {
        fragmentList.add(HomeFragment())
        fragmentList.add(LtTestFragment())
        fragmentList.add(ExerciseFragment())
        fragmentList.add(MeFragment())
        mainViewPagerAdapter = MainViewPagerAdapter(this, fragmentList)
        viewPager.adapter = mainViewPagerAdapter
        viewPager.offscreenPageLimit = 4
        // off swipe horizontall
        viewPager.setUserInputEnabled(false);
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = ""
        }.attach()

        setUpTabIcon()
        ivMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        ivMenuHome.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        imvCloseDrawer.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentTab = position
                setUpTabIcon()
            }
        })
        if (isStartExercise == true) {
            fragmentList[2] = ExerciseFragment()
            mainViewPagerAdapter = MainViewPagerAdapter(this, fragmentList)
            viewPager.adapter = mainViewPagerAdapter
            viewPager.setCurrentItem(2, false)
        }

        if (isGoLtTest == true) {
            fragmentList[1] = LtTestSummaryFragment()
            mainViewPagerAdapter = MainViewPagerAdapter(this, fragmentList)
            viewPager.adapter = mainViewPagerAdapter
            viewPager.setCurrentItem(1, false)
        }
    }

    private fun actionView() {
        ivBluetooth.setOnClickListener {
            startActivity(PairingActivity.callingIntent(this))
        }
        ivBluetoothHome.setOnClickListener {
            startActivity(PairingActivity.callingIntent(this))
        }
    }

    // let menu
    private fun setUpDrawer() {
        btnMenuNotice.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(NoticesActivity.callingIntent(this))
        }
        btnMenuLtTestHistory.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(LtTestHistoryActivity.callingIntent(this))
        }
        btnMenuExerciseHistory.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(ExerciseHistoryActivity.callingIntent(this))
        }
        btnMenuExerciseStatistics.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(ExerciseStatisticAllActivity.callingIntent(this))
        }
        btnMenuGoals.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(GoalsActivity.callingIntent(this))
        }
        btnMenuFriends.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(FriendsActivity.callingIntent(this))
        }
        ll_me.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            fragmentList[3] = MeFragment()
            mainViewPagerAdapter = MainViewPagerAdapter(this, fragmentList)
            viewPager.adapter = mainViewPagerAdapter
            viewPager.setCurrentItem(3, false)
        }
    }

    private fun setUpTabIcon() {
        tabLayout.getTabAt(0)?.icon = getDrawable(R.drawable.ic_tab_home_default)
        tabLayout.getTabAt(1)?.icon = getDrawable(R.drawable.ic_tab_lt_test_default)
        tabLayout.getTabAt(2)?.icon = getDrawable(R.drawable.ic_tab_exersise_default)
        tabLayout.getTabAt(3)?.icon = getDrawable(R.drawable.ic_tab_me_default)

        when (currentTab) {
            0 -> {
                llHeaderHome.visibility = View.VISIBLE
                llHeader.visibility = View.GONE
                tabLayout.getTabAt(0)?.icon = getDrawable(R.drawable.ic_tab_home_focus)
                tvTitleHome.text = ""
            }
            1 -> {
                llHeaderHome.visibility = View.GONE
                llHeader.visibility = View.VISIBLE
                tabLayout.getTabAt(1)?.icon = getDrawable(R.drawable.ic_tab_lt_test_focus)
                if (fragmentList[1].javaClass.name == LtTestSummaryFragment().javaClass.name || fragmentList[1].javaClass.name == LtTestFragment().javaClass.name) {
                    tvTitle.text = getString(R.string.tab_lt_test)
                    ivBluetooth.visibility = View.VISIBLE
                    tvTitle.visibility = View.VISIBLE
                    ivMenu.visibility = View.VISIBLE
                    ivShare.visibility = View.GONE
                } else {
                    llHeader.visibility = View.GONE
                }
            }
            2 -> {
                llHeaderHome.visibility = View.GONE
                llHeader.visibility = View.VISIBLE
                tabLayout.getTabAt(2)?.icon = getDrawable(R.drawable.ic_tab_exersise_forcus)
                if (fragmentList[2].javaClass.name == ExerciseFragment().javaClass.name) {
                    tvTitle.text = getString(R.string.tab_exercise)
                    ivMenu.visibility = View.VISIBLE
                    tvTitle.visibility = View.VISIBLE
                    ivBluetooth.visibility = View.VISIBLE
                    ivShare.visibility = View.GONE
                } else {
                    llHeader.visibility = View.GONE
                }
            }
            3 -> {
                llHeaderHome.visibility = View.GONE
                llHeader.visibility = View.VISIBLE
                tabLayout.getTabAt(3)?.icon = getDrawable(R.drawable.ic_tab_me_forcus)
                ivMenu.visibility = View.VISIBLE
                tvTitle.visibility = View.VISIBLE
                tvTitle.text = getString(R.string.tab_me)
                ivBluetooth.visibility = View.INVISIBLE
                ivShare.visibility = View.GONE
            }
        }
    }

    fun goToLtTestFragment() {
        fragmentList[1] = LtTestFragment()
        mainViewPagerAdapter = MainViewPagerAdapter(this, fragmentList)
        viewPager.adapter = mainViewPagerAdapter
        viewPager.setCurrentItem(1, false)
    }

    fun goToLtTestSummaryFragment() {
        fragmentList[1] = LtTestSummaryFragment()
        mainViewPagerAdapter = MainViewPagerAdapter(this, fragmentList)
        viewPager.adapter = mainViewPagerAdapter
        viewPager.setCurrentItem(1, false)
    }

    fun goToLtTestSettingFragment() {
        fragmentList[1] = LtTestSettingFragment()
        mainViewPagerAdapter = MainViewPagerAdapter(this, fragmentList)
        viewPager.adapter = mainViewPagerAdapter
        viewPager.setCurrentItem(1, false)
    }

    fun goToExerciseCalendarFragment(type: String?) {
        if (type != null) {
            typeScreen = type
        }
        fragmentList[2] = ExerciseCalendarFragment()
        mainViewPagerAdapter = MainViewPagerAdapter(this, fragmentList)
        viewPager.adapter = mainViewPagerAdapter
        viewPager.setCurrentItem(2, false)
    }

    fun goToExerciseFragment() {
        fragmentList[2] = ExerciseFragment()
        mainViewPagerAdapter = MainViewPagerAdapter(this, fragmentList)
        viewPager.adapter = mainViewPagerAdapter
        viewPager.setCurrentItem(2, false)
    }

    fun goToExerciseSelectFragment(type: String?) {
        if (type != null) {
            typeScreen = type
        }
        fragmentList[2] = ExerciseSelectFragment()
        mainViewPagerAdapter = MainViewPagerAdapter(this, fragmentList)
        viewPager.adapter = mainViewPagerAdapter
        viewPager.setCurrentItem(2, false)
    }

    fun goToExerciseMoreFragment(isLow: Boolean) {
        isLowMore = isLow
        fragmentList[2] = ExerciseMoreFragment()
        mainViewPagerAdapter = MainViewPagerAdapter(this, fragmentList)
        viewPager.adapter = mainViewPagerAdapter
        viewPager.setCurrentItem(2, false)
    }

    fun goToExerciseGuideFragment(type: String?) {
        if (type != null) {
            typeExercise = type
        }
        fragmentList[2] = ExerciseGuideFragment()
        mainViewPagerAdapter = MainViewPagerAdapter(this, fragmentList)
        viewPager.adapter = mainViewPagerAdapter
        viewPager.setCurrentItem(2, false)
    }

    fun goToExerciseGuideRestFragment() {
        fragmentList[2] = ExerciseGuideRestFragment()
        mainViewPagerAdapter = MainViewPagerAdapter(this, fragmentList)
        viewPager.adapter = mainViewPagerAdapter
        viewPager.setCurrentItem(2, false)
    }

    override fun onResume() {
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        super.onResume()
        setUpProfile()
    }

    override val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BleService.ACTION_GATT_CONNECTED == action) {
                turnOnBluetooth()
                if (currentTab == 1 && fragmentList[1].javaClass.name == LtTestFragment().javaClass.name) {
                    var ltTest: LtTestFragment = fragmentList[1] as LtTestFragment
                    ltTest.changeConnect()
                }
            } else if (BleService.ACTION_GATT_DISCONNECTED == action) {
                turnOffBluetooth()
                if (currentTab == 1 && fragmentList[1].javaClass.name == LtTestFragment().javaClass.name) {
                    var ltTest: LtTestFragment = fragmentList[1] as LtTestFragment
                    ltTest.changeConnect()
                }
                showPopupDisconnectedDevice()
            }
        }
    }

    private fun setUpProfile() {
        val profile = PrefManager.getProfile()
        tvUserName.text = Constants.DEFAULT_USERNAME
        if (profile?.nickname != null)
            tvUserName.text = profile.nickname
//

        Glide.with(this).load(profile?.avatar)
            .placeholder(R.drawable.ic_avatar_default)
            .error(R.drawable.ic_avatar_default)
            .into(civAvatar)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            showToast(getString(R.string.double_back_to_exit))
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                doubleBackToExitPressedOnce = false
            }, 2000)
        }
    }

    private fun renderMemberSetting(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            try {
                val gson = Gson()
                val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
                memberSetting = gson.fromJson(dataStr, ResMemberSettingModel::class.java)
                Functions.showLog("AAAA " + resBaseModel.success + " " + (memberSetting.guide!!.toInt() == 1) + " " + memberSetting.guide)
                if (memberSetting.guide == 1) PrefManager.saveIsSpeakerTurnOn(true) else PrefManager.saveIsSpeakerTurnOn(false)
                //Functions.showLog("resMemberSettingModel: " + memberSetting?.let { Functions.toJsonString(it) })
            } catch (e: Exception) {
                Functions.showLog("renderMemberSetting: $e")
            }
        } else {
            resBaseModel?.msg?.let { Functions.showLog(it) }
        }
    }

    private fun renderUpdateMemberSetting(resBaseModel: ResBaseModel?) {
        Functions.showLog("resUpdateMemberSetting: " + resBaseModel?.let { Functions.toJsonString(it) })
        hideLoading()
        if (resBaseModel?.success == true) {
        } else {
            resBaseModel?.msg?.let { showToast(it) }
        }
    }

    private fun renderLtTestSetting(resBaseModel: ResBaseModel?) {
        hideLoading()
        if (resBaseModel?.success == true) {
            val gson = Gson()
            val dataStr = resBaseModel.data?.let { Functions.toJsonString(it) }
            val resSettingLtTest: LtTestSettingModel? =
                gson.fromJson(dataStr, LtTestSettingModel::class.java)
            Functions.showLog("resLtTestSetting: " + resSettingLtTest?.let {
                Functions.toJsonString(
                    it
                )
            })
        } else {
            resBaseModel?.msg?.let { Functions.showLog(it) }
        }
    }

    private fun renderFcmToken(resBaseModel: ResBaseModel?) {
        hideLoading()
    }

    private fun handleFailure(failure: Failure?) {
        Functions.showLog("Get Lt Test Setting Fail: " + failure.toString())
        hideLoading()
    }
}