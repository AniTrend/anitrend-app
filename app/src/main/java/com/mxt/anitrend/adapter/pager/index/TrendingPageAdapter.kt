package com.mxt.anitrend.adapter.pager.index

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.view.fragment.list.MediaLatestList

/**
 * Created by max on 2017/10/30.
 */
class TrendingPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context
) : BaseStatePageAdapter(fragmentActivity, context) {

    init {
        setPagerTitles(R.array.trending_title)
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MediaLatestList.newInstance(
                params,
                GraphUtil.getDefaultQuery(true)
                    .putVariable(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                    .putVariable(KeyUtil.arg_sort, KeyUtil.TRENDING + KeyUtil.DESC)
            )
            1 -> MediaLatestList.newInstance(
                params,
                GraphUtil.getDefaultQuery(true)
                    .putVariable(KeyUtil.arg_mediaType, KeyUtil.MANGA)
                    .putVariable(KeyUtil.arg_sort, KeyUtil.TRENDING + KeyUtil.DESC)
            )
            2 -> MediaLatestList.newInstance(
                params,
                GraphUtil.getDefaultQuery(true)
                    .putVariable(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                    .putVariable(KeyUtil.arg_sort, KeyUtil.ID + KeyUtil.DESC)
            )
            else -> throw IndexOutOfBoundsException("Invalid position: $position")
        }
    }
}
