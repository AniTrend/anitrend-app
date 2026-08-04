package com.mxt.anitrend.adapter.recycler.detail

import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.databinding.ItemNotificationMediaBinding
import com.mxt.anitrend.databinding.ItemNotificationPersonBinding
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
 * local persistence.
 *
 * Notifications are grouped into three concrete view types so the visual layout
 * matches the use case: social interactions use a circular avatar, media updates
 * use a cover image, and forum events use a circular avatar. Click and long-click
 * affordances (row and avatar image) are forwarded to the owning fragment with the
 * target view so it can distinguish avatar navigation from row navigation.
 */
class NotificationListAdapter(
    private val onItemClick: (View, NotificationItemUiModel) -> Unit,
    private val onItemLongClick: (View, NotificationItemUiModel) -> Unit,
) : ListAdapter<NotificationItemUiModel, NotificationListAdapter.BaseNotificationViewHolder>(DIFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int = when (val type = getItem(position).record.type) {
        KeyUtil.ACTIVITY_MESSAGE,
        KeyUtil.FOLLOWING,
        KeyUtil.ACTIVITY_MENTION,
        KeyUtil.ACTIVITY_LIKE,
        KeyUtil.ACTIVITY_REPLY,
        KeyUtil.ACTIVITY_REPLY_SUBSCRIBED,
        KeyUtil.ACTIVITY_REPLY_LIKE,
        -> VIEW_TYPE_SOCIAL
        KeyUtil.AIRING,
        KeyUtil.RELATED_MEDIA_ADDITION,
        KeyUtil.MEDIA_DATA_CHANGE,
        KeyUtil.MEDIA_DELETION,
        KeyUtil.MEDIA_MERGE,
        -> VIEW_TYPE_MEDIA
        KeyUtil.THREAD_COMMENT_MENTION,
        KeyUtil.THREAD_SUBSCRIBED,
        KeyUtil.THREAD_COMMENT_REPLY,
        KeyUtil.THREAD_LIKE,
        KeyUtil.THREAD_COMMENT_LIKE,
        -> VIEW_TYPE_FORUM
        else -> VIEW_TYPE_SOCIAL
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BaseNotificationViewHolder {
        val inflater = parent.context.getLayoutInflater()
        return when (viewType) {
            VIEW_TYPE_SOCIAL -> PersonViewHolder(
                ItemNotificationPersonBinding.inflate(inflater, parent, false),
                fallbackDrawableRes = R.drawable.ic_account_circle_grey_600_24dp,
            )
            VIEW_TYPE_MEDIA -> MediaViewHolder(
                ItemNotificationMediaBinding.inflate(inflater, parent, false),
            )
            VIEW_TYPE_FORUM -> PersonViewHolder(
                ItemNotificationPersonBinding.inflate(inflater, parent, false),
                fallbackDrawableRes = R.drawable.ic_mode_comment_grey_600_18dp,
            )
            else -> PersonViewHolder(
                ItemNotificationPersonBinding.inflate(inflater, parent, false),
                fallbackDrawableRes = R.drawable.ic_account_circle_grey_600_24dp,
            )
        }
    }

    override fun onBindViewHolder(
        holder: BaseNotificationViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: BaseNotificationViewHolder) {
        holder.recycle()
        super.onViewRecycled(holder)
    }

    /** Common text and indicator views shared by notification row layouts. */
    data class NotificationTextViews(
        val subject: MaterialTextView,
        val header: MaterialTextView,
        val content: MaterialTextView,
        val time: MaterialTextView,
        val indicator: View,
    )

    /** Shared holder behavior for all notification row layouts. */
    abstract inner class BaseNotificationViewHolder(
        root: View,
        private val textViews: NotificationTextViews,
    ) : RecyclerView.ViewHolder(root) {

        init {
            bindClickListeners(R.id.container, R.id.notification_img)
            bindLongClickListeners(R.id.container)
        }

        /** Binds a notification model to the row. */
        abstract fun bind(model: NotificationItemUiModel)

        /** Releases image and row resources before the holder is recycled. */
        abstract fun recycle()

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

        protected fun currentItem(): NotificationItemUiModel? {
            val position = bindingAdapterPosition
            return if (position == RecyclerView.NO_POSITION) null else getItem(position)
        }

        protected fun bindCommonText(model: NotificationItemUiModel) {
            val record = model.record
            textViews.indicator.visibility = if (model.isRead) View.GONE else View.VISIBLE
            textViews.time.text = DateUtil.getPrettyDateUnix(record.createdAt)

            val header = record.user?.name.orEmpty()
            textViews.header.setTypeface(null, if (model.isRead) Typeface.NORMAL else Typeface.BOLD)
            textViews.subject.setText(getNotificationSubjectResource(record.type))
            when (record.type) {
                KeyUtil.ACTIVITY_MESSAGE,
                KeyUtil.FOLLOWING,
                KeyUtil.ACTIVITY_MENTION,
                KeyUtil.THREAD_COMMENT_MENTION,
                KeyUtil.THREAD_SUBSCRIBED,
                KeyUtil.THREAD_COMMENT_REPLY,
                KeyUtil.ACTIVITY_LIKE,
                KeyUtil.ACTIVITY_REPLY,
                KeyUtil.ACTIVITY_REPLY_SUBSCRIBED,
                KeyUtil.ACTIVITY_REPLY_LIKE,
                KeyUtil.THREAD_LIKE,
                KeyUtil.THREAD_COMMENT_LIKE,
                -> {
                    textViews.header.text = header
                    textViews.content.text = record.context
                }
                KeyUtil.AIRING -> {
                    textViews.header.text = record.media?.titleUserPreferred
                    textViews.content.text = itemView.context.getString(
                        R.string.notification_episode,
                        record.episode?.toString().orEmpty(),
                        record.media?.titleUserPreferred,
                    )
                }
                KeyUtil.RELATED_MEDIA_ADDITION,
                KeyUtil.MEDIA_DATA_CHANGE,
                -> {
                    textViews.header.text = record.media?.titleUserPreferred
                    textViews.content.text = record.context
                }
                KeyUtil.MEDIA_MERGE -> {
                    textViews.header.text = record.deletedMediaTitles.joinToString(", ")
                    textViews.content.text = record.context
                }
                KeyUtil.MEDIA_DELETION -> {
                    textViews.header.text = record.deletedMediaTitle
                    textViews.content.text = record.context
                }
                // Unknown types render a safe default subject with whatever
                // user or media identity is available so recycled rows never
                // retain stale text from a previously bound model.
                else -> {
                    textViews.header.text = header.ifBlank { record.media?.titleUserPreferred }
                    textViews.content.text = record.context
                }
            }
        }

        protected fun setRowContentDescription(
            containerView: View,
            model: NotificationItemUiModel,
        ) {
            val record = model.record
            val subject = containerView.resources.getString(getNotificationSubjectResource(record.type))
            val time = DateUtil.getPrettyDateUnix(record.createdAt)
            val header = when (record.type) {
                KeyUtil.AIRING,
                KeyUtil.RELATED_MEDIA_ADDITION,
                KeyUtil.MEDIA_DATA_CHANGE,
                -> record.media?.titleUserPreferred
                KeyUtil.MEDIA_MERGE -> record.deletedMediaTitles.joinToString(", ").ifBlank { null }
                KeyUtil.MEDIA_DELETION -> record.deletedMediaTitle
                else -> record.user?.name?.ifBlank { null } ?: record.media?.titleUserPreferred
            }
            val content = record.context
            val state = if (model.isRead) "" else containerView.resources.getString(R.string.layout_unread_state)
            containerView.contentDescription = buildString {
                append(subject)
                if (!header.isNullOrBlank()) {
                    append(", ")
                    append(header)
                }
                if (!content.isNullOrBlank()) {
                    append(", ")
                    append(content)
                }
                append(", ")
                append(time)
                if (state.isNotBlank()) {
                    append(", ")
                    append(state)
                }
            }
        }
    }

    /**
     * Shared holder for social and forum notifications. Both use the same
     * person row layout; only the placeholder/error drawable differs, which is
     * supplied per view type by [onCreateViewHolder].
     */
    inner class PersonViewHolder(
        private val binding: ItemNotificationPersonBinding,
        private val fallbackDrawableRes: Int,
    ) : BaseNotificationViewHolder(
        binding.root,
        NotificationTextViews(
            subject = binding.notificationSubject,
            header = binding.notificationHeader,
            content = binding.notificationContent,
            time = binding.notificationTime,
            indicator = binding.notificationIndicator,
        ),
    ) {

        override fun bind(model: NotificationItemUiModel) {
            val record = model.record
            bindCommonText(model)

            val imageUrl = record.user?.avatar?.takeIf { it.isNotBlank() }
                ?: record.media?.coverImage
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(fallbackDrawableRes)
                .error(fallbackDrawableRes)
                .into(binding.notificationImg)

            setRowContentDescription(binding.root, model)
        }

        override fun recycle() {
            Glide.with(itemView.context).clear(binding.notificationImg)
        }
    }

    /** Holder for media notification rows that use a cover image. */
    inner class MediaViewHolder(
        private val binding: ItemNotificationMediaBinding,
    ) : BaseNotificationViewHolder(
        binding.root,
        NotificationTextViews(
            subject = binding.notificationSubject,
            header = binding.notificationHeader,
            content = binding.notificationContent,
            time = binding.notificationTime,
            indicator = binding.notificationIndicator,
        ),
    ) {

        override fun bind(model: NotificationItemUiModel) {
            bindCommonText(model)

            AspectImageView.setImage(binding.notificationImg, model.record.media?.coverImage)
            setRowContentDescription(binding.root, model)
        }

        override fun recycle() {
            Glide.with(itemView.context).clear(binding.notificationImg)
        }
    }

    /** View types and diffing rules shared by notification rows. */
    companion object {
        const val VIEW_TYPE_SOCIAL = 0
        const val VIEW_TYPE_MEDIA = 1
        const val VIEW_TYPE_FORUM = 2

        private fun getNotificationSubjectResource(type: String?): Int = when (type) {
            KeyUtil.ACTIVITY_MESSAGE -> R.string.notification_user_activity_message
            KeyUtil.FOLLOWING -> R.string.notification_user_follow_activity
            KeyUtil.ACTIVITY_MENTION -> R.string.notification_user_activity_mention
            KeyUtil.THREAD_COMMENT_MENTION,
            KeyUtil.THREAD_SUBSCRIBED,
            KeyUtil.THREAD_COMMENT_REPLY,
            -> R.string.notification_user_comment_forum
            KeyUtil.AIRING -> R.string.notification_series
            KeyUtil.ACTIVITY_LIKE -> R.string.notification_user_like_activity
            KeyUtil.ACTIVITY_REPLY,
            KeyUtil.ACTIVITY_REPLY_SUBSCRIBED,
            -> R.string.notification_user_reply_activity
            KeyUtil.ACTIVITY_REPLY_LIKE -> R.string.notification_user_like_reply
            KeyUtil.THREAD_LIKE -> R.string.notification_user_like_activity
            KeyUtil.THREAD_COMMENT_LIKE -> R.string.notification_user_like_comment
            KeyUtil.RELATED_MEDIA_ADDITION -> R.string.notification_media_added
            KeyUtil.MEDIA_DATA_CHANGE -> R.string.notification_media_data_change
            KeyUtil.MEDIA_MERGE -> R.string.notification_media_merge
            KeyUtil.MEDIA_DELETION -> R.string.notification_media_deletion
            else -> R.string.notification_default
        }

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
