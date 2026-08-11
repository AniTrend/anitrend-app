package com.mxt.anitrend.view.fragment.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.NotificationListAdapter
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
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.CommentActivity
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity
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
 * from the ObjectBox `NotificationHistory` box and renders through the
 * [NotificationListAdapter]. Do not migrate back to the mutable
 * `Notification` entity lane.
 */

class NotificationFragment : FragmentBaseList<NotificationItemUiModel, NotificationPageResult>() {

    private val settings: Settings by inject()
    private val databaseHelper by inject<DatabaseHelper>()

    private val notificationViewModel: NotificationViewModel by viewModel()

    private lateinit var notificationAdapter: NotificationListAdapter

    /**
     * Every successfully loaded page projected into immutable UI models. Appends
     * on pagination, replaced on refresh, and re-projected whenever the read
     * state changes.
     */
    private var loadedItems: List<NotificationItemUiModel> = emptyList()

    /**
     * Override and set presenter, mColumnSize, and fetch argument/s
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mColumnSize = R.integer.single_list_x1
        isPager = true
        setInflateMenu(R.menu.notification_menu)
        notificationAdapter =
            NotificationListAdapter(
                onItemClick = ::onNotificationClick,
                onItemLongClick = ::onNotificationLongClick,
            )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationViewModel.state.collect { state ->
                    when (state) {
                        is NotificationViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is NotificationViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is NotificationViewModel.UiState.Error -> {
                            showError(state.message)
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
        showLoading()
        if (notificationAdapter.itemCount < 1) {
            onRefresh()
        } else {
            updateUI()
        }
    }

    override fun onResume() {
        super.onResume()
        if (this::notificationAdapter.isInitialized) {
            refreshReadStates()
        }
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

        if (notificationAdapter.itemCount > 0) {
            if (recyclerView.adapter !== notificationAdapter) {
                recyclerView.adapter = notificationAdapter
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
                if (notificationAdapter.itemCount > 0) {
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
        notificationViewModel.load(page = mScrollListener.currentPage)
    }

    private fun handleSuccess(value: NotificationPageResult) {
        value.pageInfo?.let { setPageInfo(it.toPageInfo()) }
        val readIds = readNotificationIds()
        val pageItems = value.notifications.mapNotNull { record ->
            record.toNotificationItemUiModel(isRead = record.id in readIds)
        }
        loadedItems =
            if (isPager && !swipeRefreshLayout.isRefreshing() && loadedItems.isNotEmpty()) {
                loadedItems + pageItems
            } else {
                pageItems
            }
        mScrollListener.getPageInfo()?.perPage = pageItems.size
        if (loadedItems.isEmpty()) {
            notificationAdapter.submitList(emptyList())
            setLimitReached()
            showEmpty(getString(R.string.layout_empty_response))
        } else {
            // updateUI is deferred to the submit commit callback so the adapter's
            // itemCount has settled before the content/empty teardown runs.
            notificationAdapter.submitList(loadedItems) { updateUI() }
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: NotificationPageResult?) = Unit

    /** Click and long-click affordances are forwarded through the adapter callbacks. */
    override fun onItemClick(target: View, data: IndexedValue<NotificationItemUiModel>) = Unit

    override fun onItemLongClick(target: View, data: IndexedValue<NotificationItemUiModel>) = Unit

    private fun onNotificationClick(target: View, item: NotificationItemUiModel) {
        val host = activity ?: return
        val record = item.record
        val intent: Intent
        setItemAsRead(item)
        if (target.id == R.id.notification_img &&
            !CompatUtil.equals(record.type, KeyUtil.AIRING) &&
            !CompatUtil.equals(record.type, KeyUtil.RELATED_MEDIA_ADDITION) &&
            !CompatUtil.equals(record.type, KeyUtil.MEDIA_DATA_CHANGE) &&
            !CompatUtil.equals(record.type, KeyUtil.MEDIA_DELETION) &&
            !CompatUtil.equals(record.type, KeyUtil.MEDIA_MERGE)
        ) {
            intent = Intent(host, ProfileActivity::class.java)
            intent.putExtra(KeyUtil.arg_id, record.user?.id ?: 0L)
            startActivity(intent)
        } else {
            when (record.type) {
                KeyUtil.ACTIVITY_MESSAGE -> {
                    openCommentActivity(host, record)
                }
                KeyUtil.FOLLOWING -> {
                    intent = Intent(host, ProfileActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, record.user?.id ?: 0L)
                    startActivity(intent)
                }
                KeyUtil.ACTIVITY_MENTION -> {
                    openCommentActivity(host, record)
                }
                KeyUtil.AIRING,
                KeyUtil.RELATED_MEDIA_ADDITION,
                KeyUtil.MEDIA_DATA_CHANGE,
                KeyUtil.MEDIA_DELETION,
                KeyUtil.MEDIA_MERGE,
                -> {
                    intent = MediaActivity.newIntent(host, record.media?.id ?: 0L, record.media?.type)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (record.media != null) {
                        startActivity(intent)
                    }
                }
                KeyUtil.ACTIVITY_LIKE -> {
                    openCommentActivity(host, record)
                }
                KeyUtil.ACTIVITY_REPLY, KeyUtil.ACTIVITY_REPLY_SUBSCRIBED -> {
                    openCommentActivity(host, record)
                }
                KeyUtil.ACTIVITY_REPLY_LIKE -> {
                    openCommentActivity(host, record)
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

    private fun openCommentActivity(host: Context, record: NotificationRecord) {
        // A null activity id means the referenced activity was deleted; the comment
        // screen cannot recover from a missing id, so show the message and stay put.
        val activityId = record.commentActivityId()
        if (activityId == null) {
            context?.also {
                NotifyUtil.makeText(it, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
            }
            return
        }
        val intent = Intent(host, CommentActivity::class.java)
        intent.putExtra(KeyUtil.arg_id, activityId)
        startActivity(intent)
    }

    private fun onNotificationLongClick(target: View, item: NotificationItemUiModel) {
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
     * Re-projects the loaded rows with the current read state and re-submits so
     * the unread indicators stay in sync after marks-as-read and on resume. The
     * canonical fragment list is updated in place before submitting so it never
     * diverges from the adapter list.
     */
    private fun refreshReadStates() {
        if (!this::notificationAdapter.isInitialized) return
        val readIds = readNotificationIds()
        loadedItems = loadedItems.map { item -> item.copy(isRead = item.id in readIds) }
        notificationAdapter.submitList(loadedItems)
    }

    companion object {

        fun newInstance(): NotificationFragment = NotificationFragment()
    }
}
