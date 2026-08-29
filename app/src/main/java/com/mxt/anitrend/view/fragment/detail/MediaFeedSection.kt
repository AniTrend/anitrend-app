package com.mxt.anitrend.view.fragment.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.FeedListAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerScrollListener
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.view.fragment.list.renderableFeedItems
import com.mxt.anitrend.viewmodel.MediaFeedViewModel
import kotlinx.coroutines.launch

/**
 * View-only media feed section backed by the canonical feed store projection.
 *
 * The callback-heavy constructor and grouped lifecycle helpers are intentional:
 * this section forwards the media destination's existing feed actions.
 */
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
    private val scrollListener = RecyclerScrollListener()
    private val adapter = FeedListAdapter(
        onToggleLikeAction = onToggleLike,
        onDeleteFeedAction = onDeleteFeed,
        onOpenMedia = { target, feedId -> findItem(feedId)?.let { onOpenMedia(target, it) } },
        onOpenComments = onOpenComments,
        onEditFeed = { feedId -> findItem(feedId)?.let(onEditFeed) },
        onShowLikes = { feedId -> findItem(feedId)?.let(onShowLikes) },
        onOpenProfile = onOpenProfile,
        onLongPressMedia = { target, feedId ->
            findItem(feedId)?.let { onLongPressMedia(target, it) } ?: false
        },
    )
    private var binding: FragmentListBinding? = null

    private val currentBinding: FragmentListBinding
        get() = checkNotNull(binding)

    /** Inflates and initializes the media feed view. */
    fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        val sectionBinding = FragmentListBinding.inflate(inflater, container, false)
        binding = sectionBinding
        val layoutManager = StaggeredGridLayoutManager(
            appContext.resources.getInteger(R.integer.single_list_x1),
            StaggeredGridLayoutManager.VERTICAL,
        )
        sectionBinding.recyclerView.layoutManager = layoutManager
        sectionBinding.recyclerView.setHasFixedSize(true)
        sectionBinding.refreshLayout.setOnRefreshAndLoadListener(this)
        scrollListener.initListener(
            layoutManager,
            object : RecyclerLoadListener {
                override fun onLoadMore() = loadNextPage()
            },
        )
        sectionBinding.recyclerView.addOnScrollListener(scrollListener)
        sectionBinding.recyclerView.adapter = adapter
        sectionBinding.stateLayout.showLoading()
        return sectionBinding.root
    }

    /** Starts collecting feed state for [owner]. */
    fun start(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is MediaFeedViewModel.UiState.Loading -> showLoading()
                        is MediaFeedViewModel.UiState.Success -> render(state.items, state.pageInfo)
                        is MediaFeedViewModel.UiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    /** Activates the section and loads the first page when needed. */
    fun select() {
        if (adapter.itemCount == 0) onRefresh() else showContent()
    }

    /** Releases the feed adapter and view resources. */
    fun destroyView() {
        currentBinding.recyclerView.clearOnScrollListeners()
        currentBinding.recyclerView.adapter = null
        binding = null
    }

    /** Resets pagination and requests the first feed page. */
    override fun onRefresh() {
        scrollListener.onRefreshPage()
        load(scrollListener.currentPage)
    }

    /** Requests the next feed page. */
    override fun onLoad() = loadNextPage()

    private fun loadNextPage() {
        currentBinding.refreshLayout.setLoading(true)
        load(scrollListener.currentPage)
    }

    private fun load(page: Int) {
        viewModel.load(
            mediaId = mediaId,
            isFollowing = isFollowing,
            page = page,
            pageLimit = pageLimit,
            currentUserId = currentUserId,
        )
    }

    private fun render(items: List<FeedItemUiModel>, pageInfo: com.mxt.anitrend.domain.model.PageInfoRecord?) {
        pageInfo?.let { scrollListener.setPageInfo(it.toPageInfo()) }
        val renderedItems = renderableFeedItems(items)
        if (renderedItems.isEmpty()) {
            adapter.submitList(emptyList()) { showEmpty() }
        } else {
            adapter.submitList(renderedItems) { showContent() }
        }
    }

    private fun findItem(feedId: Long): FeedItemUiModel? = adapter.currentList.firstOrNull { it.id == feedId }

    private fun showLoading() {
        currentBinding.stateLayout.showLoading()
    }

    private fun showContent() {
        currentBinding.refreshLayout.setRefreshing(false)
        currentBinding.refreshLayout.setLoading(false)
        currentBinding.stateLayout.showContent()
    }

    private fun showEmpty() {
        currentBinding.refreshLayout.setRefreshing(false)
        currentBinding.refreshLayout.setLoading(false)
        currentBinding.stateLayout.showError(
            appContext.getCompatDrawable(R.drawable.ic_emoji_sweat),
            appContext.getString(R.string.layout_empty_response),
            appContext.getString(R.string.try_again),
        ) { onRefresh() }
    }

    private fun showError(message: String) {
        currentBinding.refreshLayout.setRefreshing(false)
        currentBinding.refreshLayout.setLoading(false)
        currentBinding.stateLayout.showError(
            appContext.getCompatDrawable(R.drawable.ic_emoji_cry),
            message,
            appContext.getString(R.string.try_again),
        ) { onRefresh() }
    }
}
