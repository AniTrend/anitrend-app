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
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidget
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidget
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.AdapterFeedMessageBinding
import com.mxt.anitrend.databinding.AdapterFeedProgressBinding
import com.mxt.anitrend.databinding.AdapterFeedStatusBinding
import com.mxt.anitrend.databinding.CustomRecyclerUnresolvedBinding
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil

class FeedListAdapter(
    private val experimentalMarkdown: Boolean,
    private val currentUser: UserBase?,
    private val resolveFeed: (Long) -> FeedList?,
    private val onToggleLikeAction: (Long, LikeableType, (Result<List<UserBase>>) -> Unit) -> Unit,
    private val onDeleteFeedAction: (Long, Int, (Result<DeleteState>) -> Unit) -> Unit,
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

    private val favouriteListener =
        object : FavouriteWidget.Listener {
            override fun onToggleLike(
                id: Long,
                type: LikeableType,
                onResult: (Result<List<UserBase>>) -> Unit,
            ) = onToggleLikeAction(id, type, onResult)
        }

    private val deleteListener =
        object : StatusDeleteWidget.Listener {
            override fun onDeleteFeed(
                feedId: Long,
                @KeyUtil.RequestType requestType: Int,
                onResult: (Result<DeleteState>) -> Unit,
            ) = onDeleteFeedAction(feedId, requestType, onResult)
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
            binding.widgetFavourite.setRequestParams(KeyUtil.ACTIVITY, model.id)
            binding.widgetFavourite.setModel(feed?.likes)
            binding.widgetFavourite.setCurrentUser(currentUser)
            binding.widgetFavourite.setListener(favouriteListener)
            binding.widgetComment.setReplyCount(model.replyCount)
            if (model.canDelete && feed != null) {
                binding.widgetDelete.setModel(feed, KeyUtil.MUT_DELETE_FEED)
                binding.widgetDelete.visibility = View.VISIBLE
            } else {
                binding.widgetDelete.visibility = View.GONE
            }
            binding.widgetDelete.setListener(deleteListener)
        }

        fun recycle() {
            Glide.with(binding.root).clear(binding.userAvatar)
            Glide.with(binding.root).clear(binding.seriesImage)
            binding.widgetFavourite.setListener(null)
            binding.widgetDelete.setListener(null)
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
            binding.widgetFavourite.setRequestParams(KeyUtil.ACTIVITY, model.id)
            binding.widgetFavourite.setModel(feed?.likes)
            binding.widgetFavourite.setCurrentUser(currentUser)
            binding.widgetFavourite.setListener(favouriteListener)
            binding.widgetComment.setReplyCount(model.replyCount)

            binding.widgetEdit.visibility = if (model.canEdit) View.VISIBLE else View.GONE
            if (model.canDelete && feed != null) {
                binding.widgetDelete.setModel(feed, KeyUtil.MUT_DELETE_FEED)
                binding.widgetDelete.visibility = View.VISIBLE
            } else {
                binding.widgetDelete.visibility = View.GONE
            }
            binding.widgetDelete.setListener(deleteListener)
        }

        fun recycle() {
            Glide.with(binding.root).clear(binding.userAvatar)
            binding.widgetFavourite.setListener(null)
            binding.widgetDelete.setListener(null)
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
            binding.widgetFavourite.setRequestParams(KeyUtil.ACTIVITY, model.id)
            binding.widgetFavourite.setModel(feed?.likes)
            binding.widgetFavourite.setCurrentUser(currentUser)
            binding.widgetFavourite.setListener(favouriteListener)
            binding.widgetComment.setReplyCount(model.replyCount)

            binding.widgetEdit.visibility = if (model.canEdit) View.VISIBLE else View.GONE
            if (model.canDelete && feed != null) {
                binding.widgetDelete.setModel(feed, KeyUtil.MUT_DELETE_FEED)
                binding.widgetDelete.visibility = View.VISIBLE
            } else {
                binding.widgetDelete.visibility = View.GONE
            }
            binding.widgetDelete.setListener(deleteListener)
        }

        fun recycle() {
            Glide.with(binding.root).clear(binding.messengerAvatar)
            Glide.with(binding.root).clear(binding.recipientAvatar)
            binding.widgetFavourite.setListener(null)
            binding.widgetDelete.setListener(null)
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
            if (model.canDelete && feed != null) {
                binding.widgetDelete.setModel(feed, KeyUtil.MUT_DELETE_FEED)
                binding.widgetDelete.visibility = View.VISIBLE
            } else {
                binding.widgetDelete.visibility = View.GONE
            }
            binding.widgetDelete.setListener(deleteListener)
        }

        fun recycle() {
            Glide.with(binding.root).clear(binding.userAvatar)
            Glide.with(binding.root).clear(binding.seriesImage)
            binding.widgetDelete.setListener(null)
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
