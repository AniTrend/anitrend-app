package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.widget.FavouriteWidget
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidget
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.coordinator.WidgetMutationCoordinator
import com.mxt.anitrend.databinding.AdapterCommentBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil

/**
 * Created by max on 2017/12/03.
 * comment activity adapter
 */
class CommentAdapter(
    context: Context,
    private val coordinator: WidgetMutationCoordinator,
) : RecyclerViewAdapter<FeedReply>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<FeedReply> = CommentViewHolder(
        AdapterCommentBinding.inflate(parent.context.getLayoutInflater(), parent, false),
    )

    override fun getFilter(): Filter? = null

    private val favouriteListener = object : FavouriteWidget.Listener {
        override fun onToggleLike(
            id: Long,
            type: LikeableType,
            onResult: (Result<List<UserBase>>) -> Unit,
        ) = coordinator.toggleLike(id, type, onResult)
    }

    private val deleteListener = object : StatusDeleteWidget.Listener {
        override fun onDeleteFeed(
            feedId: Long,
            @KeyUtil.RequestType requestType: Int,
            onResult: (Result<DeleteState>) -> Unit,
        ) {
            when (requestType) {
                KeyUtil.MUT_DELETE_FEED -> coordinator.deleteActivity(feedId, onResult)
                KeyUtil.MUT_DELETE_FEED_REPLY -> coordinator.deleteActivityReply(feedId, onResult)
                else -> onResult(Result.failure(IllegalStateException("Unknown request type: $requestType")))
            }
        }
    }

    inner class CommentViewHolder(
        private val binding: AdapterCommentBinding,
    ) : RecyclerViewHolder<FeedReply>(binding.root) {
        init {
            bindClickListeners(
                R.id.widget_edit,
                R.id.widget_users,
                R.id.user_avatar,
                R.id.widget_mention,
            )
        }

        override fun onBindViewHolder(model: FeedReply) {
            binding.userAvatar.setImage(model.user?.avatar)
            binding.userName.text = model.user?.name
            binding.feedTime.text = DateUtil.getPrettyDateUnix(model.createdAt)
            if (!settings.experimentalMarkdown) {
                binding.widgetStatus.visibility = View.VISIBLE
                binding.widgetStatus.setModel(model)
            } else {
                binding.widgetStatus.visibility = View.GONE
            }
            binding.widgetStatusText.richMarkDown(model.reply)
            binding.widgetMention.visibility = View.GONE

            binding.widgetFavourite.setRequestParams(KeyUtil.ACTIVITY_REPLY, model.id)
            binding.widgetFavourite.setModel(model.likes)
            binding.widgetFavourite.setCurrentUser(coordinator.databaseHelper.currentUser)
            binding.widgetFavourite.setListener(favouriteListener)

            val isCurrentUser = coordinator.databaseHelper.currentUser?.id == model.user?.id
            if (isCurrentUser) {
                binding.widgetDelete.setModel(model, KeyUtil.MUT_DELETE_FEED_REPLY)

                binding.widgetMention.visibility = View.GONE
                binding.widgetEdit.visibility = View.VISIBLE
                binding.widgetDelete.visibility = View.VISIBLE
            } else {
                binding.widgetMention.visibility = View.VISIBLE
                binding.widgetEdit.visibility = View.GONE
                binding.widgetDelete.visibility = View.GONE
            }
            binding.widgetDelete.setListener(deleteListener)
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.userAvatar)
            binding.widgetFavourite.setListener(null)
            binding.widgetDelete.setListener(null)
            binding.widgetStatus.onViewRecycled()
            binding.widgetDelete.onViewRecycled()
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(view: View): Boolean = performLongClick(clickListener, data, view)
    }
}
