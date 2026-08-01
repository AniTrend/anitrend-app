package com.mxt.anitrend.adapter.pager.index

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.extension.LAZY_MODE_UNSAFE
import com.mxt.anitrend.ui.fragmentByTagOrNew
import com.mxt.anitrend.ui.model.FragmentItem
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

    private val fragmentItems by lazy(LAZY_MODE_UNSAFE) {
        listOf<FragmentItem<Fragment>>(
            FragmentItem(
                FeedListFragment::class.java,
                Bundle(params).apply {
                    putBoolean(KeyUtil.arg_isFollowing, true)
                    putString(KeyUtil.arg_type, KeyUtil.MEDIA_LIST)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
                "FeedListFragmentMediaList",
            ),
            FragmentItem(
                FeedListFragment::class.java,
                Bundle(params).apply {
                    putBoolean(KeyUtil.arg_isFollowing, true)
                    putString(KeyUtil.arg_type, KeyUtil.TEXT)
                    putBoolean(KeyUtil.arg_asHtml, false)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
                "FeedListFragmentText",
            ),
            FragmentItem(
                FeedListFragment::class.java,
                Bundle(params).apply {
                    putBoolean(KeyUtil.arg_isFollowing, false)
                    putBoolean(KeyUtil.arg_isMixed, true)
                    putBoolean(KeyUtil.arg_asHtml, false)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
                "FeedListFragmentMixed",
            ),
        )
    }

    override fun createFragment(position: Int): Fragment = fragmentItems[position].fragmentByTagOrNew(fragmentActivity)
}
