package com.mxt.anitrend.adapter.pager.detail

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.fragment.detail.StaffOverviewFragment
import com.mxt.anitrend.view.fragment.group.MediaAnimeRoleFragment
import com.mxt.anitrend.view.fragment.group.MediaFormatFragment
import com.mxt.anitrend.view.fragment.group.MediaStaffRoleFragment

/**
 * Created by max on 2017/12/01.
 */
class StaffPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    private val isAuthenticated = koinOf<Settings>().isAuthenticated

    init {
        setPagerTitles(R.array.staff_page_titles)
    }

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> StaffOverviewFragment.newInstance(params)
        1 ->
            MediaAnimeRoleFragment
                .newInstance(params, KeyUtil.ANIME, KeyUtil.STAFF_CHARACTERS_REQ)
                .apply { setFilterable(isAuthenticated) }
        2 ->
            MediaFormatFragment
                .newInstance(params, KeyUtil.MANGA, KeyUtil.STAFF_MEDIA_REQ)
                .apply { setFilterable(isAuthenticated) }
        3 ->
            MediaStaffRoleFragment
                .newInstance(params)
                .apply { setFilterable(isAuthenticated) }
        else -> throw IndexOutOfBoundsException("Invalid position: $position")
    }
}
