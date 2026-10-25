package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.async.ThreadPool
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.domain.model.NotificationItemUiModel
import com.mxt.anitrend.domain.model.NotificationPageResult
import com.mxt.anitrend.domain.model.NotificationRecord
import com.mxt.anitrend.domain.model.commentActivityId
import com.mxt.anitrend.domain.model.toNotificationItemUiModel
import com.mxt.anitrend.model.entity.base.NotificationHistory
import com.mxt.anitrend.model.entity.base.NotificationHistory_
import com.mxt.anitrend.navigation.extension.navigateToComment
import com.mxt.anitrend.navigation.extension.navigateToMedia
import com.mxt.anitrend.navigation.extension.navigateToProfile
import com.mxt.anitrend.navigation.model.CommentScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.ManualGridAppendGate
import com.mxt.anitrend.ui.notification.NotificationList
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2017/12/06.
 * NotificationFragment
 *
 * Migrated to the immutable [NotificationItemUiModel] / [NotificationPageResult]
 * lane. The fragment remains on the legacy [FragmentBaseList] shell for the list
 * layout, swipe-to-refresh, and pagination scaffolding; it supplies read state
 * from the ObjectBox `NotificationHistory` box and renders through the Compose
 * `NotificationList` host. Do not migrate back to the mutable
 * `Notification` entity lane.
 */

class NotificationFragment : FragmentBaseList<NotificationItemUiModel, NotificationPageResult>() {

    private val settings: Settings by inject()
    private val databaseHelper by inject<DatabaseHelper>()

    private val notificationViewModel: NotificationViewModel by viewModel()

    /** The ViewModel owns records; this view-lifecycle projection adds local read state. */
    private var loadedItems: List<NotificationItemUiModel> = emptyList()

    private val items = mutableStateOf(emptyList<NotificationItemUiModel>())
    private val notificationListState = LazyGridState()
    private val appendGate = ManualGridAppendGate()
    private var composePage = 1
    private var composeHasMore = true
    private var composeLoading = false
    private var requestIsRefresh = false
    private lateinit var composeView: ComposeView

    /** Configures the notification menu. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPager = true
        setInflateMenu(R.menu.notification_menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        composeView = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AniTrendTheme {
                    NotificationList(
                        items = items.value,
                        listState = notificationListState,
                        onRowClick = ::onNotificationRowClick,
                        onAvatarClick = ::onNotificationAvatarClick,
                        onRowLongClick = ::onNotificationLongClick,
                        onLoadMore = ::onComposeLoadMore,
                    )
                }
            }
        }
        swipeRefreshLayout.scrollBoundary = { direction ->
            if (direction < 0) notificationListState.canScrollBackward else notificationListState.canScrollForward
        }
        swipeRefreshLayout.addView(
            composeView,
            0,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationViewModel.state.collect { state ->
                    when (state) {
                        is NotificationViewModel.UiState.Loading -> {
                            state.request?.let { request ->
                                appendGate.onRequestStarted()
                                applySnapshot(state.snapshot)
                                composePage = if (request.isRefresh) 1 else request.page
                                composeLoading = true
                            }
                        }
                        is NotificationViewModel.UiState.Success -> {
                            handleSuccess(state)
                        }
                        is NotificationViewModel.UiState.Error -> {
                            handleError(state)
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        @Suppress("DEPRECATION")
        if (!isMenuDisabled) {
            setHasOptionsMenu(true)
        }
        val state = notificationViewModel.state.value
        when (state) {
            is NotificationViewModel.UiState.Loading -> {
                if (state.request != null) appendGate.onRequestStarted()
            }
            is NotificationViewModel.UiState.Success -> {
                appendGate.onRequestSucceeded(state.snapshot.lastPageUniqueCount)
            }
            is NotificationViewModel.UiState.Error -> {
                appendGate.onRequestFailed()
            }
        }
        when {
            state.snapshot.hasLoadedPage -> {
                applySnapshot(state.snapshot)
                if (state is NotificationViewModel.UiState.Loading && state.request != null) {
                    composePage = if (state.request.isRefresh) 1 else state.request.page
                    composeLoading = true
                    if (items.value.isEmpty()) {
                        showLoading()
                    } else {
                        showContent()
                    }
                } else {
                    composeLoading = false
                    updateUI()
                }
            }
            state is NotificationViewModel.UiState.Loading && state.request != null -> {
                composePage = state.request.page
                composeLoading = true
                showLoading()
            }
            items.value.isEmpty() -> {
                showLoading()
                onRefresh()
            }
            else -> updateUI()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshReadStates()
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    override fun updateUI() {
        with(databaseHelper) {
            val historyItems = getBoxStore(NotificationHistory::class.java).count()
            if (historyItems < 1) {
                markAllNotificationsAsRead()
            }

            currentUser?.also {
                it.unreadNotificationCount = 0
                currentUser = it
            }
        }

        if (items.value.isNotEmpty()) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false)
            } else if (swipeRefreshLayout.isLoading()) {
                swipeRefreshLayout.setLoading(false)
            }
            showContent()
        } else {
            showEmpty(getString(R.string.layout_empty_response))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        @Suppress("DEPRECATION")
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_mark_all).isVisible = true
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_mark_all -> {
                if (items.value.isNotEmpty()) {
                    ThreadPool.execute { markAllNotificationsAsRead() }
                } else {
                    context?.also {
                        NotifyUtil.makeText(it, R.string.text_activity_loading, Toast.LENGTH_SHORT)
                    }
                }
                return true
            }
        }
        @Suppress("DEPRECATION")
        return super.onOptionsItemSelected(item)
    }

    override fun makeRequest() {
        val isRefresh = requestIsRefresh
        requestIsRefresh = false
        composeLoading = true
        appendGate.onRequestStarted()
        pageState.currentPage = composePage
        pageState.currentOffset = (composePage - 1) * KeyUtil.PAGING_LIMIT
        notificationViewModel.load(page = composePage, isRefresh = isRefresh)
    }

    override fun onRefresh() {
        composePage = 1
        composeHasMore = true
        composeLoading = false
        requestIsRefresh = true
        super.onRefresh()
    }

    private fun onComposeLoadMore() {
        if (composeLoading || !composeHasMore || !appendGate.canRequestNextPage) return
        makeRequest()
    }

    private fun handleSuccess(state: NotificationViewModel.UiState.Success) {
        applySnapshot(state.snapshot)
        composeLoading = false
        appendGate.onRequestSucceeded(state.snapshot.lastPageUniqueCount)
        if (loadedItems.isEmpty()) {
            setLimitReached()
            showEmpty(getString(R.string.layout_empty_response))
        } else {
            updateUI()
        }
    }

    private fun handleError(state: NotificationViewModel.UiState.Error) {
        applySnapshot(state.snapshot)
        composeLoading = false
        appendGate.onRequestFailed()
        pageState.currentPage = state.request.page
        pageState.currentOffset = (state.request.page - 1) * KeyUtil.PAGING_LIMIT
        showError(state.message)
    }

    private fun applySnapshot(snapshot: NotificationViewModel.Snapshot) {
        pageState.setPageInfo(snapshot.pageInfo?.toPageInfo())
        if (snapshot.lastLoadedPage > 0) {
            pageState.currentPage = snapshot.lastLoadedPage
            pageState.currentOffset = (snapshot.lastLoadedPage - 1) * KeyUtil.PAGING_LIMIT
        }
        val readIds = readNotificationIds()
        loadedItems = snapshot.notifications.mapNotNull { record ->
            record.toNotificationItemUiModel(isRead = record.id in readIds)
        }
        items.value = loadedItems
        composePage = snapshot.nextPage
        composeHasMore = snapshot.hasMore
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: NotificationPageResult?) = Unit

    private fun onNotificationRowClick(item: NotificationItemUiModel) {
        setItemAsRead(item)
        dispatchNotificationTarget(item, avatarClicked = false)
    }

    private fun onNotificationAvatarClick(item: NotificationItemUiModel) {
        setItemAsRead(item)
        dispatchNotificationTarget(item, avatarClicked = true)
    }

    private fun dispatchNotificationTarget(item: NotificationItemUiModel, avatarClicked: Boolean) {
        val host = activity ?: return
        val record = item.record
        val intent: Intent
        if (avatarClicked &&
            !CompatUtil.equals(record.type, KeyUtil.AIRING) &&
            !CompatUtil.equals(record.type, KeyUtil.RELATED_MEDIA_ADDITION) &&
            !CompatUtil.equals(record.type, KeyUtil.MEDIA_DATA_CHANGE) &&
            !CompatUtil.equals(record.type, KeyUtil.MEDIA_DELETION) &&
            !CompatUtil.equals(record.type, KeyUtil.MEDIA_MERGE)
        ) {
            navigateToProfile(UserScreenParam(record.user?.id ?: 0L))
        } else {
            when (record.type) {
                KeyUtil.ACTIVITY_MESSAGE -> {
                    openCommentDestination(record)
                }
                KeyUtil.FOLLOWING -> {
                    navigateToProfile(UserScreenParam(record.user?.id ?: 0L))
                }
                KeyUtil.ACTIVITY_MENTION -> {
                    openCommentDestination(record)
                }
                KeyUtil.AIRING,
                KeyUtil.RELATED_MEDIA_ADDITION,
                KeyUtil.MEDIA_DATA_CHANGE,
                KeyUtil.MEDIA_DELETION,
                KeyUtil.MEDIA_MERGE,
                -> {
                    if (record.media != null) {
                        navigateToMedia(MediaScreenParam(record.media.id, record.media.type))
                    }
                }
                KeyUtil.ACTIVITY_LIKE -> {
                    openCommentDestination(record)
                }
                KeyUtil.ACTIVITY_REPLY, KeyUtil.ACTIVITY_REPLY_SUBSCRIBED -> {
                    openCommentDestination(record)
                }
                KeyUtil.ACTIVITY_REPLY_LIKE -> {
                    openCommentDestination(record)
                }
                KeyUtil.THREAD_SUBSCRIBED,
                KeyUtil.THREAD_LIKE,
                -> {
                    intent = Intent(Intent.ACTION_VIEW)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.data = Uri.parse(
                        "https://anilist.co/forum/thread/${record.threadId ?: 0L}",
                    )
                    startActivity(intent)
                }
                KeyUtil.THREAD_COMMENT_MENTION,
                KeyUtil.THREAD_COMMENT_REPLY,
                KeyUtil.THREAD_COMMENT_LIKE,
                -> {
                    intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(
                        "https://anilist.co/forum/thread/${record.threadId ?: 0L}/comment/${record.commentId ?: 0L}",
                    )
                    startActivity(intent)
                }
            }
        }
    }

    private fun openCommentDestination(record: NotificationRecord) {
        // A null activity id means the referenced activity was deleted; the comment
        // screen cannot recover from a missing id, so show the message and stay put.
        val activityId = record.commentActivityId()
        if (activityId == null) {
            context?.also {
                NotifyUtil.makeText(it, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
            }
            return
        }
        navigateToComment(CommentScreenParam(activityId))
    }

    private fun onNotificationLongClick(item: NotificationItemUiModel) {
        val record = item.record
        if (CompatUtil.equals(record.type, KeyUtil.AIRING)) {
            setItemAsRead(item)
            record.media?.also {
                if (settings.isAuthenticated) {
                    val host = activity ?: return
                    mediaActionUtil = MediaActionUtil.Builder()
                        .setId(it.id).build(host)
                    mediaActionUtil.startSeriesAction()
                }
            }
        }
    }

    /**
     * Ran on a background thread to assure we don't skip frames
     * @see ThreadPool
     */
    private fun setItemAsRead(data: NotificationItemUiModel) {
        ThreadPool.execute {
            val isNotificationRead = databaseHelper.getBoxStore(NotificationHistory::class.java)
                .query().equal(NotificationHistory_.id, data.record.id).build().count() != 0L
            if (!isNotificationRead) {
                val dismissibleNotifications = loadedItems
                    .map { item -> item.record }
                    .filter { item -> item.activityId != null && item.activityId != 0L && item.activityId == data.record.activityId }
                    .map { item -> NotificationHistory(item.id) }

                if (!CompatUtil.isEmpty(dismissibleNotifications)) {
                    databaseHelper.getBoxStore(NotificationHistory::class.java)
                        .put(dismissibleNotifications)
                } else {
                    databaseHelper.getBoxStore(NotificationHistory::class.java)
                        .put(NotificationHistory(data.record.id))
                }
                activity?.runOnUiThread { refreshReadStates() }
            }
        }
    }

    /**
     * Ran on a background thread to assure we don't skip frames
     * @see ThreadPool
     */
    private fun markAllNotificationsAsRead() {
        val notificationHistories = loadedItems
            .map { item -> NotificationHistory(item.record.id) }

        databaseHelper.getBoxStore(NotificationHistory::class.java)
            .put(notificationHistories)

        activity?.runOnUiThread { refreshReadStates() }
    }

    /**
     * Reads the current read-state ids from the `NotificationHistory` box.
     */
    private fun readNotificationIds(): Set<Long> = databaseHelper.getBoxStore(NotificationHistory::class.java).all.mapTo(mutableSetOf()) { it.id }

    /**
     * Re-projects the loaded rows with the current read state so the unread
     * indicators stay in sync after marks-as-read and on resume. The canonical
     * fragment list is updated in place before publishing so it never diverges
     * from the rendered state.
     */
    private fun refreshReadStates() {
        val readIds = readNotificationIds()
        loadedItems = loadedItems.map { item -> item.copy(isRead = item.id in readIds) }
        items.value = loadedItems
    }

    companion object {

        fun newInstance(): NotificationFragment = NotificationFragment()
    }
}
