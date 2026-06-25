package com.mxt.anitrend.adapter.pager.index

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.list.MediaBrowseFragment

/**
 * Created by Maxwell on 10/14/2016.
 */
class SeasonPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    init {
        setPagerTitles(R.array.seasons_titles)
    }

    override fun createFragment(position: Int): Fragment = when (position) {
        0 ->
            MediaBrowseFragment.newInstance(
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                    putString(KeyUtil.arg_season, KeyUtil.WINTER)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            )
        1 ->
            MediaBrowseFragment.newInstance(
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                    putString(KeyUtil.arg_season, KeyUtil.SPRING)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            )
        2 ->
            MediaBrowseFragment.newInstance(
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                    putString(KeyUtil.arg_season, KeyUtil.SUMMER)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            )
        3 ->
            MediaBrowseFragment.newInstance(
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                    putString(KeyUtil.arg_season, KeyUtil.FALL)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            )
        else -> throw IndexOutOfBoundsException("Invalid position: $position")
    }
}
