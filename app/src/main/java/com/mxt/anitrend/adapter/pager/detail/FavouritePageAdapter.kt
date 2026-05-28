package com.mxt.anitrend.adapter.pager.detail

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
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
    context: Context
) : BaseStatePageAdapter(fragmentActivity, context) {

    init {
        setPagerTitles(R.array.favorites_page_titles)
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MediaFavouriteFragment.newInstance(params, KeyUtil.ANIME)
            1 -> CharacterFavouriteFragment.newInstance(params)
            2 -> MediaFavouriteFragment.newInstance(params, KeyUtil.MANGA)
            3 -> StaffFavouriteFragment.newInstance(params)
            4 -> StudioFavouriteFragment.newInstance(params)
            else -> throw IndexOutOfBoundsException("Invalid position: $position")
        }
    }
}
