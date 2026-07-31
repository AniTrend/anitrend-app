package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.util.KeyUtil
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

    private var messageType: Int = 0

    private val messageFeedViewModel: MessageFeedViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(
            params: Bundle,
            messageType: Int,
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
                            handleSuccess(state.content, state.items, state.replaceExisting)
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

    override fun onToggleLike(feedId: Long) {
        messageFeedViewModel.toggleLike(feedId)
    }

    override fun onDeleteFeed(feedId: Long) {
        messageFeedViewModel.deleteFeed(feedId)
    }

    override fun currentRenderedFeeds(): List<FeedList> = (messageFeedViewModel.state.value as? MessageFeedViewModel.UiState.Success)?.content?.pageData.orEmpty()

    override fun editFeed(feedId: Long) {
        val feed = currentRenderedFeeds().firstOrNull { it.id == feedId } ?: return
        val recipient = feed.recipient ?: return
        mBottomSheet =
            BottomSheetComposer
                .Builder()
                .setUserActivity(feed)
                .setRequestMode(KeyUtil.MUT_SAVE_MESSAGE_FEED)
                .setUserModel(recipient)
                .setTitle(R.string.edit_status_title)
                .build()
        showBottomSheet()
    }
}
