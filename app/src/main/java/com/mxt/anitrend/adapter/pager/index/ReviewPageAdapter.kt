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

    private val fragmentItems by lazy(LAZY_MODE_UNSAFE) {
        listOf<FragmentItem<Fragment>>(
            FragmentItem(
                BrowseReviewFragment::class.java,
                Bundle().apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                },
                "BrowseReviewFragmentAnime",
            ),
            FragmentItem(
                BrowseReviewFragment::class.java,
                Bundle().apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.MANGA)
                },
                "BrowseReviewFragmentManga",
            ),
        )
    }

    override fun createFragment(position: Int): Fragment = fragmentItems[position].fragmentByTagOrNew(fragmentActivity)
}
