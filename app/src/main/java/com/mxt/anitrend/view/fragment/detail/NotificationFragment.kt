package com.mxt.anitrend.view.fragment.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.annimon.stream.IntPair
import com.annimon.stream.Stream
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.NotificationAdapter
import com.mxt.anitrend.base.custom.async.ThreadPool
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.model.entity.anilist.Notification
import com.mxt.anitrend.model.entity.base.NotificationHistory
import com.mxt.anitrend.model.entity.base.NotificationHistory_
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.CommentActivity
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.view.activity.detail.ProfileActivity

/**
 * Created by max on 2017/12/06.
 * NotificationFragment
 */

class NotificationFragment : FragmentBaseList<Notification, PageContainer<Notification>, BasePresenter>() {

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
        mAdapter = NotificationAdapter(requireContext())
        setPresenter(BasePresenter(context))
        setViewModel(true)
    }

    /**
     * Is automatically called in the @onStart Method if overridden in list implementation
     */
    override fun updateUI() {
        with (presenter.database) {
            val historyItems = getBoxStore(NotificationHistory::class.java).count()
            if (historyItems < 1)
                markAllNotificationsAsRead()
            injectAdapter()

            currentUser?.also {
                it.unreadNotificationCount = 0
                currentUser = it
            }
        }

        //Testing notifications by forcing the notification dispatcher
        /*presenter.database.currentUser?.let {
            it.unreadNotificationCount = 3
            koinOf<Settings>().lastDismissedNotificationId = -1
            koinOf<NotificationUtil>().createNotification(it, viewModel.model.value!!)
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_mark_all).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_mark_all -> {
                if (mAdapter.itemCount > 0) {
                    ThreadPool.execute { markAllNotificationsAsRead() }
                } else context?.also {
                    NotifyUtil.makeText(it, R.string.text_activity_loading, Toast.LENGTH_SHORT)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged()
    }

    override fun onChanged(content: PageContainer<Notification>?) {
        if (content != null) {
            if (content.hasPageInfo())
                presenter.pageInfo = content.pageInfo
            if (!content.isEmpty) {
                val notifications = GraphUtil.filterNotificationList(
                        presenter, content.pageData
                )
                onPostProcessed(notifications)
            } else
                onPostProcessed(emptyList())
        } else
            onPostProcessed(emptyList())
        if (mAdapter.itemCount < 1)
            onPostProcessed(null)
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    override fun makeRequest() {
        val queryContainer = GraphUtil.getDefaultQuery(isPager)
                .putVariable(KeyUtil.arg_page, presenter.currentPage)
                .putVariable(KeyUtil.arg_resetNotificationCount, true)

        getViewModel().params.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        getViewModel().requestData(KeyUtil.USER_NOTIFICATION_REQ, requireContext())
    }

    /**
     * Ran on a background thread to assure we don't skip frames
     * @see ThreadPool
     */
    private fun setItemAsRead(data: Notification) {
        ThreadPool.execute {
            val isNotificationRead = presenter.database.getBoxStore(NotificationHistory::class.java)
                    .query().equal(NotificationHistory_.id, data.id).build().count() != 0L
            if (!isNotificationRead) {
                val dismissibleNotifications = Stream.of(mAdapter.data)
                        .filter { item -> item.activityId != 0L && item.activityId == data.activityId }
                        .map { item -> NotificationHistory(item.id) }
                        .toList()

                if (!CompatUtil.isEmpty(dismissibleNotifications))
                    presenter.database.getBoxStore(NotificationHistory::class.java)
                            .put(dismissibleNotifications)
                else
                    presenter.database.getBoxStore(NotificationHistory::class.java)
                            .put(NotificationHistory(data.id))
            }
        }
    }

    /**
     * Ran on a background thread to assure we don't skip frames
     * @see ThreadPool
     */
    private fun markAllNotificationsAsRead() {
        val notificationHistories = Stream.of(mAdapter.data)
                .map { notification -> NotificationHistory(notification.id) }
                .toList()

        presenter.database.getBoxStore(NotificationHistory::class.java)
                .put(notificationHistories)

        activity?.runOnUiThread {
            if (mAdapter != null)
                mAdapter.notifyDataSetChanged()
        }
    }


    /**
     * When the target view from [View.OnClickListener]
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data   the model that at the click index
     */
    override fun onItemClick(target: View, data: IntPair<Notification>) {
        val intent: Intent
        setItemAsRead(data.second)
        if (target.id == R.id.notification_img &&
                !CompatUtil.equals(data.second.type, KeyUtil.AIRING) &&
                !CompatUtil.equals(data.second.type, KeyUtil.RELATED_MEDIA_ADDITION)
        ) {
            intent = Intent(activity, ProfileActivity::class.java)
            intent.putExtra(KeyUtil.arg_id, data.second.user.id)
            startActivity(intent)
        } else
            when (data.second.type) {
                KeyUtil.ACTIVITY_MESSAGE -> {
                    intent = Intent(activity, CommentActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.second.activityId)
                    startActivity(intent)
                }
                KeyUtil.FOLLOWING -> {
                    intent = Intent(activity, ProfileActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.second.user.id)
                    startActivity(intent)
                }
                KeyUtil.ACTIVITY_MENTION -> {
                    intent = Intent(activity, CommentActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.second.activityId)
                    startActivity(intent)
                }
                KeyUtil.AIRING, KeyUtil.RELATED_MEDIA_ADDITION -> {
                    intent = Intent(activity, MediaActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.second.media?.id)
                    intent.putExtra(KeyUtil.arg_mediaType, data.second.media?.type)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (data.second.media != null)
                        startActivity(intent)
                }
                KeyUtil.ACTIVITY_LIKE -> {
                    intent = Intent(activity, CommentActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.second.activityId)
                    startActivity(intent)
                }
                KeyUtil.ACTIVITY_REPLY, KeyUtil.ACTIVITY_REPLY_SUBSCRIBED -> {
                    intent = Intent(activity, CommentActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.second.activityId)
                    startActivity(intent)
                }
                KeyUtil.ACTIVITY_REPLY_LIKE -> {
                    intent = Intent(activity, CommentActivity::class.java)
                    intent.putExtra(KeyUtil.arg_id, data.second.activityId)
                    startActivity(intent)
                }
                KeyUtil.THREAD_SUBSCRIBED,
                KeyUtil.THREAD_LIKE -> {
                    intent = Intent(Intent.ACTION_VIEW)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.data = Uri.parse(
                        "https://anilist.co/forum/thread/${data.second.thread.id}"
                    )
                    startActivity(intent)
                }
                KeyUtil.THREAD_COMMENT_MENTION,
                KeyUtil.THREAD_COMMENT_REPLY,
                KeyUtil.THREAD_COMMENT_LIKE -> {
                    intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(
                        "https://anilist.co/forum/thread/${data.second.thread.id}/comment/${data.second.commentId}"
                    )
                    startActivity(intent)
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
    override fun onItemLongClick(target: View, data: IntPair<Notification>) {
        if (CompatUtil.equals(data.second.type, KeyUtil.AIRING)) {
            setItemAsRead(data.second)
            data.second.media?.also {
                if (presenter.settings.isAuthenticated) {
                    mediaActionUtil = MediaActionUtil.Builder()
                        .setId(it.id).build(activity)
                    mediaActionUtil.startSeriesAction()
                }
            }
        }
    }

    companion object {

        fun newInstance(): NotificationFragment {
            return NotificationFragment()
        }
    }
}
