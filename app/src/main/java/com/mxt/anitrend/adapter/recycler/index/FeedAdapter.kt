package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.shared.UnresolvedViewHolder
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.text.FeedHeadlineTextView
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidget
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidgetState
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidget
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidgetState
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.AdapterFeedMessageBinding
import com.mxt.anitrend.databinding.AdapterFeedProgressBinding
import com.mxt.anitrend.databinding.AdapterFeedStatusBinding
import com.mxt.anitrend.databinding.CustomRecyclerUnresolvedBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil

class FeedAdapter(
    context: Context,
    private val currentUser: UserBase?,
    private val onToggleLikeAction: (Long) -> Unit,
    private val onDeleteFeedAction: (Long) -> Unit,
) : RecyclerViewAdapter<FeedList>(context) {
    private companion object {
        const val FEED_STATUS = 10
        const val FEED_MESSAGE = 11
        const val FEED_LIST = 20
        const val FEED_PROGRESS = 21
    }

    @KeyUtil.MessageType
    private var messageType: Int = 0

    fun setMessageType(@KeyUtil.MessageType messageType: Int) {
        this.messageType = messageType
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<FeedList> {
        if (viewType < FEED_STATUS) {
            return UnresolvedViewHolder(
                CustomRecyclerUnresolvedBinding.inflate(parent.context.getLayoutInflater(), parent, false),
            )
        }
        return when (viewType) {
            FEED_STATUS -> StatusFeedViewHolder(AdapterFeedStatusBinding.inflate(parent.context.getLayoutInflater(), parent, false))
            FEED_MESSAGE -> MessageFeedViewHolder(AdapterFeedMessageBinding.inflate(parent.context.getLayoutInflater(), parent, false))
            FEED_LIST -> ListFeedViewHolder(AdapterFeedProgressBinding.inflate(parent.context.getLayoutInflater(), parent, false))
            else -> ProgressFeedViewHolder(AdapterFeedProgressBinding.inflate(parent.context.getLayoutInflater(), parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val model = data.getOrNull(position)
        if (model == null || model.type.isNullOrBlank()) {
            return -1
        }
        return when {
            model.type == KeyUtil.TEXT -> FEED_STATUS
            model.type == KeyUtil.MESSAGE -> FEED_MESSAGE
            model.type == KeyUtil.MEDIA_LIST && model.likes == null -> FEED_LIST
            else -> FEED_PROGRESS
        }
    }

    override fun getFilter(): Filter? = null

    private fun isLikedByCurrentUser(feed: FeedList): Boolean = currentUser?.let { activeUser -> feed.likes.orEmpty().any { it.id == activeUser.id } } == true

    private fun renderLike(
        widget: FavouriteWidget,
        feed: FeedList,
    ) {
        widget.render(
            FavouriteWidgetState(
                count = feed.likes.orEmpty().size,
                isLiked = isLikedByCurrentUser(feed),
                isEnabled = true,
                isLoading = false,
            ),
        )
        widget.setOnToggleListener { onToggleLikeAction(feed.id) }
    }

    private fun renderDelete(
        widget: StatusDeleteWidget,
        canDelete: Boolean,
        feed: FeedList,
    ) {
        if (canDelete) {
            widget.render(StatusDeleteWidgetState(isEnabled = true, isLoading = false))
            widget.visibility = View.VISIBLE
            widget.setOnDeleteListener { onDeleteFeedAction(feed.id) }
        } else {
            widget.visibility = View.GONE
            widget.setOnDeleteListener(null)
        }
    }

    inner class ProgressFeedViewHolder(
        private val binding: AdapterFeedProgressBinding,
    ) : RecyclerViewHolder<FeedList>(binding.root) {
        init {
            bindClickListeners(R.id.widget_users, R.id.user_avatar, R.id.widget_comment, R.id.series_image)
            bindLongClickListeners(R.id.series_image)
        }

        override fun onBindViewHolder(model: FeedList) {
            binding.userAvatar.setImage(model.user?.avatar)
            binding.userName.text = model.user?.name
            binding.feedTime.text = DateUtil.getPrettyDateUnix(model.createdAt)
            FeedHeadlineTextView.setHeadline(binding.feedHeadline, model)
            binding.mediaTitleEnglish.text = model.media?.title?.english
            binding.mediaTitleOriginal.text = model.media?.title?.original
            AspectImageView.setImage(binding.seriesImage, model.media?.coverImage)
            renderLike(binding.widgetFavourite, model)
            binding.widgetComment.setReplyCount(model.replyCount)
            renderDelete(binding.widgetDelete, currentUser?.id == model.user?.id, model)
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.userAvatar)
            Glide.with(getContext()).clear(binding.seriesImage)
            binding.widgetFavourite.setOnToggleListener(null)
            binding.widgetDelete.setOnDeleteListener(null)
            binding.widgetFavourite.onViewRecycled()
            binding.widgetDelete.onViewRecycled()
        }

        override fun onClick(v: View) = performClick(clickListener, data, v)

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }

    inner class StatusFeedViewHolder(
        private val binding: AdapterFeedStatusBinding,
    ) : RecyclerViewHolder<FeedList>(binding.root) {
        init {
            bindClickListeners(R.id.container, R.id.widget_edit, R.id.widget_users, R.id.user_avatar, R.id.widget_comment)
            bindLongClickListeners(R.id.container)
        }

        override fun onBindViewHolder(model: FeedList) {
            binding.userAvatar.setImage(model.user?.avatar)
            binding.userName.text = model.user?.name
            binding.feedTime.text = DateUtil.getPrettyDateUnix(model.createdAt)
            binding.widgetStatus.visibility = View.GONE
            binding.widgetStatusText.richMarkDown(model.text)
            renderLike(binding.widgetFavourite, model)
            binding.widgetComment.setReplyCount(model.replyCount)

            val canDelete = currentUser?.id == model.user?.id
            binding.widgetEdit.visibility = if (canDelete) View.VISIBLE else View.GONE
            renderDelete(binding.widgetDelete, canDelete, model)
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.userAvatar)
            binding.widgetFavourite.setOnToggleListener(null)
            binding.widgetDelete.setOnDeleteListener(null)
            binding.widgetFavourite.onViewRecycled()
            binding.widgetStatus.onViewRecycled()
            binding.widgetDelete.onViewRecycled()
        }

        override fun onClick(v: View) = performClick(clickListener, data, v)

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }

    inner class MessageFeedViewHolder(
        private val binding: AdapterFeedMessageBinding,
    ) : RecyclerViewHolder<FeedList>(binding.root) {
        init {
            bindClickListeners(R.id.widget_edit, R.id.widget_users, R.id.messenger_avatar, R.id.recipient_avatar, R.id.widget_comment)
        }

        override fun onBindViewHolder(model: FeedList) {
            val isInbox = messageType == KeyUtil.MESSAGE_TYPE_INBOX
            val isOutbox = messageType == KeyUtil.MESSAGE_TYPE_OUTBOX
            val displayName = if (isOutbox) model.recipient?.name else model.messenger?.name
            binding.messengerAvatar.setImage(model.messenger?.avatar)
            binding.recipientAvatar.setImage(model.recipient?.avatar)
            binding.recipientUserName.visibility = if (isInbox) View.VISIBLE else View.GONE
            binding.messengerUserName.visibility = if (isOutbox) View.VISIBLE else View.GONE
            binding.recipientUserName.text = displayName
            binding.messengerUserName.text = displayName
            binding.feedTime.text = DateUtil.getPrettyDateUnix(model.createdAt)
            binding.widgetStatus.visibility = View.GONE
            binding.widgetStatusText.richMarkDown(model.text)
            renderLike(binding.widgetFavourite, model)
            binding.widgetComment.setReplyCount(model.replyCount)

            val canDelete = currentUser?.id == model.messenger?.id
            binding.widgetEdit.visibility = if (canDelete) View.VISIBLE else View.GONE
            renderDelete(binding.widgetDelete, canDelete, model)
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.messengerAvatar)
            Glide.with(getContext()).clear(binding.recipientAvatar)
            binding.widgetFavourite.setOnToggleListener(null)
            binding.widgetDelete.setOnDeleteListener(null)
            binding.widgetFavourite.onViewRecycled()
            binding.widgetStatus.onViewRecycled()
            binding.widgetDelete.onViewRecycled()
        }

        override fun onClick(v: View) = performClick(clickListener, data, v)

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }

    inner class ListFeedViewHolder(
        private val binding: AdapterFeedProgressBinding,
    ) : RecyclerViewHolder<FeedList>(binding.root) {
        init {
            bindClickListeners(R.id.user_avatar, R.id.widget_comment)
        }

        override fun onBindViewHolder(model: FeedList) {
            binding.userAvatar.setImage(model.user?.avatar)
            binding.userName.text = model.user?.name
            binding.feedTime.text = DateUtil.getPrettyDateUnix(model.createdAt)
            FeedHeadlineTextView.setHeadline(binding.feedHeadline, model)
            binding.mediaTitleEnglish.text = model.media?.title?.english
            binding.mediaTitleOriginal.text = model.media?.title?.original
            AspectImageView.setImage(binding.seriesImage, model.media?.coverImage)
            binding.widgetUsers.visibility = View.GONE
            binding.widgetFavourite.visibility = View.GONE
            binding.widgetComment.setReplyCount(model.replyCount)
            renderDelete(binding.widgetDelete, currentUser?.id == model.user?.id, model)
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.userAvatar)
            Glide.with(getContext()).clear(binding.seriesImage)
            binding.widgetFavourite.setOnToggleListener(null)
            binding.widgetDelete.setOnDeleteListener(null)
            binding.widgetDelete.onViewRecycled()
        }

        override fun onClick(v: View) = performClick(clickListener, data, v)

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
