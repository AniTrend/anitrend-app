package com.mxt.anitrend.adapter.pager.detail

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.detail.UserFeedFragment
import com.mxt.anitrend.view.fragment.detail.UserOverviewFragment

/**
 * Created by max on 2017/11/16.
 */
class ProfilePageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    init {
        setPagerTitles(R.array.profile_page_titles)
    }

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> UserOverviewFragment.newInstance(params)
        1 ->
            UserFeedFragment.newInstance(
                Bundle(params).apply {
                    putString(KeyUtil.arg_type, KeyUtil.MEDIA_LIST)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            )
        2 ->
            UserFeedFragment.newInstance(
                Bundle(params).apply {
                    putString(KeyUtil.arg_type, KeyUtil.TEXT)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            )
        else -> throw IndexOutOfBoundsException("Invalid position: $position")
    }
}
