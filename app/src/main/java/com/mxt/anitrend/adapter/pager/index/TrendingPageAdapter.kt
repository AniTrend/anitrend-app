package com.mxt.anitrend.adapter.pager.index

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.list.MediaLatestList

/**
 * Created by max on 2017/10/30.
 */
class TrendingPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    init {
        setPagerTitles(R.array.trending_title)
    }

    override fun createFragment(position: Int): Fragment = when (position) {
        0 ->
            MediaLatestList.newInstance(
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                    putString(KeyUtil.arg_sort, KeyUtil.TRENDING + KeyUtil.DESC)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            )
        1 ->
            MediaLatestList.newInstance(
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.MANGA)
                    putString(KeyUtil.arg_sort, KeyUtil.TRENDING + KeyUtil.DESC)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            )
        2 ->
            MediaLatestList.newInstance(
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                    putString(KeyUtil.arg_sort, KeyUtil.ID + KeyUtil.DESC)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            )
        else -> throw IndexOutOfBoundsException("Invalid position: $position")
    }
}
