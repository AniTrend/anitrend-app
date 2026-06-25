package com.mxt.anitrend.adapter.pager.index

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.fragment.search.CharacterSearchFragment
import com.mxt.anitrend.view.fragment.search.MediaSearchFragment
import com.mxt.anitrend.view.fragment.search.StaffSearchFragment
import com.mxt.anitrend.view.fragment.search.StudioSearchFragment
import com.mxt.anitrend.view.fragment.search.UserSearchFragment

/**
 * Created by max on 2017/12/19.
 */
class SearchPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    init {
        val settings = koinOf<Settings>()
        setPagerTitles(
            if (settings.isAuthenticated) {
                R.array.search_titles_auth
            } else {
                R.array.search_titles
            },
        )
    }

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> MediaSearchFragment.newInstance(params, KeyUtil.ANIME)
        1 -> MediaSearchFragment.newInstance(params, KeyUtil.MANGA)
        2 -> StudioSearchFragment.newInstance(params)
        3 -> StaffSearchFragment.newInstance(params)
        4 -> CharacterSearchFragment.newInstance(params)
        5 -> UserSearchFragment.newInstance(params)
        else -> throw IndexOutOfBoundsException("Invalid position: $position")
    }
}
