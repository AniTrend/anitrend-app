package com.mxt.anitrend.adapter.recycler.detail

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidgetState
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidgetState
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.AdapterCommentBinding
import com.mxt.anitrend.domain.model.CommentReplyUiModel
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.UserBase
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
        AdapterCommentBinding.inflate(parent.context.getLayoutInflater(), parent, false),
    )

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: CommentViewHolder) {
        holder.recycle()
        super.onViewRecycled(holder)
    }

    inner class CommentViewHolder(
        private val binding: AdapterCommentBinding,
    ) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        init {
            binding.widgetEdit.setOnClickListener(this)
            binding.widgetUsers.setOnClickListener(this)
            binding.userAvatar.setOnClickListener(this)
            binding.widgetMention.setOnClickListener(this)
        }

        fun bind(model: CommentReplyUiModel) {
            binding.userAvatar.setImage(model.userAvatar)
            binding.userName.text = model.userName
            binding.feedTime.text = DateUtil.getPrettyDateUnix(model.createdAt)
            binding.widgetStatus.visibility = View.GONE
            binding.widgetStatusText.richMarkDown(model.reply)

            binding.widgetFavourite.render(
                FavouriteWidgetState(
                    count = model.likeCount,
                    isLiked = model.isLikedByCurrentUser,
                    isEnabled = !model.isLikePending,
                    isLoading = model.isLikePending,
                ),
            )
            binding.widgetFavourite.setOnToggleListener { onToggleLike(model.id) }

            val isCurrentUser = currentUser?.id == model.userId
            if (isCurrentUser) {
                binding.widgetDelete.render(
                    StatusDeleteWidgetState(
                        isEnabled = !model.isDeletePending,
                        isLoading = model.isDeletePending,
                    ),
                )
                binding.widgetDelete.visibility = View.VISIBLE
                binding.widgetDelete.setOnDeleteListener { onDeleteReply(model.id) }
                binding.widgetMention.visibility = View.GONE
                binding.widgetEdit.visibility = View.VISIBLE
            } else {
                binding.widgetDelete.visibility = View.GONE
                binding.widgetDelete.setOnDeleteListener(null)
                binding.widgetMention.visibility = View.VISIBLE
                binding.widgetEdit.visibility = View.GONE
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

        override fun onClick(v: View) {
            val model = currentItem() ?: return
            when (v.id) {
                R.id.widget_edit -> onEditReply(model.id)
                R.id.widget_users -> onShowLikes(model.id)
                R.id.user_avatar -> model.userId?.let { onOpenProfile(v, it) }
                R.id.widget_mention -> onMentionReply(model.id)
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
