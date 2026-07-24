package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.FeedMutation
import com.mxt.anitrend.repository.FeedRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MessageFeedViewModel(
    private val feedRepository: FeedRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: PageContainer<FeedList>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            feedRepository.mutationEvents.collect { event ->
                when (event) {
                    is FeedMutation.ReplySaved -> {
                        val current = _state.value
                        if (current is UiState.Success) {
                            val feeds = current.content.pageData.map { feed ->
                                FeedList(
                                    id = feed.id,
                                    replyCount = feed.replyCount + 1,
                                    type = feed.type,
                                    status = feed.status,
                                    text = feed.text,
                                    createdAt = feed.createdAt,
                                    user = feed.user,
                                    media = feed.media,
                                    messenger = feed.messenger,
                                    recipient = feed.recipient,
                                    likes = feed.likes,
                                    siteUrl = feed.siteUrl,
                                ).also {
                                    it.replies = (feed.replies ?: emptyList()) + event.reply
                                }
                            }
                            _state.value = current.copy(
                                content = PageContainer<FeedList>().apply {
                                    if (current.content.hasPageInfo()) pageInfo = current.content.pageInfo
                                    pageData = feeds
                                },
                            )
                        }
                    }
                    is FeedMutation.ReplyDeleted -> {
                        val current = _state.value
                        if (current is UiState.Success) {
                            val feeds = current.content.pageData.map { feed ->
                                val filtered = feed.replies?.filter { it.id != event.id }
                                if (filtered != feed.replies) {
                                    FeedList(
                                        id = feed.id,
                                        replyCount = maxOf(0, feed.replyCount - 1),
                                        type = feed.type,
                                        status = feed.status,
                                        text = feed.text,
                                        createdAt = feed.createdAt,
                                        user = feed.user,
                                        media = feed.media,
                                        messenger = feed.messenger,
                                        recipient = feed.recipient,
                                        likes = feed.likes,
                                        siteUrl = feed.siteUrl,
                                    ).also { it.replies = filtered }
                                } else {
                                    feed
                                }
                            }
                            _state.value = current.copy(
                                content = PageContainer<FeedList>().apply {
                                    if (current.content.hasPageInfo()) pageInfo = current.content.pageInfo
                                    pageData = feeds
                                },
                            )
                        }
                    }
                    else -> { /* ignore feed-level events - not relevant to message threads */ }
                }
            }
        }
    }

    /**
     * Loads message feed. Repeatable for pagination; no loadedOnce guard.
     *
     * @param messageType [KeyUtil.MESSAGE_TYPE_INBOX] (0) for inbox; otherwise outbox.
     */
    fun load(
        userId: Long,
        page: Int,
        pageLimit: Int,
        messageType: Int,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            feedRepository.getFeedMessage(
                page = page,
                perPage = pageLimit,
                userId = if (messageType == 0) userId else null,
                messengerId = if (messageType != 0) userId else null,
            ).onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "MessageFeedViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load message feed",
                )
            }
        }
    }
}
