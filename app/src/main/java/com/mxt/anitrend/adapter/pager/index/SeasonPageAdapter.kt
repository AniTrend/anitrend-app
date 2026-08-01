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

    private val fragmentItems by lazy(LAZY_MODE_UNSAFE) {
        arrayOf(KeyUtil.WINTER, KeyUtil.SPRING, KeyUtil.SUMMER, KeyUtil.FALL).map { season ->
            FragmentItem<Fragment>(
                MediaBrowseFragment::class.java,
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                    putString(KeyUtil.arg_season, season)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
                "MediaBrowseFragment$season",
            )
        }
    }

    override fun createFragment(position: Int): Fragment = fragmentItems[position].fragmentByTagOrNew(fragmentActivity)
}
