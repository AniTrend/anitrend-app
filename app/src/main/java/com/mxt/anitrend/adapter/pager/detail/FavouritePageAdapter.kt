package com.mxt.anitrend.adapter.pager.detail

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
import com.mxt.anitrend.view.fragment.favourite.CharacterFavouriteFragment
import com.mxt.anitrend.view.fragment.favourite.MediaFavouriteFragment
import com.mxt.anitrend.view.fragment.favourite.StaffFavouriteFragment
import com.mxt.anitrend.view.fragment.favourite.StudioFavouriteFragment

/**
 * Created by max on 2017/12/20.
 */
class FavouritePageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    init {
        setPagerTitles(R.array.favorites_page_titles)
    }

    private val fragmentItems by lazy(LAZY_MODE_UNSAFE) {
        listOf<FragmentItem<Fragment>>(
            FragmentItem(
                MediaFavouriteFragment::class.java,
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                },
                "MediaFavouriteFragmentAnime",
            ),
            FragmentItem(CharacterFavouriteFragment::class.java, Bundle(params)),
            FragmentItem(
                MediaFavouriteFragment::class.java,
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.MANGA)
                },
                "MediaFavouriteFragmentManga",
            ),
            FragmentItem(StaffFavouriteFragment::class.java, Bundle(params)),
            FragmentItem(StudioFavouriteFragment::class.java, Bundle(params)),
        )
    }

    override fun createFragment(position: Int): Fragment = fragmentItems[position].fragmentByTagOrNew(fragmentActivity)
}
