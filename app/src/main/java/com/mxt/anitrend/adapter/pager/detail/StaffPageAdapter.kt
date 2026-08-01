package com.mxt.anitrend.adapter.pager.detail

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
import com.mxt.anitrend.view.fragment.detail.StaffOverviewFragment
import com.mxt.anitrend.view.fragment.group.MediaAnimeRoleFragment
import com.mxt.anitrend.view.fragment.group.MediaFormatFragment
import com.mxt.anitrend.view.fragment.group.MediaStaffRoleFragment

/**
 * Created by max on 2017/12/01.
 */
class StaffPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    private val isAuthenticated = koinOf<Settings>().isAuthenticated

    init {
        setPagerTitles(R.array.staff_page_titles)
    }

    private val staffOverviewFragment by lazy(LAZY_MODE_UNSAFE) {
        FragmentItem(StaffOverviewFragment::class.java, Bundle(params))
    }

    private val mediaAnimeRoleFragment by lazy(LAZY_MODE_UNSAFE) {
        FragmentItem(
            MediaAnimeRoleFragment::class.java,
            Bundle(params).apply {
                putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                putInt(KeyUtil.arg_request_type, KeyUtil.STAFF_CHARACTERS_REQ)
            },
        )
    }

    private val mediaFormatFragment by lazy(LAZY_MODE_UNSAFE) {
        FragmentItem(
            MediaFormatFragment::class.java,
            Bundle(params).apply {
                putString(KeyUtil.arg_mediaType, KeyUtil.MANGA)
                putInt(KeyUtil.arg_request_type, KeyUtil.STAFF_MEDIA_REQ)
            },
        )
    }

    private val mediaStaffRoleFragment by lazy(LAZY_MODE_UNSAFE) {
        FragmentItem(MediaStaffRoleFragment::class.java, Bundle(params))
    }

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> staffOverviewFragment.fragmentByTagOrNew(fragmentActivity)
        1 -> mediaAnimeRoleFragment.fragmentByTagOrNew(fragmentActivity)
            .apply { setFilterable(isAuthenticated) }
        2 -> mediaFormatFragment.fragmentByTagOrNew(fragmentActivity)
            .apply { setFilterable(isAuthenticated) }
        3 -> mediaStaffRoleFragment.fragmentByTagOrNew(fragmentActivity)
            .apply { setFilterable(isAuthenticated) }
        else -> throw IndexOutOfBoundsException("Invalid position: $position")
    }
}
