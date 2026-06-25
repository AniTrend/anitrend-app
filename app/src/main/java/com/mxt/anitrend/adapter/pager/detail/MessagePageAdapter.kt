package com.mxt.anitrend.adapter.pager.detail

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.detail.MessageFeedFragment

/**
 * Created by max on 2018/03/24.
 */
class MessagePageAdapter(
    fragmentActivity: FragmentActivity,
    context: Context,
) : BaseStatePageAdapter(fragmentActivity, context) {
    init {
        setPagerTitles(R.array.messages_page_titles)
    }

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> MessageFeedFragment.newInstance(params, KeyUtil.MESSAGE_TYPE_INBOX)
        1 -> MessageFeedFragment.newInstance(params, KeyUtil.MESSAGE_TYPE_OUTBOX)
        else -> throw IndexOutOfBoundsException("Invalid position: $position")
    }
}
