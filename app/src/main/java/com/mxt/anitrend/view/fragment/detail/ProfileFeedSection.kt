package com.mxt.anitrend.view.fragment.detail

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
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.feed.FeedItemLazyList
import com.mxt.anitrend.ui.feed.FeedRowCallbacks
import com.mxt.anitrend.ui.feed.countNewFeedItems
import com.mxt.anitrend.ui.media.ManualGridAppendGate
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.fragment.list.renderableFeedItems
import com.mxt.anitrend.viewmodel.UserFeedViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Ordinary feed renderer used by ProfileFragment's local section state. */
@Suppress("LongParameterList", "TooManyFunctions")
class ProfileFeedSection(
    private val settings: Settings,
    private val databaseHelper: DatabaseHelper,
    private val userRepository: UserRepository,
    private val viewModel: UserFeedViewModel,
    private val userId: Long,
    private val userName: String?,
    private val type: String,
    private val onOpenMedia: (View, Long) -> Unit,
    private val onOpenComments: (Long) -> Unit,
    private val onEditFeed: (Long) -> Unit,
    private val onShowLikes: (Long, List<com.mxt.anitrend.domain.model.UserSummaryRecord>) -> Unit,
    private val onOpenProfile: (View, Long) -> Unit,
    private val onLongPressMedia: (View, Long) -> Boolean,
    private val onToggleLike: (Long) -> Unit,
    private val onDeleteFeed: (Long) -> Unit,
) : CustomSwipeRefreshLayout.OnRefreshAndLoadListener {
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
            onOpenMedia = { target, item -> onOpenMedia(target, item.id) },
            onOpenComments = onOpenComments,
            onEdit = { item -> currentFeedItem(item.id)?.takeIf { it.canEdit }?.let { onEditFeed(it.id) } },
            onShowLikes = { item ->
                currentFeedItem(item.id)?.let { onShowLikes(it.id, it.likes) }
            },
            onOpenProfile = onOpenProfile,
            onLongPressMedia = { target, item -> onLongPressMedia(target, item.id) },
        )

    /** Inflates and initializes the profile feed view. */
    fun inflate(inflater: LayoutInflater, container: ViewGroup): View {
        val sectionBinding = FragmentListBinding.inflate(inflater, container, false)
        binding = sectionBinding
        val composeHost =
            ComposeView(sectionBinding.root.context).apply {
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
        sectionBinding.root.context.let { context ->
            (context as? androidx.fragment.app.FragmentActivity)?.let {
                CompatUtil.configureSwipeRefreshLayout(sectionBinding.refreshLayout, it)
            }
        }
        return sectionBinding.root
    }

    /** Starts collecting profile feed state for [owner]. */
    fun start(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
        if (!requestInFlight && items.value.isEmpty()) requestPage(1)
    }

    /** Releases the feed list's scroll bridge and binding. */
    fun clear() {
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

    /** Ignores the refresh layout append callback. */
    override fun onLoad() = Unit

    private fun loadNextPage() {
        if (requestInFlight || !appendGate.canRequestNextPage || pageInfo?.hasNextPage != true) return
        currentPage++
        binding?.refreshLayout?.setLoading(true)
        requestPage(currentPage)
    }

    private fun requestPage(page: Int) {
        val resolvedUserId = resolvedUserId() ?: return
        activeRequestPage = page
        requestStartFeedIds = if (page <= 1) emptySet() else items.value.mapTo(mutableSetOf()) { it.id }
        requestInFlight = true
        appendGate.onRequestStarted()
        viewModel.load(
            userId = resolvedUserId.toInt(),
            page = page,
            pageLimit = KeyUtil.PAGING_LIMIT,
            isFollowing = null,
            type = runCatching { com.mxt.anitrend.graphql.generated.ActivityType.valueOf(type) }.getOrNull(),
            isMixed = null,
            currentUserId = databaseHelper.currentUser?.id,
        )
    }

    private fun resolvedUserId(): Long? {
        var resolved = userId
        if (settings.isAuthenticated && isCurrentUser(resolved, userName)) {
            resolved = userRepository.cachedCurrentUser?.id ?: resolved
        }
        return resolved.takeIf { it > 0L }
    }

    private fun render(state: UserFeedViewModel.UiState) {
        val sectionBinding = binding ?: return
        when (state) {
            UserFeedViewModel.UiState.Loading -> if (items.value.isEmpty()) sectionBinding.stateLayout.showLoading()
            is UserFeedViewModel.UiState.Error -> showError(state.message)
            is UserFeedViewModel.UiState.Success -> renderSuccess(
                items = state.items,
                pageInfo = state.pageInfo,
                requestedPage = state.requestedPage,
                isLoading = state.isLoading,
            )
        }
    }

    private fun renderSuccess(
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
        if (snapshot.isEmpty()) showEmpty() else sectionBinding.stateLayout.showContent()
    }

    private fun deleteFeed(feedId: Long) {
        val item = items.value.firstOrNull { it.id == feedId } ?: return
        if (item.canDelete && !item.isDeletePending) onDeleteFeed(feedId)
    }
    private fun currentFeedItem(feedId: Long): FeedItemUiModel? = items.value.firstOrNull { it.id == feedId }

    private fun showError(message: String) {
        requestInFlight = false
        activeRequestPage = null
        appendGate.onRequestFailed()
        val sectionBinding = binding ?: return
        finishRefreshIndicators(sectionBinding)
        sectionBinding.stateLayout.showError(
            sectionBinding.root.context.getCompatDrawable(R.drawable.ic_emoji_cry),
            message,
            getStringSafely(R.string.try_again),
        ) { onRefresh() }
    }

    private fun showEmpty() {
        val sectionBinding = binding ?: return
        finishRefreshIndicators(sectionBinding)
        sectionBinding.stateLayout.showError(
            sectionBinding.root.context.getCompatDrawable(R.drawable.ic_emoji_sweat),
            getStringSafely(R.string.layout_empty_response),
            getStringSafely(R.string.try_again),
        ) { onRefresh() }
    }

    private fun finishRefreshIndicators(sectionBinding: FragmentListBinding) {
        if (sectionBinding.refreshLayout.isRefreshing()) sectionBinding.refreshLayout.setRefreshing(false)
        if (sectionBinding.refreshLayout.isLoading()) sectionBinding.refreshLayout.setLoading(false)
    }

    private fun isCurrentUser(id: Long, name: String?): Boolean = settings.isAuthenticated &&
        userRepository.cachedCurrentUser?.let { current ->
            name?.let { current.name == it } ?: (id > 0L && current.id == id)
        } == true

    private fun getStringSafely(resId: Int): String = binding?.root?.context?.getString(resId).orEmpty()
}
