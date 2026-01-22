package com.mxt.anitrend.adapter.pager.detail

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.view.fragment.detail.UserFeedFragment
import com.mxt.anitrend.view.fragment.detail.UserOverviewFragment

/**
 * Created by max on 2017/11/16.
 */
class ProfilePageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context
) : BaseStatePageAdapter(fragmentActivity, context) {

    init {
        setPagerTitles(R.array.profile_page_titles)
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserOverviewFragment.newInstance(params)
            1 -> UserFeedFragment.newInstance(
                params,
                GraphUtil.getDefaultQuery(true)
                    .putVariable(KeyUtil.arg_type, KeyUtil.MEDIA_LIST)
            )
            2 -> UserFeedFragment.newInstance(
                params,
                GraphUtil.getDefaultQuery(true)
                    .putVariable(KeyUtil.arg_type, KeyUtil.TEXT)
            )
            else -> throw IndexOutOfBoundsException("Invalid position: $position")
        }
    }
}
