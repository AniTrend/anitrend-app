package com.mxt.anitrend.view.fragment.detail

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
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.databinding.FragmentListBinding
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.viewmodel.UserFeedViewModel
import kotlinx.coroutines.launch

/**
 * Ordinary feed renderer used by ProfileFragment's local section state.
 *
 * The callback-heavy constructor and grouped lifecycle helpers intentionally
 * preserve the profile destination's existing feed actions.
 */
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
) : RecyclerLoadListener,
    CustomSwipeRefreshLayout.OnRefreshAndLoadListener {
    private var binding: FragmentListBinding? = null
    private val scrollListener = RecyclerScrollListener()
    private val adapter = FeedListAdapter(
        onToggleLikeAction = onToggleLike,
        onDeleteFeedAction = onDeleteFeed,
        onOpenMedia = onOpenMedia,
        onOpenComments = onOpenComments,
        onEditFeed = onEditFeed,
        onShowLikes = { id ->
            currentItems().firstOrNull { it.id == id }?.let { onShowLikes(id, it.likes) }
        },
        onOpenProfile = onOpenProfile,
        onLongPressMedia = onLongPressMedia,
    )

    /** Inflates and initializes the profile feed view. */
    fun inflate(inflater: LayoutInflater, container: ViewGroup): View {
        val sectionBinding = FragmentListBinding.inflate(inflater, container, false)
        binding = sectionBinding
        sectionBinding.recyclerView.setHasFixedSize(true)
        sectionBinding.recyclerView.isNestedScrollingEnabled = true
        val layoutManager = StaggeredGridLayoutManager(
            sectionBinding.root.resources.getInteger(R.integer.single_list_x1),
            StaggeredGridLayoutManager.VERTICAL,
        )
        sectionBinding.recyclerView.layoutManager = layoutManager
        sectionBinding.recyclerView.adapter = adapter
        sectionBinding.refreshLayout.setOnRefreshAndLoadListener(this)
        sectionBinding.recyclerView.addOnScrollListener(scrollListener)
        scrollListener.initListener(layoutManager, this)
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
        load(1)
    }

    /** Releases the profile feed binding and scroll resources. */
    fun clear() {
        binding?.recyclerView?.clearOnScrollListeners()
        binding = null
    }

    /** Resets pagination and requests the first feed page. */
    override fun onRefresh() {
        scrollListener.onRefreshPage()
        load(1)
    }

    /** Requests the current page as an append operation. */
    override fun onLoadMore() {
        binding?.refreshLayout?.setLoading(true)
        load(scrollListener.currentPage)
    }

    /** Ignores the refresh layout append callback. */
    override fun onLoad() = Unit

    private fun load(page: Int) {
        var resolvedUserId = userId
        if (settings.isAuthenticated && isCurrentUser(resolvedUserId, userName)) {
            resolvedUserId = userRepository.cachedCurrentUser?.id ?: resolvedUserId
        }
        if (resolvedUserId <= 0L) return
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

    private fun render(state: UserFeedViewModel.UiState) {
        val sectionBinding = binding ?: return
        when (state) {
            UserFeedViewModel.UiState.Loading -> sectionBinding.stateLayout.showLoading()
            is UserFeedViewModel.UiState.Error -> sectionBinding.stateLayout.showError(
                sectionBinding.root.context.getCompatDrawable(R.drawable.ic_emoji_cry),
                state.message,
                getStringSafely(R.string.try_again),
            ) { onRefresh() }
            is UserFeedViewModel.UiState.Success -> renderSuccess(state.items, state.pageInfo)
        }
    }

    private fun renderSuccess(items: List<FeedItemUiModel>, pageInfo: PageInfoRecord?) {
        val sectionBinding = binding ?: return
        pageInfo?.let { scrollListener.setPageInfo(it.toPageInfo()) }
        adapter.submitList(items.filter { !it.type.isNullOrBlank() }) {
            sectionBinding.refreshLayout.setRefreshing(false)
            sectionBinding.refreshLayout.setLoading(false)
            if (adapter.itemCount == 0) {
                sectionBinding.stateLayout.showError(
                    sectionBinding.root.context.getCompatDrawable(R.drawable.ic_emoji_sweat),
                    getStringSafely(R.string.layout_empty_response),
                    getStringSafely(R.string.try_again),
                ) { onRefresh() }
            } else {
                sectionBinding.stateLayout.showContent()
            }
        }
    }

    private fun currentItems(): List<FeedItemUiModel> = adapter.currentList

    private fun isCurrentUser(id: Long, name: String?): Boolean = settings.isAuthenticated &&
        userRepository.cachedCurrentUser?.let { current ->
            name?.let { current.name == it } ?: (id > 0L && current.id == id)
        } == true

    private fun getStringSafely(resId: Int): String = binding?.root?.context?.getString(resId).orEmpty()
}
