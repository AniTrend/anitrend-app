package com.mxt.anitrend.adapter.pager

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup
import java.util.*


class GenericFragmentStatePagerAdapter(manager: FragmentManager, val titles: Array<String>?, val pagerCount: Int, val listener: Listener) : FragmentStatePagerAdapter(manager) {

    override fun getItem(position: Int): Fragment? = listener.getFragmentForItem(position)

    override fun getCount(): Int = pagerCount

    override fun getPageTitle(position: Int): CharSequence = if (titles != null) titles[position].toUpperCase(Locale.getDefault()) else super.getPageTitle(position)

    override fun startUpdate(container: ViewGroup) = super.startUpdate(container)

    interface Listener {
        fun getFragmentForItem(position: Int): Fragment?
    }
}