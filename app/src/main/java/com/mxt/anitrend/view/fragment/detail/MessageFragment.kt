package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.view.fragment.list.FeedListFragment
import com.mxt.anitrend.view.sheet.BottomSheetComposer
import com.mxt.anitrend.viewmodel.MessageFeedViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject

/**
 * Created by max on 2018/03/24.
 * Unified message screen. Inbox and outbox are mutually exclusive screen state,
 * not separate navigation destinations.
 */
@Suppress("TooManyFunctions") // Lifecycle, navigation, and message actions stay centralized.
class MessageFragment : FeedListFragment() {
    private var userId: Long = 0

    private var messageType: Int = KeyUtil.MESSAGE_TYPE_INBOX

    private val userRepository: UserRepository by inject()
    private val messageFeedViewModel: MessageFeedViewModel by viewModel()

    /** Saved-state keys for the message destination. */
    companion object {
        private const val STATE_MESSAGE_TYPE = "message.section"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = userRepository.cachedCurrentUser?.id ?: 0L
        messageType = savedInstanceState?.getInt(STATE_MESSAGE_TYPE) ?: KeyUtil.MESSAGE_TYPE_INBOX
        setInflateMenu(R.menu.message_menu)
        isMenuDisabled = false
        isFeed = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_MESSAGE_TYPE, messageType)
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val selectedType = when (item.itemId) {
            R.id.action_messages_inbox -> KeyUtil.MESSAGE_TYPE_INBOX
            R.id.action_messages_outbox -> KeyUtil.MESSAGE_TYPE_OUTBOX
            else -> null
        }
        if (selectedType != null) {
            selectMessageType(selectedType)
            return true
        }
        @Suppress("DEPRECATION")
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: android.view.Menu, inflater: android.view.MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        syncMessageMenu(menu)
    }

    private fun selectMessageType(selectedType: Int) {
        if (messageType == selectedType) return
        messageType = selectedType
        clearRenderedFeedItems()
        showLoading()
        onRefresh()
        activity?.invalidateOptionsMenu()
    }

    private fun syncMessageMenu(menu: android.view.Menu) {
        menu.findItem(R.id.action_messages_inbox)?.isChecked = messageType == KeyUtil.MESSAGE_TYPE_INBOX
        menu.findItem(R.id.action_messages_outbox)?.isChecked = messageType == KeyUtil.MESSAGE_TYPE_OUTBOX
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
                            handleSuccess(state.items, state.pageInfo)
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
        val args = arguments
        messageFeedViewModel.load(
            userId = userId,
            page = mScrollListener.currentPage,
            pageLimit = args?.getInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT) ?: KeyUtil.PAGING_LIMIT,
            messageType = messageType,
            currentUserId = currentUserId(),
        )
    }

    override fun onToggleLike(feedId: Long) {
        messageFeedViewModel.toggleLike(feedId)
    }

    override fun onDeleteFeed(feedId: Long) {
        messageFeedViewModel.deleteFeed(feedId)
    }

    override fun editFeed(feedId: Long) {
        val feedItem = currentRenderedFeedItems().firstOrNull { it.id == feedId } ?: return
        val recipientId = feedItem.recipientId ?: return
        val recipient = UserBase(name = feedItem.recipientName).apply { id = recipientId }
        mBottomSheet =
            BottomSheetComposer
                .Builder()
                .setUserActivity(feedItem)
                .setRequestMode(KeyUtil.MUT_SAVE_MESSAGE_FEED)
                .setUserModel(recipient)
                .setTitle(R.string.edit_status_title)
                .build()
        showBottomSheet()
    }
}
