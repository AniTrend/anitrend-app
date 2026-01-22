package com.mxt.anitrend.adapter.pager.detail

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.view.fragment.detail.MediaFeedFragment
import com.mxt.anitrend.view.fragment.detail.MediaOverviewFragment
import com.mxt.anitrend.view.fragment.detail.MediaStaffFragment
import com.mxt.anitrend.view.fragment.detail.MediaStatsFragment
import com.mxt.anitrend.view.fragment.detail.ReviewFragment
import com.mxt.anitrend.view.fragment.group.MediaCharacterFragment
import com.mxt.anitrend.view.fragment.group.MediaRecommendationsFragment
import com.mxt.anitrend.view.fragment.group.MediaRelationFragment

/**
 * Created by max on 2017/12/01.
 */
class AnimePageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context
) : BaseStatePageAdapter(fragmentActivity, context) {

    private val isAuthenticated = koinOf<Settings>().isAuthenticated

    init {
        setPagerTitles(R.array.anime_page_titles)
    }

    override fun getItemCount(): Int {
        return if (isAuthenticated) super.getItemCount() else super.getItemCount() - 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MediaOverviewFragment.newInstance(params)
            1 -> MediaRelationFragment.newInstance(params)
            2 -> MediaRecommendationsFragment.newInstance(params)
            3 -> MediaStatsFragment.newInstance(params)
            4 -> MediaCharacterFragment.newInstance(params)
            5 -> MediaStaffFragment.newInstance(params)
            6 -> MediaFeedFragment.newInstance(
                params,
                GraphUtil.getDefaultQuery(true)
                    .putVariable(KeyUtil.arg_mediaId, params.getLong(KeyUtil.arg_id))
                    .putVariable(KeyUtil.arg_type, KeyUtil.ANIME_LIST)
                    .putVariable(KeyUtil.arg_isFollowing, true)
            )
            7 -> ReviewFragment.newInstance(params)
            else -> throw IndexOutOfBoundsException("Invalid position: $position")
        }
    }
}
