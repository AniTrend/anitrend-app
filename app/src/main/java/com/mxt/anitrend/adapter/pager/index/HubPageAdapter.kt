package com.mxt.anitrend.adapter.pager.index

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.extension.LAZY_MODE_UNSAFE
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.ui.fragmentByTagOrNew
import com.mxt.anitrend.ui.model.FragmentItem
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.list.SuggestionListFragment
import com.mxt.anitrend.view.fragment.list.WatchListFragment

/**
 * Created by max on 2017/11/04.
 */
class HubPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    init {
        setPagerTitles(R.array.hub_title)
    }

    private val fragmentItems by lazy(LAZY_MODE_UNSAFE) {
        listOf<FragmentItem<Fragment>>(
            FragmentItem(
                SuggestionListFragment::class.java,
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                    putBoolean(KeyUtil.arg_onList, false)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                },
            ),
            FragmentItem(
                WatchListFragment::class.java,
                Bundle().apply {
                    putParcelableArrayList(
                        KeyUtil.arg_list_model,
                        ArrayList<Parcelable>(
                            arrayListOf(ExternalLink(BuildConfig.FEEDS_LINK, null)),
                        ),
                    )
                    putBoolean(KeyUtil.arg_popular, true)
                },
            ),
        )
    }

    override fun createFragment(position: Int): Fragment = fragmentItems[position].fragmentByTagOrNew(fragmentActivity)
}
