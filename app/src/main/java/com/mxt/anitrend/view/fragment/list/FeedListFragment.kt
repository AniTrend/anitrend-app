package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.data.mapper.toUserBase
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.navigation.extension.NavigationArgs
import com.mxt.anitrend.navigation.extension.navigateToComment
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.extension.navigateToProfile
import com.mxt.anitrend.navigation.model.CommentScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.feed.FeedItemLazyList
import com.mxt.anitrend.ui.feed.FeedRowCallbacks
import com.mxt.anitrend.ui.feed.countNewFeedItems
import com.mxt.anitrend.ui.media.ManualGridAppendGate
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.TapTargetUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.sheet.BottomSheetComposer
import com.mxt.anitrend.view.sheet.BottomSheetUsers
import com.mxt.anitrend.viewmodel.FeedListViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Filters out item types that cannot be rendered. All visible fields and action state
 * otherwise come directly from immutable snapshots emitted by the canonical feed store.
 */
internal fun renderableFeedItems(items: List<FeedItemUiModel>): List<FeedItemUiModel> = items.filter { !it.type.isNullOrBlank() }

/**
 * Created by max on 2017/11/07.
 * Home page feed base
 */
open class FeedListFragment : FragmentBaseList<FeedList, PageContainer<FeedList>>() {

    private val settings: Settings by inject()

    private val databaseHelper: DatabaseHelper by inject()

    private val feedListViewModel: FeedListViewModel by viewModel()
    private var composeView: ComposeView? = null
    private val feedItems =
        mutableStateOf<List<FeedItemUiModel>>(emptyList(), referentialEqualityPolicy())
    private val feedListState = LazyListState()
    private val pageSuccessRevision = mutableStateOf(0)
    private val appendGate = ManualGridAppendGate()
    private var activeRequestPage: Int? = null
    private var requestStartFeedIds: Set<Long> = emptySet()
    private var requestInFlight = false
    private val feedRowCallbacks =
        FeedRowCallbacks(
            onToggleLike = ::onToggleLike,
            onDelete = ::deleteCurrentFeed,
            onOpenMedia = { target, item -> openFeedMedia(target, item.id) },
            onOpenComments = ::openFeedComments,
            onEdit = { item -> editFeed(item.id) },
            onShowLikes = { item -> showFeedLikes(item.id) },
            onOpenProfile = ::openFeedProfile,
            onLongPressMedia = { target, item -> onFeedMediaLongPressed(target, item.id) },
        )

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): FeedListFragment {
            val args = Bundle(params)
            return FeedListFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPager = true
        isFeed = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        installComposeList()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                feedListViewModel.state.collect { state ->
                    when (state) {
                        is FeedListViewModel.UiState.Loading -> Unit
                        is FeedListViewModel.UiState.Success ->
                            handleSuccess(
                                items = state.items,
                                pageInfo = state.pageInfo,
                                requestedPage = state.requestedPage,
                                isLoading = state.isLoading,
                            )
                        is FeedListViewModel.UiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }
    private fun installComposeList() {
        val host =
            ComposeView(requireContext()).apply {
                id = R.id.container
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        FeedItemLazyList(
                            items = feedItems.value,
                            listState = feedListState,
                            callbacks = feedRowCallbacks,
                            loadMoreRevision = pageSuccessRevision.value,
                            onLoadMore = ::loadNextPage,
                        )
                    }
                }
            }
        swipeRefreshLayout.addView(
            host,
            0,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        swipeRefreshLayout.scrollBoundary = { direction ->
            if (direction < 0) feedListState.canScrollBackward else feedListState.canScrollForward
        }
        composeView = host
    }

    override fun onDestroyView() {
        swipeRefreshLayout.scrollBoundary = null
        composeView = null
        super.onDestroyView()
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_post -> {
                mBottomSheet =
                    BottomSheetComposer
                        .Builder()
                        .setRequestMode(KeyUtil.MUT_SAVE_TEXT_FEED)
                        .setTitle(R.string.menu_title_new_activity_post)
                        .build()
                showBottomSheet()
                return true
            }
        }
        @Suppress("DEPRECATION")
        return super.onOptionsItemSelected(item)
    }

    override fun updateUI() {
        if (feedItems.value.isNotEmpty()) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false)
            } else if (swipeRefreshLayout.isLoading()) {
                swipeRefreshLayout.setLoading(false)
            }
            showContent()
        } else {
            showEmpty(getString(R.string.layout_empty_response))
        }
        if (!TapTargetUtil.isActive(KeyUtil.KEY_POST_TYPE_TIP) && isFeed) {
            if (settings.shouldShowTipFor(KeyUtil.KEY_POST_TYPE_TIP)) {
                val host = activity ?: return
                TapTargetUtil
                    .buildDefault(host, R.string.tip_status_post_title, R.string.tip_status_post_text, R.id.action_post)
                    .setPromptStateChangeListener { _, state ->
                        if (state == uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED ||
                            state == uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_FOCAL_PRESSED
                        ) {
                            settings.disableTipFor(KeyUtil.KEY_POST_TYPE_TIP)
                        }
                        if (state == uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_DISMISSED) {
                            TapTargetUtil.setActive(KeyUtil.KEY_POST_TYPE_TIP, true)
                        }
                    }.show()
                TapTargetUtil.setActive(KeyUtil.KEY_POST_TYPE_TIP, false)
            }
        }
    }

    override fun makeRequest() {
        val args = arguments ?: return
        prepareFeedRequest()
        feedListViewModel.load(
            page = pageState.currentPage,
            pageLimit = NavigationArgs.intWithDefault(args.containsKey(KeyUtil.arg_page_limit), args.getInt(KeyUtil.arg_page_limit), KeyUtil.PAGING_LIMIT),
            isFollowing = NavigationArgs.optionalBoolean(args.containsKey(KeyUtil.arg_isFollowing), args.getBoolean(KeyUtil.arg_isFollowing)),
            type = NavigationArgs.resolveActivityType(args.getString(KeyUtil.arg_type)),
            isMixed = NavigationArgs.optionalBoolean(args.containsKey(KeyUtil.arg_isMixed), args.getBoolean(KeyUtil.arg_isMixed)),
            currentUserId = currentUserId(),
        )
    }

    /**
     * Current authenticated user id used to resolve edit/delete ownership and liked state
     * on store-backed feed items. Subclasses may override when they hold a fresher source.
     */
    protected open fun currentUserId(): Long? = databaseHelper.currentUser?.id

    protected fun Bundle.applyBaseFeedRequestArguments(source: Bundle?) {
        putInt(KeyUtil.arg_page_limit, source?.getInt(KeyUtil.arg_page_limit) ?: KeyUtil.PAGING_LIMIT)

        if (source?.containsKey(KeyUtil.arg_type) == true) {
            putString(KeyUtil.arg_type, source.getString(KeyUtil.arg_type))
        }
        if (source?.containsKey(KeyUtil.arg_isFollowing) == true) {
            putBoolean(KeyUtil.arg_isFollowing, source.getBoolean(KeyUtil.arg_isFollowing))
        }
        if (source?.containsKey(KeyUtil.arg_isMixed) == true) {
            putBoolean(KeyUtil.arg_isMixed, source.getBoolean(KeyUtil.arg_isMixed))
        }
        if (source?.containsKey(KeyUtil.arg_asHtml) == true) {
            putBoolean(KeyUtil.arg_asHtml, source.getBoolean(KeyUtil.arg_asHtml))
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: PageContainer<FeedList>?) = Unit

    override fun onStart() {
        super.onStart()
        if (feedItems.value.isEmpty()) {
            showLoading()
            onRefresh()
        } else {
            updateUI()
        }
    }

    protected open fun onToggleLike(feedId: Long) {
        feedListViewModel.toggleLike(feedId)
    }

    protected open fun onDeleteFeed(feedId: Long) {
        feedListViewModel.deleteFeed(feedId)
    }

    /**
     * Applies each complete immutable ViewModel snapshot as a replacement, not an append.
     * The append gate only observes the unique-id delta for successful page requests.
     */
    protected fun handleSuccess(
        items: List<FeedItemUiModel>,
        pageInfo: PageInfoRecord?,
        requestedPage: Int = pageState.currentPage,
        isLoading: Boolean = false,
    ) {
        pageInfo?.let { pageState.setPageInfo(it.toPageInfo()) }
        val snapshot = renderableFeedItems(items)
        feedItems.value = snapshot
        pageState.getPageInfo()?.perPage = snapshot.size
        if (activeRequestPage == requestedPage && !isLoading) {
            val previousIds = if (requestedPage <= 1) emptySet() else requestStartFeedIds
            appendGate.onRequestSucceeded(countNewFeedItems(previousIds, snapshot))
            activeRequestPage = null
            requestInFlight = false
            pageSuccessRevision.value++
        }
        if (snapshot.isNotEmpty()) {
            if (!isLoading) updateUI()
        } else if (!isLoading) {
            showEmpty(getString(R.string.layout_empty_response))
        }
    }

    protected open fun currentRenderedFeedItems(): List<FeedItemUiModel> = feedItems.value
    private fun resolveCurrentFeedItem(feedId: Long): FeedItemUiModel? = currentRenderedFeedItems().firstOrNull { it.id == feedId }

    protected fun clearRenderedFeedItems() {
        feedItems.value = emptyList()
        if (composeView != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                feedListState.scrollToItem(0)
            }
        }
    }

    protected fun prepareFeedRequest() {
        activeRequestPage = pageState.currentPage
        requestStartFeedIds = currentRenderedFeedItems().mapTo(mutableSetOf()) { it.id }
        requestInFlight = true
        appendGate.onRequestStarted()
    }

    private fun openFeedMedia(
        target: View,
        feedId: Long,
    ) {
        val feedItem = resolveCurrentFeedItem(feedId) ?: return
        val mediaId = feedItem.mediaId ?: return
        navigateToMedia(MediaScreenParam(mediaId, feedItem.mediaType))
    }

    private fun openFeedComments(feedId: Long) {
        navigateToComment(CommentScreenParam(feedId))
    }

    /**
     * Opens the composer for an existing feed. The immutable [FeedItemUiModel] is
     * resolved from the current Compose snapshot, so only the stable feed id and draft
     * text extracted by [BottomSheetComposer.Builder.setUserActivity] reach the typed
     * composer parameter.
     */
    protected open fun editFeed(feedId: Long) {
        val feedItem = currentRenderedFeedItems().firstOrNull { it.id == feedId && it.canEdit } ?: return
        mBottomSheet =
            BottomSheetComposer
                .Builder()
                .setUserActivity(feedItem)
                .setRequestMode(KeyUtil.MUT_SAVE_TEXT_FEED)
                .setTitle(R.string.edit_status_title)
                .build()
        showBottomSheet()
    }

    private fun showFeedLikes(feedId: Long) {
        val likes = resolveCurrentFeedItem(feedId)?.likes.orEmpty().map { it.toUserBase() }
        if (likes.isNotEmpty()) {
            mBottomSheet =
                BottomSheetUsers
                    .Builder()
                    .setModel(likes)
                    .setOnUserClick(::navigateToProfile)
                    .setTitle(R.string.title_bottom_sheet_likes)
                    .build()
            showBottomSheet()
        } else {
            activity?.let {
                NotifyUtil.makeText(it, R.string.text_no_likes, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openFeedProfile(
        target: View,
        userId: Long,
    ) {
        navigateToProfile(UserScreenParam(userId))
    }

    private fun onFeedMediaLongPressed(
        target: View,
        feedId: Long,
    ): Boolean {
        if (!settings.isAuthenticated) {
            context?.let {
                NotifyUtil
                    .makeText(
                        it,
                        R.string.info_login_req,
                        R.drawable.ic_group_add_grey_600_18dp,
                        Toast.LENGTH_SHORT,
                    ).show()
            }
            return false
        }
        val mediaId = resolveCurrentFeedItem(feedId)?.mediaId ?: return false
        val host = activity ?: return false
        mediaActionUtil =
            MediaActionUtil
                .Builder()
                .setId(mediaId)
                .build(host)
        mediaActionUtil.startSeriesAction()
        return true
    }
    private fun deleteCurrentFeed(feedId: Long) {
        val currentItem = currentRenderedFeedItems().firstOrNull { it.id == feedId } ?: return
        if (currentItem.canDelete && !currentItem.isDeletePending) {
            onDeleteFeed(feedId)
        }
    }

    override fun showError(error: String) {
        requestInFlight = false
        activeRequestPage = null
        appendGate.onRequestFailed()
        super.showError(error)
    }

    private fun loadNextPage() {
        if (requestInFlight || !appendGate.canRequestNextPage) return
        val pageInfo = pageState.getPageInfo()
        if (pageInfo != null && !pageInfo.hasNextPage()) return
        pageState.currentPage++
        pageState.currentOffset += KeyUtil.PAGING_LIMIT
        swipeRefreshLayout.setLoading(true)
        makeRequest()
    }
}
