package com.mxt.anitrend.adapter.pager.detail

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.extension.LAZY_MODE_UNSAFE
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.ui.fragmentByTagOrNew
import com.mxt.anitrend.ui.model.FragmentItem
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
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
class MangaPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    private val isAuthenticated = koinOf<Settings>().isAuthenticated

    init {
        setPagerTitles(R.array.manga_page_titles)
    }

    private val fragmentItems by lazy(LAZY_MODE_UNSAFE) {
        listOf<FragmentItem<Fragment>>(
            FragmentItem(MediaOverviewFragment::class.java, Bundle(params)),
            FragmentItem(MediaRelationFragment::class.java, Bundle(params)),
            FragmentItem(MediaRecommendationsFragment::class.java, Bundle(params)),
            FragmentItem(MediaStatsFragment::class.java, Bundle(params)),
            FragmentItem(MediaCharacterFragment::class.java, Bundle(params)),
            FragmentItem(MediaStaffFragment::class.java, Bundle(params)),
            FragmentItem(
                MediaFeedFragment::class.java,
                Bundle(params).apply {
                    putLong(KeyUtil.arg_mediaId, params.getLong(KeyUtil.arg_id))
                    putBoolean(KeyUtil.arg_isFollowing, true)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            ),
            FragmentItem(ReviewFragment::class.java, Bundle(params)),
        )
    }

    override fun getItemCount(): Int = if (isAuthenticated) super.getItemCount() else super.getItemCount() - 2

    override fun createFragment(position: Int): Fragment = fragmentItems[position].fragmentByTagOrNew(fragmentActivity)
}
