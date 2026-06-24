package com.mxt.anitrend.adapter.pager.detail

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.detail.CharacterOverviewFragment
import com.mxt.anitrend.view.fragment.group.CharacterActorsFragment
import com.mxt.anitrend.view.fragment.group.MediaFormatFragment

/**
 * Created by max on 2017/12/01.
 */
class CharacterPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    init {
        setPagerTitles(R.array.character_page_titles)
    }

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> CharacterOverviewFragment.newInstance(params)
        1 -> MediaFormatFragment.newInstance(params, KeyUtil.ANIME, KeyUtil.CHARACTER_MEDIA_REQ)
        2 -> MediaFormatFragment.newInstance(params, KeyUtil.MANGA, KeyUtil.CHARACTER_MEDIA_REQ)
        3 -> CharacterActorsFragment.newInstance(params)
        else -> throw IndexOutOfBoundsException("Invalid position: $position")
    }
}
