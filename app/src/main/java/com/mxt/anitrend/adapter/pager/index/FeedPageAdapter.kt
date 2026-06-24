package com.mxt.anitrend.adapter.pager.index

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.list.FeedListFragment

/**
 * Created by max on 2017/11/07.
 */
class FeedPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    init {
        setPagerTitles(R.array.feed_titles)
    }

    override fun createFragment(position: Int): Fragment = when (position) {
        0 ->
            FeedListFragment.newInstance(
                Bundle(params).apply {
                    putBoolean(KeyUtil.arg_isFollowing, true)
                    putString(KeyUtil.arg_type, KeyUtil.MEDIA_LIST)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            )
        1 ->
            FeedListFragment.newInstance(
                Bundle(params).apply {
                    putBoolean(KeyUtil.arg_isFollowing, true)
                    putString(KeyUtil.arg_type, KeyUtil.TEXT)
                    putBoolean(KeyUtil.arg_asHtml, false)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            )
        2 ->
            FeedListFragment.newInstance(
                Bundle(params).apply {
                    putBoolean(KeyUtil.arg_isFollowing, false)
                    putBoolean(KeyUtil.arg_isMixed, true)
                    putBoolean(KeyUtil.arg_asHtml, false)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            )
        else -> throw IndexOutOfBoundsException("Invalid position: $position")
    }
}
