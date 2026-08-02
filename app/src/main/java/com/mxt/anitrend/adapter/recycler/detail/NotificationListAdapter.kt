package com.mxt.anitrend.adapter.recycler.detail

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.databinding.AdapterNotificationBinding
import com.mxt.anitrend.domain.model.NotificationItemUiModel
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil

/**
 * RecyclerView adapter for the notifications screen.
 *
 * Renders exclusively from immutable [NotificationItemUiModel] rows diffed on
 * the stable server notification id. Read state is supplied by the fragment via
 * [NotificationItemUiModel.isRead]; this adapter never resolves repositories or
 * local persistence. Click and long-click affordances (row and avatar image) are
 * forwarded to the owning fragment with the target view so it can distinguish
 * avatar navigation from row navigation.
 */
class NotificationListAdapter(
    private val onItemClick: (View, NotificationItemUiModel) -> Unit,
    private val onItemLongClick: (View, NotificationItemUiModel) -> Unit,
) : ListAdapter<NotificationItemUiModel, NotificationListAdapter.NotificationViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): NotificationViewHolder = NotificationViewHolder(
        AdapterNotificationBinding.inflate(parent.context.getLayoutInflater(), parent, false),
    )

    override fun onBindViewHolder(
        holder: NotificationViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: NotificationViewHolder) {
        holder.recycle()
        super.onViewRecycled(holder)
    }

    inner class NotificationViewHolder(
        private val binding: AdapterNotificationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            bindClickListeners(R.id.container, R.id.notification_img)
            bindLongClickListeners(R.id.container)
        }

        fun bind(model: NotificationItemUiModel) {
            val record = model.record
            binding.notificationIndicator.visibility =
                if (model.isRead) View.GONE else View.VISIBLE

            binding.notificationTime.text = DateUtil.getPrettyDateUnix(record.createdAt)

            val userAvatar = record.user?.avatar
            if (!userAvatar.isNullOrBlank()) {
                AspectImageView.setImage(binding.notificationImg, userAvatar)
            } else {
                AspectImageView.setImage(binding.notificationImg, record.media?.coverImage)
            }

            val header = record.user?.name.orEmpty()
            when (record.type) {
                KeyUtil.ACTIVITY_MESSAGE -> {
                    binding.notificationSubject.setText(R.string.notification_user_activity_message)
                    binding.notificationHeader.text = header
                    binding.notificationContent.text = record.context
                }
                KeyUtil.FOLLOWING -> {
                    binding.notificationSubject.setText(R.string.notification_user_follow_activity)
                    binding.notificationHeader.text = header
                    binding.notificationContent.text = record.context
                }
                KeyUtil.ACTIVITY_MENTION -> {
                    binding.notificationSubject.setText(R.string.notification_user_activity_mention)
                    binding.notificationHeader.text = header
                    binding.notificationContent.text = record.context
                }
                KeyUtil.THREAD_COMMENT_MENTION,
                KeyUtil.THREAD_SUBSCRIBED,
                KeyUtil.THREAD_COMMENT_REPLY,
                -> {
                    binding.notificationSubject.setText(R.string.notification_user_comment_forum)
                    binding.notificationHeader.text = header
                    binding.notificationContent.text = record.context
                }
                KeyUtil.AIRING -> {
                    binding.notificationSubject.setText(R.string.notification_series)
                    binding.notificationHeader.text = record.media?.titleUserPreferred
                    binding.notificationContent.text = itemView.context.getString(
                        R.string.notification_episode,
                        record.episode?.toString().orEmpty(),
                        record.media?.titleUserPreferred,
                    )
                }
                KeyUtil.ACTIVITY_LIKE -> {
                    binding.notificationSubject.setText(R.string.notification_user_like_activity)
                    binding.notificationHeader.text = header
                    binding.notificationContent.text = record.context
                }
                KeyUtil.ACTIVITY_REPLY,
                KeyUtil.ACTIVITY_REPLY_SUBSCRIBED,
                -> {
                    binding.notificationSubject.setText(R.string.notification_user_reply_activity)
                    binding.notificationHeader.text = header
                    binding.notificationContent.text = record.context
                }
                KeyUtil.ACTIVITY_REPLY_LIKE -> {
                    binding.notificationSubject.setText(R.string.notification_user_like_reply)
                    binding.notificationHeader.text = header
                    binding.notificationContent.text = record.context
                }
                KeyUtil.THREAD_LIKE -> {
                    binding.notificationSubject.setText(R.string.notification_user_like_activity)
                    binding.notificationHeader.text = header
                    binding.notificationContent.text = record.context
                }
                KeyUtil.THREAD_COMMENT_LIKE -> {
                    binding.notificationSubject.setText(R.string.notification_user_like_comment)
                    binding.notificationHeader.text = header
                    binding.notificationContent.text = record.context
                }
                KeyUtil.RELATED_MEDIA_ADDITION -> {
                    binding.notificationSubject.setText(R.string.notification_media_added)
                    binding.notificationHeader.text = record.media?.titleUserPreferred
                    binding.notificationContent.text = record.context
                }
                KeyUtil.MEDIA_DATA_CHANGE -> {
                    binding.notificationSubject.setText(R.string.notification_media_data_change)
                    binding.notificationHeader.text = record.media?.titleUserPreferred
                    binding.notificationContent.text = record.context
                }
                KeyUtil.MEDIA_MERGE -> {
                    binding.notificationSubject.setText(R.string.notification_media_merge)
                    binding.notificationHeader.text = record.deletedMediaTitles.joinToString(", ")
                    binding.notificationContent.text = record.context
                }
                KeyUtil.MEDIA_DELETION -> {
                    binding.notificationSubject.setText(R.string.notification_media_deletion)
                    binding.notificationHeader.text = record.deletedMediaTitle
                    binding.notificationContent.text = record.context
                }
            }
        }

        fun recycle() {
            Glide.with(itemView.context).clear(binding.notificationImg)
        }

        private fun bindClickListeners(vararg viewIds: Int) {
            for (viewId in viewIds) {
                val view = itemView.findViewById<View?>(viewId)
                view?.setOnClickListener { target -> currentItem()?.let { onItemClick(target, it) } }
            }
        }

        private fun bindLongClickListeners(vararg viewIds: Int) {
            for (viewId in viewIds) {
                val view = itemView.findViewById<View?>(viewId)
                view?.setOnLongClickListener { target ->
                    currentItem()?.let { onItemLongClick(target, it) }
                    true
                }
            }
        }

        private fun currentItem(): NotificationItemUiModel? {
            val position = bindingAdapterPosition
            return if (position == RecyclerView.NO_POSITION) null else getItem(position)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NotificationItemUiModel>() {
            override fun areItemsTheSame(
                oldItem: NotificationItemUiModel,
                newItem: NotificationItemUiModel,
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: NotificationItemUiModel,
                newItem: NotificationItemUiModel,
            ): Boolean = oldItem == newItem
        }
    }
}
