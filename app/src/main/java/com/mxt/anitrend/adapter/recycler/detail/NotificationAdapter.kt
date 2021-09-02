package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Filter
import butterknife.OnClick
import butterknife.OnLongClick
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.shared.UnresolvedViewHolder
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.databinding.AdapterNotificationBinding
import com.mxt.anitrend.databinding.CustomRecyclerUnresolvedBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.Notification
import com.mxt.anitrend.model.entity.base.NotificationHistory
import com.mxt.anitrend.model.entity.base.NotificationHistory_
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil
import io.objectbox.Box

/**
 * Created by max on 2017/12/06.
 * Notification adapter
 */

class NotificationAdapter(context: Context) : RecyclerViewAdapter<Notification>(context) {

    private val historyBox: Box<NotificationHistory> by lazy {
        presenter.database.getBoxStore(NotificationHistory::class.java)
    }

    override fun onCreateViewHolder(parent: ViewGroup, @KeyUtil.RecyclerViewType viewType: Int): RecyclerViewHolder<Notification> {
        return when (viewType) {
            KeyUtil.RECYCLER_TYPE_CONTENT ->
                NotificationHolder(AdapterNotificationBinding.inflate(parent.context.getLayoutInflater(), parent, false))
            else ->
                UnresolvedViewHolder(CustomRecyclerUnresolvedBinding.inflate(parent.context.getLayoutInflater(), parent, false))
        }
    }

    /**
     * Return the view type of the item at `position` for the purposes
     * of view recycling.
     *
     *
     * The default implementation of this method returns 0, making the assumption of
     * a single view type for the adapter. Unlike ListView adapters, types need not
     * be contiguous. Consider using id resources to uniquely identify item view types.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at
     * `position`. Type codes need not be contiguous.
     */
    override fun getItemViewType(position: Int): Int {
        val notification = data[position]
        return when (notification.user) {
            null -> {
                when (notification?.type) {
                    KeyUtil.AIRING, KeyUtil.RELATED_MEDIA_ADDITION -> KeyUtil.RECYCLER_TYPE_CONTENT
                    else -> KeyUtil.RECYCLER_TYPE_ERROR
                }
            }
            else -> KeyUtil.RECYCLER_TYPE_CONTENT
        }
    }

    /**
     *
     * Returns a filter that can be used to constrain data with a filtering
     * pattern.
     *
     *
     *
     * This method is usually implemented by [Adapter]
     * classes.
     *
     * @return a filter used to constrain data
     */
    override fun getFilter(): Filter? {
        return null
    }

    /**
     * Default constructor which includes binding with butter knife
     *
     * @param binding
     */
    inner class NotificationHolder(
            private val binding: AdapterNotificationBinding
    ) : RecyclerViewHolder<Notification>(binding.root) {

        /**
         * Load image, text, buttons, etc. in this method from the given parameter
         * <br></br>
         *
         * @param model Is the model at the current adapter position
         */
        override fun onBindViewHolder(model: Notification) {
            val notificationHistory = historyBox.query()
                    .equal(NotificationHistory_.id, model.id)
                    .build().findFirst()

            if (notificationHistory != null)
                binding.notificationIndicator.visibility = View.GONE
            else
                binding.notificationIndicator.visibility = View.VISIBLE

            binding.notificationTime.text = DateUtil.getPrettyDateUnix(model.createdAt)

            if (!CompatUtil.equals(model.type, KeyUtil.AIRING) && !CompatUtil.equals(model.type, KeyUtil.RELATED_MEDIA_ADDITION)) {
                if (model.user != null && model.user.avatar != null)
                    AspectImageView.setImage(binding.notificationImg, model.user.avatar.large)
            } else
                AspectImageView.setImage(binding.notificationImg, model.media?.coverImage?.extraLarge)

            when (model.type) {
                KeyUtil.ACTIVITY_MESSAGE -> {
                    binding.notificationSubject.setText(R.string.notification_user_activity_message)
                    binding.notificationHeader.text = model.user.name
                    binding.notificationContent.text = model.context
                }
                KeyUtil.FOLLOWING -> {
                    binding.notificationSubject.setText(R.string.notification_user_follow_activity)
                    binding.notificationHeader.text = model.user.name
                    binding.notificationContent.text = model.context
                }
                KeyUtil.ACTIVITY_MENTION -> {
                    binding.notificationSubject.setText(R.string.notification_user_activity_mention)
                    binding.notificationHeader.text = model.user.name
                    binding.notificationContent.text = model.context
                }
                KeyUtil.THREAD_COMMENT_MENTION -> {
                    binding.notificationSubject.setText(R.string.notification_user_comment_forum)
                    binding.notificationHeader.text = model.user.name
                    binding.notificationContent.text = model.context
                }
                KeyUtil.THREAD_SUBSCRIBED -> {
                    binding.notificationSubject.setText(R.string.notification_user_comment_forum)
                    binding.notificationHeader.text = model.user.name
                    binding.notificationContent.text = model.context
                }
                KeyUtil.THREAD_COMMENT_REPLY -> {
                    binding.notificationSubject.setText(R.string.notification_user_comment_forum)
                    binding.notificationHeader.text = model.user.name
                    binding.notificationContent.text = model.context
                }
                KeyUtil.AIRING -> {
                    binding.notificationSubject.setText(R.string.notification_series)
                    binding.notificationHeader.text = model.media?.title?.userPreferred
                    binding.notificationContent.text = context.getString(R.string.notification_episode,
                            model.episode.toString(), model.media?.title?.userPreferred)
                }
                KeyUtil.ACTIVITY_LIKE -> {
                    binding.notificationSubject.setText(R.string.notification_user_like_activity)
                    binding.notificationHeader.text = model.user.name
                    binding.notificationContent.text = model.context
                }
                KeyUtil.ACTIVITY_REPLY, KeyUtil.ACTIVITY_REPLY_SUBSCRIBED -> {
                    binding.notificationSubject.setText(R.string.notification_user_reply_activity)
                    binding.notificationHeader.text = model.user.name
                    binding.notificationContent.text = model.context
                }
                KeyUtil.ACTIVITY_REPLY_LIKE -> {
                    binding.notificationSubject.setText(R.string.notification_user_like_reply)
                    binding.notificationHeader.text = model.user.name
                    binding.notificationContent.text = model.context
                }
                KeyUtil.THREAD_LIKE -> {
                    binding.notificationSubject.setText(R.string.notification_user_like_activity)
                    binding.notificationHeader.text = model.user.name
                    binding.notificationContent.text = model.context
                }
                KeyUtil.THREAD_COMMENT_LIKE -> {
                    binding.notificationSubject.setText(R.string.notification_user_like_comment)
                    binding.notificationHeader.text = model.user.name
                    binding.notificationContent.text = model.context
                }
                KeyUtil.RELATED_MEDIA_ADDITION -> {
                    binding.notificationSubject.setText(R.string.notification_media_added)
                    binding.notificationHeader.text = model.media?.title?.userPreferred
                    binding.notificationContent.text = model.context
                }
                KeyUtil.MEDIA_DATA_CHANGE -> {
                    binding.notificationSubject.setText(R.string.notification_media_data_change)
                    binding.notificationHeader.text = model.media?.title?.userPreferred
                    binding.notificationContent.text = model.context
                }
                KeyUtil.MEDIA_MERGE -> {
                    binding.notificationSubject.setText(R.string.notification_media_merge)
                    binding.notificationHeader.text = model.deletedMediaTitles.joinToString(", ")
                    binding.notificationContent.text = model.context
                }
                KeyUtil.MEDIA_DELETION -> {
                    binding.notificationSubject.setText(R.string.notification_media_deletion)
                    binding.notificationHeader.text = model.deletedMediaTitle
                    binding.notificationContent.text = model.context
                }
            }
            binding.executePendingBindings()
        }

        /**
         * If any image views are used within the view holder, clear any pending async img requests
         * by using Glide.clear(ImageView) or Glide.with(context).clear(view) if using Glide v4.0
         * <br></br>
         *
         * @see Glide
         */
        override fun onViewRecycled() {
            Glide.with(context).clear(binding.notificationImg)
            binding.unbind()
        }

        @OnClick(R.id.container, R.id.notification_img)
        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        @OnLongClick(R.id.container)
        override fun onLongClick(v: View): Boolean {
            return performLongClick(clickListener, data, v)
        }
    }
}
