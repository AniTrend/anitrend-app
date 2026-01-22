package com.mxt.anitrend.adapter.pager.index

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.view.fragment.list.AiringListFragment
import com.mxt.anitrend.view.fragment.list.WatchListFragment

/**
 * Created by max on 2017/11/03.
 */
class AiringPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context
) : BaseStatePageAdapter(fragmentActivity, context) {

    init {
        setPagerTitles(R.array.airing_title)
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AiringListFragment.newInstance()
            1 -> {
                val externalLinks = arrayListOf(ExternalLink(BuildConfig.FEEDS_LINK, null))
                WatchListFragment.newInstance(externalLinks, false)
            }
            else -> throw IndexOutOfBoundsException("Invalid position: $position")
        }
    }
}
