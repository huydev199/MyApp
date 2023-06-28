package com.obelab.repace.features.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.obelab.repace.core.platform.BaseFragment
import com.obelab.repace.features.exercise.ExerciseFragment
import com.obelab.repace.features.home.HomeFragment
import com.obelab.repace.features.ltTest.LtTestFragment
import com.obelab.repace.features.me.MeFragment

class MainViewPagerAdapter (fragmentActivity: FragmentActivity, private val fragments: List<Fragment>) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}