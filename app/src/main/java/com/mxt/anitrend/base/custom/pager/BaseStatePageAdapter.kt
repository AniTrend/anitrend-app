package com.mxt.anitrend.base.custom.pager

import android.content.Context
import android.os.Bundle
import androidx.annotation.ArrayRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mxt.anitrend.extension.LAZY_MODE_UNSAFE
import com.mxt.anitrend.extension.getStringList
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.locale.LocaleUtil

/**
 * Created by max on 2017/06/26.
 * Base page state adapter
 */
abstract class BaseStatePageAdapter(
    fragmentActivity: FragmentActivity,
    private val context: Context,
) : FragmentStateAdapter(fragmentActivity) {
    var params: Bundle = Bundle.EMPTY

    lateinit var pagerTitles: List<String>
        private set

    private val settings by lazy(LAZY_MODE_UNSAFE) {
        koinOf<Settings>()
    }

    fun setPagerTitles(
        @ArrayRes mTitleRes: Int,
    ) {
        pagerTitles = context.getStringList(mTitleRes, settings)
    }

    /**
     * Return the number of views available.
     */
    override fun getItemCount(): Int = pagerTitles.size

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    abstract override fun createFragment(position: Int): Fragment

    /**
     * This method may be called by the ViewPager to obtain a title string
     * to describe the specified page. This method may return null
     * indicating no title for this page. The default implementation returns
     * null.
     *
     * @param position The position of the title requested
     * @return A title for the requested page
     */
    fun getPageTitle(position: Int): CharSequence {
        val locale = LocaleUtil.scopeLocale(settings)
        return pagerTitles[position].uppercase(locale)
    }
}
