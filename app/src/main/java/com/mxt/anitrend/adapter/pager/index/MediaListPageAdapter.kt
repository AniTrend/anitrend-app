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
import com.mxt.anitrend.view.fragment.list.MediaListFragment

/**
 * Created by max on 2017/12/17.
 * users list page adapter
 */
class MediaListPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    private val mediaListStatuses =
        arrayOf(
            KeyUtil.CURRENT,
            KeyUtil.PLANNING,
            KeyUtil.COMPLETED,
            KeyUtil.DROPPED,
            KeyUtil.PAUSED,
            KeyUtil.REPEATING,
        )

    init {
        setPagerTitles(R.array.media_list_status)
    }

    private val fragmentItems: List<FragmentItem<Fragment>> by lazy(LAZY_MODE_UNSAFE) {
        mediaListStatuses.map { status ->
            FragmentItem(
                MediaListFragment::class.java,
                Bundle(params).apply {
                    putString(KeyUtil.arg_statusIn, status)
                },
                "MediaListFragment$status",
            )
        }
    }

    override fun createFragment(position: Int): Fragment = fragmentItems[position].fragmentByTagOrNew(fragmentActivity)
}
