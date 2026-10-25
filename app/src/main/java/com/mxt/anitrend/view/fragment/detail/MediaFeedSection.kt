package com.mxt.anitrend.view.fragment.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.feed.FeedItemLazyList
import com.mxt.anitrend.ui.feed.FeedRowCallbacks
import com.mxt.anitrend.ui.feed.countNewFeedItems
import com.mxt.anitrend.ui.media.ManualGridAppendGate
import com.mxt.anitrend.view.fragment.list.renderableFeedItems
import com.mxt.anitrend.viewmodel.MediaFeedViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** View-only media feed section backed by the canonical feed store projection. */
@Suppress("LongParameterList", "TooManyFunctions")
class MediaFeedSection(
    context: Context,
    private val viewModel: MediaFeedViewModel,
    private val mediaId: Long,
    private val isFollowing: Boolean,
    private val pageLimit: Int,
    private val currentUserId: Long?,
    private val onToggleLike: (Long) -> Unit,
    private val onDeleteFeed: (Long) -> Unit,
    private val onOpenMedia: (View, FeedItemUiModel) -> Unit,
    private val onOpenComments: (Long) -> Unit,
    private val onEditFeed: (FeedItemUiModel) -> Unit,
    private val onShowLikes: (FeedItemUiModel) -> Unit,
    private val onOpenProfile: (View, Long) -> Unit,
    private val onLongPressMedia: (View, FeedItemUiModel) -> Boolean,
) : CustomSwipeRefreshLayout.OnRefreshAndLoadListener {
    private val appContext = context
    private var binding: FragmentListBinding? = null
    private val listState = LazyListState()
    private val items = mutableStateOf<List<FeedItemUiModel>>(emptyList(), referentialEqualityPolicy())
    private val loadMoreRevision = mutableStateOf(0)
    private val appendGate = ManualGridAppendGate()
    private var pageInfo: PageInfoRecord? = null
    private var currentPage = 1
    private var activeRequestPage: Int? = null
    private var requestStartFeedIds: Set<Long> = emptySet()
    private var requestInFlight = false

    private val rowCallbacks =
        FeedRowCallbacks(
            onToggleLike = onToggleLike,
            onDelete = ::deleteFeed,
            onOpenMedia = { target, item -> currentFeedItem(item.id)?.let { onOpenMedia(target, it) } },
            onOpenComments = onOpenComments,
            onEdit = { item -> currentFeedItem(item.id)?.takeIf { it.canEdit }?.let(onEditFeed) },
            onShowLikes = { item -> currentFeedItem(item.id)?.let(onShowLikes) },
            onOpenProfile = onOpenProfile,
            onLongPressMedia = { target, item ->
                currentFeedItem(item.id)?.let { onLongPressMedia(target, it) } ?: false
            },
        )

    private val currentBinding: FragmentListBinding
        get() = checkNotNull(binding)

    /** Inflates and initializes the media feed view. */
    fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        val sectionBinding = FragmentListBinding.inflate(inflater, container, false)
        binding = sectionBinding
        val composeHost =
            ComposeView(appContext).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        FeedItemLazyList(
                            items = items.value,
                            listState = listState,
                            callbacks = rowCallbacks,
                            onLoadMore = ::loadNextPage,
                            loadMoreRevision = loadMoreRevision.value,
                        )
                    }
                }
            }
        sectionBinding.refreshLayout.addView(
            composeHost,
            0,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        sectionBinding.refreshLayout.setOnRefreshAndLoadListener(this)
        sectionBinding.refreshLayout.scrollBoundary = { direction ->
            if (direction < 0) listState.canScrollBackward else listState.canScrollForward
        }
        sectionBinding.stateLayout.showLoading()
        return sectionBinding.root
    }

    /** Starts collecting feed state for [owner]. */
    fun start(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is MediaFeedViewModel.UiState.Loading -> if (items.value.isEmpty()) showLoading()
                        is MediaFeedViewModel.UiState.Success ->
                            render(
                                items = state.items,
                                pageInfo = state.pageInfo,
                                requestedPage = state.requestedPage,
                                isLoading = state.isLoading,
                            )
                        is MediaFeedViewModel.UiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    /** Activates the section and loads the first page when needed. */
    fun select() {
        if (!requestInFlight && items.value.isEmpty()) onRefresh() else showContent()
    }

    /** Releases the list's scroll bridge and view resources. */
    fun destroyView() {
        binding?.refreshLayout?.apply {
            setOnRefreshAndLoadListener(null)
            scrollBoundary = null
        }
        binding = null
    }

    /** Resets pagination and requests the first feed page. */
    override fun onRefresh() {
        currentPage = 1
        pageInfo = null
        requestPage(1)
    }

    /** Requests the next feed page. */
    override fun onLoad() = loadNextPage()

    private fun loadNextPage() {
        if (requestInFlight || !appendGate.canRequestNextPage || pageInfo?.hasNextPage != true) return
        currentPage++
        currentBinding.refreshLayout.setLoading(true)
        requestPage(currentPage)
    }

    private fun requestPage(page: Int) {
        activeRequestPage = page
        requestStartFeedIds = if (page <= 1) emptySet() else items.value.mapTo(mutableSetOf()) { it.id }
        requestInFlight = true
        appendGate.onRequestStarted()
        viewModel.load(
            mediaId = mediaId,
            isFollowing = isFollowing,
            page = page,
            pageLimit = pageLimit,
            currentUserId = currentUserId,
        )
    }

    private fun render(
        items: List<FeedItemUiModel>,
        pageInfo: PageInfoRecord?,
        requestedPage: Int,
        isLoading: Boolean,
    ) {
        val sectionBinding = binding ?: return
        pageInfo?.let { this.pageInfo = it }
        val snapshot = renderableFeedItems(items)
        this.items.value = snapshot
        if (activeRequestPage == requestedPage && !isLoading) {
            val previousIds = if (requestedPage <= 1) emptySet() else requestStartFeedIds
            appendGate.onRequestSucceeded(countNewFeedItems(previousIds, snapshot))
            activeRequestPage = null
            requestInFlight = false
            loadMoreRevision.value++
        }
        if (isLoading) return
        finishRefreshIndicators(sectionBinding)
        if (snapshot.isEmpty()) showEmpty() else showContent()
    }

    private fun deleteFeed(feedId: Long) {
        val item = items.value.firstOrNull { it.id == feedId } ?: return
        if (item.canDelete && !item.isDeletePending) onDeleteFeed(feedId)
    }
    private fun currentFeedItem(feedId: Long): FeedItemUiModel? = items.value.firstOrNull { it.id == feedId }

    private fun showLoading() {
        binding?.stateLayout?.showLoading()
    }

    private fun showContent() {
        val current = binding ?: return
        finishRefreshIndicators(current)
        current.stateLayout.showContent()
    }

    private fun showEmpty() {
        val current = binding ?: return
        finishRefreshIndicators(current)
        current.stateLayout.showError(
            appContext.getCompatDrawable(R.drawable.ic_emoji_sweat),
            appContext.getString(R.string.layout_empty_response),
            appContext.getString(R.string.try_again),
        ) { onRefresh() }
    }

    private fun showError(message: String) {
        requestInFlight = false
        activeRequestPage = null
        appendGate.onRequestFailed()
        val current = binding ?: return
        finishRefreshIndicators(current)
        current.stateLayout.showError(
            appContext.getCompatDrawable(R.drawable.ic_emoji_cry),
            message,
            appContext.getString(R.string.try_again),
        ) { onRefresh() }
    }

    private fun finishRefreshIndicators(current: FragmentListBinding) {
        if (current.refreshLayout.isRefreshing()) current.refreshLayout.setRefreshing(false)
        if (current.refreshLayout.isLoading()) current.refreshLayout.setLoading(false)
    }
}
