package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.FeedListAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.data.mapper.toUserBase
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.navigation.extension.NavigationArgs
import com.mxt.anitrend.navigation.extension.navigateToComment
import com.mxt.anitrend.navigation.model.CommentScreenParam
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.TapTargetUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.navigation.extension.navigateToProfile
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.view.sheet.BottomSheetComposer
import com.mxt.anitrend.view.sheet.BottomSheetUsers
import com.mxt.anitrend.viewmodel.FeedListViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Filters out render-unresolvable item types. This is the only transformation applied
 * between the ViewModel's canonical [FeedItemUiModel] list and the adapter submission,
 * so the fragment never re-projects store records.
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
    private var feedListAdapter: FeedListAdapter? = null

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
        mColumnSize = R.integer.single_list_x1
        feedListAdapter =
            FeedListAdapter(
                onToggleLikeAction = ::onToggleLike,
                onDeleteFeedAction = ::onDeleteFeed,
                onOpenMedia = { target, feedId -> openFeedMedia(target, feedId) },
                onOpenComments = ::openFeedComments,
                onEditFeed = ::editFeed,
                onShowLikes = ::showFeedLikes,
                onOpenProfile = { target, userId -> openFeedProfile(target, userId) },
                onLongPressMedia = { target, feedId -> onFeedMediaLongPressed(target, feedId) },
            )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                feedListViewModel.state.collect { state ->
                    when (state) {
                        is FeedListViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is FeedListViewModel.UiState.Success -> {
                            handleSuccess(state.items, state.pageInfo)
                        }
                        is FeedListViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
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
        val adapter = feedListAdapter ?: return
        if (adapter.itemCount > 0) {
            if (recyclerView.adapter !== adapter) {
                recyclerView.adapter = adapter
            }
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
        feedListViewModel.load(
            page = mScrollListener.currentPage,
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
        showLoading()
        if ((feedListAdapter?.itemCount ?: 0) < 1) {
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
     * Submits the ViewModel's canonical [FeedItemUiModel] list directly to the adapter.
     * The only transformation applied is invalid-type filtering via [renderableFeedItems];
     * store records are never re-projected here and pending flags are never merged a
     * second time (they are already part of each item from the ViewModel projection).
     */
    protected fun handleSuccess(
        items: List<FeedItemUiModel>,
        pageInfo: PageInfoRecord?,
    ) {
        pageInfo?.let { setPageInfo(it.toPageInfo()) }
        val renderedItems = renderableFeedItems(items)
        mScrollListener.getPageInfo()?.perPage = renderedItems.size
        if (renderedItems.isEmpty()) {
            feedListAdapter?.submitList(emptyList())
            showEmpty(getString(R.string.layout_empty_response))
        } else {
            // updateUI is deferred to the submit commit callback so the adapter's
            // itemCount has settled before the content/empty teardown runs.
            feedListAdapter?.submitList(renderedItems) { updateUI() }
        }
    }

    protected open fun currentRenderedFeedItems(): List<FeedItemUiModel> = feedListAdapter?.currentList.orEmpty()

    protected fun clearRenderedFeedItems() {
        feedListAdapter?.submitList(emptyList())
    }

    private fun resolveCurrentFeedItem(feedId: Long): FeedItemUiModel? = currentRenderedFeedItems().firstOrNull { it.id == feedId }

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
     * resolved from the adapter's current submitted list, so only the stable feed id
     * and draft text extracted by [BottomSheetComposer.Builder.setUserActivity] reach
     * the typed composer parameter.
     */
    protected open fun editFeed(feedId: Long) {
        val feedItem = currentRenderedFeedItems().firstOrNull { it.id == feedId } ?: return
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

    override fun onItemClick(
        target: View,
        data: IndexedValue<FeedList>,
    ) {
        when (target.id) {
            R.id.series_image -> {
                val series = data.value.media ?: return
                navigateToMedia(MediaScreenParam(series.id, series.type))
            }
            R.id.widget_comment -> {
                navigateToComment(CommentScreenParam(data.value.id))
            }
            R.id.widget_edit -> {
                mBottomSheet =
                    BottomSheetComposer
                        .Builder()
                        .setUserActivity(data.value)
                        .setRequestMode(KeyUtil.MUT_SAVE_TEXT_FEED)
                        .setTitle(R.string.edit_status_title)
                        .build()
                showBottomSheet()
            }
            R.id.widget_users -> {
                val likes = data.value.likes.orEmpty()
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
            R.id.user_avatar -> {
                val user = data.value.user
                if (user != null) {
                    navigateToProfile(UserScreenParam(user.id))
                }
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<FeedList>,
    ) {
        when (target.id) {
            R.id.series_image -> {
                if (settings.isAuthenticated) {
                    val host = activity ?: return
                    data.value.media?.let { media ->
                        mediaActionUtil =
                            MediaActionUtil
                                .Builder()
                                .setId(media.id)
                                .build(host)
                        mediaActionUtil.startSeriesAction()
                    }
                } else {
                    context?.let {
                        NotifyUtil
                            .makeText(
                                it,
                                R.string.info_login_req,
                                R.drawable.ic_group_add_grey_600_18dp,
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }
            }
        }
    }

    override fun onPrepareActionMode(
        mode: ActionMode,
        menu: Menu,
    ): Boolean {
        menu.findItem(R.id.action_bookmark).isVisible = true
        return true
    }

    override fun onActionItemClicked(
        mode: ActionMode,
        item: MenuItem,
    ): Boolean {
        val selected = actionMode?.selectedItems.orEmpty()
        when (item.itemId) {
            R.id.action_bookmark -> Unit
            R.id.action_delete -> Unit
        }
        return true
    }
}
