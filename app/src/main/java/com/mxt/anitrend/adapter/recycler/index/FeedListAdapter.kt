package com.mxt.anitrend.adapter.recycler.index

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.shared.UnresolvedViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidgetState
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidget
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidgetState
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidget
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.AdapterFeedMessageBinding
import com.mxt.anitrend.databinding.AdapterFeedProgressBinding
import com.mxt.anitrend.databinding.AdapterFeedStatusBinding
import com.mxt.anitrend.databinding.CustomRecyclerUnresolvedBinding
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil

class FeedListAdapter(
    private val experimentalMarkdown: Boolean,
    private val currentUser: UserBase?,
    private val resolveFeed: (Long) -> FeedList?,
    private val onToggleLikeAction: (Long) -> Unit,
    private val onDeleteFeedAction: (Long) -> Unit,
    private val onOpenMedia: (View, Long) -> Unit,
    private val onOpenComments: (Long) -> Unit,
    private val onEditFeed: (Long) -> Unit,
    private val onShowLikes: (Long) -> Unit,
    private val onOpenProfile: (View, Long) -> Unit,
    private val onLongPressMedia: (View, Long) -> Boolean,
) : ListAdapter<FeedItemUiModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private object ViewTypes {
        const val FEED_STATUS = 10
        const val FEED_MESSAGE = 11
        const val FEED_LIST = 20
        const val FEED_PROGRESS = 21
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        if (viewType < ViewTypes.FEED_STATUS) {
            return object : RecyclerView.ViewHolder(
                CustomRecyclerUnresolvedBinding.inflate(parent.context.getLayoutInflater(), parent, false).root,
            ) {}
        }
        return when (viewType) {
            ViewTypes.FEED_STATUS ->
                StatusFeedViewHolder(
                    AdapterFeedStatusBinding.inflate(parent.context.getLayoutInflater(), parent, false),
                )
            ViewTypes.FEED_MESSAGE ->
                MessageFeedViewHolder(
                    AdapterFeedMessageBinding.inflate(parent.context.getLayoutInflater(), parent, false),
                )
            ViewTypes.FEED_LIST ->
                ListFeedViewHolder(
                    AdapterFeedProgressBinding.inflate(parent.context.getLayoutInflater(), parent, false),
                )
            else ->
                ProgressFeedViewHolder(
                    AdapterFeedProgressBinding.inflate(parent.context.getLayoutInflater(), parent, false),
                )
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val model = getItem(position)
        when (holder) {
            is StatusFeedViewHolder -> holder.bind(model)
            is MessageFeedViewHolder -> holder.bind(model)
            is ListFeedViewHolder -> holder.bind(model)
            is ProgressFeedViewHolder -> holder.bind(model)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is StatusFeedViewHolder -> holder.recycle()
            is MessageFeedViewHolder -> holder.recycle()
            is ListFeedViewHolder -> holder.recycle()
            is ProgressFeedViewHolder -> holder.recycle()
        }
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        val model = getItem(position)
        if (model.type.isNullOrBlank()) {
            return -1
        }
        return when {
            model.type == KeyUtil.TEXT -> ViewTypes.FEED_STATUS
            model.type == KeyUtil.MESSAGE -> ViewTypes.FEED_MESSAGE
            model.type == KeyUtil.MEDIA_LIST && resolveFeed(model.id)?.likes == null -> ViewTypes.FEED_LIST
            else -> ViewTypes.FEED_PROGRESS
        }
    }

    private abstract inner class FeedItemViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener,
        View.OnLongClickListener {

        protected fun currentItem(): FeedItemUiModel? {
            val position = bindingAdapterPosition
            return if (position == RecyclerView.NO_POSITION) null else getItem(position)
        }

        protected fun bindClickListeners(vararg viewIds: Int) {
            viewIds.forEach { viewId ->
                itemView.findViewById<View?>(viewId)?.setOnClickListener(this)
            }
        }

        protected fun bindLongClickListeners(vararg viewIds: Int) {
            viewIds.forEach { viewId ->
                itemView.findViewById<View?>(viewId)?.setOnLongClickListener(this)
            }
        }

        protected fun canModify(feed: FeedList?): Boolean {
            val currentUserId = currentUser?.id ?: return false
            return when {
                feed?.messenger?.id != null -> feed.messenger?.id == currentUserId
                else -> feed?.user?.id == currentUserId
            }
        }

        override fun onClick(view: View) {
            val model = currentItem() ?: return
            val feed = resolveFeed(model.id)
            when (view.id) {
                R.id.series_image -> onOpenMedia(view, model.id)
                R.id.widget_comment -> onOpenComments(model.id)
                R.id.widget_edit -> onEditFeed(model.id)
                R.id.widget_users -> onShowLikes(model.id)
                R.id.user_avatar -> feed?.user?.id?.let { onOpenProfile(view, it) }
                R.id.messenger_avatar -> feed?.messenger?.id?.let { onOpenProfile(view, it) }
                R.id.recipient_avatar -> feed?.recipient?.id?.let { onOpenProfile(view, it) }
            }
        }

        override fun onLongClick(view: View): Boolean {
            val model = currentItem() ?: return false
            return when (view.id) {
                R.id.series_image -> onLongPressMedia(view, model.id)
                else -> false
            }
        }
    }

    private inner class ProgressFeedViewHolder(
        private val binding: AdapterFeedProgressBinding,
    ) : FeedItemViewHolder(binding.root) {
        init {
            bindClickListeners(
                R.id.widget_users,
                R.id.user_avatar,
                R.id.widget_comment,
                R.id.series_image,
            )
            bindLongClickListeners(R.id.series_image)
        }

        fun bind(model: FeedItemUiModel) {
            val feed = resolveFeed(model.id)
            binding.userAvatar.setImage(feed?.user?.avatar)
            binding.userName.text = feed?.user?.name
            binding.feedTime.text = feed?.createdAt?.let(DateUtil::getPrettyDateUnix)
            binding.feedHeadline.text = model.headline
            binding.mediaTitleEnglish.text = feed?.media?.title?.english
            binding.mediaTitleOriginal.text = feed?.media?.title?.original
            AspectImageView.setImage(binding.seriesImage, feed?.media?.coverImage)
            binding.widgetFavourite.render(
                FavouriteWidgetState(
                    count = model.likeCount,
                    isLiked = model.isLikedByCurrentUser || (currentUser != null && feed?.likes.orEmpty().any { it.id == currentUser.id }),
                    isEnabled = !model.isLikePending,
                    isLoading = model.isLikePending,
                ),
            )
            binding.widgetFavourite.setOnToggleListener { onToggleLikeAction(model.id) }
            binding.widgetComment.setReplyCount(model.replyCount)
            if (canModify(feed) && feed != null) {
                binding.widgetDelete.render(
                    StatusDeleteWidgetState(
                        isEnabled = !model.isDeletePending,
                        isLoading = model.isDeletePending,
                    ),
                )
                binding.widgetDelete.visibility = View.VISIBLE
                binding.widgetDelete.setOnDeleteListener { onDeleteFeedAction(model.id) }
            } else {
                binding.widgetDelete.visibility = View.GONE
                binding.widgetDelete.setOnDeleteListener(null)
            }
        }

        fun recycle() {
            Glide.with(binding.root).clear(binding.userAvatar)
            Glide.with(binding.root).clear(binding.seriesImage)
            binding.widgetFavourite.setOnToggleListener(null)
            binding.widgetDelete.setOnDeleteListener(null)
            binding.widgetFavourite.onViewRecycled()
            binding.widgetDelete.onViewRecycled()
        }
    }

    private inner class StatusFeedViewHolder(
        private val binding: AdapterFeedStatusBinding,
    ) : FeedItemViewHolder(binding.root) {
        init {
            bindClickListeners(
                R.id.container,
                R.id.widget_edit,
                R.id.widget_users,
                R.id.user_avatar,
                R.id.widget_comment,
            )
            bindLongClickListeners(R.id.container)
        }

        fun bind(model: FeedItemUiModel) {
            val feed = resolveFeed(model.id)
            binding.userAvatar.setImage(feed?.user?.avatar)
            binding.userName.text = feed?.user?.name
            binding.feedTime.text = feed?.createdAt?.let(DateUtil::getPrettyDateUnix)
            if (!experimentalMarkdown && feed != null) {
                binding.widgetStatus.visibility = View.VISIBLE
                binding.widgetStatus.setModel(feed)
            } else {
                binding.widgetStatus.visibility = View.GONE
            }
            binding.widgetStatusText.richMarkDown(model.body?.toString())
            binding.widgetFavourite.render(
                FavouriteWidgetState(
                    count = model.likeCount,
                    isLiked = model.isLikedByCurrentUser || (currentUser != null && feed?.likes.orEmpty().any { it.id == currentUser.id }),
                    isEnabled = !model.isLikePending,
                    isLoading = model.isLikePending,
                ),
            )
            binding.widgetFavourite.setOnToggleListener { onToggleLikeAction(model.id) }
            binding.widgetComment.setReplyCount(model.replyCount)

            binding.widgetEdit.visibility = if (canModify(feed)) View.VISIBLE else View.GONE
            if (canModify(feed) && feed != null) {
                binding.widgetDelete.render(
                    StatusDeleteWidgetState(
                        isEnabled = !model.isDeletePending,
                        isLoading = model.isDeletePending,
                    ),
                )
                binding.widgetDelete.visibility = View.VISIBLE
                binding.widgetDelete.setOnDeleteListener { onDeleteFeedAction(model.id) }
            } else {
                binding.widgetDelete.visibility = View.GONE
                binding.widgetDelete.setOnDeleteListener(null)
            }
        }

        fun recycle() {
            Glide.with(binding.root).clear(binding.userAvatar)
            binding.widgetFavourite.setOnToggleListener(null)
            binding.widgetDelete.setOnDeleteListener(null)
            binding.widgetFavourite.onViewRecycled()
            binding.widgetStatus.onViewRecycled()
            binding.widgetDelete.onViewRecycled()
        }
    }

    private inner class MessageFeedViewHolder(
        private val binding: AdapterFeedMessageBinding,
    ) : FeedItemViewHolder(binding.root) {
        init {
            bindClickListeners(
                R.id.widget_edit,
                R.id.widget_users,
                R.id.messenger_avatar,
                R.id.recipient_avatar,
                R.id.widget_comment,
            )
        }

        fun bind(model: FeedItemUiModel) {
            val feed = resolveFeed(model.id)
            binding.messengerAvatar.setImage(feed?.messenger?.avatar)
            binding.recipientAvatar.setImage(feed?.recipient?.avatar)
            binding.recipientUserName.visibility = View.VISIBLE
            binding.messengerUserName.visibility = View.GONE
            binding.recipientUserName.text = feed?.recipient?.name ?: feed?.messenger?.name
            binding.feedTime.text = feed?.createdAt?.let(DateUtil::getPrettyDateUnix)
            if (!experimentalMarkdown && feed != null) {
                binding.widgetStatus.visibility = View.VISIBLE
                binding.widgetStatus.setModel(feed)
            } else {
                binding.widgetStatus.visibility = View.GONE
            }
            binding.widgetStatusText.richMarkDown(model.body?.toString())
            binding.widgetFavourite.render(
                FavouriteWidgetState(
                    count = model.likeCount,
                    isLiked = model.isLikedByCurrentUser || (currentUser != null && feed?.likes.orEmpty().any { it.id == currentUser.id }),
                    isEnabled = !model.isLikePending,
                    isLoading = model.isLikePending,
                ),
            )
            binding.widgetFavourite.setOnToggleListener { onToggleLikeAction(model.id) }
            binding.widgetComment.setReplyCount(model.replyCount)

            binding.widgetEdit.visibility = if (canModify(feed)) View.VISIBLE else View.GONE
            if (canModify(feed) && feed != null) {
                binding.widgetDelete.render(
                    StatusDeleteWidgetState(
                        isEnabled = !model.isDeletePending,
                        isLoading = model.isDeletePending,
                    ),
                )
                binding.widgetDelete.visibility = View.VISIBLE
                binding.widgetDelete.setOnDeleteListener { onDeleteFeedAction(model.id) }
            } else {
                binding.widgetDelete.visibility = View.GONE
                binding.widgetDelete.setOnDeleteListener(null)
            }
        }

        fun recycle() {
            Glide.with(binding.root).clear(binding.messengerAvatar)
            Glide.with(binding.root).clear(binding.recipientAvatar)
            binding.widgetFavourite.setOnToggleListener(null)
            binding.widgetDelete.setOnDeleteListener(null)
            binding.widgetFavourite.onViewRecycled()
            binding.widgetStatus.onViewRecycled()
            binding.widgetDelete.onViewRecycled()
        }
    }

    private inner class ListFeedViewHolder(
        private val binding: AdapterFeedProgressBinding,
    ) : FeedItemViewHolder(binding.root) {
        init {
            bindClickListeners(R.id.user_avatar, R.id.widget_comment)
        }

        fun bind(model: FeedItemUiModel) {
            val feed = resolveFeed(model.id)
            binding.userAvatar.setImage(feed?.user?.avatar)
            binding.userName.text = feed?.user?.name
            binding.feedTime.text = feed?.createdAt?.let(DateUtil::getPrettyDateUnix)
            binding.feedHeadline.text = model.headline
            binding.mediaTitleEnglish.text = feed?.media?.title?.english
            binding.mediaTitleOriginal.text = feed?.media?.title?.original
            AspectImageView.setImage(binding.seriesImage, feed?.media?.coverImage)
            binding.widgetUsers.visibility = View.GONE
            binding.widgetFavourite.visibility = View.GONE
            binding.widgetComment.setReplyCount(model.replyCount)
            if (canModify(feed) && feed != null) {
                binding.widgetDelete.render(
                    StatusDeleteWidgetState(
                        isEnabled = !model.isDeletePending,
                        isLoading = model.isDeletePending,
                    ),
                )
                binding.widgetDelete.visibility = View.VISIBLE
                binding.widgetDelete.setOnDeleteListener { onDeleteFeedAction(model.id) }
            } else {
                binding.widgetDelete.visibility = View.GONE
                binding.widgetDelete.setOnDeleteListener(null)
            }
        }

        fun recycle() {
            Glide.with(binding.root).clear(binding.userAvatar)
            Glide.with(binding.root).clear(binding.seriesImage)
            binding.widgetDelete.setOnDeleteListener(null)
            binding.widgetDelete.onViewRecycled()
        }
    }

    companion object {
        val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<FeedItemUiModel>() {
                override fun areItemsTheSame(
                    oldItem: FeedItemUiModel,
                    newItem: FeedItemUiModel,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: FeedItemUiModel,
                    newItem: FeedItemUiModel,
                ): Boolean = oldItem == newItem
            }
    }
}
