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

    private val fragmentItems by lazy(LAZY_MODE_UNSAFE) {
        listOf<FragmentItem<Fragment>>(
            FragmentItem(CharacterOverviewFragment::class.java, Bundle(params)),
            FragmentItem(
                MediaFormatFragment::class.java,
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                    putInt(KeyUtil.arg_request_type, KeyUtil.CHARACTER_MEDIA_REQ)
                },
                "MediaFormatFragmentAnime",
            ),
            FragmentItem(
                MediaFormatFragment::class.java,
                Bundle(params).apply {
                    putString(KeyUtil.arg_mediaType, KeyUtil.MANGA)
                    putInt(KeyUtil.arg_request_type, KeyUtil.CHARACTER_MEDIA_REQ)
                },
                "MediaFormatFragmentManga",
            ),
            FragmentItem(CharacterActorsFragment::class.java, Bundle(params)),
        )
    }

    override fun createFragment(position: Int): Fragment = fragmentItems[position].fragmentByTagOrNew(fragmentActivity)
}
