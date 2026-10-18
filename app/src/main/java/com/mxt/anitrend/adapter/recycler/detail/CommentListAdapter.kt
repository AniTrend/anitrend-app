package com.mxt.anitrend.adapter.recycler.detail

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidget
import com.mxt.anitrend.domain.model.CommentReplyUiModel
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.feed.CommentReplyRow
import com.mxt.anitrend.util.date.DateUtil

class CommentListAdapter(
    private val currentUser: UserBase?,
    private val onToggleLike: (Long) -> Unit,
    private val onDeleteReply: (Long) -> Unit,
    private val onEditReply: (Long) -> Unit,
    private val onMentionReply: (Long) -> Unit,
    private val onShowLikes: (Long) -> Unit,
    private val onOpenProfile: (View, Long) -> Unit,
) : ListAdapter<CommentReplyUiModel, CommentListAdapter.CommentViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CommentViewHolder = CommentViewHolder(
        ComposeView(parent.context).apply {
            id = R.id.container
            layoutParams =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    val margin = parent.resources.getDimensionPixelSize(R.dimen.lg_margin)
                    setMargins(margin, margin, margin, margin)
                }
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
            )
        },
    )

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: CommentViewHolder) {
        holder.recycle()
        super.onViewRecycled(holder)
    }

    inner class CommentViewHolder(
        private val composeView: ComposeView,
    ) : RecyclerView.ViewHolder(composeView) {
        private val rowModel = mutableStateOf<CommentReplyUiModel?>(null)
        private val createdAtText = mutableStateOf("")
        private val isCurrentUser = mutableStateOf(false)
        private var deleteWidget: StatusDeleteWidget? = null
        private val recycled = mutableStateOf(true)

        init {
            composeView.setContent {
                AniTrendTheme {
                    rowModel.value?.let { model ->
                        CommentReplyRow(
                            model = model,
                            createdAtText = createdAtText.value,
                            isCurrentUser = isCurrentUser.value,
                            isRecycled = recycled.value,
                            onToggleLike = { dispatchClick(R.id.widget_favourite) },
                            onDelete = { dispatchClick(R.id.widget_delete) },
                            onEdit = { dispatchClick(R.id.widget_edit) },
                            onMention = { dispatchClick(R.id.widget_mention) },
                            onShowLikes = { dispatchClick(R.id.widget_users) },
                            onOpenProfile = { dispatchClick(R.id.user_avatar) },
                            onDeleteWidgetCreated = { deleteWidget = it },
                        )
                    }
                }
            }
        }

        fun bind(model: CommentReplyUiModel) {
            recycled.value = false
            rowModel.value = model
            createdAtText.value = DateUtil.getPrettyDateUnix(model.createdAt)
            isCurrentUser.value = currentUser?.id == model.userId
        }

        fun recycle() {
            recycled.value = true
            deleteWidget?.apply {
                setOnDeleteListener(null)
                onViewRecycled()
            }
            rowModel.value =
                rowModel.value?.copy(
                    likes = emptyList(),
                    likeCount = 0,
                    isLikedByCurrentUser = false,
                    isLikePending = false,
                    isDeletePending = false,
                )
        }

        private fun dispatchClick(targetId: Int) {
            if (recycled.value || !composeView.isAttachedToWindow) return
            val model = currentItem() ?: return
            when (targetId) {
                R.id.widget_favourite -> onToggleLike(model.id)
                R.id.widget_delete -> onDeleteReply(model.id)
                R.id.widget_edit -> onEditReply(model.id)
                R.id.widget_users -> onShowLikes(model.id)
                R.id.widget_mention -> onMentionReply(model.id)
                R.id.user_avatar -> model.userId?.let { userId -> onOpenProfile(composeView, userId) }
            }
        }

        private fun currentItem(): CommentReplyUiModel? {
            val position = bindingAdapterPosition
            return if (position == RecyclerView.NO_POSITION) null else getItem(position)
        }
    }

    companion object {
        val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<CommentReplyUiModel>() {
                override fun areItemsTheSame(
                    oldItem: CommentReplyUiModel,
                    newItem: CommentReplyUiModel,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: CommentReplyUiModel,
                    newItem: CommentReplyUiModel,
                ): Boolean = oldItem == newItem
            }
    }
}
