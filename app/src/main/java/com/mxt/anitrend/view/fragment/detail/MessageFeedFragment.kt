package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.FeedAdapter
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.fragment.list.FeedListFragment
import com.mxt.anitrend.view.sheet.BottomSheetComposer

/**
 * Created by max on 2018/03/24.
 * MessageFeedFragment
 */
class MessageFeedFragment : FeedListFragment() {
    private var userId: Long = 0

    @KeyUtil.MessageType
    private var messageType: Int = 0

    companion object {
        @JvmStatic
        fun newInstance(
            params: Bundle,
            @KeyUtil.MessageType messageType: Int,
        ): MessageFeedFragment {
            val args =
                Bundle(params).apply {
                    putInt(KeyUtil.arg_message_type, messageType)
                }
            return MessageFeedFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            messageType = args.getInt(KeyUtil.arg_message_type)
            userId = args.getLong(KeyUtil.arg_userId)
        }
        isMenuDisabled = true
        isFeed = false
        (mAdapter as? FeedAdapter)?.setMessageType(messageType)
    }

    override fun makeRequest() {
        val ctx = context ?: return
        val params = viewModel?.params ?: return
        params.applyBaseFeedRequestArguments(arguments)
        params.putInt(KeyUtil.arg_page, presenter.currentPage)
        params.putBoolean(KeyUtil.arg_asHtml, false)
        params.remove(KeyUtil.arg_userId)
        params.remove(KeyUtil.arg_messengerId)
        params.putLong(
            if (messageType == KeyUtil.MESSAGE_TYPE_INBOX) KeyUtil.arg_userId else KeyUtil.arg_messengerId,
            userId,
        )
        viewModel?.requestData(KeyUtil.FEED_MESSAGE_REQ, ctx)
    }

    override fun onItemClick(
        target: View,
        data: IntPair<FeedList>,
    ) {
        when (target.id) {
            R.id.messenger_avatar -> {
                data.second.messenger?.let { messenger ->
                    val host = activity ?: return
                    val intent =
                        Intent(host, ProfileActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra(KeyUtil.arg_id, messenger.id)
                        }
                    CompatUtil.startRevealAnim(host, target, intent)
                }
            }
            R.id.recipient_avatar -> {
                data.second.recipient?.let { recipient ->
                    val host = activity ?: return
                    val intent =
                        Intent(host, ProfileActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra(KeyUtil.arg_id, recipient.id)
                        }
                    CompatUtil.startRevealAnim(host, target, intent)
                }
            }
            R.id.widget_edit -> {
                val recipient = data.second.recipient ?: return
                mBottomSheet =
                    BottomSheetComposer
                        .Builder()
                        .setUserActivity(data.second)
                        .setRequestMode(KeyUtil.MUT_SAVE_MESSAGE_FEED)
                        .setUserModel(recipient)
                        .setTitle(R.string.edit_status_title)
                        .build()
                showBottomSheet()
            }
            else -> super.onItemClick(target, data)
        }
    }
}
