package com.mxt.anitrend.adapter.pager.index

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.view.fragment.list.MediaListFragment

/**
 * Created by max on 2017/12/17.
 * users list page adapter
 */
class MediaListPageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context
) : BaseStatePageAdapter(fragmentActivity, context) {

    private val mediaListStatuses = arrayOf(
        KeyUtil.CURRENT,
        KeyUtil.PLANNING,
        KeyUtil.COMPLETED,
        KeyUtil.DROPPED,
        KeyUtil.PAUSED,
        KeyUtil.REPEATING
    )

    init {
        setPagerTitles(R.array.media_list_status)
    }

    override fun createFragment(position: Int): Fragment {
        if (position !in mediaListStatuses.indices) {
            throw IndexOutOfBoundsException("Invalid position: $position")
        }
        return MediaListFragment.newInstance(
            params,
            GraphUtil.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_statusIn, mediaListStatuses[position])
        )
    }
}
