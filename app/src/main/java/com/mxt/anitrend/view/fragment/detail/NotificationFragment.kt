package com.mxt.anitrend.view.fragment.detail

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
import com.mxt.anitrend.adapter.recycler.detail.NotificationAdapter
import com.mxt.anitrend.base.custom.async.ThreadPool
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.model.entity.anilist.Notification
import com.mxt.anitrend.model.entity.base.NotificationHistory
import com.mxt.anitrend.model.entity.base.NotificationHistory_
import com.mxt.anitrend.model.entity.container.body.PageContainer
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
 */

class NotificationFragment : FragmentBaseList<Notification, PageContainer<Notification>>() {

    private val settings: Settings by inject()
    private val databaseHelper by inject<DatabaseHelper>()

    private val notificationViewModel: NotificationViewModel by viewModel()

    /**
     * Override and set presenter, mColumnSize, and fetch argument/s
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        mColumnSize = R.integer.single_list_x1
        isPager = true
        setInflateMenu(R.menu.notification_menu)
        mAdapter = NotificationAdapter(ctx)
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

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    override fun updateUI() {
        with(databaseHelper) {
            val historyItems = getBoxStore(NotificationHistory::class.java).count()
            if (historyItems < 1) {
                markAllNotificationsAsRead()
            }
            injectAdapter()

            currentUser?.also {
                it.unreadNotificationCount = 0
                currentUser = it
            }
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
                if (mAdapter.itemCount > 0) {
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

    override fun onResume() {
        super.onResume()
        if (this::mAdapter.isInitialized) {
            mAdapter.notifyDataSetChanged()
        }
    }

    override fun makeRequest() {
        notificationViewModel.load(page = mScrollListener.currentPage)
    }

    private fun handleSuccess(value: PageContainer<Notification>) {
        if (value.hasPageInfo()) {
            setPageInfo(value.pageInfo)
        }
        if (!value.isEmpty) {
            @Suppress("DEPRECATION")
            val filtered = value.pageData.filter { !it.type.isNullOrBlank() }
            mScrollListener.getPageInfo()?.perPage = filtered.size
            onPostProcessed(filtered)
        } else {
            onPostProcessed(emptyList())
        }
        if (mAdapter.itemCount < 1) {
            onPostProcessed(null)
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: PageContainer<Notification>?) = Unit

    /**
     * Ran on a background thread to assure we don't skip frames
     * @see ThreadPool
     */
    private fun setItemAsRead(data: Notification) {
        ThreadPool.execute {
            val isNotificationRead = databaseHelper.getBoxStore(NotificationHistory::class.java)
                .query().equal(NotificationHistory_.id, data.id).build().count() != 0L
            if (!isNotificationRead) {
                val dismissibleNotifications = mAdapter.data
                    .filter { item -> item.activityId != 0L && item.activityId == data.activityId }
                    .map { item -> NotificationHistory(item.id) }

                if (!CompatUtil.isEmpty(dismissibleNotifications)) {
                    databaseHelper.getBoxStore(NotificationHistory::class.java)
                        .put(dismissibleNotifications)
                } else {
                    databaseHelper.getBoxStore(NotificationHistory::class.java)
                        .put(NotificationHistory(data.id))
                }
            }
        }
    }

    /**
     * Ran on a background thread to assure we don't skip frames
     * @see ThreadPool
     */
    private fun markAllNotificationsAsRead() {
        val notificationHistories = mAdapter.data
            .map { notification -> NotificationHistory(notification.id) }

        databaseHelper.getBoxStore(NotificationHistory::class.java)
            .put(notificationHistories)

        activity?.runOnUiThread {
            if (this::mAdapter.isInitialized) {
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * When the target view from [View.OnClickListener]
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    override fun onItemClick(target: View, data: IndexedValue<Notification>) {
        val host = activity ?: return
        val intent: Intent
        setItemAsRead(data.value)
        if (target.id == R.id.notification_img &&
            !CompatUtil.equals(data.value.type, KeyUtil.AIRING) &&
            !CompatUtil.equals(data.value.type, KeyUtil.RELATED_MEDIA_ADDITION) &&
            !CompatUtil.equals(data.value.type, KeyUtil.MEDIA_DATA_CHANGE) &&
            !CompatUtil.equals(data.value.type, KeyUtil.MEDIA_DELETION) &&
            !CompatUtil.equals(data.value.type, KeyUtil.MEDIA_MERGE)
        ) {
            intent = Intent(host, ProfileActivity::class.java)
            intent.putExtra(KeyUtil.arg_id, data.value.user.id)
            startActivity(intent)
        } else {
            when (data.value.type) {
                KeyUtil.ACTIVITY_MESSAGE -> {
                    intent = Intent(host, CommentActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.value.activityId)
                    startActivity(intent)
                }
                KeyUtil.FOLLOWING -> {
                    intent = Intent(host, ProfileActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.value.user.id)
                    startActivity(intent)
                }
                KeyUtil.ACTIVITY_MENTION -> {
                    intent = Intent(host, CommentActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.value.activityId)
                    startActivity(intent)
                }
                KeyUtil.AIRING,
                KeyUtil.RELATED_MEDIA_ADDITION,
                KeyUtil.MEDIA_DATA_CHANGE,
                KeyUtil.MEDIA_DELETION,
                KeyUtil.MEDIA_MERGE,
                -> {
                    intent = Intent(host, MediaActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.value.media?.id)
                    intent.putExtra(KeyUtil.arg_mediaType, data.value.media?.type)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (data.value.media != null) {
                        startActivity(intent)
                    }
                }
                KeyUtil.ACTIVITY_LIKE -> {
                    intent = Intent(host, CommentActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.value.activityId)
                    startActivity(intent)
                }
                KeyUtil.ACTIVITY_REPLY, KeyUtil.ACTIVITY_REPLY_SUBSCRIBED -> {
                    intent = Intent(host, CommentActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.value.activityId)
                    startActivity(intent)
                }
                KeyUtil.ACTIVITY_REPLY_LIKE -> {
                    intent = Intent(host, CommentActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.value.activityId)
                    startActivity(intent)
                }
                KeyUtil.THREAD_SUBSCRIBED,
                KeyUtil.THREAD_LIKE,
                -> {
                    intent = Intent(Intent.ACTION_VIEW)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.data = Uri.parse(
                        "https://anilist.co/forum/thread/${data.value.thread.id}",
                    )
                    startActivity(intent)
                }
                KeyUtil.THREAD_COMMENT_MENTION,
                KeyUtil.THREAD_COMMENT_REPLY,
                KeyUtil.THREAD_COMMENT_LIKE,
                -> {
                    intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(
                        "https://anilist.co/forum/thread/${data.value.thread.id}/comment/${data.value.commentId}",
                    )
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * When the target view from [View.OnLongClickListener]
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data   the model that at the long click index
     */
    override fun onItemLongClick(target: View, data: IndexedValue<Notification>) {
        if (CompatUtil.equals(data.value.type, KeyUtil.AIRING)) {
            setItemAsRead(data.value)
            data.value.media?.also {
                if (settings.isAuthenticated) {
                    val host = activity ?: return
                    mediaActionUtil = MediaActionUtil.Builder()
                        .setId(it.id).build(host)
                    mediaActionUtil.startSeriesAction()
                }
            }
        }
    }

    companion object {

        fun newInstance(): NotificationFragment = NotificationFragment()
    }
}
