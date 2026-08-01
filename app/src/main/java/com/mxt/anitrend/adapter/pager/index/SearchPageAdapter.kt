package com.mxt.anitrend.adapter.pager.index

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
import com.mxt.anitrend.view.fragment.search.CharacterSearchFragment
import com.mxt.anitrend.view.fragment.search.MediaSearchFragment
import com.mxt.anitrend.view.fragment.search.StaffSearchFragment
import com.mxt.anitrend.view.fragment.search.StudioSearchFragment
import com.mxt.anitrend.view.fragment.search.UserSearchFragment

/**
 * Created by max on 2017/12/19.
 */
class SearchPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    init {
        val settings = koinOf<Settings>()
        setPagerTitles(
            if (settings.isAuthenticated) {
                R.array.search_titles_auth
            } else {
                R.array.search_titles
            },
        )
    }

    private val fragmentItems by lazy(LAZY_MODE_UNSAFE) {
        listOf<FragmentItem<Fragment>>(
            FragmentItem(
                MediaSearchFragment::class.java,
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                },
                "MediaSearchFragmentAnime",
            ),
            FragmentItem(
                MediaSearchFragment::class.java,
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.MANGA)
                },
                "MediaSearchFragmentManga",
            ),
            FragmentItem(StudioSearchFragment::class.java, Bundle(params)),
            FragmentItem(StaffSearchFragment::class.java, Bundle(params)),
            FragmentItem(CharacterSearchFragment::class.java, Bundle(params)),
            FragmentItem(UserSearchFragment::class.java, Bundle(params)),
        )
    }

    override fun createFragment(position: Int): Fragment = fragmentItems[position].fragmentByTagOrNew(fragmentActivity)
}
