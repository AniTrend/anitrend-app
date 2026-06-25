package com.mxt.anitrend.adapter.pager.index

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.detail.BrowseReviewFragment

/**
 * Created by max on 2017/10/30.
 * ReviewPageAdapter
 */
class ReviewPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    init {
        setPagerTitles(R.array.reviews_title)
    }

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> BrowseReviewFragment.newInstance(KeyUtil.ANIME)
        1 -> BrowseReviewFragment.newInstance(KeyUtil.MANGA)
        else -> throw IndexOutOfBoundsException("Invalid position: $position")
    }
}
