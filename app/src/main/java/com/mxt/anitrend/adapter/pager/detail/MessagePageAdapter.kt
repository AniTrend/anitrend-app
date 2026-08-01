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

    private val fragmentItems by lazy(LAZY_MODE_UNSAFE) {
        listOf<FragmentItem<Fragment>>(
            FragmentItem(
                MessageFeedFragment::class.java,
                Bundle(params).apply {
                    putInt(KeyUtil.arg_message_type, KeyUtil.MESSAGE_TYPE_INBOX)
                },
                "MessageFeedFragmentInbox",
            ),
            FragmentItem(
                MessageFeedFragment::class.java,
                Bundle(params).apply {
                    putInt(KeyUtil.arg_message_type, KeyUtil.MESSAGE_TYPE_OUTBOX)
                },
                "MessageFeedFragmentOutbox",
            ),
        )
    }

    override fun createFragment(position: Int): Fragment = fragmentItems[position].fragmentByTagOrNew(fragmentActivity)
}
