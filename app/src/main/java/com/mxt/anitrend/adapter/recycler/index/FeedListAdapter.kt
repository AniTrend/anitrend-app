package com.mxt.anitrend.adapter.recycler.index

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidget
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidgetState
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.AdapterFeedMessageBinding
import com.mxt.anitrend.databinding.CustomRecyclerUnresolvedBinding
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.favourite.FavouriteActionController
import com.mxt.anitrend.ui.favourite.FavouriteActionState
import com.mxt.anitrend.ui.feed.FeedStatusRow
import com.mxt.anitrend.ui.feed.FeedProgressRow
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil

/**
 * UI projection boundary for the feed screen. Renders exclusively from the
 * immutable [FeedItemUiModel] produced by the feed store records and never
 * resolves repositories, stores, or legacy `FeedList` entities. All mutations
 * are forwarded to the fragment through the constructor callbacks.
 */
class FeedListAdapter(
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
            ViewTypes.FEED_STATUS -> StatusFeedViewHolder(parent)
            ViewTypes.FEED_MESSAGE ->
                MessageFeedViewHolder(
                    AdapterFeedMessageBinding.inflate(parent.context.getLayoutInflater(), parent, false),
                )
            ViewTypes.FEED_LIST -> ProgressFeedViewHolder(parent, isListVariant = true)
            else -> ProgressFeedViewHolder(parent, isListVariant = false)
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
            is ProgressFeedViewHolder -> holder.bind(model)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is StatusFeedViewHolder -> holder.recycle()
            is MessageFeedViewHolder -> holder.recycle()
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
            model.type == KeyUtil.MEDIA_LIST && !model.hasLikes -> ViewTypes.FEED_LIST
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
            when (view.id) {
                R.id.series_image -> onOpenMedia(view, model.id)
                R.id.widget_comment -> onOpenComments(model.id)
                R.id.widget_edit -> onEditFeed(model.id)
                R.id.widget_users -> onShowLikes(model.id)
                R.id.user_avatar -> model.userId?.let { onOpenProfile(view, it) }
                R.id.messenger_avatar -> model.messengerId?.let { onOpenProfile(view, it) }
                R.id.recipient_avatar -> model.recipientId?.let { onOpenProfile(view, it) }
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
        parent: ViewGroup,
        private val isListVariant: Boolean,
    ) : FeedItemViewHolder(
        ComposeView(parent.context).apply {
            id = R.id.container
            layoutParams =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    val margin = parent.resources.getDimensionPixelSize(R.dimen.lg_margin)
                    setMargins(margin, margin, margin, 0)
                }
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
            )
        },
    ) {
        private val composeView = itemView as ComposeView
        private val rowModel = mutableStateOf<FeedItemUiModel?>(null)
        private val createdAtText = mutableStateOf("")
        private val isRecycled = mutableStateOf(true)
        private val bindingEpoch = mutableStateOf(0)
        private var deleteWidget: StatusDeleteWidget? = null
        private val favouriteActionView =
            if (isListVariant) {
                null
            } else {
                ComposeView(composeView.context).apply {
                    id = R.id.widget_favourite
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                }
            }
        private val favouriteActionController = favouriteActionView?.let(::FavouriteActionController)
        private val toggleLikeAction: () -> Unit = {
            dispatchCurrentItem { model -> onToggleLikeAction(model.id) }
        }
        private val showLikesAction: () -> Unit = {
            dispatchCurrentItem { model -> onShowLikes(model.id) }
        }
        private val openCommentsAction: () -> Unit = {
            dispatchCurrentItem { model -> onOpenComments(model.id) }
        }
        private val openProfileAction: () -> Unit = ::dispatchOpenProfile
        private val openMediaAction: () -> Unit = ::dispatchOpenMedia
        private val longPressMediaAction: () -> Boolean = ::dispatchLongPressMedia
        private val deleteAction: (Long, Int) -> Unit = ::dispatchDelete
        private val deleteWidgetCreated: (StatusDeleteWidget) -> Unit = { deleteWidget = it }

        init {
            composeView.setContent {
                AniTrendTheme {
                    rowModel.value?.let { model ->
                        FeedProgressRow(
                            model = model,
                            createdAtText = createdAtText.value,
                            isListVariant = isListVariant,
                            isRecycled = isRecycled.value,
                            bindingEpoch = bindingEpoch.value,
                            favouriteActionView = favouriteActionView,
                            onToggleLike = toggleLikeAction,
                            onShowLikes = showLikesAction,
                            onOpenComments = openCommentsAction,
                            onOpenProfile = openProfileAction,
                            onOpenMedia = openMediaAction,
                            onLongPressMedia = longPressMediaAction,
                            onDelete = deleteAction,
                            onDeleteWidgetCreated = deleteWidgetCreated,
                        )
                    }
                }
            }
        }

        fun bind(model: FeedItemUiModel) {
            bindingEpoch.value += 1
            isRecycled.value = false
            createdAtText.value = DateUtil.getPrettyDateUnix(model.createdAt)
            rowModel.value = model
            favouriteActionController?.render(
                FavouriteActionState(
                    count = model.likeCount,
                    isLiked = model.isLikedByCurrentUser,
                    isEnabled = !model.isLikePending,
                    isLoading = model.isLikePending,
                ),
                toggleLikeAction,
            )
        }

        fun recycle() {
            isRecycled.value = true
            bindingEpoch.value += 1
            rowModel.value = null
            favouriteActionController?.onViewRecycled()
            deleteWidget?.apply {
                setOnDeleteListener(null)
                onViewRecycled()
            }
            deleteWidget = null
        }

        private fun dispatchCurrentItem(action: (FeedItemUiModel) -> Unit) {
            if (isRecycled.value || !composeView.isAttachedToWindow) return
            currentItem()?.let(action)
        }

        private fun dispatchOpenProfile() {
            dispatchCurrentItem { model ->
                val userId = model.userId ?: return@dispatchCurrentItem
                onOpenProfile(composeView, userId)
            }
        }

        private fun dispatchOpenMedia() {
            dispatchCurrentItem { model -> onOpenMedia(composeView, model.id) }
        }

        private fun dispatchLongPressMedia(): Boolean {
            if (isListVariant) return false
            var handled = false
            dispatchCurrentItem { model -> handled = onLongPressMedia(composeView, model.id) }
            return handled
        }

        private fun dispatchDelete(
            feedId: Long,
            actionEpoch: Int,
        ) {
            if (isRecycled.value || actionEpoch != bindingEpoch.value || !composeView.isAttachedToWindow) return
            val model = currentItem() ?: return
            if (model.id != feedId || !model.canDelete || model.isDeletePending) return
            onDeleteFeedAction(feedId)
        }
    }

    private inner class StatusFeedViewHolder(
        parent: ViewGroup,
    ) : FeedItemViewHolder(
        ComposeView(parent.context).apply {
            id = R.id.container
            layoutParams =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    val margin = parent.resources.getDimensionPixelSize(R.dimen.lg_margin)
                    setMargins(margin, margin, margin, 0)
                }
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
            )
        },
    ) {
        private val composeView = itemView as ComposeView
        private val rowModel = mutableStateOf<FeedItemUiModel?>(null)
        private val createdAtText = mutableStateOf("")
        private val isRecycled = mutableStateOf(true)
        private val bindingEpoch = mutableStateOf(0)
        private val deleteAction: (Long, Int) -> Unit = ::dispatchDelete
        private var deleteWidget: StatusDeleteWidget? = null

        init {
            composeView.setContent {
                AniTrendTheme {
                    rowModel.value?.let { model ->
                        FeedStatusRow(
                            model = model,
                            createdAtText = createdAtText.value,
                            isRecycled = isRecycled.value,
                            bindingEpoch = bindingEpoch.value,
                            onToggleLike = { dispatchClick(R.id.widget_favourite) },
                            onShowLikes = { dispatchClick(R.id.widget_users) },
                            onOpenComments = { dispatchClick(R.id.widget_comment) },
                            onEdit = { dispatchClick(R.id.widget_edit) },
                            onDelete = deleteAction,
                            onOpenProfile = { dispatchClick(R.id.user_avatar) },
                            onDeleteWidgetCreated = { deleteWidget = it },
                        )
                    }
                }
            }
        }

        fun bind(model: FeedItemUiModel) {
            bindingEpoch.value += 1
            isRecycled.value = false
            createdAtText.value = DateUtil.getPrettyDateUnix(model.createdAt)
            rowModel.value = model
        }

        fun recycle() {
            isRecycled.value = true
            bindingEpoch.value += 1
            deleteWidget?.apply {
                setOnDeleteListener(null)
                onViewRecycled()
            }
        }

        private fun dispatchClick(targetId: Int) {
            if (isRecycled.value || !composeView.isAttachedToWindow) return
            val model = currentItem() ?: return
            when (targetId) {
                R.id.widget_favourite -> onToggleLikeAction(model.id)
                R.id.widget_comment -> onOpenComments(model.id)
                R.id.widget_edit -> onEditFeed(model.id)
                R.id.widget_users -> onShowLikes(model.id)
                R.id.user_avatar -> model.userId?.let { userId -> onOpenProfile(composeView, userId) }
            }
        }

        private fun dispatchDelete(
            feedId: Long,
            actionEpoch: Int,
        ) {
            if (isRecycled.value || actionEpoch != bindingEpoch.value || !composeView.isAttachedToWindow) return
            val model = currentItem() ?: return
            if (model.id != feedId || !model.canDelete || model.isDeletePending) return
            onDeleteFeedAction(feedId)
        }
    }

    private inner class MessageFeedViewHolder(
        private val binding: AdapterFeedMessageBinding,
    ) : FeedItemViewHolder(binding.root) {
        private val favouriteActionController = FavouriteActionController(binding.widgetFavourite)

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
            binding.messengerAvatar.setImage(model.messengerAvatarUrl)
            binding.recipientAvatar.setImage(model.recipientAvatarUrl)
            binding.recipientUserName.visibility = View.VISIBLE
            binding.messengerUserName.visibility = View.GONE
            val displayName = model.recipientName ?: model.messengerName
            binding.recipientUserName.text = displayName
            binding.messengerUserName.text = displayName
            binding.feedTime.text = DateUtil.getPrettyDateUnix(model.createdAt)
            binding.widgetStatus.visibility = View.GONE
            binding.widgetStatusText.richMarkDown(model.body?.toString())
            favouriteActionController.render(
                FavouriteActionState(
                    count = model.likeCount,
                    isLiked = model.isLikedByCurrentUser,
                    isEnabled = !model.isLikePending,
                    isLoading = model.isLikePending,
                ),
            ) { onToggleLikeAction(model.id) }
            binding.widgetComment.setReplyCount(model.replyCount)

            binding.widgetEdit.visibility = if (model.canEdit) View.VISIBLE else View.GONE
            if (model.canDelete) {
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
            favouriteActionController.onViewRecycled()
            binding.widgetDelete.setOnDeleteListener(null)
            binding.widgetStatus.onViewRecycled()
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
