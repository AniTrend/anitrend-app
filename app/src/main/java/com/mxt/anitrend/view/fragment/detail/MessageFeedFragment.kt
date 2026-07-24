package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.FeedAdapter
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.detail.ProfileActivity
import com.mxt.anitrend.view.fragment.list.FeedListFragment
import com.mxt.anitrend.view.sheet.BottomSheetComposer
import com.mxt.anitrend.viewmodel.MessageFeedViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2018/03/24.
 * MessageFeedFragment
 */
class MessageFeedFragment : FeedListFragment() {
    private var userId: Long = 0

    @KeyUtil.MessageType
    private var messageType: Int = 0

    private val messageFeedViewModel: MessageFeedViewModel by viewModel()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                messageFeedViewModel.state.collect { state ->
                    when (state) {
                        is MessageFeedViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is MessageFeedViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is MessageFeedViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun makeRequest() {
        val args = arguments ?: return
        messageFeedViewModel.load(
            userId = userId,
            page = mScrollListener.currentPage,
            pageLimit = args.getInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT),
            messageType = messageType,
        )
    }

    private fun handleSuccess(value: PageContainer<FeedList>) {
        if (value.hasPageInfo()) {
            setPageInfo(value.pageInfo)
        }
        if (!value.isEmpty) {
            val filtered = value.pageData.filter { !it.type.isNullOrBlank() }
            mScrollListener.getPageInfo()?.perPage = filtered.size
            onPostProcessed(filtered)
        } else {
            onPostProcessed(emptyList())
        }
        if (mAdapter.itemCount < 1) {
            onPostProcessed(null)
        }
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<FeedList>,
    ) {
        when (target.id) {
            R.id.messenger_avatar -> {
                data.value.messenger?.let { messenger ->
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
                data.value.recipient?.let { recipient ->
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
                val recipient = data.value.recipient ?: return
                mBottomSheet =
                    BottomSheetComposer
                        .Builder()
                        .setUserActivity(data.value)
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
